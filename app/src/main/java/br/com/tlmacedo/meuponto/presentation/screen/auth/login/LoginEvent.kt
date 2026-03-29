package br.com.tlmacedo.meuponto.presentation.screen.auth.login

sealed interface LoginEvent {
    object LoginSuccess : LoginEvent
    data class ShowError(val message: String) : LoginEvent
    object NavigateToRegister : LoginEvent
    object NavigateToForgotPassword : LoginEvent
}
