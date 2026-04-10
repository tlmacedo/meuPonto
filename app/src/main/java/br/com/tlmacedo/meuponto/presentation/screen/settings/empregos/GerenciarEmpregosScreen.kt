// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/settings/empregos/GerenciarEmpregosScreen.kt
package br.com.tlmacedo.meuponto.presentation.screen.settings.empregos

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.tlmacedo.meuponto.domain.model.Emprego
import br.com.tlmacedo.meuponto.presentation.components.LocalImage
import br.com.tlmacedo.meuponto.presentation.components.MeuPontoTopBar
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * Tela de gerenciamento de empregos.
 */
@Composable
fun GerenciarEmpregosScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEmpregoSettings: (Long) -> Unit,
    onNavigateToNovoEmprego: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GerenciarEmpregosViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.eventos.collectLatest { evento ->
            when (evento) {
                is GerenciarEmpregosEvent.MostrarMensagem -> {
                    snackbarHostState.showSnackbar(evento.mensagem)
                }
            }
        }
    }

    GerenciarEmpregosContent(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onNavigateToEmpregoSettings = onNavigateToEmpregoSettings,
        onNavigateToNovoEmprego = onNavigateToNovoEmprego,
        onAction = viewModel::onAction,
        modifier = modifier,
        snackbarHostState = snackbarHostState
    )
}

/**
 * Conteúdo da tela de gerenciamento de empregos, desacoplado do ViewModel.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GerenciarEmpregosContent(
    uiState: GerenciarEmpregosUiState,
    onNavigateBack: () -> Unit,
    onNavigateToEmpregoSettings: (Long) -> Unit,
    onNavigateToNovoEmprego: () -> Unit,
    onAction: (GerenciarEmpregosAction) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    Scaffold(
        topBar = {
            val empregoAtivo = uiState.empregos.find { it.id == uiState.empregoAtivoId }
            MeuPontoTopBar(
                title = "Gerenciar Empregos",
                subtitle = empregoAtivo?.apelido,
                showBackButton = true,
                onBackClick = onNavigateBack
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToNovoEmprego,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Novo emprego"
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(
                    horizontal = 16.dp,
                    vertical = 24.dp
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Seção: Empregos ativos
                if (uiState.empregos.isNotEmpty()) {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Business,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Empregos Ativos",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    items(
                        items = uiState.empregos,
                        key = { it.id }
                    ) { emprego ->
                        SwipeableEmpregoCard(
                            emprego = emprego,
                            isAtivo = emprego.id == uiState.empregoAtivoId,
                            onDefinirAtivo = {
                                onAction(GerenciarEmpregosAction.DefinirAtivo(emprego))
                            },
                            onArquivar = {
                                onAction(GerenciarEmpregosAction.Arquivar(emprego))
                            },
                            onEditar = { onNavigateToEmpregoSettings(emprego.id) }
                        )
                    }
                }

                // Seção: Empregos arquivados
                if (uiState.empregosArquivados.isNotEmpty()) {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Text(
                                text = "Arquivados (${uiState.empregosArquivados.size})",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(
                                onClick = {
                                    onAction(GerenciarEmpregosAction.ToggleMostrarArquivados)
                                }
                            ) {
                                Text(if (uiState.mostrarArquivados) "Ocultar" else "Mostrar")
                            }
                        }
                    }

                    if (uiState.mostrarArquivados) {
                        items(
                            items = uiState.empregosArquivados,
                            key = { it.id }
                        ) { emprego ->
                            EmpregoArquivadoCard(
                                emprego = emprego,
                                onRestaurar = {
                                    onAction(GerenciarEmpregosAction.Desarquivar(emprego))
                                },
                                onExcluir = {
                                    onAction(GerenciarEmpregosAction.SolicitarExclusao(emprego))
                                }
                            )
                        }
                    }
                }

                // Estado vazio
                if (uiState.empregos.isEmpty() && uiState.empregosArquivados.isEmpty()) {
                    item {
                        EmptyState()
                    }
                }
            }
        }
    }

    // Dialog de confirmação de exclusão
    uiState.dialogConfirmacaoExclusao?.let { emprego ->
        ConfirmacaoExclusaoDialog(
            nomeEmprego = emprego.nome,
            onConfirmar = { onAction(GerenciarEmpregosAction.ConfirmarExclusao) },
            onCancelar = { onAction(GerenciarEmpregosAction.CancelarExclusao) }
        )
    }
}

// ════════════════════════════════════════════════════════════════════════════════
// SWIPEABLE CARD COM ANCORAGEM MANUAL
// ════════════════════════════════════════════════════════════════════════════════

@Composable
private fun SwipeableEmpregoCard(
    emprego: Emprego,
    isAtivo: Boolean,
    onDefinirAtivo: () -> Unit,
    onArquivar: () -> Unit,
    onEditar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()

    // Largura das ações em pixels
    val actionWidthPx = with(density) { 160.dp.toPx() }

    // Estado do offset com Animatable para animações suaves
    val offsetX = remember { Animatable(0f) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
    ) {
        // Background com ações (sempre visível atrás do card)
        SwipeActionsBackground(
            isAtivo = isAtivo,
            onAtivarDesativar = {
                scope.launch {
                    offsetX.animateTo(0f, tween(200))
                }
                if (isAtivo) onArquivar() else onDefinirAtivo()
            },
            onEditar = {
                scope.launch {
                    offsetX.animateTo(0f, tween(200))
                }
                onEditar()
            },
            modifier = Modifier.matchParentSize()
        )

        // Card principal que desliza
        EmpregoCardContent(
            emprego = emprego,
            isAtivo = isAtivo,
            modifier = Modifier
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            scope.launch {
                                // Se arrastou mais da metade, ancora aberto; senão, fecha
                                val targetValue = if (offsetX.value > actionWidthPx * 0.5f) {
                                    actionWidthPx
                                } else {
                                    0f
                                }
                                offsetX.animateTo(targetValue, tween(200))
                            }
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            scope.launch {
                                val newValue = (offsetX.value + dragAmount)
                                    .coerceIn(0f, actionWidthPx)
                                offsetX.snapTo(newValue)
                            }
                        }
                    )
                }
        )
    }
}

/**
 * Background com os botões de ação do swipe.
 */
