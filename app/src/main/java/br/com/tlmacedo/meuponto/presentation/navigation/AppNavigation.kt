// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/navigation/AppNavigation.kt
package br.com.tlmacedo.meuponto.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import br.com.tlmacedo.meuponto.presentation.screen.home.HomeScreen
import br.com.tlmacedo.meuponto.presentation.screen.settings.SettingsScreen
import br.com.tlmacedo.meuponto.presentation.screen.settings.empregos.EmpregoSettingsScreen
import br.com.tlmacedo.meuponto.presentation.screen.settings.empregos.GerenciarEmpregosScreen
import br.com.tlmacedo.meuponto.presentation.screen.settings.empregos.editar.EditarEmpregoScreen
import br.com.tlmacedo.meuponto.presentation.screen.settings.feriados.editar.EditarFeriadoScreen
import br.com.tlmacedo.meuponto.presentation.screen.settings.feriados.lista.FeriadosListScreen
import br.com.tlmacedo.meuponto.presentation.screen.settings.global.GlobalSettingsScreen
import br.com.tlmacedo.meuponto.presentation.screen.settings.sobre.SobreScreen
import br.com.tlmacedo.meuponto.presentation.screen.settings.versoes.EditarVersaoScreen
import br.com.tlmacedo.meuponto.presentation.screen.settings.versoes.VersoesJornadaScreen

/**
 * Configuração de navegação do aplicativo.
 *
 * @author Thiago
 * @since 8.0.0
 * @updated 8.2.0 - Reorganização das rotas de configurações
 */
