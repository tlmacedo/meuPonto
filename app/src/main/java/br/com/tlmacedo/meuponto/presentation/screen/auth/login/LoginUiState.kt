package br.com.tlmacedo.meuponto.presentation.screen.auth.login

data class LoginUiState(
    val email: String = "",
    val emailError: String? = null,
    val senha: String = "",
    val senhaError: String? = null,
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val biometriaDisponivel: Boolean = true
) {
    val isFormValid: Boolean get() = email.isNotBlank() && senha.isNotBlank() && emailError == null && senhaError == null
}
