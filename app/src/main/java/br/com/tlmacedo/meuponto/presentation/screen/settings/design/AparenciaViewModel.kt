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
 * Estado da tela de aparência.
 */
data class AparenciaUiState(
    val isLoading: Boolean = true,
    val temaSelecionado: String = "system",
    val empregoApelido: String? = null,
    val empregoNome: String? = null,
    val empregoLogo: String? = null
)

/**
 * Ações da tela de aparência.
 */
sealed interface AparenciaAction {
    data class SelecionarTema(val tema: String) : AparenciaAction
}

/**
 * Eventos da tela de aparência.
 */
sealed interface AparenciaEvent {
    data class MostrarMensagem(val mensagem: String) : AparenciaEvent
}

/**
 * ViewModel da tela de aparência.
 *
 * @author Thiago
 * @since 9.0.0
 */
@HiltViewModel
class AparenciaViewModel @Inject constructor(
    private val preferenciasRepository: PreferenciasRepository,
    private val obterEmpregoAtivoUseCase: ObterEmpregoAtivoUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AparenciaUiState())
    val uiState: StateFlow<AparenciaUiState> = _uiState.asStateFlow()

    private val _eventos = MutableSharedFlow<AparenciaEvent>()
    val eventos: SharedFlow<AparenciaEvent> = _eventos.asSharedFlow()

    init {
        observarTema()
        carregarEmpregoAtivo()
    }

    fun onAction(action: AparenciaAction) {
        when (action) {
            is AparenciaAction.SelecionarTema -> selecionarTema(action.tema)
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

    private fun observarTema() {
        viewModelScope.launch {
            preferenciasRepository.observarTema()
                .collectLatest { tema ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            temaSelecionado = tema
                        )
                    }
                }
        }
    }

    private fun selecionarTema(tema: String) {
        viewModelScope.launch {
            preferenciasRepository.definirTema(tema)
            val nomeTema = when (tema) {
                "light" -> "Claro"
                "dark" -> "Escuro"
                "sidia" -> "Sidia"
                "sidia_dark" -> "Sidia Dark"
                else -> "Sistema"
            }
            _eventos.emit(AparenciaEvent.MostrarMensagem("Tema alterado para $nomeTema"))
        }
    }
}
