// Arquivo: br/com/tlmacedo/meuponto/presentation/components/theme/ThemedBackground.kt
package br.com.tlmacedo.meuponto.presentation.components.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import br.com.tlmacedo.meuponto.presentation.theme.LocalAppThemeController
import br.com.tlmacedo.meuponto.presentation.theme.LocalPremiumTokens

@Composable
fun ThemedBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val theme = LocalAppThemeController.current
    val premium = LocalPremiumTokens.current

    Box(
        modifier = if (theme.isPremium) {
            modifier.background(premium.backgroundBrush)
        } else {
            modifier.background(MaterialTheme.colorScheme.background)
        },
        content = content
    )
}