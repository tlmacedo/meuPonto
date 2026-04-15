// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/ausencias/AusenciasScreen.kt
package br.com.tlmacedo.meuponto.presentation.screen.ausencias

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
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
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.tlmacedo.meuponto.presentation.components.MeuPontoTopBar
import br.com.tlmacedo.meuponto.presentation.screen.ausencias.components.AusenciaCard
import br.com.tlmacedo.meuponto.presentation.screen.ausencias.components.AusenciaFilterChips
import kotlinx.coroutines.flow.collectLatest

/**
 * Tela de listagem de ausências.
 *
 * @author Thiago
 * @since 4.0.0
 * @updated 5.6.0 - Filtros múltiplos e lista unificada
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
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    // Eventos
    LaunchedEffect(Unit) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is AusenciasUiEvent.Voltar -> onVoltar()
                is AusenciasUiEvent.NavegarParaNovaAusencia -> onNovaAusencia()
                is AusenciasUiEvent.NavegarParaEditarAusencia -> onEditarAusencia(event.ausenciaId)
                is AusenciasUiEvent.MostrarMensagem -> {
                    snackbarHostState.showSnackbar(event.mensagem, duration = SnackbarDuration.Short)
                }
                is AusenciasUiEvent.MostrarErro -> {
                    snackbarHostState.showSnackbar(event.mensagem, duration = SnackbarDuration.Long)
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
                    "Deseja excluir ${uiState.ausenciaParaExcluir!!.tipoDescricao} " +
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

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            MeuPontoTopBar(
                title = "Ausências",
                subtitle = uiState.empregoAtivo?.apelido?.uppercase(),
                logo = uiState.empregoAtivo?.logo,
                showBackButton = true,
                onBackClick = { viewModel.onAction(AusenciasAction.Voltar) },
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.onAction(AusenciasAction.NovaAusencia) },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Nova Ausência") }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Filtros
            AusenciaFilterChips(
                tiposSelecionados = uiState.filtroTipos,
                anoSelecionado = uiState.filtroAno,
                anosDisponiveis = uiState.anosDisponiveis,
                ordemData = uiState.ordemData,
                onToggleTipo = { viewModel.onAction(AusenciasAction.ToggleTipo(it)) },
                onAnoChange = { viewModel.onAction(AusenciasAction.FiltroAnoChange(it)) },
                onToggleOrdem = { viewModel.onAction(AusenciasAction.ToggleOrdem) },
                onLimparFiltros = { viewModel.onAction(AusenciasAction.LimparFiltros) }
            )

            // Conteúdo
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

                uiState.ausenciasFiltradas.isEmpty() -> {
                    EmptyState(
                        emoji = "📅",
                        titulo = if (uiState.temFiltrosAtivos) {
                            "Nenhuma ausência encontrada"
                        } else {
                            "Nenhuma ausência cadastrada"
                        },
                        mensagem = if (uiState.temFiltrosAtivos) {
                            "Tente ajustar os filtros selecionados"
                        } else {
                            "Toque no botão para registrar férias, folgas ou faltas"
                        },
                        showLimparFiltros = uiState.temFiltrosAtivos,
                        onLimparFiltros = { viewModel.onAction(AusenciasAction.LimparFiltros) }
                    )
                }

                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = 8.dp,
                            bottom = 88.dp // Espaço para o FAB
                        ),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Header com contagem e resumo
                        item {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(bottom = 8.dp)
                            ) {
                                Text(
                                    text = "${uiState.totalAusenciasFiltradas} ausência(s) • ${uiState.totalDiasAusencia} dia(s)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                // Resumo por tipo
                                if (uiState.totalDiasPorTipo.isNotEmpty()) {
                                    Text(
                                        text = uiState.totalDiasPorTipo.entries.joinToString(" • ") { (tipo, dias) ->
                                            "${tipo.emoji} $dias"
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

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
                                },
                                onToggleAtivo = {
                                    viewModel.onAction(AusenciasAction.ToggleAtivo(ausencia))
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyState(
    emoji: String,
    titulo: String,
    mensagem: String,
    showLimparFiltros: Boolean = false,
    onLimparFiltros: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = emoji,
                style = MaterialTheme.typography.displayLarge
            )
            Text(
                text = titulo,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
            Text(
                text = mensagem,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            if (showLimparFiltros) {
                TextButton(onClick = onLimparFiltros) {
                    Text("Limpar filtros")
                }
            }
        }
    }
}
