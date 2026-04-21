package br.com.tlmacedo.meuponto.presentation.screen.settings.comprovantes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.*
import androidx.compose.ui.graphics.Color
import br.com.tlmacedo.meuponto.util.toDatePickerMillis
import br.com.tlmacedo.meuponto.util.toLocalDateFromDatePicker
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.tlmacedo.meuponto.domain.model.FotoComprovante
import br.com.tlmacedo.meuponto.presentation.components.LocalImage
import br.com.tlmacedo.meuponto.presentation.components.MeuPontoTopBar
import br.com.tlmacedo.meuponto.util.toDatePickerMillis
import br.com.tlmacedo.meuponto.util.toLocalDateFromDatePicker
import java.io.File
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComprovantesScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ComprovantesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showDatePicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            if (uiState.isSelectionMode) {
                TopAppBar(
                    title = { Text("${uiState.selectedIds.size} selecionados") },
                    navigationIcon = {
                        IconButton(onClick = { viewModel.onAction(ComprovantesAction.LimparSelecao) }) {
                            Icon(Icons.Default.Close, contentDescription = "Limpar seleção")
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.onAction(ComprovantesAction.ExcluirSelecionados) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Excluir selecionados")
                        }
                    }
                )
            } else {
                MeuPontoTopBar(
                    title = "Gerenciador de Comprovantes",
                    subtitle = "${uiState.totalCount} fotos • ${String.format("%.2f", uiState.totalSizeMb)} MB",
                    showBackButton = true,
                    onBackClick = onNavigateBack,
                    actions = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = "Filtrar Data")
                        }
                        
                        var showFiltroMenu by remember { mutableStateOf(false) }
                        Box {
                            IconButton(onClick = { showFiltroMenu = true }) {
                                Icon(Icons.Default.FilterList, contentDescription = "Filtrar Associação")
                            }
                            DropdownMenu(
                                expanded = showFiltroMenu,
                                onDismissRequest = { showFiltroMenu = false }
                            ) {
                                FiltroAssociacao.entries.forEach { filtro ->
                                    DropdownMenuItem(
                                        text = { Text(when(filtro) {
                                            FiltroAssociacao.TODOS -> "Todos"
                                            FiltroAssociacao.COM_PONTO -> "Com Ponto"
                                            FiltroAssociacao.SEM_PONTO -> "Sem Ponto"
                                        }) },
                                        onClick = {
                                            viewModel.onAction(ComprovantesAction.AlterarFiltroAssociacao(filtro))
                                            showFiltroMenu = false
                                        },
                                        leadingIcon = {
                                            RadioButton(
                                                selected = uiState.filtroAssociacao == filtro,
                                                onClick = null
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                )
            }
        },
        modifier = modifier
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (uiState.items.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Nenhum comprovante encontrado.")
                    Text(
                        text = "Período: ${uiState.dataInicio} a ${uiState.dataFim}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Button(onClick = { showDatePicker = true }, modifier = Modifier.padding(top = 16.dp)) {
                        Text("Alterar Período")
                    }
                }
            }
        } else {
            ComprovantesGrid(
                items = uiState.items,
                selectedIds = uiState.selectedIds,
                onItemClick = { foto ->
                    if (uiState.isSelectionMode) {
                        viewModel.onAction(ComprovantesAction.AlternarSelecao(foto.id))
                    } else {
                        viewModel.onAction(ComprovantesAction.SelecionarComprovante(foto))
                    }
                },
                onItemLongClick = { viewModel.onAction(ComprovantesAction.AlternarSelecao(it.id)) },
                modifier = Modifier.padding(paddingValues)
            )
        }

        if (showDatePicker) {
            DateRangePickerModal(
                initialStartDate = uiState.dataInicio,
                initialEndDate = uiState.dataFim,
                onDismiss = { showDatePicker = false },
                onDateSelected = { inicio, fim ->
                    viewModel.onAction(ComprovantesAction.AlterarPeriodo(inicio, fim))
                    showDatePicker = false
                }
            )
        }

        uiState.selectedItem?.let { selected ->
            ComprovanteDetailsDialog(
                foto = selected,
                onDismiss = { viewModel.onAction(ComprovantesAction.SelecionarComprovante(null)) },
                onDelete = {
                    viewModel.onAction(ComprovantesAction.ExcluirComprovante(selected.id))
                    viewModel.onAction(ComprovantesAction.SelecionarComprovante(null))
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangePickerModal(
    initialStartDate: LocalDate,
    initialEndDate: LocalDate,
    onDismiss: () -> Unit,
    onDateSelected: (LocalDate, LocalDate) -> Unit
) {
    val dateRangePickerState = rememberDateRangePickerState(
        initialSelectedStartDateMillis = initialStartDate.toDatePickerMillis(),
        initialSelectedEndDateMillis = initialEndDate.toDatePickerMillis()
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val start = dateRangePickerState.selectedStartDateMillis?.toLocalDateFromDatePicker()
                    val end = dateRangePickerState.selectedEndDateMillis?.toLocalDateFromDatePicker()
                    if (start != null && end != null) {
                        onDateSelected(start, end)
                    }
                },
                enabled = dateRangePickerState.selectedEndDateMillis != null
            ) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    ) {
        DateRangePicker(
            state = dateRangePickerState,
            title = { Text("Selecionar Período", modifier = Modifier.padding(16.dp)) },
            headline = {
                val start = dateRangePickerState.selectedStartDateMillis?.toLocalDateFromDatePicker()
                val end = dateRangePickerState.selectedEndDateMillis?.toLocalDateFromDatePicker()
                Text(
                    text = if (start != null && end != null) "${start} - ${end}" else "Escolha as datas",
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            },
            showModeToggle = false,
            modifier = Modifier.fillMaxWidth().height(400.dp)
        )
    }
}

@Composable
private fun ComprovantesGrid(
    items: List<FotoComprovante>,
    selectedIds: Set<Long>,
    onItemClick: (FotoComprovante) -> Unit,
    onItemLongClick: (FotoComprovante) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(110.dp),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        items(items, key = { it.id }) { foto ->
            ComprovanteGridItem(
                foto = foto,
                isSelected = selectedIds.contains(foto.id),
                onClick = { onItemClick(foto) },
                onLongClick = { onItemLongClick(foto) }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ComprovanteGridItem(
    foto: FotoComprovante,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 8.dp else 2.dp),
        border = if (isSelected) CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary), width = 3.dp) else null
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            LocalImage(
                imagePath = File(File(context.filesDir, "comprovantes"), foto.fotoPath).absolutePath,
                contentDescription = "Comprovante ${foto.dataFormatada}",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            
            if (isSelected) {
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Close, // Poderia ser um Check
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }
            }

            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
                modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth()
            ) {
                Text(
                    text = foto.dataFormatada,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(2.dp),
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun ComprovanteDetailsDialog(
    foto: FotoComprovante,
    onDismiss: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Comprovante: ${foto.dataFormatada}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                ) {
                    LocalImage(
                        imagePath = File(File(context.filesDir, "comprovantes"), foto.fotoPath).absolutePath,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }
                
                DetailRow("Hora:", foto.horaFormatada)
                DetailRow("Tipo:", foto.tipoPontoDescricao)
                foto.nsr?.let { DetailRow("NSR:", it) }
                DetailRow("Tamanho:", foto.fotoTamanhoFormatado)
                if (foto.temLocalizacao) {
                    DetailRow("Local:", foto.enderecoFormatado ?: foto.coordenadasFormatadas ?: "")
                }
                DetailRow("Status:", if (foto.sincronizadoNuvem) "Sincronizado" else "Local apenas")
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Fechar") }
        },
        dismissButton = {
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Excluir", tint = MaterialTheme.colorScheme.error)
            }
        }
    )
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, fontWeight = FontWeight.Bold, modifier = Modifier.width(80.dp), style = MaterialTheme.typography.bodySmall)
        Text(text = value, style = MaterialTheme.typography.bodySmall)
    }
}
