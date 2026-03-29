package br.com.tlmacedo.meuponto.presentation.screen.auth.login

sealed interface LoginAction {
    data class OnEmailChange(val email: String) : LoginAction
    data class OnSenhaChange(val senha: String) : LoginAction
    data object ToggleSenhaVisibility : LoginAction
    data object FazerLogin : LoginAction
    data object LimparErro : LoginAction
    data object ForgotPasswordClick : LoginAction
    data object RegisterClick : LoginAction
}
