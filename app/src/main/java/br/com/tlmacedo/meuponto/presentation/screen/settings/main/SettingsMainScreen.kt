// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/settings/main/SettingsMainScreen.kt
package br.com.tlmacedo.meuponto.presentation.screen.settings.main

import androidx.compose.material.icons.outlined.Delete
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.EventNote
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.CloudSync
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material.icons.outlined.Work
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.tlmacedo.meuponto.presentation.components.MeuPontoTopBar
import br.com.tlmacedo.meuponto.presentation.screen.settings.main.components.TrocarEmpregoBottomSheet
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
 * @updated 9.1.0 - Removido card de Emprego Atual (agora exibido apenas em Gerenciar Empregos)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsMainScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEmpregoEdit: (Long) -> Unit,
    onNavigateToGerenciarEmpregos: () -> Unit,
    onNavigateToCalendario: () -> Unit,
    onNavigateToAparencia: () -> Unit,
    onNavigateToNotificacoes: () -> Unit,
    onNavigateToPrivacidade: () -> Unit,
    onNavigateToBackup: () -> Unit,
    onNavigateToSobre: () -> Unit,
    onNavigateToLixeira: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsMainViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Estado do BottomSheet
    var showTrocarEmpregoSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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

    // BottomSheet de troca de emprego
    if (showTrocarEmpregoSheet) {
        TrocarEmpregoBottomSheet(
            empregos = uiState.empregosDisponiveis,
            empregoAtivoId = uiState.empregoAtualId,
            sheetState = sheetState,
            onEmpregoSelecionado = { emprego ->
                viewModel.onAction(SettingsMainAction.TrocarEmprego(emprego))
            },
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
                showBackButton = true,
                onBackClick = onNavigateBack
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier
    ) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ══════════════════════════════════════════════════════════════
            // SEÇÃO: EMPREGOS
            // ══════════════════════════════════════════════════════════════
            item {
                SettingsSectionHeader(
                    title = "Empregos",
                    icon = Icons.Outlined.Work
                )
            }

            // Gerenciar Empregos
            item {
                SettingsNavigationItem(
                    title = "Gerenciar Empregos",
                    subtitle = "Adicionar, editar ou excluir empregos",
                    icon = Icons.Outlined.Business,
                    onClick = onNavigateToGerenciarEmpregos
                )
            }

            // Trocar Emprego Ativo (apenas se houver mais de um emprego)
            if (uiState.empregosDisponiveis.size > 1) {
                item {
                    SettingsNavigationItem(
                        title = "Trocar Emprego Ativo",
                        subtitle = "Alternar rapidamente entre empregos",
                        icon = Icons.Outlined.SwapHoriz,
                        onClick = { showTrocarEmpregoSheet = true }
                    )
                }
            }

            // ══════════════════════════════════════════════════════════════
            // SEÇÃO: CALENDÁRIO
            // ══════════════════════════════════════════════════════════════
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SettingsSectionHeader(
                    title = "Calendário",
                    icon = Icons.Outlined.CalendarMonth
                )
            }

            item {
                SettingsNavigationItem(
                    title = "Feriados",
                    subtitle = "Nacionais, Estaduais e Municipais",
                    icon = Icons.AutoMirrored.Outlined.EventNote,
                    onClick = onNavigateToCalendario
                )
            }

            // ══════════════════════════════════════════════════════════════
            // SEÇÃO: DESIGN
            // ══════════════════════════════════════════════════════════════
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SettingsSectionHeader(
                    title = "Design",
                    icon = Icons.Outlined.Palette
                )
            }

            item {
                SettingsNavigationItem(
                    title = "Aparência",
                    subtitle = "Tema, cores e densidade visual",
                    icon = Icons.Outlined.DarkMode,
                    onClick = onNavigateToAparencia
                )
            }

            item {
                SettingsNavigationItem(
                    title = "Notificações",
                    subtitle = "Lembretes e alertas de ponto",
                    icon = Icons.Outlined.Notifications,
                    onClick = onNavigateToNotificacoes
                )
            }

            item {
                SettingsNavigationItem(
                    title = "Privacidade",
                    subtitle = "Proteção do app e biometria",
                    icon = Icons.Outlined.Security,
                    onClick = onNavigateToPrivacidade
                )
            }

            // ══════════════════════════════════════════════════════════════
            // SEÇÃO: BACKUP E DADOS
            // ══════════════════════════════════════════════════════════════
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SettingsSectionHeader(
                    title = "Backup e Dados",
                    icon = Icons.Outlined.CloudSync
                )
            }

            item {
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
            }

            // ══════════════════════════════════════════════════════════════
            // SEÇÃO: SOBRE
            // ══════════════════════════════════════════════════════════════
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SettingsSectionHeader(
                    title = "Sobre",
                    icon = Icons.Outlined.Info
                )
            }

            item {
                SettingsNavigationItem(
                    title = "Sobre o App",
                    subtitle = "Versão, desenvolvedor e contato",
                    icon = Icons.Outlined.Info,
                    onClick = onNavigateToSobre
                )
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

/**
 * Cabeçalho de seção com ícone e título.
 */
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
