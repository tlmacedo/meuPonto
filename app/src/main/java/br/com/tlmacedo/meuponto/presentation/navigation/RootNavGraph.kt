package br.com.tlmacedo.meuponto.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController

@Composable
fun RootNavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = AuthDestinations.ROUTE
    ) {
        // 1. Módulo de Autenticação
        authNavGraph(navController = navController)

        // 2. Módulo Principal do App (Destinos agora no mesmo NavHost)
        meuPontoNavGraph(navController = navController)
    }
}
