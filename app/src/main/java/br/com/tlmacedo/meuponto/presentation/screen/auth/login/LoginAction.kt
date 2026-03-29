package br.com.tlmacedo.meuponto.presentation.screen.auth.login

sealed interface LoginAction {
    data class EmailChanged(val email: String) : LoginAction
    data class SenhaChanged(val senha: String) : LoginAction
    object TogglePasswordVisibility : LoginAction
    object LoginClick : LoginAction
    object LoginBiometriaClick : LoginAction
    object ForgotPasswordClick : LoginAction
    object RegisterClick : LoginAction
}
