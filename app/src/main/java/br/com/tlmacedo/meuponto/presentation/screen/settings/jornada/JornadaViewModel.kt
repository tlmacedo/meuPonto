package br.com.tlmacedo.meuponto.presentation.screen.settings.jornada

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.tlmacedo.meuponto.domain.repository.PreferenciasRepository
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
 * ViewModel para a tela de configurações globais de jornada.
 */
@HiltViewModel
class JornadaViewModel @Inject constructor(
    private val preferenciasRepository: PreferenciasRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(JornadaUiState())
    val uiState: StateFlow<JornadaUiState> = _uiState.asStateFlow()

    private val _eventos = MutableSharedFlow<JornadaEvent>()
    val eventos: SharedFlow<JornadaEvent> = _eventos.asSharedFlow()

    init {
        carregarPreferencias()
    }

    private fun carregarPreferencias() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                // Observando os valores em tempo real ou apenas carregando os iniciais? 
                // Para simplificar a edição, vamos observar e atualizar o estado.
                val carga = preferenciasRepository.obterCargaHorariaPadrao()
                val intervalo = preferenciasRepository.obterIntervaloMinimoPadrao()
                val tolerancia = preferenciasRepository.obterToleranciaGeralPadrao()

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        cargaHorariaMinutos = carga,
                        intervaloMinimoMinutos = intervalo,
                        toleranciaGeralMinutos = tolerancia
                    )
                }
            } catch (e: Exception) {
                Timber.e(e, "Erro ao carregar preferências de jornada")
                _uiState.update { it.copy(isLoading = false, mensagemErro = e.message) }
            }
        }
    }

    fun onAction(action: JornadaAction) {
        when (action) {
            is JornadaAction.AlterarCargaHoraria -> atualizarCargaHoraria(action.minutos)
            is JornadaAction.AlterarIntervaloMinimo -> atualizarIntervaloMinimo(action.minutos)
            is JornadaAction.AlterarToleranciaGeral -> atualizarToleranciaGeral(action.minutos)
            JornadaAction.LimparErro -> _uiState.update { it.copy(mensagemErro = null) }
        }
    }

    private fun atualizarCargaHoraria(minutos: Int) {
        viewModelScope.launch {
            preferenciasRepository.definirCargaHorariaPadrao(minutos)
            _uiState.update { it.copy(cargaHorariaMinutos = minutos) }
            _eventos.emit(JornadaEvent.MostrarMensagem("Carga horária atualizada"))
        }
    }

    private fun atualizarIntervaloMinimo(minutos: Int) {
        viewModelScope.launch {
            preferenciasRepository.definirIntervaloMinimoPadrao(minutos)
            _uiState.update { it.copy(intervaloMinimoMinutos = minutos) }
            _eventos.emit(JornadaEvent.MostrarMensagem("Intervalo mínimo atualizado"))
        }
    }

    private fun atualizarToleranciaGeral(minutos: Int) {
        viewModelScope.launch {
            preferenciasRepository.definirToleranciaGeralPadrao(minutos)
            _uiState.update { it.copy(toleranciaGeralMinutos = minutos) }
            _eventos.emit(JornadaEvent.MostrarMensagem("Tolerância geral atualizada"))
        }
    }
}
