package br.com.tlmacedo.meuponto.presentation.screen.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.EventNote
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.tlmacedo.meuponto.presentation.components.MeuPontoTopBar

@Composable
fun SettingsMainScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEmpregoSettings: (Long) -> Unit,
    onNavigateToGerenciarEmpregos: () -> Unit,
    onNavigateToCalendario: () -> Unit,
    onNavigateToAparencia: () -> Unit,
    onNavigateToNotificacoes: () -> Unit,
    onNavigateToPrivacidade: () -> Unit,
    onNavigateToBackup: () -> Unit,
    onNavigateToSobre: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsMainViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            MeuPontoTopBar(
                title = "Configurações",
                showBackButton = true,
                onBackClick = onNavigateBack
            )
        },
        modifier = modifier
    ) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
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

            // Emprego Atual (Card Destacado)
            item {
                EmpregoAtualCard(
                    nomeEmprego = uiState.empregoAtualNome,
                    versaoVigente = uiState.versaoVigenteDescricao,
                    onClick = { uiState.empregoAtualId?.let { onNavigateToEmpregoSettings(it) } }
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

            // ══════════════════════════════════════════════════════════════
            // SEÇÃO: CALENDÁRIO
            // ══════════════════════════════════════════════════════════════
            item {
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
                SettingsSectionHeader(
                    title = "Design",
                    icon = Icons.Outlined.Palette
                )
            }

            item {
                SettingsNavigationItem(
                    title = "Aparência",
                    subtitle = "Tema, cores, densidade visual",
                    icon = Icons.Outlined.DarkMode,
                    onClick = onNavigateToAparencia
                )
            }

            item {
                SettingsNavigationItem(
                    title = "Notificações",
                    subtitle = "Lembretes, alertas de ponto",
                    icon = Icons.Outlined.Notifications,
                    onClick = onNavigateToNotificacoes
                )
            }

            item {
                SettingsNavigationItem(
                    title = "Privacidade",
                    subtitle = "Biometria, controle de acesso",
                    icon = Icons.Outlined.Security,
                    onClick = onNavigateToPrivacidade
                )
            }

            // ══════════════════════════════════════════════════════════════
            // SEÇÃO: BACKUP E DADOS
            // ══════════════════════════════════════════════════════════════
            item {
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
            }

            // ══════════════════════════════════════════════════════════════
            // SEÇÃO: SOBRE
            // ══════════════════════════════════════════════════════════════
            item {
                SettingsSectionHeader(
                    title = "Sobre",
                    icon = Icons.Outlined.Info
                )
            }

            item {
                SettingsNavigationItem(
                    title = "Sobre o App",
                    subtitle = "Versão, desenvolvedor, contato",
                    icon = Icons.Outlined.Info,
                    onClick = onNavigateToSobre
                )
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// COMPONENTES
// ══════════════════════════════════════════════════════════════════════════════

@Composable
private fun SettingsSectionHeader(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.padding(vertical = 8.dp)
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

@Composable
private fun EmpregoAtualCard(
    nomeEmprego: String?,
    versaoVigente: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Business,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Emprego Atual",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
                Text(
                    text = nomeEmprego ?: "Nenhum emprego configurado",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                versaoVigente?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = "Editar",
                tint = MaterialTheme.colorScheme.onPrimaryContainer
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
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
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
