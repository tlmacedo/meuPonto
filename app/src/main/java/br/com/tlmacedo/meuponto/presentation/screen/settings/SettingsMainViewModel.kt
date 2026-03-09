package br.com.tlmacedo.meuponto.presentation.screen.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.tlmacedo.meuponto.domain.usecase.emprego.ObterEmpregoAtivoUseCase
import br.com.tlmacedo.meuponto.domain.repository.VersaoJornadaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsMainUiState(
    val isLoading: Boolean = true,
    val empregoAtualId: Long? = null,
    val empregoAtualNome: String? = null,
    val versaoVigenteDescricao: String? = null
)

@HiltViewModel
class SettingsMainViewModel @Inject constructor(
    private val obterEmpregoAtivoUseCase: ObterEmpregoAtivoUseCase,
    private val versaoJornadaRepository: VersaoJornadaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsMainUiState())
    val uiState: StateFlow<SettingsMainUiState> = _uiState.asStateFlow()

    init {
        observarEmpregoAtivo()
    }

    private fun observarEmpregoAtivo() {
        viewModelScope.launch {
            obterEmpregoAtivoUseCase.observar()
                .collectLatest { emprego ->
                    if (emprego != null) {
                        val versaoVigente = versaoJornadaRepository.buscarVigente(emprego.id)
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                empregoAtualId = emprego.id,
                                empregoAtualNome = emprego.nome,
                                versaoVigenteDescricao = versaoVigente?.titulo
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                empregoAtualId = null,
                                empregoAtualNome = null,
                                versaoVigenteDescricao = null
                            )
                        }
                    }
                }
        }
    }
}
