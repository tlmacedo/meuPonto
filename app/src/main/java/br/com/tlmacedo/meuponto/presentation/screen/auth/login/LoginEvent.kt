package br.com.tlmacedo.meuponto.presentation.screen.auth.login

sealed interface LoginEvent {
    object LoginSucesso : LoginEvent
    data class MostrarErro(val mensagem: String) : LoginEvent
    object NavegarParaCadastro : LoginEvent
    object NavegarParaEsqueciSenha : LoginEvent
    object NavegarParaRegistro : LoginEvent

}
