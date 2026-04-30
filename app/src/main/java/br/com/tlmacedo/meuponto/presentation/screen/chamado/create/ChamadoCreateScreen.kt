package br.com.tlmacedo.meuponto.presentation.screen.chamado.create

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import br.com.tlmacedo.meuponto.domain.model.chamado.CategoriaChamado
import br.com.tlmacedo.meuponto.domain.model.chamado.PrioridadeChamado
import br.com.tlmacedo.meuponto.presentation.components.MeuPontoTopBar
import br.com.tlmacedo.meuponto.presentation.screen.chamado.ChamadoUiState
import br.com.tlmacedo.meuponto.presentation.screen.chamado.ChamadoViewModel
import br.com.tlmacedo.meuponto.presentation.theme.MeuPontoTheme
import coil.compose.AsyncImage

@Composable
fun ChamadoCreateScreen(
    onNavigateBack: () -> Unit,
    viewModel: ChamadoViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.sucesso) {
        if (uiState.sucesso != null) {
            onNavigateBack()
            viewModel.limparMensagens()
        }
    }

    ChamadoCreateContent(
        uiState = uiState,
        onBackClick = onNavigateBack,
        onSubmit = { titulo, descricao, passos, categoria, prioridade, anexos ->
            viewModel.criarChamado(
                titulo = titulo,
                descricao = descricao,
                passosParaReproduzir = passos,
                categoria = categoria,
                prioridade = prioridade,
                anexos = anexos
            )
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChamadoCreateContent(
    uiState: ChamadoUiState,
    onBackClick: () -> Unit,
    onSubmit: (String, String, String, CategoriaChamado, PrioridadeChamado, List<Uri>) -> Unit
) {
    var titulo by remember { mutableStateOf("") }
    var descricao by remember { mutableStateOf("") }
    var passos by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf(CategoriaChamado.SUPORTE) }
    var prioridade by remember { mutableStateOf(PrioridadeChamado.MEDIA) }
    var anexos by remember { mutableStateOf<List<Uri>>(emptyList()) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(),
        onResult = { uris ->
            anexos = (anexos + uris).distinct().take(5)
        }
    )

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            MeuPontoTopBar(
                title = "Novo Chamado",
                showBackButton = true,
                onBackClick = onBackClick
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = titulo,
                onValueChange = { titulo = it },
                label = { Text("Resumo do problema") },
                placeholder = { Text("Ex: Não consigo editar um ponto antigo") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = descricao,
                onValueChange = { descricao = it },
                label = { Text("O que aconteceu?") },
                placeholder = { Text("Descreva detalhadamente o problema...") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            OutlinedTextField(
                value = passos,
                onValueChange = { passos = it },
                label = { Text("Passos para reproduzir (Opcional)") },
                placeholder = { Text("1. Abri o histórico\n2. Tentei clicar em...") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            Text("Categoria", style = MaterialTheme.typography.titleSmall)
            CategoryDropdown(
                selected = categoria,
                onSelected = { categoria = it }
            )

            Text("Prioridade", style = MaterialTheme.typography.titleSmall)
            PrioritySegmentedButton(
                selected = prioridade,
                onSelected = { prioridade = it }
            )

            Text("Anexos e Evidências", style = MaterialTheme.typography.titleSmall)
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    Card(
                        onClick = {
                            launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        modifier = Modifier.size(80.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Outlined.AttachFile, contentDescription = "Anexar")
                        }
                    }
                }

                items(anexos) { uri ->
                    Box(modifier = Modifier.size(80.dp)) {
                        AsyncImage(
                            model = uri,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(MaterialTheme.shapes.medium),
                            contentScale = ContentScale.Crop
                        )
                        IconButton(
                            onClick = { anexos = anexos - uri },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(4.dp)
                                .size(20.dp)
                                .background(
                                    MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f),
                                    CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Close,
                                contentDescription = "Remover",
                                tint = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }

            if (uiState.erro != null) {
                Text(
                    text = uiState.erro!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Button(
                onClick = { onSubmit(titulo, descricao, passos, categoria, prioridade, anexos) },
                modifier = Modifier.fillMaxWidth(),
                enabled = titulo.isNotBlank() && descricao.isNotBlank() && !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Enviar Solicitação")
                }
            }

            Text(
                text = "Ao enviar, coletaremos automaticamente informações do dispositivo e logs do sistema para ajudar na análise técnica.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun CategoryDropdown(
    selected: CategoriaChamado,
    onSelected: (CategoriaChamado) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(selected.label)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth()
        ) {
            CategoriaChamado.entries.forEach { cat ->
                DropdownMenuItem(
                    text = { Text(cat.label) },
                    onClick = {
                        onSelected(cat)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrioritySegmentedButton(
    selected: PrioridadeChamado,
    onSelected: (PrioridadeChamado) -> Unit
) {
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        PrioridadeChamado.entries.forEachIndexed { index, prioridade ->
            SegmentedButton(
                selected = selected == prioridade,
                onClick = { onSelected(prioridade) },
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = PrioridadeChamado.entries.size
                )
            ) {
                Text(prioridade.label)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ChamadoCreateContentPreview() {
    MeuPontoTheme {
        ChamadoCreateContent(
            uiState = ChamadoUiState(),
            onBackClick = {},
            onSubmit = { _, _, _, _, _, _ -> }
        )
    }
}
