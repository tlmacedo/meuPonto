package br.com.tlmacedo.meuponto.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation

import br.com.tlmacedo.meuponto.presentation.screen.auth.login.LoginScreen
import br.com.tlmacedo.meuponto.presentation.screen.auth.register.RegisterScreen
import br.com.tlmacedo.meuponto.presentation.screen.auth.ForgotPasswordScreen

fun NavGraphBuilder.authNavGraph(navController: NavHostController) {
    navigation(
        startDestination = AuthDestinations.LOGIN,
        route = AuthDestinations.ROUTE
    ) {
        // Tela de Login
        composable(route = AuthDestinations.LOGIN) {
            LoginScreen(
                onNavigateToHome = {
                    // Navega para a HomeScreen e limpa o gráfico de autenticação do histórico
                    navController.navigate(MeuPontoDestinations.HOME_BASE) {
                        popUpTo(AuthDestinations.ROUTE) { inclusive = true }
                    }
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
