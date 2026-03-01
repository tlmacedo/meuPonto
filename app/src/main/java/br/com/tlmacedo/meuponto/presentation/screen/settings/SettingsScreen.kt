// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/settings/SettingsScreen.kt
package br.com.tlmacedo.meuponto.presentation.screen.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BeachAccess
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.tlmacedo.meuponto.presentation.components.MeuPontoTopBar
import br.com.tlmacedo.meuponto.presentation.screen.settings.components.EmpregoAtivoCard
import br.com.tlmacedo.meuponto.presentation.screen.settings.components.EmpregoSelectorItem
import br.com.tlmacedo.meuponto.presentation.screen.settings.components.EmptyStateNoEmpregos
import br.com.tlmacedo.meuponto.presentation.screen.settings.components.SettingsDivider
import br.com.tlmacedo.meuponto.presentation.screen.settings.components.SettingsMenuItem
import br.com.tlmacedo.meuponto.presentation.screen.settings.components.SettingsMenuItemWithValue
import br.com.tlmacedo.meuponto.presentation.screen.settings.components.SettingsSectionHeader
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Tela principal de configurações do aplicativo.
 *
 * Organizada em seções:
 * - Emprego ativo (card destacado com resumo)
 * - Configurações do emprego (jornada, horários, feriados, ausências)
 * - Banco de horas
 * - Aplicativo (aparência, backup, sobre)
 *
 * @author Thiago
 * @since 3.0.0
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEmpregos: () -> Unit,
    onNavigateToEditarEmprego: (Long) -> Unit,
    onNavigateToNovoEmprego: () -> Unit,
    onNavigateToJornada: () -> Unit,
    onNavigateToHorarios: () -> Unit,
    onNavigateToVersoes: () -> Unit,
    onNavigateToAjustesBancoHoras: () -> Unit,
    onNavigateToFeriados: () -> Unit,
    onNavigateToAusencias: () -> Unit,
    onNavigateToAparencia: () -> Unit = {},
    onNavigateToBackup: () -> Unit = {},
    onNavigateToSobre: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val bottomSheetState = rememberModalBottomSheetState()

    // Coletar eventos
    LaunchedEffect(Unit) {
        viewModel.eventos.collectLatest { evento ->
            when (evento) {
                is SettingsEvent.MostrarMensagem -> {
                    snackbarHostState.showSnackbar(evento.mensagem)
                }
                is SettingsEvent.NavegarParaEmprego -> {
                    onNavigateToEditarEmprego(evento.empregoId)
                }
                is SettingsEvent.NavegarParaNovoEmprego -> {
                    onNavigateToNovoEmprego()
                }
            }
        }
    }

    // Fechar bottom sheet ao mudar de emprego
    LaunchedEffect(uiState.empregoAtivo?.emprego?.id) {
        if (uiState.mostrarSeletorEmprego) {
            scope.launch { bottomSheetState.hide() }
            viewModel.onAction(SettingsAction.FecharSeletorEmprego)
        }
    }

    Scaffold(
        topBar = {
            MeuPontoTopBar(
                title = "Configurações",
                showBackButton = true,
                onBackClick = onNavigateBack
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.isPrimeiroAcesso -> {
                // Primeiro acesso - sem empregos
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    EmptyStateNoEmpregos(
                        onCriarEmprego = onNavigateToNovoEmprego
                    )
                }
            }

            else -> {
                // Tela normal com emprego(s)
                SettingsContent(
                    uiState = uiState,
                    onTrocarEmprego = { viewModel.onAction(SettingsAction.AbrirSeletorEmprego) },
                    onEditarEmprego = {
                        uiState.empregoAtivo?.let { onNavigateToEditarEmprego(it.emprego.id) }
                    },
                    onNavigateToEmpregos = onNavigateToEmpregos,
                    onNavigateToJornada = onNavigateToJornada,
                    onNavigateToHorarios = onNavigateToHorarios,
                    onNavigateToVersoes = onNavigateToVersoes,
                    onNavigateToFeriados = onNavigateToFeriados,
                    onNavigateToAusencias = onNavigateToAusencias,
                    onNavigateToAjustesBancoHoras = onNavigateToAjustesBancoHoras,
                    onNavigateToAparencia = onNavigateToAparencia,
                    onNavigateToBackup = onNavigateToBackup,
                    onNavigateToSobre = onNavigateToSobre,
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }

    // Bottom Sheet de seleção de emprego
    if (uiState.mostrarSeletorEmprego) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.onAction(SettingsAction.FecharSeletorEmprego) },
            sheetState = bottomSheetState
        ) {
            EmpregoSelectorBottomSheet(
                empregoAtivo = uiState.empregoAtivo,
                outrosEmpregos = uiState.outrosEmpregos,
                onSelecionarEmprego = { empregoId ->
                    viewModel.onAction(SettingsAction.TrocarEmpregoAtivo(empregoId))
                },
                onNovoEmprego = {
                    viewModel.onAction(SettingsAction.FecharSeletorEmprego)
                    onNavigateToNovoEmprego()
                },
                onGerenciarEmpregos = {
                    viewModel.onAction(SettingsAction.FecharSeletorEmprego)
                    onNavigateToEmpregos()
                }
            )
        }
    }
}

