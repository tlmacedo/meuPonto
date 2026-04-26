// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/settings/feriados/lista/FeriadosListScreen.kt
package br.com.tlmacedo.meuponto.presentation.screen.settings.feriados.lista

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ViewModule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import br.com.tlmacedo.meuponto.presentation.components.CalendarLegend
import br.com.tlmacedo.meuponto.presentation.components.CalendarView
import br.com.tlmacedo.meuponto.presentation.components.MeuPontoTopBar
import br.com.tlmacedo.meuponto.presentation.screen.settings.feriados.components.FeriadoCard
import br.com.tlmacedo.meuponto.presentation.screen.settings.feriados.components.FeriadoFilterChips
import br.com.tlmacedo.meuponto.presentation.screen.settings.feriados.components.ImportarFeriadosDialog
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Tela de listagem de feriados.
 *
 * @author Thiago
 * @since 3.0.0
 * @updated 12.2.0 - Adicionada visualização de calendário anual
 */
@Composable
fun FeriadosListScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEditar: (Long) -> Unit,
    onNavigateToNovo: () -> Unit,
    onNavigateToNovoComData: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: FeriadosListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Mostrar mensagens
    LaunchedEffect(uiState.mensagemSucesso, uiState.mensagemErro) {
        uiState.mensagemSucesso?.let {
            snackbarHostState.showSnackbar(it, duration = SnackbarDuration.Short)
            viewModel.onEvent(FeriadosListEvent.OnDismissMessage)
        }
        uiState.mensagemErro?.let {
            snackbarHostState.showSnackbar(it, duration = SnackbarDuration.Long)
            viewModel.onEvent(FeriadosListEvent.OnDismissMessage)
        }
    }

    FeriadosListContent(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onNavigateToEditar = onNavigateToEditar,
        onNavigateToNovo = onNavigateToNovo,
        onNavigateToNovoComData = onNavigateToNovoComData,
        onEvent = viewModel::onEvent,
        modifier = modifier,
        snackbarHostState = snackbarHostState
    )
}

