// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/theme/PremiumThemeTokens.kt
package br.com.tlmacedo.meuponto.presentation.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

data class PremiumThemeTokens(
    val backgroundBrush: Brush,
    val cardBrush: Brush,
    val cardBorder: Color,
    val elevatedCardBrush: Brush,
    val iconContainerBrush: Brush,
    val primaryGlow: Color,
    val successGlow: Color,
    val dangerGlow: Color,
    val warningGlow: Color,
    val purpleGlow: Color
)

val DarkPremiumTokens = PremiumThemeTokens(
    backgroundBrush = Brush.radialGradient(
        colors = listOf(
            Color(0xFF102A55),
            Color(0xFF06162E),
            Color(0xFF020814)
        ),
        radius = 1200f
    ),
    cardBrush = Brush.linearGradient(
        colors = listOf(
            Color(0x99204273),
            Color(0x6610223F),
            Color(0x99081224)
        )
    ),
    elevatedCardBrush = Brush.linearGradient(
        colors = listOf(
            Color(0xAA173863),
            Color(0x8810223F)
        )
    ),
    iconContainerBrush = Brush.radialGradient(
        colors = listOf(
            Color(0x663B82F6),
            Color(0x221E40AF)
        )
    ),
    cardBorder = Color(0x664F7DCC),
    primaryGlow = Color(0xFF3B82F6),
    successGlow = Color(0xFF22C55E),
    dangerGlow = Color(0xFFFF4D67),
    warningGlow = Color(0xFFF59E0B),
    purpleGlow = Color(0xFFA855F7)
)

val LightPremiumTokens = PremiumThemeTokens(
    backgroundBrush = Brush.radialGradient(
        colors = listOf(
            Color(0xFFFFFFFF),
            Color(0xFFEAF3FF),
            Color(0xFFDCEBFF)
        ),
        radius = 1200f
    ),
    cardBrush = Brush.linearGradient(
        colors = listOf(
            Color(0xFFFFFFFF),
            Color(0xFFF4F8FF),
            Color(0xFFE6F0FF)
        )
    ),
    elevatedCardBrush = Brush.linearGradient(
        colors = listOf(
            Color(0xFFFFFFFF),
            Color(0xFFEAF2FF)
        )
    ),
    iconContainerBrush = Brush.radialGradient(
        colors = listOf(
            Color(0x553B82F6),
            Color(0x11FFFFFF)
        )
    ),
    cardBorder = Color(0x558FB4FF),
    primaryGlow = Color(0xFF2563EB),
    successGlow = Color(0xFF16A34A),
    dangerGlow = Color(0xFFE11D48),
    warningGlow = Color(0xFFD97706),
    purpleGlow = Color(0xFF9333EA)
)

val LocalPremiumTokens = staticCompositionLocalOf {
    DarkPremiumTokens
}