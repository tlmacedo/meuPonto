package br.com.tlmacedo.meuponto.presentation.screen.settings.empregos

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.tlmacedo.meuponto.domain.model.ConfiguracaoEmprego
import br.com.tlmacedo.meuponto.domain.model.Emprego
import br.com.tlmacedo.meuponto.domain.model.HistoricoCargo
import br.com.tlmacedo.meuponto.domain.repository.ConfiguracaoEmpregoRepository
import br.com.tlmacedo.meuponto.domain.repository.EmpregoRepository
import br.com.tlmacedo.meuponto.domain.repository.HistoricoCargoRepository
import br.com.tlmacedo.meuponto.domain.repository.VersaoJornadaRepository
import br.com.tlmacedo.meuponto.presentation.navigation.MeuPontoDestinations
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate
import javax.inject.Inject

/**
 * ViewModel para a tela de detalhes/configurações de um emprego específico.
 *
 * Gerencia o carregamento dos dados do emprego e navegação para sub-telas
 * de configuração (versões de jornada, cargos, configuração geral, ausências, ajustes de saldo).
 *
 * @author Thiago
 * @since 4.0.0
 * @updated 29.0.0 - Adicionado suporte a cargos, configuração geral e novos eventos de navegação
 */
@HiltViewModel
class EmpregoSettingsDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val empregoRepository: EmpregoRepository,
    private val versaoJornadaRepository: VersaoJornadaRepository,
    private val historicoCargoRepository: HistoricoCargoRepository,
    private val configuracaoEmpregoRepository: ConfiguracaoEmpregoRepository
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
            is EmpregoSettingsDetailAction.NavegarParaEditar,
            is EmpregoSettingsDetailAction.NavegarParaConfiguracaoGeral -> navegarParaEditar()
            is EmpregoSettingsDetailAction.NavegarParaCargos -> navegarParaCargos()
            is EmpregoSettingsDetailAction.NavegarParaOpcoesRegistro -> navegarParaOpcoesRegistro()
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

                // Carregar cargos
                val cargos = historicoCargoRepository.listarPorEmprego(empregoId).first()
                val cargoAtual = cargos.firstOrNull { it.dataFim == null || !it.dataFim.isBefore(LocalDate.now()) }

                // Carregar configuração
                val configuracao = configuracaoEmpregoRepository.buscarPorEmpregoId(empregoId)

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        empregoId = empregoId,
                        emprego = emprego,
                        totalVersoes = totalVersoes,
                        versaoVigenteDescricao = versaoVigente?.titulo,
                        cargos = cargos,
                        cargoAtual = cargoAtual?.funcao,
                        configuracao = configuracao
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

    private fun navegarParaEditar() {
        viewModelScope.launch {
            _eventos.emit(EmpregoSettingsDetailEvent.NavegarParaEditar(empregoId))
        }
    }

    private fun navegarParaCargos() {
        viewModelScope.launch {
            _eventos.emit(EmpregoSettingsDetailEvent.NavegarParaCargos(empregoId))
        }
    }

    private fun navegarParaOpcoesRegistro() {
        viewModelScope.launch {
            _eventos.emit(EmpregoSettingsDetailEvent.NavegarParaOpcoesRegistro(empregoId))
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
    val cargos: List<HistoricoCargo> = emptyList(),
    val cargoAtual: String? = null,
    val configuracao: ConfiguracaoEmprego? = null,
    val errorMessage: String? = null
) {
    val nomeEmprego: String
        get() = emprego?.nome ?: "Emprego"

    val empregoAtivo: Boolean
        get() = emprego?.ativo == true

    val totalCargos: Int
        get() = cargos.size
}

/**
 * Ações disparadas pela UI.
 */
sealed interface EmpregoSettingsDetailAction {
    data object Recarregar : EmpregoSettingsDetailAction
    data object NavegarParaVersoes : EmpregoSettingsDetailAction
    data object NavegarParaAusencias : EmpregoSettingsDetailAction
    data object NavegarParaAjustesSaldo : EmpregoSettingsDetailAction
    data object NavegarParaEditar : EmpregoSettingsDetailAction
    data object NavegarParaCargos : EmpregoSettingsDetailAction
    data object NavegarParaOpcoesRegistro : EmpregoSettingsDetailAction
    data object NavegarParaConfiguracaoGeral : EmpregoSettingsDetailAction
    data object LimparErro : EmpregoSettingsDetailAction
}

/**
 * Eventos emitidos pelo ViewModel.
 */
sealed interface EmpregoSettingsDetailEvent {
    data class NavegarParaVersoes(val empregoId: Long) : EmpregoSettingsDetailEvent
    data class NavegarParaAusencias(val empregoId: Long) : EmpregoSettingsDetailEvent
    data class NavegarParaAjustesSaldo(val empregoId: Long) : EmpregoSettingsDetailEvent
    data class NavegarParaEditar(val empregoId: Long) : EmpregoSettingsDetailEvent
    data class NavegarParaCargos(val empregoId: Long) : EmpregoSettingsDetailEvent
    data class NavegarParaOpcoesRegistro(val empregoId: Long) : EmpregoSettingsDetailEvent
    data class MostrarMensagem(val mensagem: String) : EmpregoSettingsDetailEvent
}
