package br.com.tlmacedo.meuponto.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.tlmacedo.meuponto.domain.repository.PreferenciasRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * ViewModel global para gerenciar o estado do aplicativo que deve persistir
 * entre diferentes telas, como o tema selecionado.
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    private val preferenciasRepository: PreferenciasRepository
) : ViewModel() {

    /**
     * Observa o tema configurado nas preferências.
     */
    val tema: StateFlow<String> = preferenciasRepository.observarTema()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "system"
        )
}
