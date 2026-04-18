package br.com.tlmacedo.meuponto.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.tlmacedo.meuponto.domain.repository.EmpregoRepository
import br.com.tlmacedo.meuponto.domain.repository.PreferenciasRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * ViewModel global para gerenciar o estado do aplicativo que deve persistir
 * entre diferentes telas, como o tema selecionado.
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    private val preferenciasRepository: PreferenciasRepository,
    private val empregoRepository: EmpregoRepository
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

    /**
     * Determina se é a primeira execução.
     * Considera 'true' apenas se:
     * 1. A flag de onboarding não estiver concluída nas preferências.
     * 2. NÃO existirem empregos cadastrados no banco de dados.
     */
    val isPrimeiraExecucao: StateFlow<Boolean> = combine(
        preferenciasRepository.observarPrimeiraExecucao(),
        empregoRepository.observarTodos().map { it.isEmpty() }
    ) { flagPref, semEmpregos ->
        flagPref && semEmpregos
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )
}
