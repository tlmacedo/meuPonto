// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/navigation/MeuPontoNavHost.kt
package br.com.tlmacedo.meuponto.presentation.navigation

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
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
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import br.com.tlmacedo.meuponto.presentation.components.MeuPontoTopBar
import br.com.tlmacedo.meuponto.presentation.screen.auditoria.AuditoriaScreen
import br.com.tlmacedo.meuponto.presentation.screen.pendencias.PendenciasScreen
import br.com.tlmacedo.meuponto.presentation.screen.ausencias.form.AusenciaFormScreen
import br.com.tlmacedo.meuponto.presentation.screen.ausencias.list.AusenciasScreen
import br.com.tlmacedo.meuponto.presentation.screen.chamado.create.ChamadoCreateScreen
import br.com.tlmacedo.meuponto.presentation.screen.chamado.detail.ChamadoDetailScreen
import br.com.tlmacedo.meuponto.presentation.screen.chamado.list.ChamadoListScreen
import br.com.tlmacedo.meuponto.presentation.screen.editponto.EditPontoScreen
import br.com.tlmacedo.meuponto.presentation.screen.historicociclos.HistoricoCiclosScreen
import br.com.tlmacedo.meuponto.presentation.screen.history.HistoryScreen
import br.com.tlmacedo.meuponto.presentation.screen.home.HomeScreen
import br.com.tlmacedo.meuponto.presentation.screen.lixeira.LixeiraScreen
import br.com.tlmacedo.meuponto.presentation.screen.settings.comprovantes.ComprovantesScreen
import br.com.tlmacedo.meuponto.presentation.screen.settings.empregos.EmpregoSettingsDetailScreen
import br.com.tlmacedo.meuponto.presentation.screen.settings.empregos.GerenciarEmpregosScreen
import br.com.tlmacedo.meuponto.presentation.screen.settings.empregos.cargos.CargosScreen
import br.com.tlmacedo.meuponto.presentation.screen.settings.empregos.cargos.EditarCargoScreen
import br.com.tlmacedo.meuponto.presentation.screen.settings.empregos.configuracoes.LocalizacaoTrabalhoScreen
import br.com.tlmacedo.meuponto.presentation.screen.settings.empregos.configuracoes.OpcoesRegistroScreen
import br.com.tlmacedo.meuponto.presentation.screen.settings.empregos.editar.EditarEmpregoScreen
import br.com.tlmacedo.meuponto.presentation.screen.settings.feriados.editar.EditarFeriadoScreen
import br.com.tlmacedo.meuponto.presentation.screen.settings.feriados.lista.FeriadosListScreen
import br.com.tlmacedo.meuponto.presentation.screen.settings.global.GlobalSettingsScreen
import br.com.tlmacedo.meuponto.presentation.screen.settings.horarios.HorariosScreen
import br.com.tlmacedo.meuponto.presentation.screen.settings.jornada.JornadaScreen
import br.com.tlmacedo.meuponto.presentation.screen.settings.main.SettingsMainScreen
import br.com.tlmacedo.meuponto.presentation.screen.settings.sobre.AjudaScreen
import br.com.tlmacedo.meuponto.presentation.screen.settings.sobre.SobreScreen
import br.com.tlmacedo.meuponto.presentation.screen.settings.versoes.EditarVersaoScreen
import br.com.tlmacedo.meuponto.presentation.screen.settings.versoes.VersoesJornadaScreen
import br.com.tlmacedo.meuponto.presentation.screen.settings.versoes.comparar.CompararVersoesScreen

/**
 * Extensão para definir as rotas principais da aplicação MeuPonto.
 *
 * @param navController Controlador de navegação
 */
