// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/settings/feriados/lista/FeriadosListScreen.kt
package br.com.tlmacedo.meuponto.presentation.screen.settings.feriados.lista

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import br.com.tlmacedo.meuponto.presentation.screen.settings.feriados.components.FeriadoCard
import br.com.tlmacedo.meuponto.presentation.screen.settings.feriados.components.FeriadoFilterChips
import br.com.tlmacedo.meuponto.presentation.screen.settings.feriados.components.ImportarFeriadosDialog

/**
 * Tela de listagem de feriados.
 *
 * @author Thiago
 * @since 3.0.0
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeriadosListScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEditar: (Long) -> Unit,
    onNavigateToNovo: () -> Unit,
    viewModel: FeriadosListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    var searchActive by remember { mutableStateOf(false) }

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

    // Dialog de importação
    if (uiState.showImportDialog || uiState.importacaoEmAndamento) {
        ImportarFeriadosDialog(
            isLoading = uiState.importacaoEmAndamento,
            onConfirmar = { viewModel.onEvent(FeriadosListEvent.OnImportarFeriados) },
            onDismiss = { viewModel.onEvent(FeriadosListEvent.OnDismissImportDialog) }
        )
    }

    // Dialog de exclusão
    if (uiState.showDeleteDialog && uiState.feriadoParaExcluir != null) {
        AlertDialog(
            onDismissRequest = {
                viewModel.onEvent(FeriadosListEvent.OnDismissDeleteDialog)
            },
            title = { Text("Excluir Feriado") },
            text = {
                Text("Deseja realmente excluir \"${uiState.feriadoParaExcluir?.nome}\"?")
            },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.onEvent(FeriadosListEvent.OnConfirmarExclusao) }
                ) {
                    Text("Excluir", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.onEvent(FeriadosListEvent.OnDismissDeleteDialog) }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text("Feriados") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar"
                        )
                    }
                },
                actions = {
                    // Botão de busca
                    IconButton(onClick = { searchActive = true }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Buscar"
                        )
                    }
                    // Botão de importar
                    IconButton(
                        onClick = { viewModel.onEvent(FeriadosListEvent.OnShowImportDialog) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudDownload,
                            contentDescription = "Importar feriados"
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToNovo,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Novo Feriado") }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // SearchBar
            AnimatedVisibility(
                visible = searchActive,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                SearchBar(
                    inputField = {
                        SearchBarDefaults.InputField(
                            query = uiState.searchQuery,
                            onQueryChange = {
                                viewModel.onEvent(FeriadosListEvent.OnSearchQueryChange(it))
                            },
                            onSearch = { searchActive = false },
                            expanded = false,
                            onExpandedChange = { },
                            placeholder = { Text("Buscar feriados...") },
                            trailingIcon = {
                                if (uiState.searchQuery.isNotBlank()) {
                                    IconButton(
                                        onClick = {
                                            viewModel.onEvent(
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

            // Filtros
            FeriadoFilterChips(
                tipoSelecionado = uiState.filtroTipo,
                anoSelecionado = uiState.filtroAno,
                anosDisponiveis = uiState.anosDisponiveis,
                onTipoChange = { viewModel.onEvent(FeriadosListEvent.OnFiltroTipoChange(it)) },
                onAnoChange = { viewModel.onEvent(FeriadosListEvent.OnFiltroAnoChange(it)) },
                onLimparFiltros = { viewModel.onEvent(FeriadosListEvent.OnLimparFiltros) }
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
                uiState.feriadosFiltrados.isEmpty() -> {
                    EmptyState(
                        temFiltros = uiState.temFiltrosAtivos,
                        onImportar = {
                            viewModel.onEvent(FeriadosListEvent.OnShowImportDialog)
                        },
                        onLimparFiltros = {
                            viewModel.onEvent(FeriadosListEvent.OnLimparFiltros)
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

                        items(
                            items = uiState.feriadosFiltrados,
                            key = { it.id }
                        ) { feriado ->
                            FeriadoCard(
                                feriado = feriado,
                                onEditar = { onNavigateToEditar(feriado.id) },
                                onExcluir = {
                                    viewModel.onEvent(FeriadosListEvent.OnShowDeleteDialog(feriado))
                                },
                                onToggleAtivo = {
                                    viewModel.onEvent(FeriadosListEvent.OnToggleAtivo(feriado))
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
