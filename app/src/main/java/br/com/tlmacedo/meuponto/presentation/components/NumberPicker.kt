package br.com.tlmacedo.meuponto.presentation.components

import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Um seletor numérico estilo "scroll" (roda) para selecionar valores de uma faixa.
 *
 * @param value Valor atual selecionado.
 * @param onValueChange Callback chamado quando o valor muda.
 * @param range Faixa de valores possíveis.
 * @param modifier Modificador para o componente.
 * @param label Sufixo ou label opcional para o valor (ex: "min", "h").
 * @param visibleItems Número de itens visíveis simultaneamente.
 */
@Composable
fun NumberPicker(
    value: Int,
    onValueChange: (Int) -> Unit,
    range: IntRange,
    modifier: Modifier = Modifier,
    label: String? = null,
    visibleItems: Int = 3,
    suffix: String = ""
) {
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = (value - range.first)
    )
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
    
    val itemHeight = 40.dp
    val height = itemHeight * visibleItems
    
    val items = remember(range) { range.toList() }

    LaunchedEffect(listState.isScrollInProgress) {
        if (!listState.isScrollInProgress) {
            val centerIndex = listState.firstVisibleItemIndex
            if (centerIndex in items.indices) {
                onValueChange(items[centerIndex])
            }
        }
    }

    Box(
        modifier = modifier
            .height(height)
            .width(80.dp),
        contentAlignment = Alignment.Center
    ) {
        // Marcador central
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(itemHeight)
                .drawWithContent {
                    drawContent()
                    // Linhas separadoras
                    drawLine(
                        color = Color.Gray.copy(alpha = 0.3f),
                        start = androidx.compose.ui.geometry.Offset(0f, 0f),
                        end = androidx.compose.ui.geometry.Offset(size.width, 0f),
                        strokeWidth = 1.dp.toPx()
                    )
                    drawLine(
                        color = Color.Gray.copy(alpha = 0.3f),
                        start = androidx.compose.ui.geometry.Offset(0f, size.height),
                        end = androidx.compose.ui.geometry.Offset(size.width, size.height),
                        strokeWidth = 1.dp.toPx()
                    )
                }
        )

        LazyColumn(
            state = listState,
            flingBehavior = flingBehavior,
            contentPadding = PaddingValues(vertical = itemHeight * (visibleItems / 2)),
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(items.size) { index ->
                val itemValue = items[index]
                Box(
                    modifier = Modifier
                        .height(itemHeight)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$itemValue$suffix",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = if (itemValue == value) FontWeight.Bold else FontWeight.Normal,
                            fontSize = if (itemValue == value) 20.sp else 16.sp
                        ),
                        color = if (itemValue == value) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        }
        
        // Efeito de fade nas bordas
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
                .drawWithContent {
                    drawContent()
                    drawRect(
                        brush = Brush.verticalGradient(
                            0f to Color.Transparent,
                            0.2f to Color.Black,
                            0.8f to Color.Black,
                            1f to Color.Transparent
                        ),
                        blendMode = BlendMode.DstIn
                    )

                }
        )
    }
}
