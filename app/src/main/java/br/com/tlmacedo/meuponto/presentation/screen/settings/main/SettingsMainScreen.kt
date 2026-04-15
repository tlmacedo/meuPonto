// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/settings/main/SettingsMainScreen.kt
package br.com.tlmacedo.meuponto.presentation.screen.settings.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.EventNote
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.CloudSync
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.QuestionMark
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material.icons.outlined.Work
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.tlmacedo.meuponto.domain.model.Emprego
import br.com.tlmacedo.meuponto.presentation.components.LocalImage
import br.com.tlmacedo.meuponto.presentation.components.MeuPontoTopBar
import br.com.tlmacedo.meuponto.presentation.screen.settings.main.components.TrocarEmpregoBottomSheet
import br.com.tlmacedo.meuponto.presentation.theme.MeuPontoTheme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Tela principal de configurações do app.
 *
 * Apresenta as configurações organizadas em seções:
 * - Empregos (gerenciamento + troca rápida)
 * - Calendário (feriados)
 * - Design (aparência, notificações, privacidade)
 * - Backup e Dados
 * - Sobre
 *
 * @author Thiago
 * @since 9.0.0
 * @updated 9.2.0 - Refatorado para separar Content e adicionar Previews
 */
@Composable
fun SettingsMainScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEmpregoEdit: (Long) -> Unit,
    onNavigateToGerenciarEmpregos: () -> Unit,
    onNavigateToEmpregoSettings: (Long) -> Unit,
    onNavigateToAusencias: (Long) -> Unit,
    onNavigateToCalendario: () -> Unit,
    onNavigateToAparencia: () -> Unit,
    onNavigateToNotificacoes: () -> Unit,
    onNavigateToPrivacidade: () -> Unit,
    onNavigateToBackup: () -> Unit,
    onNavigateToSobre: () -> Unit,
    onNavigateToAjuda: () -> Unit,
    onNavigateToReportarProblema: () -> Unit,
    onNavigateToLixeira: () -> Unit,
    onNavigateToAuditoria: () -> Unit,
    onNavigateToOpcoesRegistro: (Long) -> Unit,
    viewModel: SettingsMainViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Coleta eventos do ViewModel
    LaunchedEffect(Unit) {
        viewModel.eventos.collectLatest { evento ->
            when (evento) {
                is SettingsMainEvent.MostrarMensagem -> {
                    snackbarHostState.showSnackbar(evento.mensagem)
                }

                is SettingsMainEvent.EmpregoTrocado -> {
                    snackbarHostState.showSnackbar("Emprego alterado para ${evento.nomeEmprego}")
                }
            }
        }
    }

    SettingsMainContent(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onNavigateToEmpregoEdit = onNavigateToEmpregoEdit,
        onNavigateToGerenciarEmpregos = onNavigateToGerenciarEmpregos,
        onNavigateToEmpregoSettings = onNavigateToEmpregoSettings,
        onNavigateToAusencias = onNavigateToAusencias,
        onNavigateToCalendario = onNavigateToCalendario,
        onNavigateToAparencia = onNavigateToAparencia,
        onNavigateToNotificacoes = onNavigateToNotificacoes,
        onNavigateToPrivacidade = onNavigateToPrivacidade,
        onNavigateToBackup = onNavigateToBackup,
        onNavigateToSobre = onNavigateToSobre,
        onNavigateToAjuda = onNavigateToAjuda,
        onNavigateToReportarProblema = onNavigateToReportarProblema,
        onNavigateToLixeira = onNavigateToLixeira,
        onNavigateToAuditoria = onNavigateToAuditoria,
        onNavigateToOpcoesRegistro = onNavigateToOpcoesRegistro,
        onTrocarEmprego = { viewModel.onAction(SettingsMainAction.TrocarEmprego(it)) },
        onAlternarSecao = { viewModel.onAction(SettingsMainAction.AlternarExpansaoSecao(it)) },
        snackbarHostState = snackbarHostState
    )
}

