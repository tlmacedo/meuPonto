// app/src/main/java/br/com/tlmacedo/meuponto/presentation/components/swipe/SwipeRevealCard.kt

package br.com.tlmacedo.meuponto.presentation.components.swipe

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * Card com suporte a swipe horizontal para revelar ações.
 *
 * - Swipe para **esquerda** (←): revela ações do lado **direito** (endActions)
 * - Swipe para **direita** (→): revela ações do lado **esquerdo** (startActions)
 *
 * @param modifier Modificador do componente
 * @param startActions Ações reveladas ao arrastar para direita (aparecem à esquerda)
 * @param endActions Ações reveladas ao arrastar para esquerda (aparecem à direita)
 * @param actionWidth Largura de cada botão de ação
 * @param cornerRadius Raio do arredondamento dos cantos
 * @param enabled Se o swipe está habilitado
 * @param onSwipeStateChange Callback quando o estado do swipe muda
 * @param content Conteúdo principal do card
 *
 * @author Thiago
 * @since 7.1.0
 * @updated 7.2.0 - Renomeado leftActions→startActions, rightActions→endActions
 */
@Composable
fun SwipeRevealCard(
    modifier: Modifier = Modifier,
    startActions: List<SwipeAction> = emptyList(),
    endActions: List<SwipeAction> = emptyList(),
    actionWidth: Dp = 72.dp,
    cornerRadius: Dp = 16.dp,
    enabled: Boolean = true,
    onSwipeStateChange: ((SwipeState) -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()

    val actionWidthPx = with(density) { actionWidth.toPx() }

    val maxStartSwipe = if (startActions.isNotEmpty()) actionWidthPx * startActions.size else 0f
    val maxEndSwipe = if (endActions.isNotEmpty()) actionWidthPx * endActions.size else 0f

    val offsetX = remember { Animatable(0f) }
    var currentState by remember { mutableStateOf(SwipeState.COLLAPSED) }
    val snapThreshold = 0.4f

    // Notifica mudanças de estado
    LaunchedEffect(currentState) {
        onSwipeStateChange?.invoke(currentState)
    }

    // Função para resetar o swipe
    fun resetSwipe() {
        scope.launch {
            offsetX.animateTo(0f, tween(200))
            currentState = SwipeState.COLLAPSED
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(cornerRadius))
    ) {
        // ══════════════════════════════════════════════════════════
        // AÇÕES DA ESQUERDA (START) - Reveladas ao arrastar para →
        // ══════════════════════════════════════════════════════════
        if (startActions.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(cornerRadius)),
                horizontalArrangement = Arrangement.Start
            ) {
                startActions.forEach { action ->
                    SwipeActionButton(
                        action = action,
                        width = actionWidth,
                        onClick = {
                            resetSwipe()
                            if (action.enabled) action.onClick()
                        }
                    )
                }
            }
        }

        // ══════════════════════════════════════════════════════════
        // AÇÕES DA DIREITA (END) - Reveladas ao arrastar para ←
        // ══════════════════════════════════════════════════════════
        if (endActions.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(cornerRadius)),
                horizontalArrangement = Arrangement.End
            ) {
                endActions.forEach { action ->
                    SwipeActionButton(
                        action = action,
                        width = actionWidth,
                        onClick = {
                            resetSwipe()
                            if (action.enabled) action.onClick()
                        }
                    )
                }
            }
        }

        // ══════════════════════════════════════════════════════════
        // CONTEÚDO PRINCIPAL (desliza com o gesto)
        // ══════════════════════════════════════════════════════════
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .then(
                    if (enabled && (startActions.isNotEmpty() || endActions.isNotEmpty())) {
                        Modifier.pointerInput(Unit) {
                            detectHorizontalDragGestures(
                                onDragEnd = {
                                    scope.launch {
                                        val current = offsetX.value
                                        when {
                                            // Swipe para direita: revelar ações START
                                            current > maxStartSwipe * snapThreshold && startActions.isNotEmpty() -> {
                                                offsetX.animateTo(maxStartSwipe, tween(200))
                                                currentState = SwipeState.EXPANDED_START
                                            }
                                            // Swipe para esquerda: revelar ações END
                                            current < -maxEndSwipe * snapThreshold && endActions.isNotEmpty() -> {
                                                offsetX.animateTo(-maxEndSwipe, tween(200))
                                                currentState = SwipeState.EXPANDED_END
                                            }
                                            // Voltar para posição inicial
                                            else -> {
                                                offsetX.animateTo(0f, tween(200))
                                                currentState = SwipeState.COLLAPSED
                                            }
                                        }
                                    }
                                },
                                onHorizontalDrag = { _, dragAmount ->
                                    scope.launch {
                                        val newOffset = (offsetX.value + dragAmount).coerceIn(
                                            if (endActions.isNotEmpty()) -maxEndSwipe else 0f,
                                            if (startActions.isNotEmpty()) maxStartSwipe else 0f
                                        )
                                        offsetX.snapTo(newOffset)
                                    }
                                }
                            )
                        }
                    } else Modifier
                )
        ) {
            content()
        }
    }
}

/**
 * Botão individual de ação do swipe.
 */
@Composable
private fun SwipeActionButton(
    action: SwipeAction,
    width: Dp,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .width(width)
            .fillMaxHeight(),
        color = if (action.enabled) action.backgroundColor else action.backgroundColor.copy(alpha = 0.5f),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = action.icon,
                contentDescription = action.label,
                tint = if (action.enabled) action.contentColor else action.contentColor.copy(alpha = 0.5f),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = action.label,
                style = MaterialTheme.typography.labelSmall,
                color = if (action.enabled) action.contentColor else action.contentColor.copy(alpha = 0.5f)
            )
        }
    }
}
