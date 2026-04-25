package br.com.tlmacedo.meuponto.presentation.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Extensão de Modifier para aplicar efeito Shimmer (esqueleto de carregamento).
 */
fun Modifier.shimmerEffect(): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1200,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translation"
    )

    val shimmerColors = listOf(
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset.Zero,
        end = Offset(x = translateAnim.value, y = translateAnim.value)
    )

    background(brush)
}

/**
 * Item Shimmer que imita o card de um dia no histórico.
 */
@Composable
fun HistoryShimmerItem(
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Row(verticalAlignment = Alignment.Top, modifier = Modifier.weight(1f)) {
                // Emoji placeholder
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .shimmerEffect()
                        .background(Color.Gray.copy(alpha = 0.1f), CircleShape)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Column {
                    // Data placeholder
                    Box(
                        modifier = Modifier
                            .width(100.dp)
                            .height(20.dp)
                            .shimmerEffect()
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Dia da semana placeholder
                    Box(
                        modifier = Modifier
                            .width(150.dp)
                            .height(14.dp)
                            .shimmerEffect()
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                // Horas placeholder
                Box(
                    modifier = Modifier
                        .width(60.dp)
                        .height(20.dp)
                        .shimmerEffect()
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Pontos count placeholder
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(14.dp)
                        .shimmerEffect()
                )
            }
        }
    }
}
