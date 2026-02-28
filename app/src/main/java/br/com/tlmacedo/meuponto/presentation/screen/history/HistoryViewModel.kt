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
 * @author Thiago
 * @since 1.0.0
 * @updated 3.3.0 - Usa observarPorEmpregoEPeriodo para suporte a múltiplos empregos
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

                val versoesJornada = versaoJornadaRepository.buscarPorEmprego(empregoId)
                val horariosPorVersao = mutableMapOf<Long, Map<DiaSemana, HorarioDiaSemana>>()

                val mesSelecionado = _uiState.value.mesSelecionado
                val primeiroDia = mesSelecionado.withDayOfMonth(1)
                val ultimoDia = mesSelecionado.withDayOfMonth(mesSelecionado.lengthOfMonth())

                // Usa observarPorEmpregoEPeriodo em vez de observarPontosPorPeriodo (deprecated)
                repository.observarPorEmpregoEPeriodo(empregoId, primeiroDia, ultimoDia)
                    .collect { pontos ->
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
