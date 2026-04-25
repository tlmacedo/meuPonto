package br.com.tlmacedo.meuponto.presentation.screen.chamado.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.tlmacedo.meuponto.domain.model.chamado.Chamado
import br.com.tlmacedo.meuponto.domain.model.chamado.HistoricoChamado
import br.com.tlmacedo.meuponto.domain.repository.ChamadoRepository
import br.com.tlmacedo.meuponto.presentation.navigation.MeuPontoDestinations
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ChamadoDetailViewModel @Inject constructor(
    private val chamadoRepository: ChamadoRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val chamadoId: Long = savedStateHandle[MeuPontoDestinations.ARG_CHAMADO_ID] ?: -1L

    private val _uiState = MutableStateFlow<ChamadoDetailUiState>(ChamadoDetailUiState.Loading)
    val uiState: StateFlow<ChamadoDetailUiState> = _uiState.asStateFlow()

    init {
        loadChamadoDetails()
    }

    private fun loadChamadoDetails() {
        if (chamadoId == -1L) {
            _uiState.value = ChamadoDetailUiState.Error("ID do chamado inválido.")
            return
        }

        viewModelScope.launch {
            combine(
                chamadoRepository.observarPorId(chamadoId),
                chamadoRepository.observarHistorico(chamadoId)
            ) { chamado, historico ->
                if (chamado == null) {
                    ChamadoDetailUiState.Error("Chamado não encontrado.")
                } else {
                    ChamadoDetailUiState.Success(chamado, historico)
                }
            }.catch { e ->
                Timber.e(e, "Erro ao carregar detalhes do chamado")
                _uiState.value =
                    ChamadoDetailUiState.Error("Erro ao carregar detalhes: ${e.localizedMessage}")
            }.collect { state ->
                _uiState.value = state
            }
        }
    }
}

sealed class ChamadoDetailUiState {
    object Loading : ChamadoDetailUiState()
    data class Success(val chamado: Chamado, val historico: List<HistoricoChamado>) :
        ChamadoDetailUiState()

    data class Error(val message: String) : ChamadoDetailUiState()
}
