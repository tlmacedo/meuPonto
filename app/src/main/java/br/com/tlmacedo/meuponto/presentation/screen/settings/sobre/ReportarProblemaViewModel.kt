package br.com.tlmacedo.meuponto.presentation.screen.settings.sobre

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.tlmacedo.meuponto.domain.usecase.emprego.ObterEmpregoAtivoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReportarProblemaUiState(
    val empregoApelido: String? = null,
    val empregoNome: String? = null,
    val empregoLogo: String? = null
)

@HiltViewModel
class ReportarProblemaViewModel @Inject constructor(
    private val obterEmpregoAtivoUseCase: ObterEmpregoAtivoUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportarProblemaUiState())
    val uiState: StateFlow<ReportarProblemaUiState> = _uiState.asStateFlow()

    init {
        carregarEmpregoAtivo()
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
}
