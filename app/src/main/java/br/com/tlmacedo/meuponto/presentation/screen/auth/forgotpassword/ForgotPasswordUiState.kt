package br.com.tlmacedo.meuponto.presentation.screen.auth.forgotpassword

data class ForgotPasswordUiState(
    val email: String = "",
    val emailErro: String? = null,
    val isCarregando: Boolean = false,
    val mensagemSucesso: String? = null,
    val erro: String? = null,
    val isFormValido: Boolean = false
)
