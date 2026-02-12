// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/navigation/MeuPontoNavHost.kt
package br.com.tlmacedo.meuponto.presentation.navigation

import androidx.compose.foundation.layout.Box
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
 * NavHost principal da aplicação MeuPonto.
 *
 * Define todas as rotas de navegação e suas respectivas telas,
 * gerenciando a pilha de navegação do aplicativo.
 *
 * @param navController Controlador de navegação
 * @param modifier Modificador para o container
 * @param startDestination Destino inicial (padrão: HOME)
 *
 * @author Thiago
 * @since 1.0.0
 */
@Composable
fun MeuPontoNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = MeuPontoDestinations.HOME
) {
    Box(modifier = modifier) {
        NavHost(
            navController = navController,
            startDestination = startDestination
        ) {
            // Tela inicial (Home)
            composable(MeuPontoDestinations.HOME) {
                HomeScreen(
                    onNavigateToHistory = {
                        navController.navigate(MeuPontoDestinations.HISTORY)
                    },
                    onNavigateToSettings = {
                        navController.navigate(MeuPontoDestinations.SETTINGS)
                    },
                    onNavigateToEditPonto = { pontoId ->
                        navController.navigate(MeuPontoDestinations.editPonto(pontoId))
                    }
                )
            }

            // Tela de histórico
            composable(MeuPontoDestinations.HISTORY) {
                HistoryScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToEditPonto = { pontoId ->
                        navController.navigate(MeuPontoDestinations.editPonto(pontoId))
                    }
                )
            }

            // Tela de configurações
            composable(MeuPontoDestinations.SETTINGS) {
                SettingsScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Tela de edição de ponto
            composable(
                route = MeuPontoDestinations.EDIT_PONTO,
                arguments = listOf(
                    navArgument("pontoId") {
                        type = NavType.LongType
                        defaultValue = -1L
                    }
                )
            ) {
                EditPontoScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
