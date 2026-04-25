package br.com.tlmacedo.meuponto.presentation.screen.settings.design

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.tlmacedo.meuponto.domain.repository.PreferenciasRepository
import br.com.tlmacedo.meuponto.domain.usecase.emprego.ObterEmpregoAtivoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PrivacidadeUiState(
    val biometriaHabilitada: Boolean = false,
    val bloqueioAutomatico: Boolean = false,
    val ocultarPreview: Boolean = false,
    val empregoApelido: String? = null,
    val empregoNome: String? = null,
    val empregoLogo: String? = null
)

@HiltViewModel
class PrivacidadeViewModel @Inject constructor(
    private val preferenciasRepository: PreferenciasRepository,
    private val obterEmpregoAtivoUseCase: ObterEmpregoAtivoUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PrivacidadeUiState())
    val uiState: StateFlow<PrivacidadeUiState> = _uiState.asStateFlow()

    init {
        carregarPreferencias()
        carregarEmpregoAtivo()
    }

    private fun carregarPreferencias() {
        viewModelScope.launch {
            preferenciasRepository.observarBiometriaHabilitada().collectLatest { habilitada ->
                _uiState.update { it.copy(biometriaHabilitada = habilitada) }
            }
        }
        viewModelScope.launch {
            preferenciasRepository.observarBloqueioAutomaticoHabilitado()
                .collectLatest { habilitada ->
                    _uiState.update { it.copy(bloqueioAutomatico = habilitada) }
                }
        }
        viewModelScope.launch {
            preferenciasRepository.observarOcultarPreviewHabilitado().collectLatest { habilitada ->
                _uiState.update { it.copy(ocultarPreview = habilitada) }
            }
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

    fun toggleBiometria(habilitado: Boolean) {
        viewModelScope.launch {
            preferenciasRepository.definirBiometriaHabilitada(habilitado)
            if (!habilitado) {
                preferenciasRepository.definirBloqueioAutomaticoHabilitado(false)
            }
        }
    }

    fun toggleBloqueioAutomatico(habilitado: Boolean) {
        viewModelScope.launch {
            preferenciasRepository.definirBloqueioAutomaticoHabilitado(habilitado)
        }
    }

    fun toggleOcultarPreview(habilitado: Boolean) {
        viewModelScope.launch {
            preferenciasRepository.definirOcultarPreviewHabilitado(habilitado)
        }
    }
}
