package br.com.tlmacedo.meuponto.presentation.screen.auth.forgotpassword

sealed interface ForgotPasswordEvent {
    data class MostrarErro(val mensagem: String) : ForgotPasswordEvent
    data class MostrarSucesso(val mensagem: String) : ForgotPasswordEvent
    data object NavegarVoltar : ForgotPasswordEvent
}
