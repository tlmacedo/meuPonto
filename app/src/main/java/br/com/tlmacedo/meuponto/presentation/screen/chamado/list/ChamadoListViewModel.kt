// path: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/chamado/list/ChamadoListViewModel.kt
package br.com.tlmacedo.meuponto.presentation.screen.chamado.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.tlmacedo.meuponto.domain.model.chamado.Chamado
import br.com.tlmacedo.meuponto.domain.repository.ChamadoRepository
import br.com.tlmacedo.meuponto.domain.repository.PreferenciasRepository // Importar PreferenciasRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// Define o estado da UI para a tela de listagem de chamados
data class ChamadoListUiState(
    val isLoading: Boolean = true,
    val chamados: List<Chamado> = emptyList(),
    val errorMessage: String? = null,
    val usuarioEmail: String? = null // Adiciona o email do usuário ao estado
)

@HiltViewModel
class ChamadoListViewModel @Inject constructor(
    private val chamadoRepository: ChamadoRepository,
    private val preferenciasRepository: PreferenciasRepository // Injetar PreferenciasRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChamadoListUiState())
    val uiState: StateFlow<ChamadoListUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            // Obter o email do usuário logado das preferências
            // CORREÇÃO AQUI: Usando obterUltimoEmailLogado() do PreferenciasRepository
            val email = preferenciasRepository.obterUltimoEmailLogado()
            _uiState.update { it.copy(usuarioEmail = email) }

            if (email != null) {
                carregarChamados(email)
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Usuário não logado. Não é possível carregar chamados."
                    )
                }
            }
        }
    }

    fun carregarChamados(usuarioEmail: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                // Modificar o repositório para buscar chamados por usuário
                // Se o ChamadoRepository ainda não tem um método para isso, precisaremos criá-lo.
                // Por enquanto, vamos assumir que existe um método observarTodosPorUsuario(email)
                chamadoRepository.observarTodosPorUsuario(usuarioEmail).collectLatest { chamados ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            chamados = chamados,
                            errorMessage = null
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        chamados = emptyList(),
                        errorMessage = "Erro ao carregar chamados: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    // Futuramente, aqui teremos funções para filtrar, pesquisar, etc.
}