/**
 * Conteúdo da tela principal de configurações, desacoplado do ViewModel.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsMainContent(
    uiState: SettingsMainUiState,
    onNavigateBack: () -> Unit,
    onNavigateToEmpregoEdit: (Long) -> Unit,
    onNavigateToGerenciarEmpregos: () -> Unit,
    onNavigateToEmpregoSettings: (Long) -> Unit,
    onNavigateToAusencias: (Long) -> Unit,
    onNavigateToCalendario: () -> Unit,
    onNavigateToAparencia: () -> Unit,
    onNavigateToNotificacoes: () -> Unit,
    onNavigateToPrivacidade: () -> Unit,
    onNavigateToBackup: () -> Unit,
    onNavigateToSobre: () -> Unit,
    onNavigateToAjuda: () -> Unit,
    onNavigateToReportarProblema: () -> Unit,
    onNavigateToLixeira: () -> Unit,
    onNavigateToAuditoria: () -> Unit,
    onNavigateToOpcoesRegistro: (Long) -> Unit,
    onTrocarEmprego: (Emprego) -> Unit,
    onAlternarSecao: (String) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    val scope = rememberCoroutineScope()

    // Estado do BottomSheet
    var showTrocarEmpregoSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // BottomSheet de troca de emprego
    if (showTrocarEmpregoSheet) {
        TrocarEmpregoBottomSheet(
            empregos = uiState.empregosDisponiveis,
            empregoAtivoId = uiState.empregoAtualId,
            sheetState = sheetState,
            onEmpregoSelecionado = onTrocarEmprego,
            onGerenciarEmpregos = onNavigateToGerenciarEmpregos,
            onDismiss = {
                scope.launch {
                    sheetState.hide()
                    showTrocarEmpregoSheet = false
                }
            }
        )
    }

    Scaffold(
        topBar = {
            MeuPontoTopBar(
                title = "Configurações",
                subtitle = uiState.empregoAtual?.apelido?.uppercase(),
                logo = uiState.empregoAtual?.logo,
                showBackButton = true,
                onBackClick = onNavigateBack
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier
    ) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(
                horizontal = 16.dp,
                vertical = 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ══════════════════════════════════════════════════════════════
            // SEÇÃO: EMPREGO ATIVO E JORNADA (DESTAQUE)
            // ══════════════════════════════════════════════════════════════
            uiState.empregoAtual?.let { emprego ->
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        ActiveEmploymentCard(
                            nomeEmprego = emprego.nome,
                            logoUri = emprego.logo,
                            versaoVigente = uiState.versaoVigenteDescricao
                                ?: "Nenhuma versão ativa",
                            onClick = { onNavigateToEmpregoSettings(emprego.id) }
                        )

                        SystemStatusCard(
                            saldoMensal = uiState.saldoAtualTexto,
                            ultimoBackup = uiState.dataUltimoBackup,
                            onBackupClick = onNavigateToBackup
                        )
                    }
                }
            }

            // ══════════════════════════════════════════════════════════════
            // SEÇÃO: EMPREGOS
            // ══════════════════════════════════════════════════════════════
            item {
                CollapsibleSettingsSection(
                    title = "Gestão de Empregos",
                    icon = Icons.Outlined.Work,
                    isExpanded = uiState.secoesExpandidas.contains("Empregos"),
                    onToggle = { onAlternarSecao("Empregos") }
                ) {
                    SettingsItemsLayout {
                        SettingsNavigationItem(
                            title = "Meus Empregos",
                            subtitle = "Adicionar, editar ou arquivar",
                            icon = Icons.Outlined.Business,
                            onClick = onNavigateToGerenciarEmpregos
                        )

                        // Trocar Emprego Ativo (apenas se houver mais de um emprego)
                        if (uiState.empregosDisponiveis.size > 1) {
                            SettingsNavigationItem(
                                title = "Trocar Emprego Ativo",
                                subtitle = "Alternar rapidamente entre empregos",
                                icon = Icons.Outlined.SwapHoriz,
                                onClick = { showTrocarEmpregoSheet = true }
                            )
                        }
                    }
                }
            }

            // ══════════════════════════════════════════════════════════════
            // SEÇÃO: CALENDÁRIO
            // ══════════════════════════════════════════════════════════════
            item {
                CollapsibleSettingsSection(
                    title = "Calendário",
                    icon = Icons.Outlined.CalendarMonth,
                    isExpanded = uiState.secoesExpandidas.contains("Calendario"),
                    onToggle = { onAlternarSecao("Calendario") }
                ) {
                    SettingsItemsLayout {
                        SettingsNavigationItem(
                            title = "Feriados",
                            subtitle = "Nacionais, Estaduais e Municipais",
                            icon = Icons.AutoMirrored.Outlined.EventNote,
                            onClick = onNavigateToCalendario
                        )
                        SettingsNavigationItem(
                            title = "Ausências",
                            subtitle = "Gerenciar faltas e afastamentos",
                            icon = Icons.Outlined.Add,
                            onClick = {
                                uiState.empregoAtualId?.let {
                                    onNavigateToAusencias(it)
                                }
                            }
                        )
                    }
                }
            }

            // ══════════════════════════════════════════════════════════════
            // SEÇÃO: CONFIGURAÇÕES GERAIS
            // ══════════════════════════════════════════════════════════════
            item {
                CollapsibleSettingsSection(
                    title = "Configurações Gerais",
                    icon = Icons.Outlined.Palette,
                    isExpanded = uiState.secoesExpandidas.contains("ConfiguracoesGerais"),
                    onToggle = { onAlternarSecao("ConfiguracoesGerais") }
                ) {
                    SettingsItemsLayout {
                        SettingsNavigationItem(
                            title = "Aparência",
                            subtitle = "Tema, cores e densidade visual",
                            icon = Icons.Outlined.DarkMode,
                            onClick = onNavigateToAparencia
                        )
                        SettingsNavigationItem(
                            title = "Notificações",
                            subtitle = "Lembretes e alertas de ponto",
                            icon = Icons.Outlined.Notifications,
                            onClick = onNavigateToNotificacoes
                        )
                        SettingsNavigationItem(
                            title = "Privacidade",
                            subtitle = "Proteção do app e biometria",
                            icon = Icons.Outlined.Security,
                            onClick = onNavigateToPrivacidade
                        )
                    }
                }
            }

            // ══════════════════════════════════════════════════════════════
            // SEÇÃO: BACKUP E DADOS
            // ══════════════════════════════════════════════════════════════
            item {
                CollapsibleSettingsSection(
                    title = "Backups e Dados",
                    icon = Icons.Outlined.CloudSync,
                    isExpanded = uiState.secoesExpandidas.contains("Dados"),
                    onToggle = { onAlternarSecao("Dados") }
                ) {
                    SettingsItemsLayout {
                        SettingsNavigationItem(
                            title = "Gerenciar Dados",
                            subtitle = "Exportação, importação e manutenção",
                            icon = Icons.Outlined.Storage,
                            onClick = onNavigateToBackup
                        )
                        SettingsNavigationItem(
                            icon = Icons.Outlined.Delete,
                            title = "Lixeira",
                            subtitle = "Itens excluídos recentemente",
                            onClick = onNavigateToLixeira
                        )
                        SettingsNavigationItem(
                            icon = Icons.Outlined.History,
                            title = "Auditoria",
                            subtitle = "Histórico de alterações no sistema",
                            onClick = onNavigateToAuditoria
                        )
                    }
                }
            }

            // ══════════════════════════════════════════════════════════════
            // SEÇÃO: SOBRE
            // ══════════════════════════════════════════════════════════════
            item {
                CollapsibleSettingsSection(
                    title = "Sobre",
                    icon = Icons.Outlined.Info,
                    isExpanded = uiState.secoesExpandidas.contains("Sobre"),
                    onToggle = { onAlternarSecao("Sobre") }
                ) {
                    SettingsItemsLayout {
                        SettingsNavigationItem(
                            title = "Sobre o App",
                            subtitle = "Versão, desenvolvedor e contato",
                            icon = Icons.Outlined.Info,
                            onClick = onNavigateToSobre
                        )
                        SettingsNavigationItem(
                            title = "Ajuda",
                            subtitle = "Suporte técnico e tutoriais",
                            icon = Icons.Outlined.QuestionMark,
                            onClick = onNavigateToAjuda
                        )
                        SettingsNavigationItem(
                            title = "Reportar Problema",
                            subtitle = "Beta: Erros, bugs ou sugestões",
                            icon = Icons.Outlined.BugReport,
                            onClick = onNavigateToReportarProblema
                        )
                    }
                }
            }

            // Espaçamento final
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════════════════════
// COMPONENTES INTERNOS
// ════════════════════════════════════════════════════════════════════════════════

@Composable
private fun SettingsItemsLayout(
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        content()
    }
}

@Composable
private fun SystemStatusCard(
    saldoMensal: String,
    ultimoBackup: String,
    onBackupClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        // Card de Saldo
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxHeight(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Saldo no Mês",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = saldoMensal,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = when {
                            saldoMensal.startsWith("-") -> MaterialTheme.colorScheme.error
                            saldoMensal.contains("00:00") -> MaterialTheme.colorScheme.onSurface
                            else -> MaterialTheme.colorScheme.secondary // Sidia Green
                        }
                    )
                }
                Icon(
                    imageVector = Icons.Outlined.History,
                    contentDescription = null,
                    tint = when {
                        saldoMensal.startsWith("-") -> MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
                        saldoMensal.contains("00:00") -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        else -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f)
                    },
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Card de Backup
        Card(
            onClick = onBackupClick,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxHeight(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Último Backup",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                    Text(
                        text = ultimoBackup,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                }
                Icon(
                    imageVector = Icons.Outlined.CloudSync,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun ActiveEmploymentCard(
    nomeEmprego: String,
    logoUri: String?,
    versaoVigente: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Logo da Empresa
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (logoUri != null) {
                        LocalImage(
                            imagePath = logoUri,
                            contentDescription = "Logo da empresa",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Outlined.Business,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.width(20.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Emprego Ativo",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        text = nomeEmprego,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.EventNote,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = versaoVigente,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Outlined.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

/**
 * Componente para seções colapsáveis nas configurações.
 */
