package br.com.tlmacedo.meuponto.presentation.screen.settings.empregos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
 * Tela de detalhes e configurações de um emprego específico.
 *
 * Exibe informações do emprego e permite navegação para sub-configurações
 * como versões de jornada, ausências e ajustes de saldo.
 *
 * @param onNavigateBack Callback para voltar à tela anterior
 * @param onNavigateToVersoes Callback para navegar às versões de jornada
 * @param onNavigateToAusencias Callback para navegar às ausências (opcional)
 * @param onNavigateToAjustesSaldo Callback para navegar aos ajustes de saldo (opcional)
 * @param modifier Modificador opcional para customização do layout
 * @param viewModel ViewModel injetado via Hilt
 *
 * @author Thiago
 * @since 4.0.0
 */
@Composable
fun EmpregoSettingsDetailScreen(
    onNavigateBack: () -> Unit,
    onNavigateToVersoes: (Long) -> Unit,
    onNavigateToAusencias: ((Long) -> Unit)? = null,
    onNavigateToAjustesSaldo: ((Long) -> Unit)? = null,
    modifier: Modifier = Modifier,
    viewModel: EmpregoSettingsDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.eventos.collectLatest { evento ->
            when (evento) {
                is EmpregoSettingsDetailEvent.NavegarParaVersoes -> {
                    onNavigateToVersoes(evento.empregoId)
                }
                is EmpregoSettingsDetailEvent.NavegarParaAusencias -> {
                    onNavigateToAusencias?.invoke(evento.empregoId)
                }
                is EmpregoSettingsDetailEvent.NavegarParaAjustesSaldo -> {
                    onNavigateToAjustesSaldo?.invoke(evento.empregoId)
                }
                is EmpregoSettingsDetailEvent.MostrarMensagem -> {
                    snackbarHostState.showSnackbar(evento.mensagem)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            MeuPontoTopBar(
                title = uiState.nomeEmprego,
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

            uiState.errorMessage != null -> {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = uiState.errorMessage ?: "Erro desconhecido",
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.onAction(EmpregoSettingsDetailAction.Recarregar) }
                        ) {
                            Text("Tentar novamente")
                        }
                    }
                }
            }

            else -> {
                EmpregoSettingsDetailContent(
                    uiState = uiState,
                    onNavigateToVersoes = {
                        viewModel.onAction(EmpregoSettingsDetailAction.NavegarParaVersoes)
                    },
                    onNavigateToAusencias = {
                        viewModel.onAction(EmpregoSettingsDetailAction.NavegarParaAusencias)
                    },
                    onNavigateToAjustesSaldo = {
                        viewModel.onAction(EmpregoSettingsDetailAction.NavegarParaAjustesSaldo)
                    },
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

@Composable
private fun EmpregoSettingsDetailContent(
    uiState: EmpregoSettingsDetailUiState,
    onNavigateToVersoes: () -> Unit,
    onNavigateToAusencias: () -> Unit,
    onNavigateToAjustesSaldo: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header do emprego
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(20.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Business,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = uiState.nomeEmprego,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    if (uiState.empregoAtivo) {
                        Text(
                            text = "✓ Emprego ativo",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }

        // Seção: Jornada de Trabalho
        Text(
            text = "Jornada de Trabalho",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        SettingsMenuItem(
            icon = Icons.Default.Schedule,
            title = "Versões de Jornada",
            subtitle = buildString {
                append("${uiState.totalVersoes} versão(ões)")
                uiState.versaoVigenteDescricao?.let {
                    append(" • Vigente: $it")
                }
            },
            onClick = onNavigateToVersoes
        )

        HorizontalDivider()

        // Seção: Registros
        Text(
            text = "Registros",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        SettingsMenuItem(
            icon = Icons.AutoMirrored.Filled.EventNote,
            title = "Ausências",
            subtitle = "Férias, licenças e afastamentos",
            onClick = onNavigateToAusencias
        )

        SettingsMenuItem(
            icon = Icons.Default.AccountBalance,
            title = "Ajustes de Saldo",
            subtitle = "Ajustes manuais no banco de horas",
            onClick = onNavigateToAjustesSaldo
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun SettingsMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
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
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
