package br.com.tlmacedo.meuponto.presentation.navigation

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import br.com.tlmacedo.meuponto.presentation.MainViewModel
import br.com.tlmacedo.meuponto.presentation.screen.onboarding.OnboardingScreen

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun RootNavGraph(
    viewModel: MainViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val isPrimeiraExecucao by viewModel.isPrimeiraExecucao.collectAsState()

    SharedTransitionLayout {
        NavHost(
            navController = navController,
            startDestination = if (isPrimeiraExecucao) MeuPontoDestinations.ONBOARDING else AuthDestinations.ROUTE
        ) {
            // 0. Onboarding
            composable(MeuPontoDestinations.ONBOARDING) {
                OnboardingScreen(
                    onFinish = {
                        navController.navigate(AuthDestinations.ROUTE) {
                            popUpTo(MeuPontoDestinations.ONBOARDING) { inclusive = true }
                        }
                    }
                )
            }

            // 1. Módulo de Autenticação
            authNavGraph(navController = navController)

            // 2. Módulo Principal do App (Destinos agora no mesmo NavHost)
            meuPontoNavGraph(
                navController = navController,
                sharedTransitionScope = this@SharedTransitionLayout
            )
        }
    }
}
