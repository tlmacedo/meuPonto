// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/components/premium/PremiumSurfaces.kt
package br.com.tlmacedo.meuponto.presentation.components.premium

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import br.com.tlmacedo.meuponto.presentation.theme.LocalPremiumTokens

@Composable
fun PremiumBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val tokens = LocalPremiumTokens.current

    Box(
        modifier = modifier.background(tokens.backgroundBrush),
        content = content
    )
}

@Composable
fun ThemedCard(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val tokens = LocalPremiumTokens.current
    val shape = RoundedCornerShape(28.dp)

    Box(
        modifier = modifier
            .shadow(
                elevation = 18.dp,
                shape = shape,
                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.22f),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.28f)
            )
            .background(tokens.cardBrush, shape)
            .border(1.dp, tokens.cardBorder, shape),
        content = content
    )
}