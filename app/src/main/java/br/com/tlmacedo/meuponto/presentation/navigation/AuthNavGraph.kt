package br.com.tlmacedo.meuponto.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation

// Importando a nova tela de Login que acabamos de criar
import br.com.tlmacedo.meuponto.presentation.screen.auth.login.LoginScreen

// Importando as telas de Register e ForgotPassword
import br.com.tlmacedo.meuponto.presentation.screen.auth.RegisterScreen
import br.com.tlmacedo.meuponto.presentation.screen.auth.ForgotPasswordScreen

fun NavGraphBuilder.authNavGraph(navController: NavHostController) {
    navigation(
        startDestination = AuthDestinations.LOGIN,
        route = "auth_graph"
    ) {
        // Tela de Login
        composable(route = AuthDestinations.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    // TODO: Navegar para a Home principal após o login
                },
                onNavigateToRegister = { navController.navigate(AuthDestinations.REGISTER) },
                onNavigateToForgotPassword = { navController.navigate(AuthDestinations.FORGOT_PASSWORD) }
            )
        }

        // Tela de Registro
        composable(route = AuthDestinations.REGISTER) {
            RegisterScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Tela de Recuperação de Senha
        composable(route = AuthDestinations.FORGOT_PASSWORD) {
            ForgotPasswordScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
