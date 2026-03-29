package br.com.tlmacedo.meuponto.presentation.screen.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _eventos = MutableSharedFlow<LoginEvent>()
    val eventos: SharedFlow<LoginEvent> = _eventos.asSharedFlow()

    fun onAction(action: LoginAction) {
        when (action) {
            is LoginAction.OnEmailChange -> _uiState.update { it.copy(email = action.email, erro = null) }
            is LoginAction.OnSenhaChange -> _uiState.update { it.copy(senha = action.senha, erro = null) }
            LoginAction.ToggleSenhaVisibility -> _uiState.update { it.copy(isSenhaVisivel = !it.isSenhaVisivel) }
            LoginAction.LimparErro -> _uiState.update { it.copy(erro = null) }
            LoginAction.FazerLogin -> fazerLogin()
            LoginAction.ForgotPasswordClick -> viewModelScope.launch { _eventos.emit(LoginEvent.NavigateToForgotPassword) }
            LoginAction.RegisterClick -> viewModelScope.launch { _eventos.emit(LoginEvent.NavigateToRegister) }
        }
    }

    private fun fazerLogin() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            delay(1000) // Simula rede
            if (_uiState.value.email == "admin@admin.com" && _uiState.value.senha == "123456") {
                _uiState.update { it.copy(isLoading = false) }
                _eventos.emit(LoginEvent.LoginSucesso)
            } else {
                _uiState.update { it.copy(isLoading = false, erro = "E-mail ou senha incorretos") }
                _eventos.emit(LoginEvent.MostrarErro("E-mail ou senha incorretos"))
            }
        }
    }
}
