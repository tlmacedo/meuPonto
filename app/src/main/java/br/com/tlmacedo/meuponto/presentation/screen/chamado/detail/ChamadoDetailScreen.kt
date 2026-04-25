package br.com.tlmacedo.meuponto.presentation.screen.chamado.detail

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import br.com.tlmacedo.meuponto.domain.model.chamado.CategoriaChamado
import br.com.tlmacedo.meuponto.domain.model.chamado.Chamado
import br.com.tlmacedo.meuponto.domain.model.chamado.HistoricoChamado
import br.com.tlmacedo.meuponto.domain.model.chamado.PrioridadeChamado
import br.com.tlmacedo.meuponto.domain.model.chamado.StatusChamado
import br.com.tlmacedo.meuponto.presentation.components.MeuPontoTopBar
import br.com.tlmacedo.meuponto.presentation.screen.chamado.list.getStatusColor
import br.com.tlmacedo.meuponto.presentation.theme.MeuPontoTheme
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun ChamadoDetailScreen(
    onNavigateBack: () -> Unit,
    viewModel: ChamadoDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    ChamadoDetailContent(
        uiState = uiState,
        onBackClick = onNavigateBack
    )
}

@Composable
fun ChamadoDetailContent(
    uiState: ChamadoDetailUiState,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            MeuPontoTopBar(
                title = "Detalhes do Chamado",
                showBackButton = true,
                onBackClick = onBackClick
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (uiState) {
                ChamadoDetailUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                is ChamadoDetailUiState.Error -> {
                    Text(
                        text = uiState.message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                    )
                }

                is ChamadoDetailUiState.Success -> {
                    ChamadoDetailSuccessContent(uiState.chamado, uiState.historico)
                }
            }
        }
    }
}

@Composable
fun ChamadoDetailSuccessContent(chamado: Chamado, historico: List<HistoricoChamado>) {
    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = getStatusColor(chamado.status).copy(
                        alpha = 0.1f
                    )
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = chamado.titulo,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    DetailRow("ID", chamado.identificador)
                    DetailRow("Status", chamado.status.name)
                    DetailRow("Prioridade", chamado.prioridade.name)
                    DetailRow("Categoria", chamado.categoria.name)
                    DetailRow("Criado em", chamado.criadoEm.format(dateFormatter))
                }
            }
        }

        item {
            Text(
                text = "Descrição",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = chamado.descricao,
                style = MaterialTheme.typography.bodyLarge
            )
        }

        if (chamado.resposta != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Resposta Técnica",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = chamado.resposta,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }

        if (!chamado.anexos.isNullOrEmpty()) {
            item {
                Text(
                    text = "Anexos",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    chamado.anexos.forEach { anexo ->
                        AttachmentItem(anexo)
                    }
                }
            }
        }

        item {
            Text(
                text = "Histórico de Status",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        items(historico) { item ->
            HistoricoItem(item, dateFormatter)
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 2.dp)) {
        Text(
            text = "$label: ",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun AttachmentItem(fileName: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                alpha = 0.5f
            )
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.AttachFile,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = fileName,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = { /* TODO: Implementar visualização */ }) {
                Text("Visualizar")
            }
        }
    }
}

@Composable
fun HistoricoItem(item: HistoricoChamado, formatter: DateTimeFormatter) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = item.statusNovo.name,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = item.criadoEm.format(formatter),
                    style = MaterialTheme.typography.labelSmall
                )
            }
            if (item.mensagem.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.mensagem,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Por: ${item.autor}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ChamadoDetailContentPreview() {
    val chamado = Chamado(
        id = 1,
        identificador = "MP-2024-001",
        titulo = "Erro no backup",
        descricao = "O backup em nuvem não está funcionando desde a última atualização.",
        categoria = CategoriaChamado.SUPORTE_TECNICO,
        prioridade = PrioridadeChamado.ALTA,
        status = StatusChamado.ABERTO,
        usuarioEmail = "user@test.com",
        usuarioNome = "User Test",
        resposta = "Estamos analisando o logs do sistema.",
        anexos = arrayListOf("log_backup.txt", "print_erro.png"),
        criadoEm = LocalDateTime.now(),
        atualizadoEm = LocalDateTime.now(),
        empregoId = null
    )
    val historico = listOf(
        HistoricoChamado(
            id = 1,
            chamadoId = 1,
            statusAnterior = null,
            statusNovo = StatusChamado.ABERTO,
            mensagem = "Chamado aberto pelo usuário.",
            autor = "User Test",
            criadoEm = LocalDateTime.now().minusHours(2)
        )
    )
    MeuPontoTheme {
        ChamadoDetailContent(
            uiState = ChamadoDetailUiState.Success(chamado, historico),
            onBackClick = {}
        )
    }
}
