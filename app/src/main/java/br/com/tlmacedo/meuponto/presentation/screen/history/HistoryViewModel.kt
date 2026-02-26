// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/history/HistoryViewModel.kt
package br.com.tlmacedo.meuponto.presentation.screen.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.tlmacedo.meuponto.domain.model.ConfiguracaoEmprego
import br.com.tlmacedo.meuponto.domain.model.DiaSemana
import br.com.tlmacedo.meuponto.domain.model.HorarioDiaSemana
import br.com.tlmacedo.meuponto.domain.model.VersaoJornada
import br.com.tlmacedo.meuponto.domain.repository.ConfiguracaoEmpregoRepository
import br.com.tlmacedo.meuponto.domain.repository.FechamentoPeriodoRepository
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
 * @author Thiago
 * @since 1.0.0
 * @updated 7.0.0 - Adicionado cálculo de saldo acumulado do banco de horas
 */
@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: PontoRepository,
    private val calcularResumoDiaUseCase: CalcularResumoDiaUseCase,
    private val horarioDiaSemanaRepository: HorarioDiaSemanaRepository,
    private val versaoJornadaRepository: VersaoJornadaRepository,
    private val obterEmpregoAtivoUseCase: ObterEmpregoAtivoUseCase,
    private val configuracaoEmpregoRepository: ConfiguracaoEmpregoRepository,
    private val fechamentoPeriodoRepository: FechamentoPeriodoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    private var carregarJob: Job? = null
    private var configuracaoCache: ConfiguracaoEmprego? = null

    init {
        carregarHistorico()
    }

    /**
     * Carrega o histórico do período RH selecionado.
     */
    fun carregarHistorico() {
        carregarJob?.cancel()
        carregarJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                val empregoId: Long? = when (val resultado = obterEmpregoAtivoUseCase()) {
                    is ObterEmpregoAtivoUseCase.Resultado.Sucesso -> resultado.emprego.id
                    else -> null
                }

                if (empregoId == null) {
                    _uiState.update { it.copy(isLoading = false) }
                    return@launch
                }

                // Buscar configuração do emprego (com cache)
                val configuracao = configuracaoCache
                    ?: configuracaoEmpregoRepository.buscarPorEmpregoId(empregoId)
                        ?.also { configuracaoCache = it }

                val versoesJornada = versaoJornadaRepository.buscarPorEmprego(empregoId)
                val horariosPorVersao = mutableMapOf<Long, Map<DiaSemana, HorarioDiaSemana>>()

                // Calcular período RH
                val mesSelecionado = _uiState.value.mesSelecionado
                val (primeiroDia, ultimoDia) = calcularPeriodoRH(mesSelecionado, configuracao)

                Timber.d("Período RH: $primeiroDia até $ultimoDia")

                // Buscar saldo inicial do banco (último fechamento antes do período)
                val saldoInicialBanco = buscarSaldoInicialBanco(empregoId, primeiroDia)

                // Atualizar o state com as datas do período
                _uiState.update {
                    it.copy(
                        periodoInicio = primeiroDia,
                        periodoFim = ultimoDia,
                        diaInicioFechamentoRH = configuracao?.diaInicioFechamentoRH ?: 1,
                        saldoInicialBancoMinutos = saldoInicialBanco
                    )
                }

                repository.observarPorEmpregoEPeriodo(empregoId, primeiroDia, ultimoDia)
                    .collect { pontos ->
                        // Calcular resumos por dia
                        val resumosPorDia = pontos
                            .groupBy { it.data }
                            .map { (data, pontosData) ->
                                val versaoVigente = encontrarVersaoVigente(versoesJornada, data)

                                val horariosDaVersao = versaoVigente?.let { versao ->
                                    horariosPorVersao.getOrPut(versao.id) {
                                        horarioDiaSemanaRepository
                                            .buscarPorVersaoJornada(versao.id)
                                            .associateBy { it.diaSemana }
                                    }
                                } ?: emptyMap()

                                val diaSemana = DiaSemana.fromDayOfWeek(data.dayOfWeek)
                                val horarioDia = horariosDaVersao[diaSemana]

                                calcularResumoDiaUseCase(
                                    pontos = pontosData,
                                    data = data,
                                    horarioDiaSemana = horarioDia
                                )
                            }
                            .sortedBy { it.data } // Ordenar por data crescente para calcular acumulado

                        // Calcular saldo acumulado do banco para cada dia
                        val resumosComBanco = calcularSaldosAcumulados(
                            resumos = resumosPorDia,
                            saldoInicial = saldoInicialBanco
                        )

                        _uiState.update { state ->
                            state.copy(
                                // Inverter para exibir mais recente primeiro
                                resumosPorDia = resumosComBanco.sortedByDescending { it.data },
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
     * Busca o saldo inicial do banco de horas para o período.
     *
     * Após um fechamento, o saldo é zerado, então o saldo inicial
     * do próximo período é sempre 0.
     */
    private suspend fun buscarSaldoInicialBanco(empregoId: Long, dataInicioPeriodo: LocalDate): Int {
        // Após fechamento o saldo zera, então sempre começa em 0
        // O método existe para futura expansão caso seja necessário
        // considerar saldos transportados ou ajustes manuais
        return 0
    }

    /**
     * Calcula o saldo acumulado do banco para cada dia.
     *
     * @param resumos Lista de resumos ordenada por data CRESCENTE
     * @param saldoInicial Saldo inicial do banco (do fechamento anterior)
     * @return Lista de ResumoDiaComBanco com saldos acumulados
     */
    private fun calcularSaldosAcumulados(
        resumos: List<br.com.tlmacedo.meuponto.domain.model.ResumoDia>,
        saldoInicial: Int
    ): List<ResumoDiaComBanco> {
        var saldoAcumulado = saldoInicial

        return resumos.map { resumo ->
            // Soma o saldo do dia ao acumulado (apenas para dias completos)
            if (resumo.jornadaCompleta && !resumo.isFuturo) {
                saldoAcumulado += resumo.saldoDiaMinutos
            }

            ResumoDiaComBanco(
                resumoDia = resumo,
                saldoBancoAcumuladoMinutos = saldoAcumulado
            )
        }
    }

    /**
     * Calcula o período RH baseado no mês de referência e configuração.
     */
    private fun calcularPeriodoRH(
        mesSelecionado: LocalDate,
        configuracao: ConfiguracaoEmprego?
    ): Pair<LocalDate, LocalDate> {
        val diaFechamento = (configuracao?.diaInicioFechamentoRH ?: 1).coerceIn(1, 28)

        if (diaFechamento == 1) {
            val primeiroDia = mesSelecionado.withDayOfMonth(1)
            val ultimoDia = mesSelecionado.withDayOfMonth(mesSelecionado.lengthOfMonth())
            return primeiroDia to ultimoDia
        }

        val dataInicio = mesSelecionado.withDayOfMonth(diaFechamento)
        val dataFim = dataInicio.plusMonths(1).minusDays(1)

        return dataInicio to dataFim
    }

    private fun encontrarVersaoVigente(
        versoes: List<VersaoJornada>,
        data: LocalDate
    ): VersaoJornada? {
        return versoes.find { versao -> versao.contemData(data) }
    }

    fun selecionarMes(novoMes: LocalDate) {
        _uiState.update { it.copy(mesSelecionado = novoMes, diaExpandido = null) }
        carregarHistorico()
    }

    fun mesAnterior() {
        val novoMes = _uiState.value.mesSelecionado.minusMonths(1)
        selecionarMes(novoMes)
    }

    fun proximoMes() {
        if (_uiState.value.podeIrProximoMes) {
            val novoMes = _uiState.value.mesSelecionado.plusMonths(1)
            selecionarMes(novoMes)
        }
    }

    fun alterarFiltro(filtro: FiltroHistorico) {
        _uiState.update { it.copy(filtroAtivo = filtro, diaExpandido = null) }
    }

    fun toggleDiaExpandido(data: LocalDate) {
        _uiState.update { state ->
            state.copy(
                diaExpandido = if (state.diaExpandido == data) null else data
            )
        }
    }

    fun limparErro() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
