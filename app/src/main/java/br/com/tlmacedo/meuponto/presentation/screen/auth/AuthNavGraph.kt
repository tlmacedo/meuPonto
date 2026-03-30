package br.com.tlmacedo.meuponto.presentation.screen.auth

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import br.com.tlmacedo.meuponto.presentation.navigation.AuthDestinations
import br.com.tlmacedo.meuponto.presentation.screen.auth.login.LoginScreen
import br.com.tlmacedo.meuponto.presentation.screen.auth.register.RegisterScreen

fun NavGraphBuilder.authNavGraph(navController: NavController) {
    navigation(
        startDestination = AuthDestinations.LOGIN,
        route = AuthDestinations.ROUTE
    ) {
        // Tela de Login
        composable(route = AuthDestinations.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    // Navega para o módulo principal e remove a tela de login do histórico
                    navController.navigate("main_app") {
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
