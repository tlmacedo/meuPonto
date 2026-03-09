package br.com.tlmacedo.meuponto.presentation.screen.settings.backup

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
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.tlmacedo.meuponto.presentation.components.MeuPontoTopBar
import kotlinx.coroutines.flow.collectLatest

/**
 * Tela de gerenciamento de backup e dados.
 *
 * Permite ao usuário:
 * - Exportar backup completo (JSON)
 * - Importar backup
 * - Limpar dados antigos
 * - Visualizar estatísticas do banco
 *
 * @author Thiago
 * @since 9.0.0
 */
@Composable
fun BackupScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BackupViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Dialogs
    var showConfirmLimpeza by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.eventos.collectLatest { evento ->
            when (evento) {
                is BackupEvent.MostrarMensagem -> {
                    snackbarHostState.showSnackbar(evento.mensagem)
                }
                is BackupEvent.ExportacaoConcluida -> {
                    snackbarHostState.showSnackbar("Backup exportado com sucesso!")
                }
                is BackupEvent.ImportacaoConcluida -> {
                    snackbarHostState.showSnackbar("Backup importado com sucesso!")
                }
                is BackupEvent.LimpezaConcluida -> {
                    snackbarHostState.showSnackbar("${evento.registrosRemovidos} registros removidos")
                }
            }
        }
    }

    // Dialog de confirmação de limpeza
    if (showConfirmLimpeza) {
        AlertDialog(
            onDismissRequest = { showConfirmLimpeza = false },
            icon = {
                Icon(
                    imageVector = Icons.Outlined.DeleteSweep,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text("Limpar Dados Antigos") },
            text = {
                Text(
                    "Esta ação removerá registros de ponto com mais de 12 meses.\n\n" +
                            "Recomendamos fazer um backup antes de prosseguir.\n\n" +
                            "Esta ação NÃO pode ser desfeita."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showConfirmLimpeza = false
                        viewModel.onAction(BackupAction.LimparDadosAntigos)
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Limpar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmLimpeza = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            MeuPontoTopBar(
                title = "Backup e Dados",
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
            // ESTATÍSTICAS DO BANCO
            // ══════════════════════════════════════════════════════════════
            item {
                SectionHeader(
                    title = "Estatísticas",
                    icon = Icons.Outlined.Storage
                )
            }

            item {
                EstatisticasCard(
                    totalEmpregos = uiState.totalEmpregos,
                    totalPontos = uiState.totalPontos,
                    totalFeriados = uiState.totalFeriados,
                    tamanhoEstimado = uiState.tamanhoEstimado,
                    isLoading = uiState.isLoading
                )
            }

            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }

            // ══════════════════════════════════════════════════════════════
            // EXPORTAR
            // ══════════════════════════════════════════════════════════════
            item {
                SectionHeader(
                    title = "Exportar Backup",
                    icon = Icons.Outlined.CloudUpload
                )
            }

            item {
                Text(
                    text = "Salve uma cópia de segurança de todos os seus dados em um arquivo JSON.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            item {
                Button(
                    onClick = { viewModel.onAction(BackupAction.ExportarBackup) },
                    enabled = !uiState.isProcessando,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (uiState.isProcessando && uiState.operacaoAtual == "exportar") {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Icon(
                        imageVector = Icons.Outlined.CloudUpload,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Exportar Backup Completo")
                }
            }

            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }

            // ══════════════════════════════════════════════════════════════
            // IMPORTAR
            // ══════════════════════════════════════════════════════════════
            item {
                SectionHeader(
                    title = "Importar Backup",
                    icon = Icons.Outlined.CloudDownload
                )
            }

            item {
                Text(
                    text = "Restaure seus dados a partir de um arquivo de backup.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            item {
                OutlinedButton(
                    onClick = { viewModel.onAction(BackupAction.ImportarBackup) },
                    enabled = !uiState.isProcessando,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (uiState.isProcessando && uiState.operacaoAtual == "importar") {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Icon(
                        imageVector = Icons.Outlined.CloudDownload,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Selecionar Arquivo de Backup")
                }
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "A importação substitui os dados existentes. Faça um backup antes de importar.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }

            // ══════════════════════════════════════════════════════════════
            // MANUTENÇÃO
            // ══════════════════════════════════════════════════════════════
            item {
                SectionHeader(
                    title = "Manutenção",
                    icon = Icons.Outlined.DeleteSweep
                )
            }

            item {
                Text(
                    text = "Libere espaço removendo registros antigos que não são mais necessários.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            item {
                OutlinedCard(
                    onClick = { showConfirmLimpeza = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.DeleteSweep,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Limpar Dados Antigos",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = "Remover registros com mais de 12 meses",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
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
private fun EstatisticasCard(
    totalEmpregos: Int,
    totalPontos: Int,
    totalFeriados: Int,
    tamanhoEstimado: String,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        if (isLoading) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp)
            ) {
                CircularProgressIndicator(modifier = Modifier.size(32.dp))
            }
        } else {
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                EstatisticaItem(
                    valor = totalEmpregos.toString(),
                    label = "Empregos"
                )
                EstatisticaItem(
                    valor = totalPontos.toString(),
                    label = "Registros"
                )
                EstatisticaItem(
                    valor = totalFeriados.toString(),
                    label = "Feriados"
                )
                EstatisticaItem(
                    valor = tamanhoEstimado,
                    label = "Tamanho"
                )
            }
        }
    }
}

@Composable
private fun EstatisticaItem(
    valor: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text = valor,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
