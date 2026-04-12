package br.com.tlmacedo.meuponto.presentation.screen.settings.design

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.Vibration
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.tlmacedo.meuponto.presentation.components.MeuPontoTopBar
import kotlinx.coroutines.flow.collectLatest

/**
 * Tela de configurações de notificações.
 *
 * @author Thiago
 * @since 9.0.0
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NotificacoesScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NotificacoesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.eventos.collectLatest { evento ->
            when (evento) {
                is NotificacoesEvent.MostrarMensagem -> {
                    snackbarHostState.showSnackbar(evento.mensagem)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            MeuPontoTopBar(
                title = "Notificações",
                showBackButton = true,
                onBackClick = onNavigateBack
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
            // MASTER SWITCH
            // ══════════════════════════════════════════════════════════════
            item {
                NotificacaoMasterCard(
                    habilitadas = uiState.notificacoesHabilitadas,
                    onToggle = {
                        viewModel.onAction(NotificacoesAction.ToggleNotificacoes)
                    }
                )
            }

            if (uiState.notificacoesHabilitadas) {
                // ══════════════════════════════════════════════════════════════
                // LEMBRETES
                // ══════════════════════════════════════════════════════════════
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SectionHeader(
                            title = "Lembretes",
                            icon = Icons.Outlined.Alarm
                        )
                        NotificacaoSwitch(
                            title = "Lembrete de entrada",
                            subtitle = "Avisar quando estiver perto do horário de entrada",
                            checked = uiState.lembreteEntrada,
                            onCheckedChange = {
                                viewModel.onAction(NotificacoesAction.ToggleLembreteEntrada)
                            }
                        )

                        NotificacaoSwitch(
                            title = "Lembrete de saída",
                            subtitle = "Avisar quando estiver perto do horário de saída",
                            checked = uiState.lembreteSaida,
                            onCheckedChange = {
                                viewModel.onAction(NotificacoesAction.ToggleLembreteSaida)
                            }
                        )

                        NotificacaoSwitch(
                            title = "Lembrete de intervalo",
                            subtitle = "Avisar quando o intervalo estiver terminando",
                            checked = uiState.lembreteIntervalo,
                            onCheckedChange = {
                                viewModel.onAction(NotificacoesAction.ToggleLembreteIntervalo)
                            }
                        )
                    }
                }

                // ══════════════════════════════════════════════════════════════
                // ALERTAS
                // ══════════════════════════════════════════════════════════════
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SectionHeader(
                            title = "Alertas",
                            icon = Icons.Outlined.Timer
                        )
                        NotificacaoSwitch(
                            title = "Alerta de hora extra",
                            subtitle = "Avisar quando ultrapassar a jornada normal",
                            checked = uiState.alertaHoraExtra,
                            onCheckedChange = {
                                viewModel.onAction(NotificacoesAction.ToggleAlertaHoraExtra)
                            }
                        )

                        NotificacaoSwitch(
                            title = "Alerta de jornada máxima",
                            subtitle = "Avisar quando se aproximar da jornada máxima diária",
                            checked = uiState.alertaJornadaMaxima,
                            onCheckedChange = {
                                viewModel.onAction(NotificacoesAction.ToggleAlertaJornadaMaxima)
                            }
                        )
                    }
                }

                // ══════════════════════════════════════════════════════════════
                // COMPORTAMENTO
                // ══════════════════════════════════════════════════════════════
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SectionHeader(
                            title = "Comportamento",
                            icon = Icons.Outlined.Vibration
                        )
                        NotificacaoSwitch(
                            title = "Vibração",
                            subtitle = "Vibrar ao exibir notificações",
                            checked = uiState.vibracaoHabilitada,
                            onCheckedChange = {
                                viewModel.onAction(NotificacoesAction.ToggleVibracao)
                            }
                        )

                        NotificacaoSwitch(
                            title = "Som",
                            subtitle = "Reproduzir som ao exibir notificações",
                            checked = uiState.somHabilitado,
                            onCheckedChange = {
                                viewModel.onAction(NotificacoesAction.ToggleSom)
                            }
                        )
                    }
                }
            }


            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun NotificacaoMasterCard(
    habilitadas: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (habilitadas) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                imageVector = if (habilitadas) {
                    Icons.Outlined.NotificationsActive
                } else {
                    Icons.Outlined.NotificationsOff
                },
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = if (habilitadas) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Notificações",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (habilitadas) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
                Text(
                    text = if (habilitadas) "Ativadas" else "Desativadas",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (habilitadas) {
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }

            Switch(
                checked = habilitadas,
                onCheckedChange = { onToggle() }
            )
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
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
private fun NotificacaoSwitch(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
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

            Switch(
                checked = checked,
                onCheckedChange = { onCheckedChange() }
            )
        }
    }
}
