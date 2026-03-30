package br.com.tlmacedo.meuponto.presentation.screen.auth.login

data class LoginUiState(
    val email: String = "",
    val emailErro: String? = null,
    val senha: String = "",
    val senhaErro: String? = null,
    val isCarregando: Boolean = false,
    val isSenhaVisivel: Boolean = false,
    val biometriaDisponivel: Boolean = false,
    val isFormValido: Boolean = false,
    val erro: String? = null,
    val lembrarMe: Boolean = false
)
