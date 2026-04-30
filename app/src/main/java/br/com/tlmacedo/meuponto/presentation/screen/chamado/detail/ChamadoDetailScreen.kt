package br.com.tlmacedo.meuponto.presentation.screen.chamado.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.InsertDriveFile
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import br.com.tlmacedo.meuponto.domain.model.chamado.CategoriaChamado
import br.com.tlmacedo.meuponto.domain.model.chamado.Chamado
import br.com.tlmacedo.meuponto.domain.model.chamado.HistoricoChamado
import br.com.tlmacedo.meuponto.domain.model.chamado.PrioridadeChamado
import br.com.tlmacedo.meuponto.domain.model.chamado.StatusChamado
import br.com.tlmacedo.meuponto.presentation.components.MeuPontoTopBar
import br.com.tlmacedo.meuponto.presentation.screen.chamado.list.StatusChip
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
                title = "Detalhes",
                subtitle = "CHAMADO",
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
    val statusColor = getStatusColor(chamado.status)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Cabeçalho do Chamado
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = chamado.identificador,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    StatusChip(status = chamado.status, color = statusColor)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = chamado.titulo,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Aberto em ${chamado.criadoEm.format(dateFormatter)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }

        // Informações Adicionais
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                        alpha = 0.3f
                    )
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        DetailItem(
                            label = "Categoria",
                            value = chamado.categoria.label,
                            modifier = Modifier.weight(1f)
                        )
                        DetailItem(
                            label = "Prioridade",
                            value = chamado.prioridade.label,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Descrição
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Descrição da solicitação",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = chamado.descricao,
                    style = MaterialTheme.typography.bodyLarge,
                    lineHeight = 22.sp
                )
            }
        }

        // Resposta Técnica
        if (chamado.resposta != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(
                            alpha = 0.5f
                        )
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.History,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Resposta Técnica",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = chamado.resposta,
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        }

        // Anexos
        if (!chamado.anexos.isNullOrEmpty()) {
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Anexos",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        chamado.anexos.forEach { anexo ->
                            AttachmentItem(anexo)
                        }
                    }
                }
            }
        }

        // Histórico (Timeline)
        item {
            Text(
                text = "Histórico de atualizações",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        itemsIndexed(historico) { index, item ->
            TimelineItem(
                item = item,
                isLast = index == historico.size - 1,
                dateFormatter = dateFormatter
            )
        }

        item { Spacer(modifier = Modifier.height(32.dp)) }
    }
}

@Composable
fun DetailItem(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun AttachmentItem(fileName: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.InsertDriveFile,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = fileName,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
                maxLines = 1
            )
            Text(
                text = "ABRIR",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

@Composable
fun TimelineItem(
    item: HistoricoChamado,
    isLast: Boolean,
    dateFormatter: DateTimeFormatter
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(getStatusColor(item.statusNovo))
            )
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .weight(1f)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.padding(bottom = 24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.statusNovo.label,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = getStatusColor(item.statusNovo)
                )
                Text(
                    text = item.criadoEm.format(dateFormatter),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }

            if (item.mensagem.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.mensagem,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Atualizado por: ${item.autor}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                fontSize = 10.sp
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
