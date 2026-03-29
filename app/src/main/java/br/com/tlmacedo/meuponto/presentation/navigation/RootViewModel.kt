package br.com.tlmacedo.meuponto.presentation.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.tlmacedo.meuponto.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RootViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _startDestination = MutableStateFlow("auth_graph")
    val startDestination: StateFlow<String> = _startDestination.asStateFlow()

    init {
        verificarSessao()
    }

    private fun verificarSessao() {
        viewModelScope.launch {
            val isLogado = authRepository.isUsuarioLogado()
            _startDestination.value = if (isLogado) "main_app" else "auth_graph"
            _isLoading.value = false
        }
    }
}
