package br.com.tlmacedo.meuponto.presentation.screen.auth.register

sealed interface RegisterEvent {
    data class CadastroSucesso(val mensagem: String) : RegisterEvent
    data class MostrarErro(val mensagem: String) : RegisterEvent
    data object NavegarParaLogin : RegisterEvent
}
