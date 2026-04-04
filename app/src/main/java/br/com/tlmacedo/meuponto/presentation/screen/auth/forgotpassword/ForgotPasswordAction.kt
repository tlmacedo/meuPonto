package br.com.tlmacedo.meuponto.presentation.screen.auth.forgotpassword

sealed interface ForgotPasswordAction {
    data class EmailAlterado(val email: String) : ForgotPasswordAction
    data object ClicarEnviar : ForgotPasswordAction
    data object ClicarVoltar : ForgotPasswordAction
}
