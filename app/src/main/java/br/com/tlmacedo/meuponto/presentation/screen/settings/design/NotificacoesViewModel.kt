package br.com.tlmacedo.meuponto.presentation.screen.settings.design

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.tlmacedo.meuponto.domain.repository.PreferenciasRepository
import br.com.tlmacedo.meuponto.domain.usecase.emprego.ObterEmpregoAtivoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Estado da tela de notificações.
 */
data class NotificacoesUiState(
    val isLoading: Boolean = true,
    val notificacoesHabilitadas: Boolean = true,
    val lembreteEntrada: Boolean = true,
    val lembreteSaida: Boolean = true,
    val lembreteIntervalo: Boolean = false,
    val alertaHoraExtra: Boolean = true,
    val alertaJornadaMaxima: Boolean = true,
    val vibracaoHabilitada: Boolean = true,
    val somHabilitado: Boolean = true,
    val empregoApelido: String? = null,
    val empregoNome: String? = null,
    val empregoLogo: String? = null
)

/**
 * Ações da tela de notificações.
 */
sealed interface NotificacoesAction {
    data object ToggleNotificacoes : NotificacoesAction
    data object ToggleLembreteEntrada : NotificacoesAction
    data object ToggleLembreteSaida : NotificacoesAction
    data object ToggleLembreteIntervalo : NotificacoesAction
    data object ToggleAlertaHoraExtra : NotificacoesAction
    data object ToggleAlertaJornadaMaxima : NotificacoesAction
    data object ToggleVibracao : NotificacoesAction
    data object ToggleSom : NotificacoesAction
}

/**
 * Eventos da tela de notificações.
 */
sealed interface NotificacoesEvent {
    data class MostrarMensagem(val mensagem: String) : NotificacoesEvent
}

/**
 * ViewModel da tela de notificações.
 *
 * @author Thiago
 * @since 9.0.0
 */
@HiltViewModel
class NotificacoesViewModel @Inject constructor(
    private val preferenciasRepository: PreferenciasRepository,
    private val obterEmpregoAtivoUseCase: ObterEmpregoAtivoUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificacoesUiState())
    val uiState: StateFlow<NotificacoesUiState> = _uiState.asStateFlow()

    private val _eventos = MutableSharedFlow<NotificacoesEvent>()
    val eventos: SharedFlow<NotificacoesEvent> = _eventos.asSharedFlow()

    init {
        carregarPreferencias()
        carregarEmpregoAtivo()
    }

    fun onAction(action: NotificacoesAction) {
        when (action) {
            NotificacoesAction.ToggleNotificacoes -> toggleNotificacoes()
            NotificacoesAction.ToggleLembreteEntrada -> toggleLembreteEntrada()
            NotificacoesAction.ToggleLembreteSaida -> toggleLembreteSaida()
            NotificacoesAction.ToggleLembreteIntervalo -> toggleLembreteIntervalo()
            NotificacoesAction.ToggleAlertaHoraExtra -> toggleAlertaHoraExtra()
            NotificacoesAction.ToggleAlertaJornadaMaxima -> toggleAlertaJornadaMaxima()
            NotificacoesAction.ToggleVibracao -> toggleVibracao()
            NotificacoesAction.ToggleSom -> toggleSom()
        }
    }

    private fun carregarEmpregoAtivo() {
        viewModelScope.launch {
            obterEmpregoAtivoUseCase.observar().collectLatest { emprego ->
                _uiState.update {
                    it.copy(
                        empregoApelido = emprego?.apelido,
                        empregoNome = emprego?.nome,
                        empregoLogo = emprego?.logo
                    )
                }
            }
        }
    }

    private fun carregarPreferencias() {
        viewModelScope.launch {
            preferenciasRepository.observarNotificacoesHabilitadas()
                .collectLatest { habilitadas ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            notificacoesHabilitadas = habilitadas
                        )
                    }
                }
        }
    }

    private fun toggleNotificacoes() {
        viewModelScope.launch {
            val novoValor = !_uiState.value.notificacoesHabilitadas
            preferenciasRepository.definirNotificacoesHabilitadas(novoValor)
            _uiState.update { it.copy(notificacoesHabilitadas = novoValor) }
            val mensagem = if (novoValor) "Notificações ativadas" else "Notificações desativadas"
            _eventos.emit(NotificacoesEvent.MostrarMensagem(mensagem))
        }
    }

    // TODO: Implementar persistência dessas preferências específicas
    private fun toggleLembreteEntrada() {
        _uiState.update { it.copy(lembreteEntrada = !it.lembreteEntrada) }
    }

    private fun toggleLembreteSaida() {
        _uiState.update { it.copy(lembreteSaida = !it.lembreteSaida) }
    }

    private fun toggleLembreteIntervalo() {
        _uiState.update { it.copy(lembreteIntervalo = !it.lembreteIntervalo) }
    }

    private fun toggleAlertaHoraExtra() {
        _uiState.update { it.copy(alertaHoraExtra = !it.alertaHoraExtra) }
    }

    private fun toggleAlertaJornadaMaxima() {
        _uiState.update { it.copy(alertaJornadaMaxima = !it.alertaJornadaMaxima) }
    }

    private fun toggleVibracao() {
        _uiState.update { it.copy(vibracaoHabilitada = !it.vibracaoHabilitada) }
    }

    private fun toggleSom() {
        _uiState.update { it.copy(somHabilitado = !it.somHabilitado) }
    }
}
