package br.com.tlmacedo.meuponto.presentation.screen.auth.login

data class LoginUiState(
    val email: String = "",
    val senha: String = "",
    val isSenhaVisivel: Boolean = false,
    val isLoading: Boolean = false,
    val erro: String? = null
) {
    val isBotaoHabilitado: Boolean
        get() = email.isNotBlank() && senha.isNotBlank() && !isLoading
}