@Composable
fun AppNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = AppRoutes.HOME,
        modifier = modifier
    ) {
        // ═══════════════════════════════════════════════════════════════
        // HOME
        // ═══════════════════════════════════════════════════════════════
        composable(AppRoutes.HOME) {
            HomeScreen(
                onNavigateToSettings = { navController.navigate(AppRoutes.SETTINGS) },
                onNavigateToNovoEmprego = { navController.navigate(AppRoutes.empregoForm(null)) },
                onNavigateToEditarEmprego = { empregoId ->
                    navController.navigate(AppRoutes.empregoForm(empregoId))
                }
            )
        }

        // ═══════════════════════════════════════════════════════════════
        // CONFIGURAÇÕES - PRINCIPAL
        // ═══════════════════════════════════════════════════════════════
        composable(AppRoutes.SETTINGS) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEmpregos = { navController.navigate(AppRoutes.EMPREGOS_GERENCIAR) },
                onNavigateToEmpregoSettings = { empregoId ->
                    navController.navigate(AppRoutes.empregoSettings(empregoId))
                },
                onNavigateToNovoEmprego = { navController.navigate(AppRoutes.empregoForm(null)) },
                onNavigateToFeriados = { navController.navigate(AppRoutes.FERIADOS_LISTA) },
                onNavigateToConfiguracoesGlobais = { navController.navigate(AppRoutes.SETTINGS_GLOBAL) },
                onNavigateToSobre = { navController.navigate(AppRoutes.SOBRE) }
            )
        }

        // ═══════════════════════════════════════════════════════════════
        // CONFIGURAÇÕES GLOBAIS
        // ═══════════════════════════════════════════════════════════════
        composable(AppRoutes.SETTINGS_GLOBAL) {
            GlobalSettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ═══════════════════════════════════════════════════════════════
        // SOBRE
        // ═══════════════════════════════════════════════════════════════
        composable(AppRoutes.SOBRE) {
            SobreScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ═══════════════════════════════════════════════════════════════
        // EMPREGOS
        // ═══════════════════════════════════════════════════════════════

        // Gerenciar Empregos (lista)
        composable(AppRoutes.EMPREGOS_GERENCIAR) {
            GerenciarEmpregosScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEditarEmprego = { empregoId ->
                    navController.navigate(AppRoutes.empregoForm(empregoId))
                },
                onNavigateToNovoEmprego = { navController.navigate(AppRoutes.empregoForm(null)) }
            )
        }

        // Configurações do Emprego (jornada, ajustes, ausências)
        composable(
            route = AppRoutes.EMPREGO_SETTINGS,
            arguments = listOf(
                navArgument("empregoId") {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->
            val empregoId = backStackEntry.arguments?.getLong("empregoId") ?: return@composable
            EmpregoSettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEditarEmprego = { id ->
                    navController.navigate(AppRoutes.empregoForm(id))
                },
                onNavigateToVersoes = { id ->
                    navController.navigate(AppRoutes.versoes(id))
                },
                onNavigateToEditarVersao = { versaoId ->
                    navController.navigate(AppRoutes.editarVersao(empregoId, versaoId))
                },
                onNavigateToAjustesSaldo = { id ->
                    navController.navigate(AppRoutes.ajustesSaldo(id))
                },
                onNavigateToAusencias = { id ->
                    navController.navigate(AppRoutes.ausencias(id))
                }
            )
        }

        // Formulário de Emprego (criar/editar dados básicos)
        composable(
            route = AppRoutes.EMPREGO_FORM,
            arguments = listOf(
                navArgument("empregoId") {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) {
            EditarEmpregoScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ═══════════════════════════════════════════════════════════════
        // VERSÕES DE JORNADA
        // ═══════════════════════════════════════════════════════════════

        // Lista de versões
        composable(
            route = AppRoutes.VERSOES_LISTA,
            arguments = listOf(
                navArgument("empregoId") {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->
            val empregoId = backStackEntry.arguments?.getLong("empregoId") ?: return@composable
            VersoesJornadaScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEditar = { versaoId ->
                    navController.navigate(AppRoutes.editarVersao(empregoId, versaoId))
                }
            )
        }

        // Editar/Criar versão
        composable(
            route = AppRoutes.VERSAO_EDITAR,
            arguments = listOf(
                navArgument("empregoId") {
                    type = NavType.LongType
                },
                navArgument("versaoId") {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) { backStackEntry ->
            val empregoId = backStackEntry.arguments?.getLong("empregoId") ?: return@composable
            EditarVersaoScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToHorarios = { versaoId ->
                    // TODO: Implementar navegação para tela de horários quando existir
                    // navController.navigate(AppRoutes.horariosVersao(versaoId))
                }
            )
        }

        // ═══════════════════════════════════════════════════════════════
        // AJUSTES DE SALDO (por emprego)
        // ═══════════════════════════════════════════════════════════════
        composable(
            route = AppRoutes.AJUSTES_SALDO,
            arguments = listOf(
                navArgument("empregoId") {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->
            val empregoId = backStackEntry.arguments?.getLong("empregoId") ?: return@composable
            // TODO: Implementar AjustesSaldoScreen
            // AjustesSaldoScreen(
            //     empregoId = empregoId,
            //     onNavigateBack = { navController.popBackStack() }
            // )
        }

        // ═══════════════════════════════════════════════════════════════
        // AUSÊNCIAS (por emprego)
        // ═══════════════════════════════════════════════════════════════
        composable(
            route = AppRoutes.AUSENCIAS,
            arguments = listOf(
                navArgument("empregoId") {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->
            val empregoId = backStackEntry.arguments?.getLong("empregoId") ?: return@composable
            // TODO: Implementar AusenciasScreen
            // AusenciasScreen(
            //     empregoId = empregoId,
            //     onNavigateBack = { navController.popBackStack() }
            // )
        }

        // ═══════════════════════════════════════════════════════════════
        // FERIADOS (globais)
        // ═══════════════════════════════════════════════════════════════

        // Lista de feriados
        composable(AppRoutes.FERIADOS_LISTA) {
            FeriadosListScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEditar = { feriadoId ->
                    navController.navigate(AppRoutes.feriadoForm(feriadoId))
                },
                onNavigateToNovo = {
                    navController.navigate(AppRoutes.feriadoForm(null))
                }
            )
        }

        // Formulário de feriado
        composable(
            route = AppRoutes.FERIADO_FORM,
            arguments = listOf(
                navArgument("feriadoId") {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) { backStackEntry ->
            val feriadoId = backStackEntry.arguments?.getLong("feriadoId")?.takeIf { it != -1L }
            EditarFeriadoScreen(
                feriadoId = feriadoId,
                onNavigateBack = { navController.popBackStack() },
                onSalvoComSucesso = { navController.popBackStack() }
            )
        }
    }
}

/**
 * Rotas do aplicativo.
 */
object AppRoutes {
    // Home
    const val HOME = "home"

    // Configurações
    const val SETTINGS = "settings"
    const val SETTINGS_GLOBAL = "settings/global"
    const val SOBRE = "settings/sobre"

    // Empregos
    const val EMPREGOS_GERENCIAR = "empregos/gerenciar"
    const val EMPREGO_SETTINGS = "empregos/{empregoId}/settings"
    const val EMPREGO_FORM = "empregos/form?empregoId={empregoId}"

    // Versões de Jornada
    const val VERSOES_LISTA = "empregos/{empregoId}/versoes"
    const val VERSAO_EDITAR = "empregos/{empregoId}/versoes/editar?versaoId={versaoId}"

    // Ajustes de Saldo
    const val AJUSTES_SALDO = "empregos/{empregoId}/ajustes"

    // Ausências
    const val AUSENCIAS = "empregos/{empregoId}/ausencias"

    // Feriados
    const val FERIADOS_LISTA = "feriados"
    const val FERIADO_FORM = "feriados/form?feriadoId={feriadoId}"

    // Funções de navegação
    fun empregoSettings(empregoId: Long): String = "empregos/$empregoId/settings"

    fun empregoForm(empregoId: Long? = null): String =
        "empregos/form?empregoId=${empregoId ?: -1L}"

    fun versoes(empregoId: Long): String = "empregos/$empregoId/versoes"

    fun editarVersao(empregoId: Long, versaoId: Long? = null): String =
        "empregos/$empregoId/versoes/editar?versaoId=${versaoId ?: -1L}"

    fun ajustesSaldo(empregoId: Long): String = "empregos/$empregoId/ajustes"

    fun ausencias(empregoId: Long): String = "empregos/$empregoId/ausencias"

    fun feriadoForm(feriadoId: Long? = null): String =
        "feriados/form?feriadoId=${feriadoId ?: -1L}"
}
