package br.com.tlmacedo.meuponto.presentation.screen.settings.jornada

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
 * Tela de configurações de jornada (padrões globais).
 */
@Composable
fun JornadaScreen(
    onNavigateBack: () -> Unit,
    viewModel: JornadaViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.eventos.collectLatest { evento ->
            when (evento) {
                is JornadaEvent.MostrarMensagem -> {
                    snackbarHostState.showSnackbar(evento.mensagem)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            MeuPontoTopBar(
                title = "Configurações de Jornada",
                subtitle = "PADRÕES PARA NOVOS REGISTROS",
                showBackButton = true,
                onBackClick = onNavigateBack
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Defina os valores padrão que serão sugeridos ao criar novos empregos ou versões de jornada.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            JornadaSettingItem(
                title = "Carga Horária Diária",
                value = uiState.cargaHorariaMinutos,
                icon = Icons.Outlined.AccessTime,
                valueFormatter = { minutos ->
                    val horas = minutos / 60
                    val mins = minutos % 60
                    String.format(java.util.Locale.getDefault(), "%02dh %02dm", horas, mins)
                },
                range = 0f..1440f, // 0 a 24h
                onValueChange = { viewModel.onAction(JornadaAction.AlterarCargaHoraria(it)) }
            )

            JornadaSettingItem(
                title = "Intervalo Mínimo",
                value = uiState.intervaloMinimoMinutos,
                icon = Icons.Outlined.Timer,
                valueFormatter = { "$it minutos" },
                range = 0f..240f, // 0 a 4h
                onValueChange = { viewModel.onAction(JornadaAction.AlterarIntervaloMinimo(it)) }
            )

            JornadaSettingItem(
                title = "Tolerância Geral",
                value = uiState.toleranciaGeralMinutos,
                icon = Icons.Outlined.History,
                valueFormatter = { "$it minutos" },
                range = 0f..60f, // 0 a 60 min
                onValueChange = { viewModel.onAction(JornadaAction.AlterarToleranciaGeral(it)) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Dica",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Estas configurações são salvas automaticamente no seu perfil e sincronizadas com a nuvem.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
private fun JornadaSettingItem(
    title: String,
    value: Int,
    icon: ImageVector,
    valueFormatter: (Int) -> String,
    range: ClosedFloatingPointRange<Float>,
    onValueChange: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
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
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = valueFormatter(value),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(16.dp))

            Slider(
                value = value.toFloat(),
                onValueChange = { onValueChange(it.toInt()) },
                valueRange = range,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
