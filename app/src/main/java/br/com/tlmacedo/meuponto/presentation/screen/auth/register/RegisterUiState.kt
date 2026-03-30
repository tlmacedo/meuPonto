package br.com.tlmacedo.meuponto.presentation.screen.auth.register

data class RegisterUiState(
    val nome: String = "",
    val email: String = "",
    val senha: String = "",
    val confirmarSenha: String = "",
    val isCarregando: Boolean = false,
    val erro: String? = null,
    val isSenhaVisivel: Boolean = false
)