@Composable
private fun CollapsibleSettingsSection(
    title: String,
    icon: ImageVector,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.medium)
                    .clickable { onToggle() }
                    .padding(vertical = 12.dp, horizontal = 4.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                    contentDescription = if (isExpanded) "Recolher" else "Expandir",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .padding(top = 4.dp, bottom = 12.dp)
                        .fillMaxWidth()
                ) {
                    content()
                }
            }
            
            HorizontalDivider(
                modifier = Modifier.padding(top = 8.dp),
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
private fun SettingsSectionHeader(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.padding(vertical = 8.dp, horizontal = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

/**
 * Item de navegação para configurações.
 */
@Composable
private fun SettingsNavigationItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Card(
        onClick = onClick,
        enabled = enabled,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ════════════════════════════════════════════════════════════════════════════════
// PREVIEWS
// ════════════════════════════════════════════════════════════════════════════════

@Preview(showBackground = true)
@Composable
private fun SettingsMainContentPreview() {
    MeuPontoTheme {
        SettingsMainContent(
            uiState = SettingsMainUiState(
                empregoAtual = Emprego(id = 1, nome = "Empresa de Exemplo"),
                versaoVigenteDescricao = "Regra Geral 2024",
                secoesExpandidas = setOf("Empregos", "Calendario")
            ),
            onNavigateBack = {},
            onNavigateToEmpregoEdit = {},
            onNavigateToGerenciarEmpregos = {},
            onNavigateToEmpregoSettings = {},
            onNavigateToAusencias = {},
            onNavigateToCalendario = {},
            onNavigateToAparencia = {},
            onNavigateToNotificacoes = {},
            onNavigateToPrivacidade = {},
            onNavigateToBackup = {},
            onNavigateToSobre = {},
            onNavigateToAjuda = {},
            onNavigateToReportarProblema = {},
            onNavigateToLixeira = {},
            onNavigateToAuditoria = {},
            onNavigateToOpcoesRegistro = {},
            onTrocarEmprego = {},
            onAlternarSecao = {}
        )
    }
}
