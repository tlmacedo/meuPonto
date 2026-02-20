// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/ausencias/AusenciasScreen.kt
package br.com.tlmacedo.meuponto.presentation.screen.ausencias

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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.tlmacedo.meuponto.presentation.screen.ausencias.components.AusenciaCard
import kotlinx.coroutines.flow.collectLatest

/**
 * Tela de listagem de ausências.
 *
 * @author Thiago
 * @since 4.0.0
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AusenciasScreen(
    onVoltar: () -> Unit,
    onNovaAusencia: () -> Unit,
    onEditarAusencia: (Long) -> Unit,
    viewModel: AusenciasViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Eventos
    LaunchedEffect(Unit) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is AusenciasUiEvent.Voltar -> onVoltar()
                is AusenciasUiEvent.NavegarParaNovaAusencia -> onNovaAusencia()
                is AusenciasUiEvent.NavegarParaEditarAusencia -> onEditarAusencia(event.ausenciaId)
                is AusenciasUiEvent.MostrarMensagem -> snackbarHostState.showSnackbar(event.mensagem)
                is AusenciasUiEvent.MostrarErro -> snackbarHostState.showSnackbar(event.mensagem)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ausências") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.onAction(AusenciasAction.Voltar) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.onAction(AusenciasAction.NovaAusencia) },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Nova ausência",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Navegador de mês
            MonthNavigator(
                mesFormatado = uiState.mesFormatado,
                podeAnterior = uiState.podeNavegaMesAnterior,
                podeProximo = uiState.podeNavegarMesProximo,
                onAnterior = { viewModel.onAction(AusenciasAction.MesAnterior) },
                onProximo = { viewModel.onAction(AusenciasAction.ProximoMes) }
            )

            // Conteúdo principal
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                !uiState.temEmpregoAtivo -> {
                    EmptyState(
                        emoji = "🏢",
                        titulo = "Nenhum emprego ativo",
                        mensagem = "Configure um emprego para registrar ausências"
                    )
                }

                !uiState.temAusencias -> {
                    EmptyState(
                        emoji = "📅",
                        titulo = "Nenhuma ausência",
                        mensagem = "Toque no + para registrar férias, folgas ou faltas"
                    )
                }

                else -> {
                    // Resumo
                    if (uiState.totalDiasAusencia > 0) {
                        ResumoAusencias(
                            totalDias = uiState.totalDiasAusencia,
                            totalPorTipo = uiState.totalDiasPorTipo,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }

                    // Lista de ausências
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = uiState.ausenciasFiltradas,
                            key = { it.id }
                        ) { ausencia ->
                            AusenciaCard(
                                ausencia = ausencia,
                                onEditar = {
                                    viewModel.onAction(AusenciasAction.EditarAusencia(ausencia))
                                },
                                onExcluir = {
                                    viewModel.onAction(AusenciasAction.SolicitarExclusao(ausencia))
                                }
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(80.dp)) // Espaço para FAB
                        }
                    }
                }
            }
        }
    }

    // Dialog de confirmação de exclusão
    if (uiState.showDeleteDialog && uiState.ausenciaParaExcluir != null) {
        AlertDialog(
            onDismissRequest = { viewModel.onAction(AusenciasAction.CancelarExclusao) },
            title = { Text("Excluir ausência?") },
            text = {
                Text(
                    "Deseja excluir ${uiState.ausenciaParaExcluir!!.tipo.descricao} " +
                            "de ${uiState.ausenciaParaExcluir!!.formatarPeriodo()}?"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.onAction(AusenciasAction.ConfirmarExclusao) }
                ) {
                    Text("Excluir", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.onAction(AusenciasAction.CancelarExclusao) }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun MonthNavigator(
    mesFormatado: String,
    podeAnterior: Boolean,
    podeProximo: Boolean,
    onAnterior: () -> Unit,
    onProximo: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onAnterior,
            enabled = podeAnterior
        ) {
            Icon(
                imageVector = Icons.Default.ChevronLeft,
                contentDescription = "Mês anterior"
            )
        }

        Text(
            text = mesFormatado,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        IconButton(
            onClick = onProximo,
            enabled = podeProximo
        ) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Próximo mês"
            )
        }
    }
}

@Composable
private fun ResumoAusencias(
    totalDias: Int,
    totalPorTipo: Map<br.com.tlmacedo.meuponto.domain.model.ausencia.TipoAusencia, Int>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Total: $totalDias ${if (totalDias == 1) "dia" else "dias"} de ausência",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary
        )

        Row(
            modifier = Modifier.padding(top = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            totalPorTipo.forEach { (tipo, dias) ->
                Text(
                    text = "${tipo.emoji} $dias",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun EmptyState(
    emoji: String,
    titulo: String,
    mensagem: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = emoji,
                style = MaterialTheme.typography.displayLarge
            )
            Text(
                text = titulo,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = mensagem,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
