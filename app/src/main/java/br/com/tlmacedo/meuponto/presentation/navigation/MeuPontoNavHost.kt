// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/navigation/MeuPontoNavHost.kt
package br.com.tlmacedo.meuponto.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import br.com.tlmacedo.meuponto.presentation.components.MeuPontoTopBar
import br.com.tlmacedo.meuponto.presentation.screen.ausencias.AusenciaFormScreen
import br.com.tlmacedo.meuponto.presentation.screen.ausencias.AusenciasScreen
import br.com.tlmacedo.meuponto.presentation.screen.editponto.EditPontoScreen
import br.com.tlmacedo.meuponto.presentation.screen.history.HistoryScreen
import br.com.tlmacedo.meuponto.presentation.screen.home.HomeScreen
import br.com.tlmacedo.meuponto.presentation.screen.settings.SettingsScreen
import br.com.tlmacedo.meuponto.presentation.screen.settings.empregos.GerenciarEmpregosScreen
import br.com.tlmacedo.meuponto.presentation.screen.settings.empregos.editar.EditarEmpregoScreen
import br.com.tlmacedo.meuponto.presentation.screen.settings.feriados.editar.EditarFeriadoScreen
import br.com.tlmacedo.meuponto.presentation.screen.settings.feriados.lista.FeriadosListScreen
import br.com.tlmacedo.meuponto.presentation.screen.settings.sobre.SobreScreen

/**
 * NavHost principal da aplicação MeuPonto.
 *
 * @param navController Controlador de navegação
 * @param modifier Modificador para o container
 * @param startDestination Destino inicial (padrão: HOME_BASE)
 *
 * @author Thiago
 * @since 1.0.0
 * @updated 3.3.0 - Adicionado suporte a navegação com data para Home
 * @updated 3.4.0 - Adicionado módulo de Feriados
 * @updated 4.0.0 - Adicionado módulo de Ausências
 */
