package br.com.tlmacedo.meuponto.presentation.screen.auth.login

sealed interface LoginEvent {
    data object LoginSucesso : LoginEvent
    data class MostrarErro(val mensagem: String) : LoginEvent
    data object NavigateToRegister : LoginEvent
    data object NavigateToForgotPassword : LoginEvent
}