@Composable
private fun SwipeActionsBackground(
    isAtivo: Boolean,
    onAtivarDesativar: () -> Unit,
    onEditar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            .padding(start = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        // Botão Ativar/Desativar
        SwipeActionButton(
            icon = Icons.Default.PowerSettingsNew,
            label = if (isAtivo) "Arquivar" else "Ativar",
            containerColor = if (isAtivo) {
                MaterialTheme.colorScheme.errorContainer
            } else {
                MaterialTheme.colorScheme.primaryContainer
            },
            contentColor = if (isAtivo) {
                MaterialTheme.colorScheme.onErrorContainer
            } else {
                MaterialTheme.colorScheme.onPrimaryContainer
            },
            onClick = onAtivarDesativar
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Botão Editar
        SwipeActionButton(
            icon = Icons.Default.Edit,
            label = "Editar",
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
            onClick = onEditar
        )
    }
}

/**
 * Botão de ação individual no swipe.
 */
@Composable
private fun SwipeActionButton(
    icon: ImageVector,
    label: String,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(20.dp)  // Reduzido de 24dp para 20dp
            )
            Spacer(modifier = Modifier.height(2.dp))  // Reduzido de 4dp para 2dp
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
        }
    }
}

/**
 * Conteúdo do card de emprego.
 */
@Composable
private fun EmpregoCardContent(
    emprego: Emprego,
    isAtivo: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isAtivo) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Logo da Empresa
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                if (emprego.logo != null) {
                    LocalImage(
                        imagePath = emprego.logo,
                        contentDescription = "Logo da empresa",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Business,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = if (isAtivo) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                Text(
                    text = emprego.nome,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (isAtivo) "Emprego ativo" else "← Arraste para opções",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isAtivo) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    }
                )
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════════════════════
// CARDS AUXILIARES
// ════════════════════════════════════════════════════════════════════════════════

@Composable
private fun EmpregoArquivadoCard(
    emprego: Emprego,
    onRestaurar: () -> Unit,
    onExcluir: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Logo da Empresa (apagada no arquivado)
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .alpha(0.5f)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                if (emprego.logo != null) {
                    LocalImage(
                        imagePath = emprego.logo,
                        contentDescription = "Logo da empresa",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Archive,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline
                    )
                }
            }

            Text(
                text = emprego.nome,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.outline,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            )

            TextButton(onClick = onRestaurar) {
                Text("Restaurar")
            }

            IconButton(onClick = onExcluir) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Excluir",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun EmptyState(
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Business,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Nenhum emprego cadastrado",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.outline
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Toque em + para adicionar",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
private fun ConfirmacaoExclusaoDialog(
    nomeEmprego: String,
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancelar,
        icon = {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = { Text("Excluir Emprego") },
        text = {
            Text(
                "Tem certeza que deseja excluir \"$nomeEmprego\"?\n\n" +
                        "Esta ação é irreversível e todos os registros de ponto associados serão perdidos."
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirmar,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
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

// ════════════════════════════════════════════════════════════════════════════════
// PREVIEWS
// ════════════════════════════════════════════════════════════════════════════════

@Preview(showBackground = true)
@Composable
private fun GerenciarEmpregosContentPreview() {
    val uiState = GerenciarEmpregosUiState(
        isLoading = false,
        empregos = listOf(
            Emprego(id = 1, nome = "Empresa A", ativo = true),
            Emprego(id = 2, nome = "Empresa B", ativo = false)
        ),
        empregoAtivoId = 1,
        mostrarArquivados = true,
        empregosArquivados = listOf(
            Emprego(id = 3, nome = "Empresa C Antiga", arquivado = true)
        )
    )

    br.com.tlmacedo.meuponto.presentation.theme.MeuPontoTheme {
        androidx.compose.material3.Surface {
            GerenciarEmpregosContent(
                uiState = uiState,
                onNavigateBack = {},
                onNavigateToEmpregoSettings = {},
                onNavigateToNovoEmprego = {},
                onAction = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun GerenciarEmpregosEmptyPreview() {
    val uiState = GerenciarEmpregosUiState(
        isLoading = false,
        empregos = emptyList()
    )

    br.com.tlmacedo.meuponto.presentation.theme.MeuPontoTheme {
        androidx.compose.material3.Surface {
            GerenciarEmpregosContent(
                uiState = uiState,
                onNavigateBack = {},
                onNavigateToEmpregoSettings = {},
                onNavigateToNovoEmprego = {},
                onAction = {}
            )
        }
    }
}
