package br.com.tlmacedo.meuponto.presentation.screen.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email, emailError = null) }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password, passwordError = null) }
    }

    fun onConfirmPasswordChange(password: String) {
        _uiState.update { it.copy(confirmPassword = password, confirmPasswordError = null) }
    }

    fun onNameChange(name: String) {
        _uiState.update { it.copy(name = name, nameError = null) }
    }

    fun login() {
        val state = _uiState.value
        if (state.email.isBlank() || state.password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Preencha todos os campos") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            // TODO: Substituir pelo seu UseCase/Repository de Autenticação real
            delay(1500) // Simulando chamada de rede

            if (state.email == "admin@teste.com" && state.password == "123456") {
                _uiState.update { it.copy(isLoading = false, isAuthenticated = true) }
            } else {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Credenciais inválidas") }
            }
        }
    }

    fun register() {
        val state = _uiState.value
        if (state.password != state.confirmPassword) {
            _uiState.update { it.copy(confirmPasswordError = "As senhas não coincidem") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            delay(1500) // Simulando chamada de rede
            _uiState.update { it.copy(isLoading = false, successMessage = "Conta criada com sucesso!") }
        }
    }

    fun resetPassword() {
        val state = _uiState.value
        if (state.email.isBlank()) {
            _uiState.update { it.copy(emailError = "Informe seu e-mail") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            delay(1500) // Simulando chamada de rede
            _uiState.update { it.copy(isLoading = false, successMessage = "E-mail de recuperação enviado!") }
        }
    }

    fun clearError() { _uiState.update { it.copy(errorMessage = null) } }
    fun clearSuccess() { _uiState.update { it.copy(successMessage = null) } }
}
