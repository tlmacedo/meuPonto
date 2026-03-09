package br.com.tlmacedo.meuponto.presentation.screen.settings.empregos

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.tlmacedo.meuponto.domain.model.Emprego
import br.com.tlmacedo.meuponto.domain.repository.EmpregoRepository
import br.com.tlmacedo.meuponto.domain.repository.VersaoJornadaRepository
import br.com.tlmacedo.meuponto.presentation.navigation.MeuPontoDestinations
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel para a tela de detalhes/configurações de um emprego específico.
 *
 * Gerencia o carregamento dos dados do emprego e navegação para sub-telas
 * de configuração (versões de jornada, ausências, ajustes de saldo).
 *
 * @author Thiago
 * @since 4.0.0
 * @updated 9.1.0 - Migração para MeuPontoDestinations
 */
@HiltViewModel
class EmpregoSettingsDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val empregoRepository: EmpregoRepository,
    private val versaoJornadaRepository: VersaoJornadaRepository
) : ViewModel() {

    private val empregoId: Long =
        savedStateHandle.get<Long>(MeuPontoDestinations.ARG_EMPREGO_ID) ?: -1L

    private val _uiState = MutableStateFlow(EmpregoSettingsDetailUiState())
    val uiState: StateFlow<EmpregoSettingsDetailUiState> = _uiState.asStateFlow()

    private val _eventos = MutableSharedFlow<EmpregoSettingsDetailEvent>()
    val eventos: SharedFlow<EmpregoSettingsDetailEvent> = _eventos.asSharedFlow()

    init {
        carregarEmprego()
    }

    /**
     * Processa as ações da UI.
     */
    fun onAction(action: EmpregoSettingsDetailAction) {
        when (action) {
            is EmpregoSettingsDetailAction.Recarregar -> carregarEmprego()
            is EmpregoSettingsDetailAction.NavegarParaVersoes -> navegarParaVersoes()
            is EmpregoSettingsDetailAction.NavegarParaAusencias -> navegarParaAusencias()
            is EmpregoSettingsDetailAction.NavegarParaAjustesSaldo -> navegarParaAjustesSaldo()
            is EmpregoSettingsDetailAction.LimparErro -> limparErro()
        }
    }

    private fun carregarEmprego() {
        if (empregoId <= 0L) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = "Emprego inválido"
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                val emprego = empregoRepository.buscarPorId(empregoId)

                if (emprego == null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Emprego não encontrado"
                        )
                    }
                    return@launch
                }

                val totalVersoes = versaoJornadaRepository.contarPorEmprego(empregoId)
                val versaoVigente = versaoJornadaRepository.buscarVigente(empregoId)

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        empregoId = empregoId,
                        emprego = emprego,
                        totalVersoes = totalVersoes,
                        versaoVigenteDescricao = versaoVigente?.titulo
                    )
                }
            } catch (e: Exception) {
                Timber.e(e, "Erro ao carregar emprego %d", empregoId)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Erro ao carregar emprego"
                    )
                }
            }
        }
    }

    private fun navegarParaVersoes() {
        viewModelScope.launch {
            _eventos.emit(EmpregoSettingsDetailEvent.NavegarParaVersoes(empregoId))
        }
    }

    private fun navegarParaAusencias() {
        viewModelScope.launch {
            _eventos.emit(EmpregoSettingsDetailEvent.NavegarParaAusencias(empregoId))
        }
    }

    private fun navegarParaAjustesSaldo() {
        viewModelScope.launch {
            _eventos.emit(EmpregoSettingsDetailEvent.NavegarParaAjustesSaldo(empregoId))
        }
    }

    private fun limparErro() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}

/**
 * Estado da UI para a tela de detalhes do emprego.
 */
data class EmpregoSettingsDetailUiState(
    val isLoading: Boolean = true,
    val empregoId: Long = 0L,
    val emprego: Emprego? = null,
    val totalVersoes: Int = 0,
    val versaoVigenteDescricao: String? = null,
    val errorMessage: String? = null
) {
    val nomeEmprego: String
        get() = emprego?.nome ?: "Emprego"

    val empregoAtivo: Boolean
        get() = emprego?.ativo == true
}

/**
 * Ações disparadas pela UI.
 */
sealed interface EmpregoSettingsDetailAction {
    data object Recarregar : EmpregoSettingsDetailAction
    data object NavegarParaVersoes : EmpregoSettingsDetailAction
    data object NavegarParaAusencias : EmpregoSettingsDetailAction
    data object NavegarParaAjustesSaldo : EmpregoSettingsDetailAction
    data object LimparErro : EmpregoSettingsDetailAction
}

/**
 * Eventos emitidos pelo ViewModel.
 */
sealed interface EmpregoSettingsDetailEvent {
    data class NavegarParaVersoes(val empregoId: Long) : EmpregoSettingsDetailEvent
    data class NavegarParaAusencias(val empregoId: Long) : EmpregoSettingsDetailEvent
    data class NavegarParaAjustesSaldo(val empregoId: Long) : EmpregoSettingsDetailEvent
    data class MostrarMensagem(val mensagem: String) : EmpregoSettingsDetailEvent
}
