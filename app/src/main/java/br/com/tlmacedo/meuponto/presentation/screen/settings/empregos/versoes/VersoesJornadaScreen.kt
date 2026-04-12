package br.com.tlmacedo.meuponto.presentation.screen.settings.empregos.versoes

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.tlmacedo.meuponto.domain.model.VersaoJornada
import br.com.tlmacedo.meuponto.presentation.components.MeuPontoTopBar
import kotlinx.coroutines.flow.collectLatest
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Tela de histórico de versões de jornada.
 */
@Composable
fun VersoesJornadaScreen(
    onNavigateBack: () -> Unit,
    onNavigateToNovaVersao: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: VersoesJornadaViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.eventos.collectLatest { evento ->
            when (evento) {
                is VersoesJornadaEvent.NavegarParaNovaVersao -> {
                    onNavigateToNovaVersao(evento.empregoId)
                }
                is VersoesJornadaEvent.MostrarErro -> {
                    // Tratar erro (ex: snackbar)
                }
            }
        }
    }

    VersoesJornadaContent(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onNovaVersaoClick = viewModel::criarNovaVersao,
        modifier = modifier
    )
}

/**
 * Conteúdo da tela de histórico de versões de jornada, desacoplado do ViewModel.
 */
@Composable
fun VersoesJornadaContent(
    uiState: VersoesJornadaUiState,
    onNavigateBack: () -> Unit,
    onNovaVersaoClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            MeuPontoTopBar(
                title = "Histórico de Jornadas",
                subtitle = uiState.nomeEmprego, // Aqui nomeEmprego já é o apelido vindo do UI State
                showBackButton = true,
                onBackClick = onNavigateBack
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNovaVersaoClick,
                containerColor = MaterialTheme.colorScheme.secondary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nova Versão")
            }
        },
        modifier = modifier
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                CircularProgressIndicator()
            }
        } else {
            VersoesJornadaList(
                uiState = uiState,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

@Composable
private fun VersoesJornadaList(
    uiState: VersoesJornadaUiState,
    modifier: Modifier = Modifier
) {
    if (uiState.versoes.isEmpty()) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = modifier.fillMaxSize()
        ) {
            Text(
                text = "Nenhuma versão encontrada.",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = modifier.fillMaxSize()
        ) {
            items(uiState.versoes) { versao ->
                VersaoJornadaCard(versao = versao)
            }
        }
    }
}

@Composable
private fun VersaoJornadaCard(
    versao: VersaoJornada,
    modifier: Modifier = Modifier
) {
    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val dataFimStr = versao.dataFim?.format(dateFormatter) ?: "Atual"

    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${versao.dataInicio.format(dateFormatter)} - $dataFimStr",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                if (versao.dataFim == null) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        )
                    ) {
                        Text(
                            text = "VIGENTE",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                VersaoDestaqueInfo(
                    label = "Carga Diária",
                    value = formatarMinutos(versao.cargaHorariaDiariaMinutos),
                    icon = Icons.Default.Schedule,
                    modifier = Modifier.weight(1f)
                )
                VersaoDestaqueInfo(
                    label = "Turno Máx.",
                    value = formatarMinutos(versao.turnoMaximoMinutos),
                    icon = Icons.Default.History,
                    modifier = Modifier.weight(1f)
                )
            }

            if (versao.acrescimoMinutosDiasPontes > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "+ ${versao.acrescimoMinutosDiasPontes} min (Dias Ponte)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@Composable
private fun VersaoDestaqueInfo(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun formatarMinutos(minutos: Int): String {
    val h = minutos / 60
    val m = minutos % 60
    return String.format(Locale.getDefault(), "%02d:%02d", h, m)
}
