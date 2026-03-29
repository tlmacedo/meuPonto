package br.com.tlmacedo.meuponto.presentation.screen.auth.register

sealed interface RegisterAction {
    data class AlterarNome(val nome: String) : RegisterAction
    data class AlterarEmail(val email: String) : RegisterAction
    data class AlterarSenha(val senha: String) : RegisterAction
    data class AlterarConfirmarSenha(val senha: String) : RegisterAction
    data object Cadastrar : RegisterAction
    data object LimparErro : RegisterAction
    data object NavegarParaLogin : RegisterAction
}
