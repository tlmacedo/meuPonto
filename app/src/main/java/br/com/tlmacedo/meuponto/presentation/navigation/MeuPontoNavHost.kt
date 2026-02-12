// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/navigation/MeuPontoNavHost.kt
package br.com.tlmacedo.meuponto.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import br.com.tlmacedo.meuponto.presentation.screen.editponto.EditPontoScreen
import br.com.tlmacedo.meuponto.presentation.screen.history.HistoryScreen
import br.com.tlmacedo.meuponto.presentation.screen.home.HomeScreen
import br.com.tlmacedo.meuponto.presentation.screen.settings.SettingsScreen

/**
 * Componente de navegação principal do aplicativo.
 *
 * Define o grafo de navegação com todas as telas e suas rotas,
 * gerenciando a pilha de navegação através do [NavHostController].
 *
 * @param navController Controlador de navegação do Jetpack Compose
 * @param modifier Modificador opcional para customização do layout
 *
 * @author Thiago
 * @since 1.0.0
 */
@Composable
fun MeuPontoNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Route.Home.route,
        modifier = modifier
    ) {
        // Tela inicial
        composable(route = Route.Home.route) {
            HomeScreen(
                onNavigateToHistory = {
                    navController.navigate(Route.History.route)
                },
                onNavigateToEditPonto = { pontoId ->
                    navController.navigate(Route.EditPonto.createRoute(pontoId))
                }
            )
        }

        // Tela de histórico
        composable(route = Route.History.route) {
            HistoryScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToEditPonto = { pontoId ->
                    navController.navigate(Route.EditPonto.createRoute(pontoId))
                }
            )
        }

        // Tela de configurações
        composable(route = Route.Settings.route) {
            SettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Tela de edição de ponto
        composable(
            route = Route.EditPonto.route,
            arguments = listOf(
                navArgument(Route.ARG_PONTO_ID) {
                    type = NavType.LongType
                }
            )
        ) {
            EditPontoScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
