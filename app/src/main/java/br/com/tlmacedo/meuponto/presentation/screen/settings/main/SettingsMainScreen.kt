// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/settings/main/SettingsMainScreen.kt
package br.com.tlmacedo.meuponto.presentation.screen.settings.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.tlmacedo.meuponto.domain.model.Emprego
import br.com.tlmacedo.meuponto.presentation.components.LocalImage
import br.com.tlmacedo.meuponto.presentation.components.MeuPontoTopBar
import br.com.tlmacedo.meuponto.presentation.screen.settings.main.components.TrocarEmpregoBottomSheet
import br.com.tlmacedo.meuponto.presentation.theme.MeuPontoTheme
import br.com.tlmacedo.meuponto.presentation.theme.SidiaBlue
import br.com.tlmacedo.meuponto.presentation.theme.SidiaDarkBlue
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Composable
fun SettingsMainScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEmpregoEdit: (Long) -> Unit,
    onNavigateToGerenciarEmpregos: () -> Unit,
    onNavigateToEmpregoSettings: (Long) -> Unit,
    onNavigateToAusencias: (Long) -> Unit,
    onNavigateToCalendario: () -> Unit,
    onNavigateToHistoryCalendar: () -> Unit,
    onNavigateToAparencia: () -> Unit,
    onNavigateToNotificacoes: () -> Unit,
    onNavigateToPrivacidade: () -> Unit,
    onNavigateToBackup: () -> Unit,
    onNavigateToSobre: () -> Unit,
    onNavigateToAjuda: () -> Unit,
    onNavigateToLixeira: () -> Unit,
    onNavigateToAuditoria: () -> Unit,
    onNavigateToPendencias: () -> Unit,
    onNavigateToRelatorios: () -> Unit,
    onNavigateToComprovantes: () -> Unit,
    onNavigateToOpcoesRegistro: (Long) -> Unit,
    onNavigateToJornada: () -> Unit,
    onNavigateToChamados: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsMainViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

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
        onNavigateToHistoryCalendar = onNavigateToHistoryCalendar,
        onNavigateToAparencia = onNavigateToAparencia,
        onNavigateToNotificacoes = onNavigateToNotificacoes,
        onNavigateToPrivacidade = onNavigateToPrivacidade,
        onNavigateToBackup = onNavigateToBackup,
        onNavigateToSobre = onNavigateToSobre,
        onNavigateToAjuda = onNavigateToAjuda,
        onNavigateToLixeira = onNavigateToLixeira,
        onNavigateToAuditoria = onNavigateToAuditoria,
        onNavigateToPendencias = onNavigateToPendencias,
        onNavigateToRelatorios = onNavigateToRelatorios,
        onNavigateToComprovantes = onNavigateToComprovantes,
        onNavigateToOpcoesRegistro = onNavigateToOpcoesRegistro,
        onNavigateToJornada = onNavigateToJornada,
        onNavigateToChamados = onNavigateToChamados,
        onTrocarEmprego = { viewModel.onAction(SettingsMainAction.TrocarEmprego(it)) },
        onAlternarSecao = { viewModel.onAction(SettingsMainAction.AlternarExpansaoSecao(it)) },
        snackbarHostState = snackbarHostState,
        modifier = modifier
    )
}

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
    onNavigateToHistoryCalendar: () -> Unit,
    onNavigateToAparencia: () -> Unit,
    onNavigateToNotificacoes: () -> Unit,
    onNavigateToPrivacidade: () -> Unit,
    onNavigateToBackup: () -> Unit,
    onNavigateToSobre: () -> Unit,
    onNavigateToAjuda: () -> Unit,
    onNavigateToLixeira: () -> Unit,
    onNavigateToAuditoria: () -> Unit,
    onNavigateToPendencias: () -> Unit,
    onNavigateToRelatorios: () -> Unit,
    onNavigateToComprovantes: () -> Unit,
    onNavigateToOpcoesRegistro: (Long) -> Unit,
    onNavigateToJornada: () -> Unit,
    onNavigateToChamados: () -> Unit,
    onTrocarEmprego: (Emprego) -> Unit,
    onAlternarSecao: (String) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    val scope = rememberCoroutineScope()

    var showTrocarEmpregoSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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

            item {
                CollapsibleSettingsSection(
                    title = "Calendário",
                    icon = Icons.Outlined.CalendarMonth,
                    isExpanded = uiState.secoesExpandidas.contains("Calendario"),
                    onToggle = { onAlternarSecao("Calendario") }
                ) {
                    SettingsItemsLayout {
                        SettingsNavigationItem(
                            title = "Visualizar Calendário",
                            subtitle = "Visão mensal completa de registros",
                            icon = Icons.Outlined.CalendarMonth,
                            onClick = onNavigateToHistoryCalendar
                        )
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

            item {
                CollapsibleSettingsSection(
                    title = "Relatórios",
                    icon = Icons.AutoMirrored.Outlined.EventNote,
                    isExpanded = uiState.secoesExpandidas.contains("Relatorios"),
                    onToggle = { onAlternarSecao("Relatorios") }
                ) {
                    SettingsItemsLayout {
                        SettingsNavigationItem(
                            title = "Espelho de Ponto",
                            subtitle = "Gerar PDF ou CSV mensal",
                            icon = Icons.Outlined.History,
                            onClick = onNavigateToRelatorios
                        )
                        SettingsNavigationItem(
                            title = "Painel de Pendências",
                            subtitle = "Saúde e inconsistências do ponto",
                            icon = Icons.Outlined.Notifications,
                            onClick = onNavigateToPendencias
                        )
                    }
                }
            }

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
                        SettingsNavigationItem(
                            title = "Configuração de Jornada",
                            subtitle = "Padrões de carga horária e tolerâncias",
                            icon = Icons.Outlined.AccessTime,
                            onClick = onNavigateToJornada
                        )
                    }
                }
            }

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
                        SettingsNavigationItem(
                            icon = Icons.AutoMirrored.Outlined.EventNote,
                            title = "Gerenciador de Comprovantes",
                            subtitle = "Galeria de fotos e recibos",
                            onClick = onNavigateToComprovantes
                        )
                    }
                }
            }

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
                            title = "Suporte e Chamados",
                            subtitle = "Dúvidas, sugestões ou problemas",
                            icon = Icons.AutoMirrored.Outlined.EventNote,
                            onClick = onNavigateToChamados
                        )
                    }
                }
            }

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
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            border = BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
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
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = when {
                            saldoMensal.startsWith("-") -> MaterialTheme.colorScheme.error
                            saldoMensal.contains("00:00") -> MaterialTheme.colorScheme.onSurface
                            else -> MaterialTheme.colorScheme.secondary
                        }
                    )
                }
                Icon(
                    imageVector = Icons.Outlined.History,
                    contentDescription = null,
                    tint = when {
                        saldoMensal.startsWith("-") -> MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
                        saldoMensal.contains("00:00") -> MaterialTheme.colorScheme.onSurfaceVariant.copy(
                            alpha = 0.6f
                        )

                        else -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f)
                    },
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Card(
            onClick = onBackupClick,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            border = BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
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
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f

    Card(
        onClick = onClick,
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent,
            contentColor = if (isDark) Color.White else SidiaDarkBlue
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier
            .fillMaxWidth()
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.04f)
                    )
                ),
                shape = MaterialTheme.shapes.extraLarge
            )
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                    )
                ),
                shape = MaterialTheme.shapes.extraLarge
            )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier
                        .size(72.dp)
                        .shadow(4.dp, CircleShape),
                    shape = CircleShape,
                    color = Color.White
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (logoUri != null) {
                            LocalImage(
                                imagePath = logoUri,
                                contentDescription = "Logo da empresa",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Outlined.Business,
                                contentDescription = null,
                                modifier = Modifier.size(36.dp),
                                tint = SidiaBlue
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(20.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Emprego Ativo",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = nomeEmprego,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isDark) Color.White else SidiaDarkBlue,
                        lineHeight = 26.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.EventNote,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = versaoVigente,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Outlined.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

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
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
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
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
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
            onNavigateToEmpregoEdit = { _ -> },
            onNavigateToGerenciarEmpregos = {},
            onNavigateToEmpregoSettings = { _ -> },
            onNavigateToAusencias = { _ -> },
            onNavigateToCalendario = {},
            onNavigateToAparencia = {},
            onNavigateToNotificacoes = {},
            onNavigateToPrivacidade = {},
            onNavigateToBackup = {},
            onNavigateToSobre = {},
            onNavigateToAjuda = {},
            onNavigateToLixeira = {},
            onNavigateToAuditoria = {},
            onNavigateToComprovantes = {},
            onNavigateToOpcoesRegistro = { _ -> },
            onNavigateToJornada = {},
            onNavigateToChamados = {},
            onNavigateToHistoryCalendar = {},
            onNavigateToPendencias = {},
            onNavigateToRelatorios = {},
            onTrocarEmprego = { _ -> },
            onAlternarSecao = { _ -> }
        )
    }
}
