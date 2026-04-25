package br.com.tlmacedo.meuponto.presentation.screen.chamado.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.tlmacedo.meuponto.domain.model.chamado.Chamado
import br.com.tlmacedo.meuponto.domain.repository.AuthRepository
import br.com.tlmacedo.meuponto.domain.repository.ChamadoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
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
        viewModelScope.launch {
            authRepository.observarUsuarioLogado()
                .filterNotNull()
                .flatMapLatest { usuario ->
                    chamadoRepository.observarTodosPorUsuario(usuario.email)
                }
                .onEach { chamados ->
                    if (chamados.isEmpty()) {
                        _uiState.value = ChamadoListUiState.Empty
                    } else {
                        _uiState.value = ChamadoListUiState.Success(chamados)
                    }
                }
                .catch { e ->
                    Timber.e(e, "Erro ao carregar chamados")
                    _uiState.value = ChamadoListUiState.Error("Erro ao carregar chamados: ${e.localizedMessage}")
                }
                .launchIn(viewModelScope)
        }
    }
}

sealed class ChamadoListUiState {
    object Loading : ChamadoListUiState()
    object Empty : ChamadoListUiState()
    data class Success(val chamados: List<Chamado>) : ChamadoListUiState()
    data class Error(val message: String) : ChamadoListUiState()
}