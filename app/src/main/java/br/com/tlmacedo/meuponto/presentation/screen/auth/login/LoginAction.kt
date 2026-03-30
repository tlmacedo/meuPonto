package br.com.tlmacedo.meuponto.presentation.screen.auth.login

sealed interface LoginAction {
    data class EmailAlterado(val email: String) : LoginAction
    data class SenhaAlterada(val senha: String) : LoginAction
    data class LembrarMeAlterado(val lembrar: Boolean) : LoginAction
    object AlternarSenhaVisibilidade : LoginAction
    object LoginBiometriaClick : LoginAction
    object ClicarEntrar : LoginAction
    object ClicarCadastrar : LoginAction
    object ClicarEsqueciSenha : LoginAction
    
    // Novas ações para biometria
    data class BiometriaDisponibilidadeAlterada(val disponivel: Boolean) : LoginAction
    object HabilitarBiometriaConfirmado : LoginAction
    object HabilitarBiometriaCancelado : LoginAction
}
