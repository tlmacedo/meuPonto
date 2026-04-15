package br.com.tlmacedo.meuponto.presentation.screen.settings.empregos.cargos

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.tlmacedo.meuponto.domain.model.AjusteSalarial
import br.com.tlmacedo.meuponto.domain.model.HistoricoCargo
import br.com.tlmacedo.meuponto.domain.repository.EmpregoRepository
import br.com.tlmacedo.meuponto.domain.repository.HistoricoCargoRepository
import br.com.tlmacedo.meuponto.presentation.navigation.MeuPontoDestinations
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate
import javax.inject.Inject

/**
 * ViewModel para a tela de listagem de cargos e salários.
 *
 * @author Thiago
 * @since 29.0.0
 */
@HiltViewModel
class CargosViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val empregoRepository: EmpregoRepository,
    private val historicoCargoRepository: HistoricoCargoRepository
) : ViewModel() {

    private val empregoId: Long =
        savedStateHandle.get<Long>(MeuPontoDestinations.ARG_EMPREGO_ID) ?: -1L

    private val _uiState = MutableStateFlow(CargosUiState())
    val uiState: StateFlow<CargosUiState> = _uiState.asStateFlow()

    private val _eventos = MutableSharedFlow<CargosEvent>()
    val eventos: SharedFlow<CargosEvent> = _eventos.asSharedFlow()

    init {
        carregarDados()
    }

    fun onAction(action: CargosAction) {
        when (action) {
            is CargosAction.NovoCargo -> navegarParaNovo()
            is CargosAction.EditarCargo -> navegarParaEditar(action.cargo)
            is CargosAction.SolicitarExclusao -> solicitarExclusao(action.cargo)
            is CargosAction.ConfirmarExclusao -> confirmarExclusao()
            is CargosAction.CancelarExclusao -> cancelarExclusao()
        }
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    private fun carregarDados() {
        if (empregoId <= 0L) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                // Observar emprego para pegar nome, apelido e logo
                launch {
                    empregoRepository.observarPorId(empregoId).collect { emprego ->
                        _uiState.update {
                            it.copy(
                                nomeEmprego = emprego?.nome ?: "",
                                apelidoEmprego = emprego?.apelido,
                                logoEmprego = emprego?.logo
                            )
                        }
                    }
                }

                // Observar cargos e seus ajustes reativamente
                historicoCargoRepository.listarPorEmprego(empregoId)
                    .flatMapLatest { cargos ->
                        if (cargos.isEmpty()) {
                            flowOf(cargos to emptyMap<Long, List<AjusteSalarial>>())
                        } else {
                            val adjustmentFlows = cargos.map { cargo ->
                                historicoCargoRepository.listarAjustes(cargo.id)
                                    .map { cargo.id to it }
                            }
                            combine(adjustmentFlows) { pairs ->
                                pairs.toMap()
                            }.map { cargos to it }
                        }
                    }
                    .collect { (cargos, ajustesMap) ->
                        val hoje = LocalDate.now()
                        val cargoAtual = cargos.firstOrNull {
                            it.dataFim == null || !it.dataFim.isAfter(hoje) // Simplificação: assume o último sem data fim ou vigente hoje
                        }

                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                cargos = cargos.sortedByDescending { c -> c.dataInicio },
                                cargoAtual = cargoAtual,
                                ajustesPorCargo = ajustesMap
                            )
                        }
                    }
            } catch (e: Exception) {
                Timber.e(e, "Erro ao carregar cargos do emprego %d", empregoId)
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun navegarParaNovo() {
        viewModelScope.launch {
            _eventos.emit(CargosEvent.NavegarParaNovo(empregoId))
        }
    }

    private fun navegarParaEditar(cargo: HistoricoCargo) {
        viewModelScope.launch {
            _eventos.emit(CargosEvent.NavegarParaEditar(empregoId, cargo.id))
        }
    }

    private fun solicitarExclusao(cargo: HistoricoCargo) {
        _uiState.update { it.copy(cargoParaExcluir = cargo) }
    }

    private fun cancelarExclusao() {
        _uiState.update { it.copy(cargoParaExcluir = null) }
    }

    private fun confirmarExclusao() {
        val cargo = _uiState.value.cargoParaExcluir ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isExcluindo = true) }
            try {
                historicoCargoRepository.excluir(cargo)
                _uiState.update { it.copy(cargoParaExcluir = null, isExcluindo = false) }
                _eventos.emit(CargosEvent.MostrarMensagem("Cargo excluído com sucesso"))
            } catch (e: Exception) {
                Timber.e(e, "Erro ao excluir cargo %d", cargo.id)
                _uiState.update { it.copy(isExcluindo = false) }
                _eventos.emit(CargosEvent.MostrarMensagem("Erro ao excluir cargo"))
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════════════════════
// UI STATE
// ════════════════════════════════════════════════════════════════════════════════

data class CargosUiState(
    val isLoading: Boolean = true,
    val nomeEmprego: String = "",
    val apelidoEmprego: String? = null,
    val logoEmprego: String? = null,
    val cargos: List<HistoricoCargo> = emptyList(),
    val cargoAtual: HistoricoCargo? = null,
    val ajustesPorCargo: Map<Long, List<AjusteSalarial>> = emptyMap(),
    val cargoParaExcluir: HistoricoCargo? = null,
    val isExcluindo: Boolean = false
)

// ════════════════════════════════════════════════════════════════════════════════
// ACTIONS
// ════════════════════════════════════════════════════════════════════════════════

sealed interface CargosAction {
    data object NovoCargo : CargosAction
    data class EditarCargo(val cargo: HistoricoCargo) : CargosAction
    data class SolicitarExclusao(val cargo: HistoricoCargo) : CargosAction
    data object ConfirmarExclusao : CargosAction
    data object CancelarExclusao : CargosAction
}

// ════════════════════════════════════════════════════════════════════════════════
// EVENTS
// ════════════════════════════════════════════════════════════════════════════════

sealed interface CargosEvent {
    data class MostrarMensagem(val mensagem: String) : CargosEvent
    data class NavegarParaEditar(val empregoId: Long, val cargoId: Long) : CargosEvent
    data class NavegarParaNovo(val empregoId: Long) : CargosEvent
}
