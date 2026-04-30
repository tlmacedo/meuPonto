// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/ausencias/list/AusenciasScreen.kt
package br.com.tlmacedo.meuponto.presentation.screen.ausencias.list

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ViewModule
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.tlmacedo.meuponto.R
import br.com.tlmacedo.meuponto.presentation.components.CalendarLegend
import br.com.tlmacedo.meuponto.presentation.components.CalendarView
import br.com.tlmacedo.meuponto.presentation.components.MeuPontoTopBar
import br.com.tlmacedo.meuponto.presentation.components.theme.ThemedBackground
import br.com.tlmacedo.meuponto.presentation.screen.ausencias.components.AusenciaCard
import br.com.tlmacedo.meuponto.presentation.screen.ausencias.components.AusenciaFilterChips
import kotlinx.coroutines.flow.collectLatest
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Tela de listagem de ausências.
 *
 * @author Thiago
 * @since 4.0.0
 * @updated 5.6.0 - Filtros múltiplos e lista unificada
 * @updated 12.2.0 - Adicionada visualização de calendário anual
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AusenciasScreen(
    onVoltar: () -> Unit,
    onNovaAusencia: () -> Unit,
    onNovaAusenciaComData: (String) -> Unit,
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
                    snackbarHostState.showSnackbar(
                        event.mensagem,
                        duration = SnackbarDuration.Short
                    )
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
            title = { Text(stringResource(R.string.ausencia_excluir)) },
            text = {
                Text(
                    stringResource(
                        R.string.ausencia_confirmar_exclusao_pergunta,
                        uiState.ausenciaParaExcluir!!.tipoDescricao,
                        uiState.ausenciaParaExcluir!!.formatarPeriodo()
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.onAction(AusenciasAction.ConfirmarExclusao) }
                ) {
                    Text(
                        stringResource(R.string.btn_excluir),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.onAction(AusenciasAction.CancelarExclusao) }
                ) {
                    Text(stringResource(R.string.btn_cancelar))
                }
            }
        )
    }

    ThemedBackground(
        modifier = Modifier.fillMaxSize()
    ) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onBackground,
            topBar = {
                MeuPontoTopBar(
                    title = stringResource(R.string.ausencias_titulo),
                    subtitle = uiState.empregoAtivo?.apelido?.uppercase(),
                    logo = uiState.empregoAtivo?.logo,
                    showBackButton = true,
                    onBackClick = { viewModel.onAction(AusenciasAction.Voltar) },
                    actions = {
                        IconButton(onClick = { viewModel.onAction(AusenciasAction.ToggleVisualizacao) }) {
                            Icon(
                                imageVector = if (uiState.visualizacaoCalendario) Icons.AutoMirrored.Filled.List else Icons.Default.ViewModule,
                                contentDescription = if (uiState.visualizacaoCalendario) "Ver Lista" else "Ver Calendário"
                            )
                        }
                    },
                    scrollBehavior = scrollBehavior
                )
            },
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = { viewModel.onAction(AusenciasAction.NovaAusencia) },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text(stringResource(R.string.ausencia_adicionar)) }
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                if (!uiState.visualizacaoCalendario) {
                    // Filtros (Apenas na Lista)
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
                } else {
                    // Seletor de Ano no Calendário
                    YearNavigator(
                        ano = uiState.filtroAno ?: LocalDate.now().year,
                        onAnoAnterior = {
                            viewModel.onAction(
                                AusenciasAction.FiltroAnoChange(
                                    (uiState.filtroAno ?: LocalDate.now().year) - 1
                                )
                            )
                        },
                        onProximoAno = {
                            viewModel.onAction(
                                AusenciasAction.FiltroAnoChange(
                                    (uiState.filtroAno ?: LocalDate.now().year) + 1
                                )
                            )
                        }
                    )
                }

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
                            titulo = stringResource(R.string.ausencia_sem_emprego_titulo),
                            mensagem = stringResource(R.string.ausencia_sem_emprego_desc)
                        )
                    }

                    uiState.visualizacaoCalendario -> {
                        val listState = rememberLazyListState()
                        val ano = uiState.filtroAno ?: LocalDate.now().year
                        val months = remember(ano) { (1..12).map { YearMonth.of(ano, it) } }

                        // Scroll para o mês atual
                        LaunchedEffect(ano) {
                            val currentYearMonth = YearMonth.now()
                            if (currentYearMonth.year == ano) {
                                listState.scrollToItem(currentYearMonth.monthValue - 1)
                            } else {
                                listState.scrollToItem(0)
                            }
                        }

                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 80.dp)
                        ) {
                            itemsIndexed(months) { _, month ->
                                val locale = Locale.forLanguageTag("pt-BR")
                                val formatter =
                                    DateTimeFormatter.ofPattern("MMMM 'de' yyyy", locale)
                                val title =
                                    month.atDay(1).format(formatter)
                                        .replaceFirstChar { it.uppercase() }

                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(
                                        horizontal = 16.dp,
                                        vertical = 12.dp
                                    )
                                )

                                CalendarView(
                                    yearMonth = month,
                                    diasHistorico = uiState.diasHistorico,
                                    highlightOnlySpecials = true,
                                    showLegend = false,
                                    onDateClick = { data -> onNovaAusenciaComData(data.toString()) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp)
                                )

                                Spacer(Modifier.height(24.dp))
                            }

                            item {
                                Spacer(Modifier.height(16.dp))
                                CalendarLegend()
                            }
                        }
                    }

                    uiState.ausenciasFiltradas.isEmpty() -> {
                        EmptyState(
                            emoji = "📅",
                            titulo = if (uiState.temFiltrosAtivos) {
                                stringResource(R.string.ausencia_vazia_filtros_titulo)
                            } else {
                                stringResource(R.string.ausencia_vazia_titulo)
                            },
                            mensagem = if (uiState.temFiltrosAtivos) {
                                stringResource(R.string.ausencia_vazia_filtros_desc)
                            } else {
                                stringResource(R.string.ausencia_vazia_desc)
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
                                        text = stringResource(
                                            R.string.ausencia_total_resumo,
                                            uiState.totalAusenciasFiltradas,
                                            uiState.totalDiasAusencia
                                        ),
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
                                        viewModel.onAction(
                                            AusenciasAction.SolicitarExclusao(
                                                ausencia
                                            )
                                        )
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
}

@Composable
private fun YearNavigator(
    ano: Int,
    onAnoAnterior: () -> Unit,
    onProximoAno: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp)
        ) {
            IconButton(
                onClick = onAnoAnterior,
                modifier = Modifier
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        RoundedCornerShape(8.dp)
                    )
                    .size(40.dp)
            ) {
                Icon(Icons.Default.ChevronLeft, "Anterior")
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Icon(
                    Icons.Default.CalendarMonth,
                    null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = ano.toString(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }

            IconButton(
                onClick = onProximoAno,
                modifier = Modifier
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        RoundedCornerShape(8.dp)
                    )
                    .size(40.dp)
            ) {
                Icon(
                    Icons.Default.ChevronRight,
                    "Próximo",
                    tint = MaterialTheme.colorScheme.onSurface
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
                    Text(stringResource(R.string.ausencia_filtros_limpar))
                }
            }
        }
    }
}
