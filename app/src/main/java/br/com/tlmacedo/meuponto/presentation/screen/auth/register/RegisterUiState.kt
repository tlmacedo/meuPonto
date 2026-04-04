package br.com.tlmacedo.meuponto.presentation.screen.auth.register

data class RegisterUiState(
    val nome: String = "",
    val nomeErro: String? = null,
    val email: String = "",
    val emailErro: String? = null,
    val senha: String = "",
    val senhaErro: String? = null,
    val confirmarSenha: String = "",
    val confirmarSenhaErro: String? = null,
    val isCarregando: Boolean = false,
    val erro: String? = null,
    val isSenhaVisivel: Boolean = false,
    val isFormValido: Boolean = false
)
