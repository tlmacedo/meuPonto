// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/navigation/Route.kt
package br.com.tlmacedo.meuponto.presentation.navigation

/**
 * Sealed class que define todas as rotas de navegação do aplicativo.
 */
sealed class Route(val route: String) {

    // ===== AUTENTICAÇÃO =====
    data object Login : Route("login")
    data object Register : Route("register")
    data object ForgotPassword : Route("forgot_password")

    // ===== TELAS PRINCIPAIS =====
    data object Home : Route("home")
    data object History : Route("history")
    data object Settings : Route("settings")
    data object Reports : Route("reports")

    /**
     * Tela de edição de ponto específico.
     */
    data object EditPonto : Route("edit_ponto/{pontoId}") {
        fun createRoute(pontoId: Long): String = "edit_ponto/$pontoId"
    }

    companion object {
        const val ARG_PONTO_ID = "pontoId"
    }
}
