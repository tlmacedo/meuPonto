package br.com.tlmacedo.meuponto.presentation.screen.settings.comprovantes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
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
import java.io.File

@Composable
fun ComprovantesScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ComprovantesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            MeuPontoTopBar(
                title = "Gerenciador de Comprovantes",
                subtitle = "${uiState.totalCount} fotos • ${String.format("%.2f", uiState.totalSizeMb)} MB",
                showBackButton = true,
                onBackClick = onNavigateBack,
                actions = {
                    IconButton(onClick = { /* TODO: Mostrar DatePickerDialog */ }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filtrar")
                    }
                }
            )
        },
        modifier = modifier
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (uiState.items.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Nenhum comprovante encontrado no período.")
            }
        } else {
            ComprovantesGrid(
                items = uiState.items,
                onItemClick = { viewModel.onAction(ComprovantesAction.SelecionarComprovante(it)) },
                modifier = Modifier.padding(paddingValues)
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

@Composable
private fun ComprovantesGrid(
    items: List<FotoComprovante>,
    onItemClick: (FotoComprovante) -> Unit,
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
            ComprovanteGridItem(foto = foto, onClick = { onItemClick(foto) })
        }
    }
}

@Composable
private fun ComprovanteGridItem(
    foto: FotoComprovante,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            LocalImage(
                imagePath = File(context.filesDir, foto.fotoPath).absolutePath,
                contentDescription = "Comprovante ${foto.dataFormatada}",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            
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
                        imagePath = File(context.filesDir, foto.fotoPath).absolutePath,
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