@OptIn(ExperimentalSharedTransitionApi::class)
fun NavGraphBuilder.meuPontoNavGraph(
    navController: NavHostController,
    sharedTransitionScope: SharedTransitionScope
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
            sharedTransitionScope = sharedTransitionScope,
            animatedVisibilityScope = this@composable,
            onNavigateToHistorico = {
                navController.navigate(MeuPontoDestinations.HISTORY_BASE)
            },
            onNavigateToConfiguracoes = {
                navController.navigate(MeuPontoDestinations.SETTINGS)
            },
            onNavigateToEditarPonto = { pontoId ->
                navController.navigate(MeuPontoDestinations.editPonto(pontoId))
            },
            onNavigateToNovoEmprego = {
                navController.navigate(MeuPontoDestinations.editarEmprego(-1L))
            },
            onNavigateToEditarEmprego = { empregoId ->
                navController.navigate(MeuPontoDestinations.editarEmprego(empregoId))
            },
            onNavigateToHistoricoCiclos = {
                navController.navigate(MeuPontoDestinations.HISTORICO_CICLOS)
            },
            onNavigateToEditarJornada = { empregoId ->
                navController.navigate(MeuPontoDestinations.versoesJornada(empregoId))
            },
            onNavigateToFotoVisualizacao = { pontoId ->
                navController.navigate(MeuPontoDestinations.fotoVisualizacao(pontoId))
            }
        )
    }

    composable(
        route = MeuPontoDestinations.FOTO_VISUALIZACAO,
        arguments = listOf(
            navArgument(MeuPontoDestinations.ARG_PONTO_ID) {
                type = NavType.LongType
            }
        )
    ) { backStackEntry ->
        val pontoId = backStackEntry.arguments?.getLong(MeuPontoDestinations.ARG_PONTO_ID) ?: -1L

        br.com.tlmacedo.meuponto.presentation.screen.comprovante.ComprovanteVisualizacaoScreen(
            pontoId = pontoId,
            sharedTransitionScope = sharedTransitionScope,
            animatedVisibilityScope = this@composable,
            onNavigateBack = { navController.popBackStack() }
        )
    }

    composable(
        route = MeuPontoDestinations.HISTORY,
        arguments = listOf(
            navArgument(MeuPontoDestinations.ARG_FILTRO) {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            }
        )
    ) {
        HistoryScreen(
            onNavigateBack = { navController.popBackStack() },
            onNavigateToDay = { data ->
                navController.navigate(MeuPontoDestinations.homeComData(data.toString()))
            },
            onNovaAusenciaComData = { data ->
                navController.navigate(MeuPontoDestinations.novaAusencia(data = data))
            },
            onNovoFeriadoComData = { data ->
                navController.navigate(MeuPontoDestinations.novoFeriado(data = data))
            }
        )
    }

    // ===== CHAMADOS =====
    composable(MeuPontoDestinations.CHAMADOS_LIST) {
        ChamadoListScreen(
            onNavigateBack = { navController.popBackStack() },
            onNavigateToDetail = { chamadoId ->
                navController.navigate(MeuPontoDestinations.chamadoDetail(chamadoId))
            },
            onNavigateToCreate = {
                navController.navigate(MeuPontoDestinations.CHAMADO_CREATE)
            }
        )
    }

    composable(
        route = MeuPontoDestinations.CHAMADO_DETAIL,
        arguments = listOf(
            navArgument(MeuPontoDestinations.ARG_CHAMADO_ID) {
                type = NavType.LongType
            }
        )
    ) {
        ChamadoDetailScreen(
            onNavigateBack = { navController.popBackStack() }
        )
    }

    composable(MeuPontoDestinations.CHAMADO_CREATE) {
        ChamadoCreateScreen(
            onNavigateBack = { navController.popBackStack() }
        )
    }

    // ===== HISTÓRICO DE CICLOS =====
    composable(MeuPontoDestinations.HISTORICO_CICLOS) {
        HistoricoCiclosScreen(
            onNavigateBack = { navController.popBackStack() }
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
            onNovaAusenciaComData = { data ->
                navController.navigate(MeuPontoDestinations.novaAusencia(data = data))
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

    // ===== CONFIGURAÇÕES PRINCIPAIS =====

    composable(MeuPontoDestinations.SETTINGS) {
        SettingsMainScreen(
            onNavigateBack = { navController.popBackStack() },
            onNavigateToEmpregoEdit = { empregoId ->
                navController.navigate(MeuPontoDestinations.editarEmprego(empregoId))
            },
            onNavigateToGerenciarEmpregos = {
                navController.navigate(MeuPontoDestinations.GERENCIAR_EMPREGOS)
            },
            onNavigateToEmpregoSettings = { empregoId ->
                navController.navigate(MeuPontoDestinations.empregoSettings(empregoId))
            },
            onNavigateToAusencias = { empregoId ->
                navController.navigate(MeuPontoDestinations.ausenciasEmprego(empregoId))
            },
            onNavigateToCalendario = {
                navController.navigate(MeuPontoDestinations.FERIADOS)
            },
            onNavigateToHistoryCalendar = {
                navController.navigate(
                    MeuPontoDestinations.historicoComFiltro(br.com.tlmacedo.meuponto.presentation.screen.history.FiltroHistorico.CALENDARIO.name)
                )
            },
            onNavigateToAparencia = {
                navController.navigate(MeuPontoDestinations.APARENCIA)
            },
            onNavigateToNotificacoes = {
                navController.navigate(MeuPontoDestinations.NOTIFICACOES)
            },
            onNavigateToPrivacidade = {
                navController.navigate(MeuPontoDestinations.PRIVACIDADE)
            },
            onNavigateToBackup = {
                navController.navigate(MeuPontoDestinations.BACKUP)
            },
            onNavigateToSobre = {
                navController.navigate(MeuPontoDestinations.SOBRE)
            },
            onNavigateToAjuda = {
                navController.navigate(MeuPontoDestinations.AJUDA)
            },
            onNavigateToLixeira = {
                navController.navigate(MeuPontoDestinations.LIXEIRA)
            },
            onNavigateToAuditoria = {
                navController.navigate(MeuPontoDestinations.AUDITORIA)
            },
            onNavigateToPendencias = {
                navController.navigate(MeuPontoDestinations.PENDENCIAS)
            },
            onNavigateToComprovantes = {
                navController.navigate(MeuPontoDestinations.COMPROVANTES_MANAGER)
            },
            onNavigateToOpcoesRegistro = { empregoId ->
                navController.navigate(MeuPontoDestinations.opcoesRegistro(empregoId))
            },
            onNavigateToJornada = {
                navController.navigate(MeuPontoDestinations.CONFIGURACAO_JORNADA)
            },
            onNavigateToChamados = {
                navController.navigate(MeuPontoDestinations.CHAMADOS_LIST)
            }
        )
    }

    composable(
        route = MeuPontoDestinations.EMPREGO_SETTINGS,
        arguments = listOf(
            navArgument(MeuPontoDestinations.ARG_EMPREGO_ID) {
                type = NavType.LongType
            }
        )
    ) {
        EmpregoSettingsDetailScreen(
            onNavigateBack = { navController.popBackStack() },
            onNavigateToVersoes = { id ->
                navController.navigate(MeuPontoDestinations.versoesJornada(id))
            },
            onNavigateToEditarEmprego = { id ->
                navController.navigate(MeuPontoDestinations.editarEmprego(id))
            },
            onNavigateToCargos = { id ->
                navController.navigate(MeuPontoDestinations.cargosEmprego(id))
            },
            onNavigateToAusencias = { id ->
                navController.navigate(MeuPontoDestinations.ausenciasEmprego(id))
            },
            onNavigateToAjustesSaldo = { id ->
                navController.navigate(MeuPontoDestinations.ajustesSaldo(id))
            },
            onNavigateToOpcoesRegistro = { id ->
                navController.navigate(MeuPontoDestinations.opcoesRegistro(id))
            }
        )
    }

    // ===== OPÇÕES DE REGISTRO =====

    composable(
        route = MeuPontoDestinations.OPCOES_REGISTRO,
        arguments = listOf(
            navArgument(MeuPontoDestinations.ARG_EMPREGO_ID) {
                type = NavType.LongType
            }
        )
    ) {
        OpcoesRegistroScreen(
            onNavigateBack = { navController.popBackStack() },
            onNavigateToLocalizacao = { id ->
                navController.navigate(MeuPontoDestinations.localizacaoTrabalho(id))
            }
        )
    }

    composable(
        route = MeuPontoDestinations.LOCALIZACAO_TRABALHO,
        arguments = listOf(
            navArgument(MeuPontoDestinations.ARG_EMPREGO_ID) {
                type = NavType.LongType
            }
        )
    ) {
        LocalizacaoTrabalhoScreen(
            onNavigateBack = { navController.popBackStack() },
            onConfirmSelection = { lat, lng, raio ->
                // Ao confirmar, voltamos para a tela anterior passando os dados (ou salvando direto)
                // Por simplicidade aqui, vamos salvar via ViewModel de OpcoesRegistro se possível
                // ou apenas voltar e deixar a tela anterior recarregar se o ViewModel salvou.
                navController.popBackStack()
            }
        )
    }

    // ===== CARGOS E SALÁRIOS =====

    composable(
        route = MeuPontoDestinations.CARGOS_EMPREGO,
        arguments = listOf(
            navArgument(MeuPontoDestinations.ARG_EMPREGO_ID) {
                type = NavType.LongType
            }
        )
    ) {
        CargosScreen(
            onNavigateBack = { navController.popBackStack() },
            onNavigateToEditarCargo = { empId, cargoId ->
                navController.navigate(MeuPontoDestinations.editarCargoEmprego(empId, cargoId))
            },
            onNavigateToNovoCargo = { empId ->
                navController.navigate(MeuPontoDestinations.novoCargoEmprego(empId))
            }
        )
    }

    composable(
        route = MeuPontoDestinations.NOVO_CARGO_EMPREGO,
        arguments = listOf(
            navArgument(MeuPontoDestinations.ARG_EMPREGO_ID) {
                type = NavType.LongType
            }
        )
    ) {
        EditarCargoScreen(
            onNavigateBack = { navController.popBackStack() }
        )
    }

    composable(
        route = MeuPontoDestinations.EDITAR_CARGO_EMPREGO,
        arguments = listOf(
            navArgument(MeuPontoDestinations.ARG_EMPREGO_ID) {
                type = NavType.LongType
            },
            navArgument(MeuPontoDestinations.ARG_CARGO_ID) {
                type = NavType.LongType
                defaultValue = -1L
            }
        )
    ) {
        EditarCargoScreen(
            onNavigateBack = { navController.popBackStack() }
        )
    }

    // ===== VERSÕES DE JORNADA =====

    // Lista de versões (por emprego)
    composable(
        route = MeuPontoDestinations.VERSOES_JORNADA_EMPREGO,
        arguments = listOf(
            navArgument(MeuPontoDestinations.ARG_EMPREGO_ID) {
                type = NavType.LongType
            }
        )
    ) {
        VersoesJornadaScreen(
            onNavigateBack = { navController.popBackStack() },
            onNavigateToEditar = { empId, versaoId ->
                navController.navigate(MeuPontoDestinations.editarVersaoEmprego(empId, versaoId))
            },
            onNavigateToComparar = { empId, v1, v2 ->
                navController.navigate(MeuPontoDestinations.compararVersoes(empId, v1, v2))
            }
        )
    }

    // Editar versão (com emprego)
    composable(
        route = MeuPontoDestinations.EDITAR_VERSAO_EMPREGO,
        arguments = listOf(
            navArgument(MeuPontoDestinations.ARG_EMPREGO_ID) {
                type = NavType.LongType
            },
            navArgument(MeuPontoDestinations.ARG_VERSAO_ID) {
                type = NavType.LongType
                defaultValue = -1L
            }
        )
    ) { backStackEntry ->
        val empId = backStackEntry.arguments?.getLong(MeuPontoDestinations.ARG_EMPREGO_ID) ?: -1L
        EditarVersaoScreen(
            onNavigateBack = { navController.popBackStack() },
            onNavigateToHorarios = { versaoId ->
                navController.navigate(MeuPontoDestinations.horariosVersao(empId, versaoId))
            }
        )
    }

    // Horários da versão
    composable(
        route = MeuPontoDestinations.HORARIOS_VERSAO,
        arguments = listOf(
            navArgument(MeuPontoDestinations.ARG_EMPREGO_ID) {
                type = NavType.LongType
            },
            navArgument(MeuPontoDestinations.ARG_VERSAO_ID) {
                type = NavType.LongType
            }
        )
    ) {
        HorariosScreen(
            onNavigateBack = { navController.popBackStack() }
        )
    }

    // Comparar versões
    composable(
        route = MeuPontoDestinations.COMPARAR_VERSOES,
        arguments = listOf(
            navArgument(MeuPontoDestinations.ARG_EMPREGO_ID) {
                type = NavType.LongType
            },
            navArgument(MeuPontoDestinations.ARG_VERSAO_ID_1) {
                type = NavType.LongType
            },
            navArgument(MeuPontoDestinations.ARG_VERSAO_ID_2) {
                type = NavType.LongType
            }
        )
    ) {
        CompararVersoesScreen(
            onNavigateBack = { navController.popBackStack() }
        )
    }

    // Lista de versões (legacy - sem emprego específico)
    composable(MeuPontoDestinations.VERSOES_JORNADA) {
        VersoesJornadaScreen(
            onNavigateBack = { navController.popBackStack() },
            onNavigateToEditar = { empregoId, versaoId ->
                navController.navigate(
                    MeuPontoDestinations.editarVersaoEmprego(
                        empregoId,
                        versaoId
                    )
                )
            },
            onNavigateToComparar = { empId, v1, v2 ->
                navController.navigate(MeuPontoDestinations.compararVersoes(empId, v1, v2))
            }
        )
    }

    // Editar versão (legacy)
    composable(
        route = MeuPontoDestinations.EDITAR_VERSAO,
        arguments = listOf(
            navArgument(MeuPontoDestinations.ARG_VERSAO_ID) {
                type = NavType.LongType
            }
        )
    ) {
        EditarVersaoScreen(
            onNavigateBack = { navController.popBackStack() },
            onNavigateToHorarios = { _ ->
                // TODO: Implementar navegação para horários
            }
        )
    }

    // ===== CONFIGURAÇÕES GLOBAIS =====

    composable(MeuPontoDestinations.CONFIGURACOES_GLOBAIS) {
        GlobalSettingsScreen(
            onNavigateBack = { navController.popBackStack() }
        )
    }

    // ===== APARÊNCIA, NOTIFICAÇÕES, PRIVACIDADE E BACKUP =====

    composable(MeuPontoDestinations.APARENCIA) {
        br.com.tlmacedo.meuponto.presentation.screen.settings.design.AparenciaScreen(
            onNavigateBack = { navController.popBackStack() }
        )
    }

    composable(MeuPontoDestinations.NOTIFICACOES) {
        br.com.tlmacedo.meuponto.presentation.screen.settings.design.NotificacoesScreen(
            onNavigateBack = { navController.popBackStack() }
        )
    }

    composable(MeuPontoDestinations.PRIVACIDADE) {
        br.com.tlmacedo.meuponto.presentation.screen.settings.design.PrivacidadeScreen(
            onNavigateBack = { navController.popBackStack() }
        )
    }

    composable(MeuPontoDestinations.BACKUP) {
        br.com.tlmacedo.meuponto.presentation.screen.settings.backup.BackupScreen(
            onNavigateBack = { navController.popBackStack() }
        )
    }

    // ===== EMPREGOS =====

    composable(MeuPontoDestinations.GERENCIAR_EMPREGOS) {
        GerenciarEmpregosScreen(
            onNavigateBack = { navController.popBackStack() },
            onNavigateToEmpregoSettings = { empregoId ->
                navController.navigate(MeuPontoDestinations.empregoSettings(empregoId))
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
    ) { backStackEntry ->
        val empregoId =
            backStackEntry.arguments?.getLong(MeuPontoDestinations.ARG_EMPREGO_ID) ?: -1L

        EditarEmpregoScreen(
            onNavigateBack = { navController.popBackStack() },
            onNavigateToVersoes = {
                if (empregoId > 0) {
                    navController.navigate(MeuPontoDestinations.versoesJornada(empregoId))
                }
            },
            onNavigateToCargos = {
                if (empregoId > 0) {
                    navController.navigate(MeuPontoDestinations.cargosEmprego(empregoId))
                }
            }
        )
    }

    // ===== AJUSTES DE SALDO =====

    composable(
        route = MeuPontoDestinations.AJUSTES_SALDO_EMPREGO,
        arguments = listOf(
            navArgument(MeuPontoDestinations.ARG_EMPREGO_ID) {
                type = NavType.LongType
            }
        )
    ) {
        PlaceholderScreen(
            titulo = "Ajustes de Saldo",
            onNavigateBack = { navController.popBackStack() }
        )
    }

    composable(MeuPontoDestinations.AJUSTES_BANCO_HORAS) {
        PlaceholderScreen(
            titulo = "Ajustes de Saldo",
            onNavigateBack = { navController.popBackStack() }
        )
    }

    // ===== AUSÊNCIAS POR EMPREGO =====

    composable(
        route = MeuPontoDestinations.AUSENCIAS_EMPREGO,
        arguments = listOf(
            navArgument(MeuPontoDestinations.ARG_EMPREGO_ID) {
                type = NavType.LongType
            }
        )
    ) {
        AusenciasScreen(
            onVoltar = { navController.popBackStack() },
            onNovaAusencia = {
                navController.navigate(MeuPontoDestinations.NOVA_AUSENCIA_BASE)
            },
            onNovaAusenciaComData = { data ->
                navController.navigate(MeuPontoDestinations.novaAusencia(data = data))
            },
            onEditarAusencia = { ausenciaId ->
                navController.navigate(MeuPontoDestinations.editarAusencia(ausenciaId))
            }
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
                navController.navigate(MeuPontoDestinations.NOVO_FERIADO_BASE)
            },
            onNavigateToNovoComData = { data ->
                navController.navigate(MeuPontoDestinations.novoFeriado(data = data))
            }
        )
    }

    composable(
        route = MeuPontoDestinations.NOVO_FERIADO,
        arguments = listOf(
            navArgument(MeuPontoDestinations.ARG_DATA) {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            }
        )
    ) {
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

    // ===== OUTRAS CONFIGURAÇÕES (Legacy/Placeholder) =====

    composable(MeuPontoDestinations.CONFIGURACAO_JORNADA) {
        JornadaScreen(
            onNavigateBack = { navController.popBackStack() }
        )
    }

    composable(MeuPontoDestinations.HORARIOS_TRABALHO) {
        PlaceholderScreen(
            titulo = "Horários por Dia",
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

    composable(MeuPontoDestinations.AJUDA) {
        AjudaScreen(
            onNavigateBack = { navController.popBackStack() }
        )
    }

    composable(MeuPontoDestinations.LIXEIRA) {
        LixeiraScreen(
            onNavigateBack = { navController.popBackStack() }
        )
    }

    composable(MeuPontoDestinations.AUDITORIA) {
        AuditoriaScreen(
            onNavigateBack = { navController.popBackStack() }
        )
    }

    composable(MeuPontoDestinations.PENDENCIAS) {
        PendenciasScreen(
            onNavigateBack = { navController.popBackStack() },
            onNavigateToDia = { data: java.time.LocalDate ->
                navController.navigate(MeuPontoDestinations.homeComData(data.toString()))
            }
        )
    }

    composable(MeuPontoDestinations.COMPROVANTES_MANAGER) {
        ComprovantesScreen(
            onNavigateBack = { navController.popBackStack() }
        )
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
