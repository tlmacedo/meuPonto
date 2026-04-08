package br.com.tlmacedo.meuponto.presentation.screen.settings.design

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.tlmacedo.meuponto.domain.repository.PreferenciasRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PrivacidadeViewModel @Inject constructor(
    private val preferenciasRepository: PreferenciasRepository
) : ViewModel() {

    val biometriaHabilitada: StateFlow<Boolean> = preferenciasRepository
        .observarBiometriaHabilitada()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val bloqueioAutomatico: StateFlow<Boolean> = preferenciasRepository
        .observarBloqueioAutomaticoHabilitado()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val ocultarPreview: StateFlow<Boolean> = preferenciasRepository
        .observarOcultarPreviewHabilitado()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

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
