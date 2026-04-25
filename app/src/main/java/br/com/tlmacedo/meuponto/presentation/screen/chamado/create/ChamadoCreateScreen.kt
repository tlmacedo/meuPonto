package br.com.tlmacedo.meuponto.presentation.screen.chamado.create

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import br.com.tlmacedo.meuponto.domain.model.chamado.CategoriaChamado
import br.com.tlmacedo.meuponto.domain.model.chamado.PrioridadeChamado
import br.com.tlmacedo.meuponto.presentation.components.MeuPontoTopBar
import br.com.tlmacedo.meuponto.presentation.screen.chamado.ChamadoUiState
import br.com.tlmacedo.meuponto.presentation.screen.chamado.ChamadoViewModel
import br.com.tlmacedo.meuponto.presentation.theme.MeuPontoTheme

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
        onSubmit = { titulo, descricao, categoria, prioridade ->
            viewModel.criarChamado(
                titulo = titulo,
                descricao = descricao,
                categoria = categoria,
                prioridade = prioridade,
                anexos = emptyList()
            )
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChamadoCreateContent(
    uiState: ChamadoUiState,
    onBackClick: () -> Unit,
    onSubmit: (String, String, CategoriaChamado, PrioridadeChamado) -> Unit
) {
    var titulo by remember { mutableStateOf("") }
    var descricao by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf(CategoriaChamado.OUTRO) }
    var prioridade by remember { mutableStateOf(PrioridadeChamado.MEDIA) }

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
                label = { Text("Título") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = descricao,
                onValueChange = { descricao = it },
                label = { Text("Descrição detalhada") },
                modifier = Modifier.fillMaxWidth().height(150.dp),
                minLines = 3
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

            Spacer(modifier = Modifier.weight(1f))

            if (uiState.erro != null) {
                Text(
                    text = uiState.erro,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Button(
                onClick = { onSubmit(titulo, descricao, categoria, prioridade) },
                modifier = Modifier.fillMaxWidth(),
                enabled = titulo.isNotBlank() && descricao.isNotBlank() && !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Enviar Chamado")
                }
            }
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
            Text(selected.name.replace("_", " "))
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth()
        ) {
            CategoriaChamado.entries.forEach { cat ->
                DropdownMenuItem(
                    text = { Text(cat.name.replace("_", " ")) },
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
                shape = SegmentedButtonDefaults.itemShape(index = index, count = PrioridadeChamado.entries.size)
            ) {
                Text(prioridade.name)
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
            onSubmit = { _, _, _, _ -> }
        )
    }
}
