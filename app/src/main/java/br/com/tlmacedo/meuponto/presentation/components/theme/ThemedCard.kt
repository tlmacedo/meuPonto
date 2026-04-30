// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/components/theme/ThemedCard.kt
package br.com.tlmacedo.meuponto.presentation.components.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import br.com.tlmacedo.meuponto.presentation.theme.LocalAppThemeController
import br.com.tlmacedo.meuponto.presentation.theme.LocalPremiumTokens

@Composable
fun ThemedCard(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val theme = LocalAppThemeController.current
    val premium = LocalPremiumTokens.current
    val shape = RoundedCornerShape(if (theme.isPremium) 28.dp else 20.dp)

    when {
        theme.isPremium -> {
            Box(
                modifier = modifier
                    .shadow(
                        elevation = 18.dp,
                        shape = shape,
                        ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.22f),
                        spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.28f)
                    )
                    .background(premium.cardBrush, shape)
                    .border(1.dp, premium.cardBorder, shape),
                content = content
            )
        }

        theme.isDarkula -> {
            Box(
                modifier = modifier
                    .background(MaterialTheme.colorScheme.surface, shape)
                    .border(1.dp, MaterialTheme.colorScheme.outline, shape),
                content = content
            )
        }

        else -> {
            Card(
                modifier = modifier,
                shape = shape,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Box(content = content)
            }
        }
    }
}