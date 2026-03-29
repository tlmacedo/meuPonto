package br.com.tlmacedo.meuponto.presentation.screen.auth

data class AuthUiState(
    val isLoading: Boolean = false,
    val email: String = "",
    val emailError: String? = null,
    val password: String = "",
    val passwordError: String? = null,
    val confirmPassword: String = "",
    val confirmPasswordError: String? = null,
    val name: String = "",
    val nameError: String? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isAuthenticated: Boolean = false
)