@Composable
fun MeuPontoNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = MeuPontoDestinations.HOME_BASE
) {
    Box(modifier = modifier) {
        NavHost(
            navController = navController,
            startDestination = startDestination
        ) {
            // ===== TELAS PRINCIPAIS =====

            composable(
                route = MeuPontoDestinations.HOME,
                arguments = listOf(
                    navArgument(MeuPontoDestinations.ARG_DATA) {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { backStackEntry ->
                val dataString = backStackEntry.arguments?.getString(MeuPontoDestinations.ARG_DATA)

                HomeScreen(
                    dataSelecionadaInicial = dataString,
                    onNavigateToHistory = {
                        navController.navigate(MeuPontoDestinations.HISTORY)
                    },
                    onNavigateToSettings = {
                        navController.navigate(MeuPontoDestinations.SETTINGS)
                    },
                    onNavigateToEditPonto = { pontoId ->
                        navController.navigate(MeuPontoDestinations.editPonto(pontoId))
                    },
                    onNavigateToNovoEmprego = {
                        navController.navigate(MeuPontoDestinations.editarEmprego(-1L))
                    },
                    onNavigateToEditarEmprego = { empregoId ->
                        navController.navigate(MeuPontoDestinations.editarEmprego(empregoId))
                    }
                )
            }

            composable(MeuPontoDestinations.HISTORY) {
                HistoryScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToDay = { data ->
                        navController.navigate(MeuPontoDestinations.homeComData(data.toString())) {
                            popUpTo(MeuPontoDestinations.HOME_BASE) {
                                inclusive = true
                            }
                        }
                    }
                )
            }

            composable(
                route = MeuPontoDestinations.EDIT_PONTO,
                arguments = listOf(
                    navArgument(MeuPontoDestinations.ARG_PONTO_ID) {
                        type = NavType.LongType
                        defaultValue = -1L
                    }
                )
            ) {
                EditPontoScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // ===== AUSÊNCIAS =====

            composable(MeuPontoDestinations.AUSENCIAS) {
                AusenciasScreen(
                    onVoltar = { navController.popBackStack() },
                    onNovaAusencia = {
                        navController.navigate(MeuPontoDestinations.NOVA_AUSENCIA_BASE)
                    },
                    onEditarAusencia = { ausenciaId ->
                        navController.navigate(MeuPontoDestinations.editarAusencia(ausenciaId))
                    }
                )
            }

            composable(
                route = MeuPontoDestinations.NOVA_AUSENCIA,
                arguments = listOf(
                    navArgument(MeuPontoDestinations.ARG_TIPO) {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    },
                    navArgument(MeuPontoDestinations.ARG_DATA) {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) {
                AusenciaFormScreen(
                    onVoltar = { navController.popBackStack() },
                    onSalvo = { navController.popBackStack() }
                )
            }

            composable(
                route = MeuPontoDestinations.EDITAR_AUSENCIA,
                arguments = listOf(
                    navArgument(MeuPontoDestinations.ARG_AUSENCIA_ID) {
                        type = NavType.LongType
                    }
                )
            ) {
                AusenciaFormScreen(
                    onVoltar = { navController.popBackStack() },
                    onSalvo = { navController.popBackStack() }
                )
            }

            // ===== CONFIGURAÇÕES =====

            composable(MeuPontoDestinations.SETTINGS) {
                SettingsScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToEmpregos = {
                        navController.navigate(MeuPontoDestinations.GERENCIAR_EMPREGOS)
                    },
                    onNavigateToJornada = {
                        navController.navigate(MeuPontoDestinations.CONFIGURACAO_JORNADA)
                    },
                    onNavigateToHorarios = {
                        navController.navigate(MeuPontoDestinations.HORARIOS_TRABALHO)
                    },
                    onNavigateToAjustesBancoHoras = {
                        navController.navigate(MeuPontoDestinations.AJUSTES_BANCO_HORAS)
                    },
                    onNavigateToMarcadores = {
                        navController.navigate(MeuPontoDestinations.MARCADORES)
                    },
                    onNavigateToFeriados = {
                        navController.navigate(MeuPontoDestinations.FERIADOS)
                    },
                    onNavigateToAusencias = {
                        navController.navigate(MeuPontoDestinations.AUSENCIAS)
                    },
                    onNavigateToSobre = {
                        navController.navigate(MeuPontoDestinations.SOBRE)
                    }
                )
            }

            composable(MeuPontoDestinations.GERENCIAR_EMPREGOS) {
                GerenciarEmpregosScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToEditarEmprego = { empregoId ->
                        navController.navigate(MeuPontoDestinations.editarEmprego(empregoId))
                    },
                    onNavigateToNovoEmprego = {
                        navController.navigate(MeuPontoDestinations.editarEmprego(-1L))
                    }
                )
            }

            composable(
                route = MeuPontoDestinations.EDITAR_EMPREGO,
                arguments = listOf(
                    navArgument(MeuPontoDestinations.ARG_EMPREGO_ID) {
                        type = NavType.LongType
                        defaultValue = -1L
                    }
                )
            ) {
                EditarEmpregoScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // ===== FERIADOS =====

            composable(MeuPontoDestinations.FERIADOS) {
                FeriadosListScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToEditar = { feriadoId ->
                        navController.navigate(MeuPontoDestinations.editarFeriado(feriadoId))
                    },
                    onNavigateToNovo = {
                        navController.navigate(MeuPontoDestinations.NOVO_FERIADO)
                    }
                )
            }

            composable(MeuPontoDestinations.NOVO_FERIADO) {
                EditarFeriadoScreen(
                    feriadoId = null,
                    onNavigateBack = { navController.popBackStack() },
                    onSalvoComSucesso = { navController.popBackStack() }
                )
            }

            composable(
                route = MeuPontoDestinations.EDITAR_FERIADO,
                arguments = listOf(
                    navArgument(MeuPontoDestinations.ARG_FERIADO_ID) {
                        type = NavType.LongType
                    }
                )
            ) { backStackEntry ->
                val feriadoId = backStackEntry.arguments?.getLong(MeuPontoDestinations.ARG_FERIADO_ID)
                EditarFeriadoScreen(
                    feriadoId = feriadoId,
                    onNavigateBack = { navController.popBackStack() },
                    onSalvoComSucesso = { navController.popBackStack() }
                )
            }

            // ===== OUTRAS CONFIGURAÇÕES =====

            composable(MeuPontoDestinations.CONFIGURACAO_JORNADA) {
                PlaceholderScreen(
                    titulo = "Configuração de Jornada",
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(MeuPontoDestinations.HORARIOS_TRABALHO) {
                PlaceholderScreen(
                    titulo = "Horários por Dia",
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(MeuPontoDestinations.AJUSTES_BANCO_HORAS) {
                PlaceholderScreen(
                    titulo = "Ajustes de Saldo",
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(MeuPontoDestinations.MARCADORES) {
                PlaceholderScreen(
                    titulo = "Marcadores",
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(MeuPontoDestinations.SOBRE) {
                SobreScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
private fun PlaceholderScreen(
    titulo: String,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            MeuPontoTopBar(
                title = titulo,
                showBackButton = true,
                onBackClick = onNavigateBack
            )
        }
    ) { paddingValues ->
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Construction,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Em desenvolvimento",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}
