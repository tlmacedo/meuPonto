package br.com.tlmacedo.meuponto.presentation.screen.auth.register

sealed interface RegisterAction {
    data class NomeAlterado(val nome: String) : RegisterAction
    data class EmailAlterado(val email: String) : RegisterAction
    data class SenhaAlterada(val senha: String) : RegisterAction
    data class ConfirmarSenhaAlterada(val senha: String) : RegisterAction
    object ClicarCadastrar : RegisterAction
    object NavegarParaLogin : RegisterAction
    object AlternarSenhaVisibilidade : RegisterAction
}
