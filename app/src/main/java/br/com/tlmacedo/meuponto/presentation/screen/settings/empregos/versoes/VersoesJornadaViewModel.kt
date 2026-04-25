package br.com.tlmacedo.meuponto.presentation.screen.settings.empregos.versoes

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import javax.inject.Inject

/**
 * ViewModel da tela de histórico de versões de jornada.
 */
@HiltViewModel
class VersoesJornadaViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val empregoRepository: EmpregoRepository,
    private val versaoJornadaRepository: VersaoJornadaRepository
) : ViewModel() {

    private val empregoId: Long =
        savedStateHandle.get<Long>(MeuPontoDestinations.ARG_EMPREGO_ID) ?: -1L

    private val _uiState = MutableStateFlow(VersoesJornadaUiState(empregoId = empregoId))
    val uiState: StateFlow<VersoesJornadaUiState> = _uiState.asStateFlow()

    private val _eventos = MutableSharedFlow<VersoesJornadaEvent>()
    val eventos: SharedFlow<VersoesJornadaEvent> = _eventos.asSharedFlow()

    init {
        carregarDados()
    }

    private fun carregarDados() {
        if (empregoId <= 0) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Observar emprego para pegar nome, apelido e logo
            launch {
                empregoRepository.observarPorId(empregoId).collect { emprego ->
                    _uiState.update {
                        it.copy(
                            nomeEmprego = emprego?.nome ?: "Emprego",
                            apelidoEmprego = emprego?.apelido,
                            logoEmprego = emprego?.logo
                        )
                    }
                }
            }

            val versoes = versaoJornadaRepository.listarPorEmprego(empregoId)
                .sortedByDescending { it.dataInicio }

            _uiState.update {
                it.copy(
                    versoes = versoes,
                    isLoading = false
                )
            }
        }
    }

    fun criarNovaVersao() {
        // Lógica para navegar para a tela de criação de nova versão
        // ou abrir um dialog. Por enquanto, vamos emitir um evento.
        viewModelScope.launch {
            _eventos.emit(VersoesJornadaEvent.NavegarParaNovaVersao(empregoId))
        }
    }
}

sealed class VersoesJornadaEvent {
    data class NavegarParaNovaVersao(val empregoId: Long) : VersoesJornadaEvent()
    data class MostrarErro(val mensagem: String) : VersoesJornadaEvent()
}
