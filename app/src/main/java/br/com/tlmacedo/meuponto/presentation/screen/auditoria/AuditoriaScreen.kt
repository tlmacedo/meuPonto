// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/auditoria/AuditoriaScreen.kt
package br.com.tlmacedo.meuponto.presentation.screen.auditoria

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FilterListOff
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.RestoreFromTrash
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.tlmacedo.meuponto.domain.model.AcaoAuditoria
import br.com.tlmacedo.meuponto.domain.model.AuditLog
import br.com.tlmacedo.meuponto.presentation.components.MeuPontoTopBar
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Tela de Auditoria para visualizar histórico de alterações.
 *
 * @author Thiago
 * @since 11.0.0
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuditoriaScreen(
    onNavigateBack: () -> Unit,
    viewModel: AuditoriaViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Mostrar erros
    LaunchedEffect(uiState.mensagemErro) {
        uiState.mensagemErro?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onEvent(AuditoriaEvent.LimparMensagem)
        }
    }

    Scaffold(
        topBar = {
            MeuPontoTopBar(
                title = "Auditoria",
                subtitle = uiState.empregoApelido?.uppercase(),
                logo = uiState.empregoLogo,
                showBackButton = true,
                onBackClick = onNavigateBack,
                actions = {
                    if (uiState.filtroAtivo.temFiltrosAtivos) {
                        IconButton(onClick = { viewModel.onEvent(AuditoriaEvent.LimparFiltros) }) {
                            Icon(
                                imageVector = Icons.Default.FilterListOff,
                                contentDescription = "Limpar filtros"
                            )
                        }
                    }
                    IconButton(onClick = { viewModel.onEvent(AuditoriaEvent.ToggleFiltros) }) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filtros",
                            tint = if (uiState.filtroAtivo.temFiltrosAtivos) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Painel de filtros
            AnimatedVisibility(
                visible = uiState.showFiltros,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                FiltrosPanel(
                    filtro = uiState.filtroAtivo,
                    onFiltroChanged = { viewModel.onEvent(AuditoriaEvent.AtualizarFiltro(it)) }
                )
            }

            // Conteúdo principal
            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    uiState.isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    !uiState.temLogs -> {
                        AuditoriaVazia(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    else -> {
                        AuditoriaContent(
                            logsAgrupados = uiState.logsAgrupados,
                            onLogClick = { viewModel.onEvent(AuditoriaEvent.SelecionarLog(it)) }
                        )
                    }
                }
            }
        }

        // Bottom sheet de detalhes
        uiState.logSelecionado?.let { log ->
            LogDetalhesBottomSheet(
                log = log,
                onDismiss = { viewModel.onEvent(AuditoriaEvent.FecharDetalhes) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AuditoriaTopBar(
    quantidadeLogs: Int,
    temFiltrosAtivos: Boolean,
    onNavigateBack: () -> Unit,
    onToggleFiltros: () -> Unit,
    onLimparFiltros: () -> Unit
) {
    TopAppBar(
        title = {
            Text("Auditoria ($quantidadeLogs)")
        },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Voltar"
                )
            }
        },
        actions = {
            if (temFiltrosAtivos) {
                IconButton(onClick = onLimparFiltros) {
                    Icon(
                        imageVector = Icons.Default.FilterListOff,
                        contentDescription = "Limpar filtros"
                    )
                }
            }
            IconButton(onClick = onToggleFiltros) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = "Filtros",
                    tint = if (temFiltrosAtivos) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
            }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FiltrosPanel(
    filtro: FiltroAuditoria,
    onFiltroChanged: (FiltroAuditoria) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Campo de busca
            OutlinedTextField(
                value = filtro.termoBusca,
                onValueChange = { onFiltroChanged(filtro.copy(termoBusca = it)) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Buscar...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null)
                },
                trailingIcon = {
                    if (filtro.termoBusca.isNotBlank()) {
                        IconButton(onClick = {
                            onFiltroChanged(filtro.copy(termoBusca = ""))
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Limpar")
                        }
                    }
                },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Filtro por ação
            Text(
                text = "Tipo de Ação",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                AcaoAuditoria.entries.forEach { acao ->
                    FilterChip(
                        selected = acao in filtro.acoes,
                        onClick = {
                            val novasAcoes = if (acao in filtro.acoes) {
                                filtro.acoes - acao
                            } else {
                                filtro.acoes + acao
                            }
                            onFiltroChanged(filtro.copy(acoes = novasAcoes))
                        },
                        label = { Text(acao.descricao) },
                        leadingIcon = {
                            Icon(
                                imageVector = acao.getIcon(),
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Filtro por entidade
            Text(
                text = "Tipo de Registro",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf("Ponto", "Emprego", "Feriado", "Configuração").forEach { entityType ->
                    FilterChip(
                        selected = entityType in filtro.entityTypes,
                        onClick = {
                            val novosTypes = if (entityType in filtro.entityTypes) {
                                filtro.entityTypes - entityType
                            } else {
                                filtro.entityTypes + entityType
                            }
                            onFiltroChanged(filtro.copy(entityTypes = novosTypes))
                        },
                        label = { Text(entityType) }
                    )
                }
            }
        }
    }
}

@Composable
private fun AuditoriaVazia(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.History,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Nenhum registro encontrado",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "O histórico de alterações aparecerá aqui",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun AuditoriaContent(
    logsAgrupados: Map<LocalDate, List<AuditLog>>,
    onLogClick: (AuditLog) -> Unit
) {
    val dateFormatter = remember { DateTimeFormatter.ofPattern("EEEE, dd 'de' MMMM 'de' yyyy") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        logsAgrupados.forEach { (data, logs) ->
            // Header do grupo (data)
            item(key = "header_$data") {
                Text(
                    text = data.format(dateFormatter).replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            // Logs do dia
            items(
                items = logs,
                key = { it.id }
            ) { log ->
                AuditLogItem(
                    log = log,
                    onClick = { onLogClick(log) }
                )
            }
        }
    }
}

@Composable
private fun AuditLogItem(
    log: AuditLog,
    onClick: () -> Unit
) {
    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm:ss") }
    val logTime = remember(log.timestamp) {
        Instant.ofEpochMilli(log.timestamp)
            .atZone(ZoneId.systemDefault())
            .toLocalTime()
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ícone da ação
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(log.acao.getColor().copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = log.acao.getIcon(),
                    contentDescription = null,
                    tint = log.acao.getColor(),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Descrição
                Text(
                    text = log.description,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Tipo de entidade e hora
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = log.entidade,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = " • ",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = logTime.format(timeFormatter),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Indicador de detalhes
            if (log.temDetalhes) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Ver detalhes",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LogDetalhesBottomSheet(
    log: AuditLog,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val dateTimeFormatter = remember {
        DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm:ss")
    }
    val logDateTime = remember(log.timestamp) {
        Instant.ofEpochMilli(log.timestamp)
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(log.acao.getColor().copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = log.acao.getIcon(),
                        contentDescription = null,
                        tint = log.acao.getColor(),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = log.acao.descricao,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = log.entidade,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Descrição
            DetailSection(titulo = "Descrição", conteudo = log.description)

            Spacer(modifier = Modifier.height(16.dp))

            // Data e hora
            DetailSection(
                titulo = "Data e Hora",
                conteudo = logDateTime.format(dateTimeFormatter)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ID da entidade
            DetailSection(
                titulo = "ID do Registro",
                conteudo = "#${log.entidadeId}"
            )

            // Valor anterior
            log.dadosAnteriores?.let { oldValue ->
                Spacer(modifier = Modifier.height(16.dp))
                DetailSection(
                    titulo = "Valor Anterior",
                    conteudo = oldValue,
                    isCode = true
                )
            }

            // Novo valor
            log.dadosNovos?.let { newValue ->
                Spacer(modifier = Modifier.height(16.dp))
                DetailSection(
                    titulo = "Novo Valor",
                    conteudo = newValue,
                    isCode = true
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botão fechar
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Fechar")
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun DetailSection(
    titulo: String,
    conteudo: String,
    isCode: Boolean = false
) {
    Column {
        Text(
            text = titulo,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        if (isCode) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Text(
                    text = conteudo,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace
                    ),
                    modifier = Modifier.padding(12.dp),
                    maxLines = 10,
                    overflow = TextOverflow.Ellipsis
                )
            }
        } else {
            Text(
                text = conteudo,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

// === Extensões para AcaoAuditoria ===

@Composable
fun AcaoAuditoria.getIcon(): ImageVector {
    return when (this) {
        AcaoAuditoria.INSERT -> Icons.Default.Add
        AcaoAuditoria.UPDATE -> Icons.Default.Edit
        AcaoAuditoria.DELETE -> Icons.Default.Delete
        AcaoAuditoria.SOFT_DELETE -> Icons.Default.DeleteOutline
        AcaoAuditoria.RESTORE -> Icons.Default.RestoreFromTrash
        AcaoAuditoria.PERMANENT_DELETE -> Icons.Default.DeleteForever
    }
}

@Composable
fun AcaoAuditoria.getColor(): Color {
    return when (this) {
        AcaoAuditoria.INSERT -> MaterialTheme.colorScheme.primary
        AcaoAuditoria.UPDATE -> MaterialTheme.colorScheme.tertiary
        AcaoAuditoria.DELETE, AcaoAuditoria.SOFT_DELETE -> MaterialTheme.colorScheme.error
        AcaoAuditoria.RESTORE -> MaterialTheme.colorScheme.secondary
        AcaoAuditoria.PERMANENT_DELETE -> MaterialTheme.colorScheme.error
    }
}
