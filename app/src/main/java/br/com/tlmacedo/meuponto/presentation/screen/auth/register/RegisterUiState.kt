package br.com.tlmacedo.meuponto.presentation.screen.auth.register

data class RegisterUiState(
    val nome: String = "",
    val email: String = "",
    val senha: String = "",
    val confirmarSenha: String = "",
    val erro: String? = null,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false
) {
    val formularioValido: Boolean
        get() = nome.isNotBlank() && email.isNotBlank() && senha.isNotBlank() && senha == confirmarSenha
}
