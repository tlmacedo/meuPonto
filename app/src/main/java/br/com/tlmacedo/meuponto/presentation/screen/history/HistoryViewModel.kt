// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/history/HistoryViewModel.kt
package br.com.tlmacedo.meuponto.presentation.screen.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.tlmacedo.meuponto.domain.model.DiaSemana
import br.com.tlmacedo.meuponto.domain.model.HorarioDiaSemana
import br.com.tlmacedo.meuponto.domain.model.VersaoJornada
import br.com.tlmacedo.meuponto.domain.repository.HorarioDiaSemanaRepository
import br.com.tlmacedo.meuponto.domain.repository.PontoRepository
import br.com.tlmacedo.meuponto.domain.repository.VersaoJornadaRepository
import br.com.tlmacedo.meuponto.domain.usecase.emprego.ObterEmpregoAtivoUseCase
import br.com.tlmacedo.meuponto.domain.usecase.ponto.CalcularResumoDiaUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate
import javax.inject.Inject

/**
 * ViewModel da tela de Histórico.
 *
 * Gerencia o estado da tela de histórico, carregando os registros
 * de ponto do mês selecionado e usando ResumoDia para cálculos consistentes.
 *
 * @property repository Repositório de pontos para busca dos dados
 * @property calcularResumoDiaUseCase UseCase para cálculo de resumo (single source of truth)
 * @property horarioDiaSemanaRepository Repositório para buscar configurações de horário
 * @property versaoJornadaRepository Repositório para buscar versões de jornada
 * @property obterEmpregoAtivoUseCase UseCase para obter o emprego ativo
 *
 * @author Thiago
 * @since 1.0.0
 * @updated 2.5.0 - Adicionados filtros e controle de expansão
 * @updated 3.0.0 - Refatorado para usar CalcularResumoDiaUseCase
 * @updated 3.1.0 - Corrigido: agora passa HorarioDiaSemana para cálculos consistentes com Home
 * @updated 3.2.0 - Corrigido: agora usa versionamento de jornada para buscar carga horária histórica
 */
@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: PontoRepository,
    private val calcularResumoDiaUseCase: CalcularResumoDiaUseCase,
    private val horarioDiaSemanaRepository: HorarioDiaSemanaRepository,
    private val versaoJornadaRepository: VersaoJornadaRepository,
    private val obterEmpregoAtivoUseCase: ObterEmpregoAtivoUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    private var carregarJob: Job? = null

    init {
        carregarHistorico()
    }

    /**
     * Carrega o histórico do mês selecionado.
     * Usa CalcularResumoDiaUseCase para garantir consistência nos cálculos.
     *
     * IMPORTANTE: Busca a versão de jornada vigente para cada data,
     * garantindo que a carga horária histórica seja usada corretamente.
     */
    fun carregarHistorico() {
        carregarJob?.cancel()
        carregarJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                // Obter emprego ativo para buscar configurações de horário
                val empregoId: Long? = when (val resultado = obterEmpregoAtivoUseCase()) {
                    is ObterEmpregoAtivoUseCase.Resultado.Sucesso -> resultado.emprego.id
                    else -> null
                }

                if (empregoId == null) {
                    _uiState.update { it.copy(isLoading = false) }
                    return@launch
                }

                // Pré-carregar todas as versões de jornada do emprego
                val versoesJornada = versaoJornadaRepository.buscarPorEmprego(empregoId)

                // Cache de horários por versão de jornada (para evitar N+1 queries)
                val horariosPorVersao = mutableMapOf<Long, Map<DiaSemana, HorarioDiaSemana>>()

                val mesSelecionado = _uiState.value.mesSelecionado
                val primeiroDia = mesSelecionado.withDayOfMonth(1)
                val ultimoDia = mesSelecionado.withDayOfMonth(mesSelecionado.lengthOfMonth())

                repository.observarPontosPorPeriodo(primeiroDia, ultimoDia)
                    .collect { pontos ->
                        // Agrupa pontos por dia e usa o UseCase para calcular cada resumo
                        val resumosPorDia = pontos
                            .groupBy { it.data }
                            .map { (data, pontosData) ->
                                // Buscar a versão de jornada vigente para esta data específica
                                val versaoVigente = encontrarVersaoVigente(versoesJornada, data)

                                // Buscar horários da versão vigente (com cache)
                                val horariosDaVersao = versaoVigente?.let { versao ->
                                    horariosPorVersao.getOrPut(versao.id) {
                                        horarioDiaSemanaRepository
                                            .buscarPorVersaoJornada(versao.id)
                                            .associateBy { it.diaSemana }
                                    }
                                } ?: emptyMap()

                                // Buscar configuração do dia da semana
                                val diaSemana = DiaSemana.fromDayOfWeek(data.dayOfWeek)
                                val horarioDia = horariosDaVersao[diaSemana]

                                // Usa o mesmo UseCase da Home - SINGLE SOURCE OF TRUTH
                                calcularResumoDiaUseCase(
                                    pontos = pontosData,
                                    data = data,
                                    horarioDiaSemana = horarioDia
                                )
                            }
                            .sortedByDescending { it.data }

                        _uiState.update { state ->
                            state.copy(
                                resumosPorDia = resumosPorDia,
                                isLoading = false
                            )
                        }
                    }
            } catch (e: Exception) {
                Timber.e(e, "Erro ao carregar histórico")
                _uiState.update { it.copy(errorMessage = e.message, isLoading = false) }
            }
        }
    }

    /**
     * Encontra a versão de jornada vigente para uma data específica.
     *
     * @param versoes Lista de todas as versões do emprego (ordenadas por dataInicio DESC)
     * @param data Data para verificar
     * @return Versão vigente ou null se não houver
     */
    private fun encontrarVersaoVigente(
        versoes: List<VersaoJornada>,
        data: LocalDate
    ): VersaoJornada? {
        return versoes.find { versao -> versao.contemData(data) }
    }

    /**
     * Altera o mês selecionado para visualização.
     */
    fun selecionarMes(novoMes: LocalDate) {
        _uiState.update { it.copy(mesSelecionado = novoMes, diaExpandido = null) }
        carregarHistorico()
    }

    /** Navega para o mês anterior */
    fun mesAnterior() {
        val novoMes = _uiState.value.mesSelecionado.minusMonths(1)
        selecionarMes(novoMes)
    }

    /** Navega para o próximo mês */
    fun proximoMes() {
        if (_uiState.value.podeIrProximoMes) {
            val novoMes = _uiState.value.mesSelecionado.plusMonths(1)
            selecionarMes(novoMes)
        }
    }

    /**
     * Altera o filtro ativo.
     */
    fun alterarFiltro(filtro: FiltroHistorico) {
        _uiState.update { it.copy(filtroAtivo = filtro, diaExpandido = null) }
    }

    /**
     * Expande ou colapsa um dia específico.
     */
    fun toggleDiaExpandido(data: LocalDate) {
        _uiState.update { state ->
            state.copy(
                diaExpandido = if (state.diaExpandido == data) null else data
            )
        }
    }

    /**
     * Limpa a mensagem de erro.
     */
    fun limparErro() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
