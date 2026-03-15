package br.com.tlmacedo.meuponto.presentation.components.swipe

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.gestures.snapTo
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import br.com.tlmacedo.meuponto.domain.model.Ponto
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

/**
 * Estados possíveis do swipe para um registro de ponto.
 */
enum class PontoSwipeState {
    HIDDEN,        // Posição normal
    LEFT_REVEALED, // Swipe para esquerda - revela Ver Foto e Ver Localização
    RIGHT_REVEALED // Swipe para direita - revela Excluir e Editar
}

/**
 * Linha de ponto com suporte a swipe para revelar ações.
 *
 * - Swipe para DIREITA: revela Excluir (vermelho) e Editar (azul)
 * - Swipe para ESQUERDA: revela Ver Localização e Ver Foto (se disponíveis)
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SwipeablePontoRow(
    ponto: Ponto,
    onEditar: (Ponto) -> Unit = {},
    onExcluir: (Ponto) -> Unit = {},
    onVerFoto: (Ponto) -> Unit = {},
    onVerLocalizacao: (Ponto) -> Unit = {},
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    // Largura de cada botão de ação
    val actionButtonWidthDp = 56.dp
    val actionButtonWidthPx = with(density) { actionButtonWidthDp.toPx() }

    // Verificar recursos disponíveis
    val temFoto = ponto.temFotoComprovante
    val temLocalizacao = ponto.temLocalizacao

    // Calcular largura total das ações à DIREITA (reveladas ao swipe para ESQUERDA)
    val numAcoesDireita = (if (temFoto) 1 else 0) + (if (temLocalizacao) 1 else 0)
    val leftSwipeRevealPx = if (numAcoesDireita > 0) actionButtonWidthPx * numAcoesDireita else 0f

    // Largura das ações à ESQUERDA (reveladas ao swipe para DIREITA) - Excluir + Editar
    val rightSwipeRevealPx = actionButtonWidthPx * 2

    // Estado do draggable
    val anchoredDraggableState = remember {
        AnchoredDraggableState(
            initialValue = PontoSwipeState.HIDDEN,
            positionalThreshold = { distance: Float -> distance * 0.4f },
            velocityThreshold = { with(density) { 100.dp.toPx() } },
            snapAnimationSpec = tween(durationMillis = 200),
            decayAnimationSpec = exponentialDecay()
        )
    }

    // Atualizar âncoras
    LaunchedEffect(leftSwipeRevealPx, rightSwipeRevealPx, numAcoesDireita) {
        val newAnchors = DraggableAnchors {
            PontoSwipeState.HIDDEN at 0f
            // Swipe para DIREITA (offset positivo) → revela ações na ESQUERDA (Excluir/Editar)
            PontoSwipeState.RIGHT_REVEALED at rightSwipeRevealPx
            // Swipe para ESQUERDA (offset negativo) → revela ações na DIREITA (Foto/Local)
            if (numAcoesDireita > 0) {
                PontoSwipeState.LEFT_REVEALED at -leftSwipeRevealPx
            }
        }
        anchoredDraggableState.updateAnchors(newAnchors)
    }

    // Feedback háptico ao revelar
    var lastState by remember { mutableStateOf(PontoSwipeState.HIDDEN) }
    LaunchedEffect(anchoredDraggableState.currentValue) {
        if (anchoredDraggableState.currentValue != lastState &&
            anchoredDraggableState.currentValue != PontoSwipeState.HIDDEN
        ) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
        lastState = anchoredDraggableState.currentValue
    }

    // Helper para resetar e executar ação
    fun executeAction(action: () -> Unit) {
        scope.launch {
            anchoredDraggableState.animateTo(PontoSwipeState.HIDDEN)
            action()
        }
    }

    // Offset atual
    val currentOffset = anchoredDraggableState.offset.takeIf { !it.isNaN() } ?: 0f

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
    ) {
        // ══════════════════════════════════════════════════════════
        // BACKGROUND ESQUERDO - Ações reveladas ao swipe para DIREITA
        // (Excluir e Editar)
        // ══════════════════════════════════════════════════════════
        if (currentOffset > 0) {
            Row(
                modifier = Modifier
                    .matchParentSize(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Excluir (primeiro, mais à esquerda)
                SwipePontoActionButton(
                    icon = Icons.Default.Delete,
                    contentDescription = "Excluir",
                    iconColor = MaterialTheme.colorScheme.onError,
                    backgroundColor = MaterialTheme.colorScheme.error,
                    revealed = currentOffset >= rightSwipeRevealPx * 0.5f,
                    onClick = { executeAction { onExcluir(ponto) } },
                    modifier = Modifier.width(actionButtonWidthDp)
                )

                // Editar (segundo)
                SwipePontoActionButton(
                    icon = Icons.Default.Edit,
                    contentDescription = "Editar",
                    iconColor = MaterialTheme.colorScheme.onPrimary,
                    backgroundColor = MaterialTheme.colorScheme.primary,
                    revealed = currentOffset >= rightSwipeRevealPx * 0.5f,
                    onClick = { executeAction { onEditar(ponto) } },
                    modifier = Modifier.width(actionButtonWidthDp)
                )
            }
        }

        // ══════════════════════════════════════════════════════════
        // BACKGROUND DIREITO - Ações reveladas ao swipe para ESQUERDA
        // (Ver Localização e Ver Foto)
        // ══════════════════════════════════════════════════════════
        if (currentOffset < 0 && numAcoesDireita > 0) {
            Row(
                modifier = Modifier
                    .matchParentSize(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Ver Localização (se disponível)
                if (temLocalizacao) {
                    SwipePontoActionButton(
                        icon = Icons.Default.LocationOn,
                        contentDescription = "Ver Localização",
                        iconColor = MaterialTheme.colorScheme.onTertiary,
                        backgroundColor = MaterialTheme.colorScheme.tertiary,
                        revealed = currentOffset.absoluteValue >= leftSwipeRevealPx * 0.5f,
                        onClick = { executeAction { onVerLocalizacao(ponto) } },
                        modifier = Modifier.width(actionButtonWidthDp)
                    )
                }

                // Ver Foto (se disponível)
                if (temFoto) {
                    SwipePontoActionButton(
                        icon = Icons.Default.Photo,
                        contentDescription = "Ver Foto",
                        iconColor = MaterialTheme.colorScheme.onSecondary,
                        backgroundColor = MaterialTheme.colorScheme.secondary,
                        revealed = currentOffset.absoluteValue >= leftSwipeRevealPx * 0.5f,
                        onClick = { executeAction { onVerFoto(ponto) } },
                        modifier = Modifier.width(actionButtonWidthDp)
                    )
                }
            }
        }

        // ══════════════════════════════════════════════════════════
        // FOREGROUND - Conteúdo do ponto com swipe (CENTRALIZADO)
        // ══════════════════════════════════════════════════════════
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(currentOffset.roundToInt(), 0) }
                .anchoredDraggable(
                    state = anchoredDraggableState,
                    orientation = Orientation.Horizontal
                )
                .background(MaterialTheme.colorScheme.surface),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}

/**
 * Botão de ação compacto (apenas ícone) revelado pelo swipe.
 */
@Composable
private fun SwipePontoActionButton(
    icon: ImageVector,
    contentDescription: String,
    iconColor: Color,
    backgroundColor: Color,
    revealed: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (revealed) 1f else 0.6f,
        animationSpec = tween(150),
        label = "scale_$contentDescription"
    )

    Surface(
        onClick = onClick,
        modifier = modifier
            .fillMaxHeight()
            .scale(scale),
        color = backgroundColor,
        shape = RoundedCornerShape(0.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