/**
 * Conteúdo da tela de listagem de feriados, desacoplado do ViewModel.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FeriadosListContent(
    uiState: FeriadosListUiState,
    onNavigateBack: () -> Unit,
    onNavigateToEditar: (Long) -> Unit,
    onNavigateToNovo: () -> Unit,
    onNavigateToNovoComData: (String) -> Unit,
    onEvent: (FeriadosListEvent) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    var searchActive by remember { mutableStateOf(false) }

    // Dialog de importação
    if (uiState.showImportDialog || uiState.importacaoEmAndamento) {
        ImportarFeriadosDialog(
            isLoading = uiState.importacaoEmAndamento,
            onConfirmar = { onEvent(FeriadosListEvent.OnImportarFeriados) },
            onDismiss = { onEvent(FeriadosListEvent.OnDismissImportDialog) }
        )
    }

    // Dialog de exclusão
    if (uiState.showDeleteDialog && uiState.feriadoParaExcluir != null) {
        AlertDialog(
            onDismissRequest = {
                onEvent(FeriadosListEvent.OnDismissDeleteDialog)
            },
            title = { Text("Excluir Feriado") },
            text = {
                Text("Deseja realmente excluir \"${uiState.feriadoParaExcluir?.nome}\"?")
            },
            confirmButton = {
                TextButton(
                    onClick = { onEvent(FeriadosListEvent.OnConfirmarExclusao) }
                ) {
                    Text("Excluir", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { onEvent(FeriadosListEvent.OnDismissDeleteDialog) }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            MeuPontoTopBar(
                title = "Feriados",
                subtitle = uiState.empregoApelido?.uppercase(),
                logo = uiState.empregoLogo,
                showBackButton = true,
                onBackClick = onNavigateBack,
                actions = {
                    IconButton(onClick = { onEvent(FeriadosListEvent.OnToggleVisualizacao) }) {
                        Icon(
                            imageVector = if (uiState.visualizacaoCalendario) Icons.AutoMirrored.Filled.List else Icons.Default.ViewModule,
                            contentDescription = if (uiState.visualizacaoCalendario) "Ver Lista" else "Ver Calendário"
                        )
                    }
                    // Botão de busca
                    IconButton(onClick = { searchActive = !searchActive }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Buscar"
                        )
                    }
                    // Botão de importar
                    IconButton(
                        onClick = { onEvent(FeriadosListEvent.OnShowImportDialog) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudDownload,
                            contentDescription = "Importar feriados"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToNovo,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Novo Feriado") }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.visualizacaoCalendario) {
                YearNavigator(
                    ano = uiState.filtroAno ?: java.time.LocalDate.now().year,
                    onAnoAnterior = { onEvent(FeriadosListEvent.OnFiltroAnoChange((uiState.filtroAno ?: java.time.LocalDate.now().year) - 1)) },
                    onProximoAno = { onEvent(FeriadosListEvent.OnFiltroAnoChange((uiState.filtroAno ?: java.time.LocalDate.now().year) + 1)) }
                )
            } else {
                // SearchBar
                AnimatedVisibility(
                    visible = searchActive,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        SearchBar(
                            inputField = {
                                SearchBarDefaults.InputField(
                                    query = uiState.searchQuery,
                                    onQueryChange = {
                                        onEvent(FeriadosListEvent.OnSearchQueryChange(it))
                                    },
                                    onSearch = { searchActive = false },
                                    expanded = false,
                                    onExpandedChange = { },
                                    placeholder = { Text("Buscar feriados...") },
                                    trailingIcon = {
                                        if (uiState.searchQuery.isNotBlank()) {
                                            IconButton(
                                                onClick = {
                                                    onEvent(
                                                        FeriadosListEvent.OnSearchQueryChange("")
                                                    )
                                                }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Search,
                                                    contentDescription = "Limpar"
                                                )
                                            }
                                        }
                                    }
                                )
                            },
                            expanded = false,
                            onExpandedChange = { searchActive = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) { }
                    }
                }

                // Filtros
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    FeriadoFilterChips(
                        tiposSelecionados = uiState.filtroTipos,
                        anoSelecionado = uiState.filtroAno,
                        anosDisponiveis = uiState.anosDisponiveis,
                        ordemData = uiState.ordemData,
                        onToggleTipo = { onEvent(FeriadosListEvent.OnToggleTipo(it)) },
                        onAnoChange = { onEvent(FeriadosListEvent.OnFiltroAnoChange(it)) },
                        onToggleOrdem = { onEvent(FeriadosListEvent.OnToggleOrdem) },
                        onLimparFiltros = { onEvent(FeriadosListEvent.OnLimparFiltros) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 0.dp)
                    )
                }
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

                uiState.visualizacaoCalendario -> {
                    val listState = rememberLazyListState()
                    val ano = uiState.filtroAno ?: java.time.LocalDate.now().year
                    val months = remember(ano) { (1..12).map { YearMonth.of(ano, it) } }

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
                            val formatter = DateTimeFormatter.ofPattern("MMMM 'de' yyyy", locale)
                            val title = month.atDay(1).format(formatter).replaceFirstChar { it.uppercase() }

                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                            )

                            CalendarView(
                                yearMonth = month,
                                diasHistorico = uiState.diasHistorico,
                                highlightOnlySpecials = true,
                                showLegend = false,
                                onDateClick = { data -> onNavigateToNovoComData(data.toString()) },
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

                uiState.feriadosFiltrados.isEmpty() -> {
                    EmptyState(
                        temFiltros = uiState.temFiltrosAtivos,
                        onImportar = {
                            onEvent(FeriadosListEvent.OnShowImportDialog)
                        },
                        onLimparFiltros = {
                            onEvent(FeriadosListEvent.OnLimparFiltros)
                        }
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
                        // Header com contagem
                        item {
                            Text(
                                text = "${uiState.totalFeriadosFiltrados} feriado(s)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }

                        uiState.feriadosAgrupados.forEach { (tipo, feriados) ->
                            item {
                                Text(
                                    text = tipo.descricao,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                            items(
                                items = feriados,
                                key = { it.id }
                            ) { feriado ->
                                FeriadoCard(
                                    feriado = feriado,
                                    onEditar = { onNavigateToEditar(feriado.id) },
                                    onExcluir = {
                                        onEvent(FeriadosListEvent.OnShowDeleteDialog(feriado))
                                    },
                                    onToggleAtivo = {
                                        onEvent(FeriadosListEvent.OnToggleAtivo(feriado))
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
    temFiltros: Boolean,
    onImportar: () -> Unit,
    onLimparFiltros: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = "📅",
                style = MaterialTheme.typography.displayLarge
            )

            Text(
                text = if (temFiltros) {
                    "Nenhum feriado encontrado com os filtros selecionados"
                } else {
                    "Nenhum feriado cadastrado"
                },
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (temFiltros) {
                TextButton(onClick = onLimparFiltros) {
                    Text("Limpar filtros")
                }
            } else {
                TextButton(onClick = onImportar) {
                    Text("Importar feriados nacionais")
                }
            }
        }
    }
}
