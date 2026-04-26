package br.com.tlmacedo.meuponto.presentation.screen.chamado.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.tlmacedo.meuponto.domain.model.chamado.Chamado
import br.com.tlmacedo.meuponto.domain.repository.AuthRepository
import br.com.tlmacedo.meuponto.domain.repository.ChamadoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import timber.log.Timber
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ChamadoListViewModel @Inject constructor(
    private val chamadoRepository: ChamadoRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ChamadoListUiState>(ChamadoListUiState.Loading)
    val uiState: StateFlow<ChamadoListUiState> = _uiState.asStateFlow()

    init {
        loadChamados()
    }

    fun loadChamados() {
        authRepository.observarUsuarioLogado()
            .flatMapLatest { usuario ->
                if (usuario == null) {
                    flowOf(ChamadoListUiState.Error("Usuário não autenticado. Por favor, faça login novamente."))
                } else {
                    chamadoRepository.observarTodosPorUsuario(usuario.email)
                        .map { chamados ->
                            if (chamados.isEmpty()) {
                                ChamadoListUiState.Empty
                            } else {
                                ChamadoListUiState.Success(chamados)
                            }
                        }
                }
            }
            .onStart { _uiState.value = ChamadoListUiState.Loading }
            .catch { e ->
                Timber.e(e, "Erro ao carregar chamados")
                _uiState.value =
                    ChamadoListUiState.Error("Erro ao carregar chamados: ${e.localizedMessage}")
            }
            .onEach { state ->
                _uiState.value = state
            }
            .launchIn(viewModelScope)
    }
}

sealed class ChamadoListUiState {
    object Loading : ChamadoListUiState()
    object Empty : ChamadoListUiState()
    data class Success(val chamados: List<Chamado>) : ChamadoListUiState()
    data class Error(val message: String) : ChamadoListUiState()
}
