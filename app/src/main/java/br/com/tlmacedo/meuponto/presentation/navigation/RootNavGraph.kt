package br.com.tlmacedo.meuponto.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun RootNavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = AuthDestinations.ROUTE
    ) {
        // 1. Módulo de Autenticação (chamado como extension function)
        authNavGraph(navController = navController)

        // 2. Módulo Principal do App (passando o navController exigido)
        composable(route = "main_app") {
            MeuPontoNavHost(navController = navController)
        }
    }
}
