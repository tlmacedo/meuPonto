// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/lixeira/LixeiraScreen.kt
package br.com.tlmacedo.meuponto.presentation.screen.lixeira

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.tlmacedo.meuponto.domain.model.TipoPonto
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Tela de Lixeira.
 *
 * Exibe pontos excluídos (soft delete) com opções de restaurar
 * ou excluir permanentemente.
 *
 * @author Thiago
 * @since 9.2.0
 * @updated 11.0.0 - Refatorado para soft delete
 */
@Composable
fun LixeiraScreen(
    onNavigateBack: () -> Unit,
    viewModel: LixeiraViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Eventos
    LaunchedEffect(Unit) {
        viewModel.eventos.collect { evento ->
            when (evento) {
                is LixeiraUiEvent.Erro -> {
                    snackbarHostState.showSnackbar(evento.mensagem)
                }

                is LixeiraUiEvent.Voltar -> onNavigateBack()

                is LixeiraUiEvent.ItemRestaurado -> {
                    snackbarHostState.showSnackbar("Ponto de ${evento.dataFormatada} restaurado")
                }

                is LixeiraUiEvent.ItensRestaurados -> {
                    snackbarHostState.showSnackbar("${evento.quantidade} ponto(s) restaurado(s)")
                }

                is LixeiraUiEvent.ItemExcluido -> {
                    snackbarHostState.showSnackbar("Ponto de ${evento.dataFormatada} excluído permanentemente")
                }

                is LixeiraUiEvent.ItensExcluidos -> {
                    snackbarHostState.showSnackbar("${evento.quantidade} ponto(s) excluído(s) permanentemente")
                }

                is LixeiraUiEvent.LixeiraEsvaziada -> {
                    snackbarHostState.showSnackbar("Lixeira esvaziada com sucesso")
                }

                is LixeiraUiEvent.MostrarMensagem -> {
                    snackbarHostState.showSnackbar(evento.mensagem)
                }
            }
        }
    }

    // Back handler para modo seleção
    BackHandler(enabled = uiState.modoSelecao) {
        viewModel.onAction(LixeiraAction.DesativarModoSelecao)
    }

    LixeiraContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onAction = viewModel::onAction
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LixeiraContent(
    uiState: LixeiraUiState,
    snackbarHostState: SnackbarHostState,
    onAction: (LixeiraAction) -> Unit
) {
    Scaffold(
        topBar = {
            if (uiState.modoSelecao) {
                SelectionTopBar(
                    quantidadeSelecionados = uiState.quantidadeSelecionados,
                    todosSelecionados = uiState.todosSelecionados,
                    onSelecionarTodos = { onAction(LixeiraAction.SelecionarTodos) },
                    onLimparSelecao = { onAction(LixeiraAction.LimparSelecao) },
                    onRestaurarSelecionados = { onAction(LixeiraAction.RestaurarSelecionados) },
                    onExcluirSelecionados = { onAction(LixeiraAction.ExcluirSelecionados) },
                    onFechar = { onAction(LixeiraAction.DesativarModoSelecao) }
                )
            } else {
                LixeiraTopBar(
                    quantidadeItens = uiState.quantidadeItens,
                    ordenacao = uiState.ordenacao,
                    onVoltar = { onAction(LixeiraAction.Voltar) },
                    onAlterarOrdenacao = { onAction(LixeiraAction.AlterarOrdenacao(it)) },
                    onEsvaziarLixeira = { onAction(LixeiraAction.SolicitarEsvaziarLixeira) },
                    habilitarEsvaziar = uiState.pontosNaLixeira.isNotEmpty()
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    LoadingState()
                }

                uiState.isEmpty -> {
                    EmptyState()
                }

                else -> {
                    LixeiraList(
                        itens = uiState.pontosFiltrados,
                        pontosSelecionados = uiState.pontosSelecionados,
                        modoSelecao = uiState.modoSelecao,
                        quantidadeExpirandoEmBreve = uiState.quantidadeExpirandoEmBreve,
                        onItemClick = { item ->
                            if (uiState.modoSelecao) {
                                onAction(LixeiraAction.ToggleSelecao(item.id))
                            }
                        },
                        onItemLongClick = { item ->
                            if (!uiState.modoSelecao) {
                                onAction(LixeiraAction.AtivarModoSelecao)
                            }
                            onAction(LixeiraAction.ToggleSelecao(item.id))
                        },
                        onRestaurar = { item ->
                            onAction(LixeiraAction.SolicitarRestaurar(item.ponto))
                        },
                        onExcluir = { item ->
                            onAction(LixeiraAction.SolicitarExcluir(item.ponto))
                        }
                    )
                }
            }

            // Erro
            uiState.mensagemErro?.let { erro ->
                ErrorBanner(
                    mensagem = erro,
                    onDismiss = { onAction(LixeiraAction.LimparErro) },
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            }
        }
    }

    // Diálogos
    if (uiState.showConfirmacaoRestaurar && uiState.pontoParaAcao != null) {
        ConfirmacaoRestaurarDialog(
            dataFormatada = uiState.pontoParaAcao.dataFormatada,
            onConfirmar = { onAction(LixeiraAction.ConfirmarRestaurar) },
            onCancelar = { onAction(LixeiraAction.CancelarRestaurar) }
        )
    }

    if (uiState.showConfirmacaoExcluir && uiState.pontoParaAcao != null) {
        ConfirmacaoExcluirDialog(
            dataFormatada = uiState.pontoParaAcao.dataFormatada,
            onConfirmar = { onAction(LixeiraAction.ConfirmarExcluir) },
            onCancelar = { onAction(LixeiraAction.CancelarExcluir) }
        )
    }

    if (uiState.showConfirmacaoEsvaziar) {
        ConfirmacaoEsvaziarDialog(
            quantidade = uiState.quantidadeItens,
            onConfirmar = { onAction(LixeiraAction.ConfirmarEsvaziarLixeira) },
            onCancelar = { onAction(LixeiraAction.CancelarEsvaziarLixeira) }
        )
    }
}

// =============================================================================
// TOP BARS
// =============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LixeiraTopBar(
    quantidadeItens: Int,
    ordenacao: OrdenacaoLixeira,
    onVoltar: () -> Unit,
    onAlterarOrdenacao: (OrdenacaoLixeira) -> Unit,
    onEsvaziarLixeira: () -> Unit,
    habilitarEsvaziar: Boolean
) {
    var showMenuOrdenacao by remember { mutableStateOf(false) }
    var showMenuOpcoes by remember { mutableStateOf(false) }

    TopAppBar(
        title = {
            Column {
                Text("Lixeira")
                if (quantidadeItens > 0) {
                    Text(
                        text = "$quantidadeItens item(ns)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = onVoltar) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Voltar"
                )
            }
        },
        actions = {
            // Ordenação
            Box {
                IconButton(onClick = { showMenuOrdenacao = true }) {
                    Icon(
                        imageVector = Icons.Default.Sort,
                        contentDescription = "Ordenar"
                    )
                }

                DropdownMenu(
                    expanded = showMenuOrdenacao,
                    onDismissRequest = { showMenuOrdenacao = false }
                ) {
                    OrdenacaoLixeira.entries.forEach { opcao ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = opcao.label,
                                    fontWeight = if (opcao == ordenacao) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            onClick = {
                                onAlterarOrdenacao(opcao)
                                showMenuOrdenacao = false
                            }
                        )
                    }
                }
            }

            // Menu de opções
            Box {
                IconButton(onClick = { showMenuOpcoes = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Mais opções"
                    )
                }

                DropdownMenu(
                    expanded = showMenuOpcoes,
                    onDismissRequest = { showMenuOpcoes = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Esvaziar lixeira") },
                        onClick = {
                            showMenuOpcoes = false
                            onEsvaziarLixeira()
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                        },
                        enabled = habilitarEsvaziar
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectionTopBar(
    quantidadeSelecionados: Int,
    todosSelecionados: Boolean,
    onSelecionarTodos: () -> Unit,
    onLimparSelecao: () -> Unit,
    onRestaurarSelecionados: () -> Unit,
    onExcluirSelecionados: () -> Unit,
    onFechar: () -> Unit
) {
    TopAppBar(
        title = {
            Text("$quantidadeSelecionados selecionado(s)")
        },
        navigationIcon = {
            IconButton(onClick = onFechar) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Fechar seleção"
                )
            }
        },
        actions = {
            // Selecionar todos
            IconButton(
                onClick = if (todosSelecionados) onLimparSelecao else onSelecionarTodos
            ) {
                Icon(
                    imageVector = Icons.Default.SelectAll,
                    contentDescription = if (todosSelecionados) "Limpar seleção" else "Selecionar todos"
                )
            }

            // Restaurar selecionados
            IconButton(
                onClick = onRestaurarSelecionados,
                enabled = quantidadeSelecionados > 0
            ) {
                Icon(
                    imageVector = Icons.Default.Restore,
                    contentDescription = "Restaurar selecionados",
                    tint = if (quantidadeSelecionados > 0)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                )
            }

            // Excluir selecionados
            IconButton(
                onClick = onExcluirSelecionados,
                enabled = quantidadeSelecionados > 0
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteForever,
                    contentDescription = "Excluir selecionados permanentemente",
                    tint = if (quantidadeSelecionados > 0)
                        MaterialTheme.colorScheme.error
                    else
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    )
}

// =============================================================================
// LISTA
// =============================================================================

@Composable
private fun LixeiraList(
    itens: List<PontoLixeiraItem>,
    pontosSelecionados: Set<Long>,
    modoSelecao: Boolean,
    quantidadeExpirandoEmBreve: Int,
    onItemClick: (PontoLixeiraItem) -> Unit,
    onItemLongClick: (PontoLixeiraItem) -> Unit,
    onRestaurar: (PontoLixeiraItem) -> Unit,
    onExcluir: (PontoLixeiraItem) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        // Aviso de itens expirando
        if (quantidadeExpirandoEmBreve > 0) {
            item {
                AvisoExpiracaoCard(quantidade = quantidadeExpirandoEmBreve)
            }
        }

        items(
            items = itens,
            key = { it.id }
        ) { item ->
            LixeiraItemCard(
                item = item,
                selecionado = item.id in pontosSelecionados,
                modoSelecao = modoSelecao,
                onClick = { onItemClick(item) },
                onLongClick = { onItemLongClick(item) },
                onRestaurar = { onRestaurar(item) },
                onExcluir = { onExcluir(item) }
            )
        }

        // Info de retenção
        item {
            InfoRetencaoCard()
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LixeiraItemCard(
    item: PontoLixeiraItem,
    selecionado: Boolean,
    modoSelecao: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onRestaurar: () -> Unit,
    onExcluir: () -> Unit
) {
    val haptic = LocalHapticFeedback.current

    val cardColor = when {
        selecionado -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        item.expirandoEmBreve -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
        else -> MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongClick()
                }
            ),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (selecionado) 4.dp else 1.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox ou ícone de tipo
            AnimatedVisibility(visible = modoSelecao) {
                Icon(
                    imageVector = if (selecionado)
                        Icons.Default.CheckCircle
                    else
                        Icons.Default.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = if (selecionado)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .padding(end = 12.dp)
                        .size(24.dp)
                )
            }

            // Ícone de lixeira
            if (!modoSelecao) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier
                        .padding(end = 12.dp)
                        .size(24.dp)
                )
            }

// Informações do ponto
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Data e hora
                Text(
                    text = item.ponto.dataFormatada,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )

                // Horário (sem tipo - pois é determinado pelo índice na lista)
                Text(
                    text = item.ponto.horaFormatada,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Emprego
                Text(
                    text = item.nomeEmprego,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Dias restantes
                DiasRestantesIndicator(
                    diasRestantes = item.diasRestantes,
                    expirandoEmBreve = item.expirandoEmBreve
                )
            }

            // Ações (apenas se não estiver em modo seleção)
            if (!modoSelecao) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Restaurar
                    IconButton(
                        onClick = onRestaurar,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Restore,
                            contentDescription = "Restaurar",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Excluir permanentemente
                    IconButton(
                        onClick = onExcluir,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteForever,
                            contentDescription = "Excluir permanentemente",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DiasRestantesIndicator(
    diasRestantes: Int,
    expirandoEmBreve: Boolean
) {
    val progress = diasRestantes / 30f
    val progressAnimated by animateFloatAsState(
        targetValue = progress,
        label = "progress"
    )

    val color = when {
        diasRestantes <= 3 -> MaterialTheme.colorScheme.error
        diasRestantes <= 7 -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.primary
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = Icons.Default.Schedule,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = color
        )

        Spacer(modifier = Modifier.width(4.dp))

        Text(
            text = when {
                diasRestantes <= 0 -> "Expira hoje"
                diasRestantes == 1 -> "Expira amanhã"
                else -> "Expira em $diasRestantes dias"
            },
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = if (expirandoEmBreve) FontWeight.Bold else FontWeight.Normal
        )

        Spacer(modifier = Modifier.width(8.dp))

        LinearProgressIndicator(
            progress = { progressAnimated },
            modifier = Modifier
                .weight(1f)
                .height(4.dp),
            color = color,
            trackColor = color.copy(alpha = 0.2f),
            strokeCap = StrokeCap.Round
        )
    }
}

// =============================================================================
// ESTADOS
// =============================================================================

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LinearProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Carregando lixeira...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.DeleteSweep,
                contentDescription = null,
                modifier = Modifier
                    .size(80.dp)
                    .alpha(0.5f),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Lixeira vazia",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Pontos excluídos aparecerão aqui e serão\nremovidos permanentemente após 30 dias",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

// =============================================================================
// CARDS INFORMATIVOS
// =============================================================================

@Composable
private fun AvisoExpiracaoCard(quantidade: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Atenção",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Text(
                    text = "$quantidade item(ns) expira(m) em até 7 dias",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

@Composable
private fun InfoRetencaoCard() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "Itens na lixeira são excluídos permanentemente após 30 dias",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ErrorBanner(
    mensagem: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = true,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut(),
        modifier = modifier
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.errorContainer
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = mensagem,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.weight(1f)
                )

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Fechar",
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    }
}

// =============================================================================
// DIÁLOGOS
// =============================================================================

@Composable
private fun ConfirmacaoRestaurarDialog(
    dataFormatada: String,
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancelar,
        icon = {
            Icon(
                imageVector = Icons.Default.Restore,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = { Text("Restaurar ponto?") },
        text = {
            Text("O ponto de $dataFormatada será restaurado para a lista principal.")
        },
        confirmButton = {
            FilledTonalButton(onClick = onConfirmar) {
                Text("Restaurar")
            }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
private fun ConfirmacaoExcluirDialog(
    dataFormatada: String,
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancelar,
        icon = {
            Icon(
                imageVector = Icons.Default.DeleteForever,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = { Text("Excluir permanentemente?") },
        text = {
            Column {
                Text("O ponto de $dataFormatada será excluído permanentemente.")
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Esta ação não pode ser desfeita.",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        confirmButton = {
            FilledTonalButton(
                onClick = onConfirmar,
                colors = androidx.compose.material3.ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            ) {
                Text("Excluir")
            }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
private fun ConfirmacaoEsvaziarDialog(
    quantidade: Int,
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancelar,
        icon = {
            Icon(
                imageVector = Icons.Default.DeleteSweep,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = { Text("Esvaziar lixeira?") },
        text = {
            Column {
                Text("Todos os $quantidade item(ns) serão excluídos permanentemente.")
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Esta ação não pode ser desfeita!",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        confirmButton = {
            FilledTonalButton(
                onClick = onConfirmar,
                colors = androidx.compose.material3.ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            ) {
                Text("Esvaziar")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onCancelar) {
                Text("Cancelar")
            }
        }
    )
}