@Composable
private fun SettingsContent(
    uiState: SettingsUiState,
    onTrocarEmprego: () -> Unit,
    onEditarEmprego: () -> Unit,
    onNavigateToEmpregos: () -> Unit,
    onNavigateToJornada: () -> Unit,
    onNavigateToHorarios: () -> Unit,
    onNavigateToVersoes: () -> Unit,
    onNavigateToFeriados: () -> Unit,
    onNavigateToAusencias: () -> Unit,
    onNavigateToAjustesBancoHoras: () -> Unit,
    onNavigateToAparencia: () -> Unit,
    onNavigateToBackup: () -> Unit,
    onNavigateToSobre: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // ═══════════════════════════════════════════════════════════════
        // SEÇÃO: EMPREGO ATIVO
        // ═══════════════════════════════════════════════════════════════
        uiState.empregoAtivo?.let { resumo ->
            item(key = "emprego_ativo") {
                EmpregoAtivoCard(
                    resumo = resumo,
                    totalOutrosEmpregos = uiState.outrosEmpregos.size,
                    onTrocarEmprego = onTrocarEmprego,
                    onEditarEmprego = onEditarEmprego
                )
            }
        }

        // ═══════════════════════════════════════════════════════════════
        // SEÇÃO: EMPREGOS
        // ═══════════════════════════════════════════════════════════════
        item(key = "section_empregos") {
            SettingsSectionHeader(
                title = "Empregos",
                action = {
                    if (uiState.totalEmpregosAtivos > 1) {
                        Text(
                            text = "${uiState.totalEmpregosAtivos} ativos",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        }

        item(key = "menu_gerenciar_empregos") {
            SettingsMenuItem(
                icon = Icons.Default.Business,
                title = "Gerenciar Empregos",
                subtitle = "Adicionar, editar ou arquivar empregos",
                badge = if (uiState.empregosArquivados.isNotEmpty())
                    "${uiState.empregosArquivados.size} arquivados" else null,
                onClick = onNavigateToEmpregos
            )
        }

        item(key = "divider_1") { SettingsDivider() }

        // ═══════════════════════════════════════════════════════════════
        // SEÇÃO: JORNADA DE TRABALHO (do emprego ativo)
        // ═══════════════════════════════════════════════════════════════
        item(key = "section_jornada") {
            SettingsSectionHeader(title = "Jornada de Trabalho")
        }

        item(key = "menu_jornada") {
            SettingsMenuItemWithValue(
                icon = Icons.Default.Schedule,
                title = "Configuração de Jornada",
                value = uiState.empregoAtivo?.cargaHorariaFormatada ?: "08:12",
                onClick = onNavigateToJornada
            )
        }

        item(key = "menu_horarios") {
            SettingsMenuItem(
                icon = Icons.Default.CalendarMonth,
                title = "Horários por Dia",
                subtitle = "Definir horários para cada dia da semana",
                onClick = onNavigateToHorarios
            )
        }

        item(key = "menu_versoes") {
            SettingsMenuItem(
                icon = Icons.Default.History,
                title = "Versões de Jornada",
                subtitle = "Histórico de alterações na jornada",
                badge = uiState.empregoAtivo?.totalVersoes?.let {
                    if (it > 0) "$it versões" else null
                },
                onClick = onNavigateToVersoes
            )
        }

        item(key = "divider_2") { SettingsDivider() }

        // ═══════════════════════════════════════════════════════════════
        // SEÇÃO: CALENDÁRIO
        // ═══════════════════════════════════════════════════════════════
        item(key = "section_calendario") {
            SettingsSectionHeader(title = "Calendário")
        }

        item(key = "menu_feriados") {
            SettingsMenuItem(
                icon = Icons.Default.Event,
                title = "Feriados",
                subtitle = "Gerenciar feriados e pontes facultativas",
                badge = if (uiState.totalFeriadosGlobais > 0)
                    "${uiState.totalFeriadosGlobais}" else null,
                onClick = onNavigateToFeriados
            )
        }

        item(key = "menu_ausencias") {
            SettingsMenuItem(
                icon = Icons.Default.BeachAccess,
                title = "Ausências",
                subtitle = "Férias, folgas, faltas e licenças",
                badge = uiState.empregoAtivo?.totalAusencias?.let {
                    if (it > 0) "$it" else null
                },
                onClick = onNavigateToAusencias
            )
        }

        item(key = "divider_3") { SettingsDivider() }

        // ═══════════════════════════════════════════════════════════════
        // SEÇÃO: BANCO DE HORAS
        // ═══════════════════════════════════════════════════════════════
        item(key = "section_banco") {
            SettingsSectionHeader(title = "Banco de Horas")
        }

        item(key = "menu_ajustes") {
            SettingsMenuItem(
                icon = Icons.Default.AccountBalance,
                title = "Ajustes de Saldo",
                subtitle = "Adicionar ou remover horas manualmente",
                badge = uiState.empregoAtivo?.totalAjustes?.let {
                    if (it > 0) "$it" else null
                },
                onClick = onNavigateToAjustesBancoHoras
            )
        }

        item(key = "divider_4") { SettingsDivider() }

        // ═══════════════════════════════════════════════════════════════
        // SEÇÃO: APLICATIVO
        // ═══════════════════════════════════════════════════════════════
        item(key = "section_app") {
            SettingsSectionHeader(title = "Aplicativo")
        }

        item(key = "menu_aparencia") {
            SettingsMenuItem(
                icon = Icons.Default.Palette,
                title = "Aparência",
                subtitle = "Tema e personalização visual",
                onClick = onNavigateToAparencia
            )
        }

        item(key = "menu_backup") {
            SettingsMenuItem(
                icon = Icons.Default.Storage,
                title = "Backup & Dados",
                subtitle = "Exportar, importar e gerenciar dados",
                onClick = onNavigateToBackup
            )
        }

        item(key = "menu_sobre") {
            SettingsMenuItem(
                icon = Icons.Default.Info,
                title = "Sobre",
                subtitle = "Versão ${uiState.versaoFormatada}",
                onClick = onNavigateToSobre
            )
        }
    }
}

/**
 * Bottom Sheet para seleção de emprego.
 */
@Composable
private fun EmpregoSelectorBottomSheet(
    empregoAtivo: EmpregoResumo?,
    outrosEmpregos: List<EmpregoResumo>,
    onSelecionarEmprego: (Long) -> Unit,
    onNovoEmprego: () -> Unit,
    onGerenciarEmpregos: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 32.dp)
    ) {
        Text(
            text = "Selecionar Emprego",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Emprego ativo
        empregoAtivo?.let { resumo ->
            EmpregoSelectorItem(
                resumo = resumo,
                isAtivo = true,
                onClick = { /* Já é o ativo */ }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Outros empregos
        outrosEmpregos.forEach { resumo ->
            EmpregoSelectorItem(
                resumo = resumo,
                isAtivo = false,
                onClick = { onSelecionarEmprego(resumo.emprego.id) }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Ações
        Surface(
            onClick = onNovoEmprego,
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.secondaryContainer,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                Text(
                    text = "Novo Emprego",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(
            onClick = onGerenciarEmpregos,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Gerenciar todos os empregos")
        }
    }
}
