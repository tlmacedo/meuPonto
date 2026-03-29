package br.com.tlmacedo.meuponto.presentation.screen.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<LoginEvent>()
    val events: SharedFlow<LoginEvent> = _events.asSharedFlow()

    fun onAction(action: LoginAction) {
        when (action) {
            is LoginAction.EmailChanged -> _uiState.update { it.copy(email = action.email, emailError = null) }
            is LoginAction.SenhaChanged -> _uiState.update { it.copy(senha = action.senha, senhaError = null) }
            LoginAction.TogglePasswordVisibility -> _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
            LoginAction.LoginClick -> realizarLogin()
            LoginAction.LoginBiometriaClick -> realizarLoginBiometria()
            LoginAction.ForgotPasswordClick -> viewModelScope.launch { _events.emit(LoginEvent.NavigateToForgotPassword) }
            LoginAction.RegisterClick -> viewModelScope.launch { _events.emit(LoginEvent.NavigateToRegister) }
        }
    }

    private fun realizarLogin() {
        val state = _uiState.value
        if (state.email.isBlank() || state.senha.isBlank()) {
            viewModelScope.launch { _events.emit(LoginEvent.ShowError("Preencha todos os campos")) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            delay(1500) // Simulação de rede

            if (state.email == "admin@admin.com" && state.senha == "123456") {
                _uiState.update { it.copy(isLoading = false) }
                _events.emit(LoginEvent.LoginSuccess)
            } else {
                _uiState.update { it.copy(isLoading = false) }
                _events.emit(LoginEvent.ShowError("Credenciais inválidas! Tente admin@admin.com / 123456"))
            }
        }
    }

    private fun realizarLoginBiometria() {
        viewModelScope.launch {
            _events.emit(LoginEvent.ShowError("Biometria em desenvolvimento"))
        }
    }
}
