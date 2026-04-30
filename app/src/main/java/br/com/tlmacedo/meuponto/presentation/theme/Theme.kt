package br.com.tlmacedo.meuponto.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val SidiaLightColorScheme = lightColorScheme(
    primary = SidiaBlue,
    onPrimary = Color.White,
    primaryContainer = SidiaBlue.copy(alpha = 0.08f),
    onPrimaryContainer = SidiaDarkBlue,
    secondary = SidiaGreen,
    onSecondary = Color.White,
    secondaryContainer = SidiaGreen.copy(alpha = 0.1f),
    onSecondaryContainer = SidiaDarkGreen,
    tertiary = SidiaMediumGray,
    onTertiary = Color.White,
    background = Color(0xFFF8FAFF),
    onBackground = SidiaNavy,
    surface = Color.White,
    onSurface = SidiaNavy,
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = SidiaMediumGray,
    error = Error,
    onError = OnError,
    outline = SidiaMediumGray.copy(alpha = 0.5f),
    outlineVariant = SidiaMediumGray.copy(alpha = 0.2f)
)

private val SidiaDarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = Color.White,
    primaryContainer = DarkPrimary.copy(alpha = 0.15f),
    onPrimaryContainer = DarkPrimary,
    secondary = DarkSecondary,
    onSecondary = Color.Black,
    secondaryContainer = DarkSecondary.copy(alpha = 0.15f),
    onSecondaryContainer = DarkSecondary,
    tertiary = DarkOnSurfaceVariant,
    onTertiary = Color.Black,
    background = Color(0xFF020617),
    onBackground = Color(0xFFF1F5F9),
    surface = Color(0xFF0B1120),
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF1E293B),
    onSurfaceVariant = Color(0xFF94A3B8),
    error = DarkError,
    onError = Color.White,
    outline = Color(0xFF334155),
    outlineVariant = Color(0xFF1E293B)
)

private val SidiaPremiumLightColorScheme = lightColorScheme(
    primary = Color(0xFF2563EB),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDCEAFF),
    onPrimaryContainer = Color(0xFF082F66),

    secondary = Color(0xFF16A34A),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDCFCE7),
    onSecondaryContainer = Color(0xFF052E16),

    tertiary = Color(0xFF9333EA),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFF3E8FF),
    onTertiaryContainer = Color(0xFF3B0764),

    background = Color(0xFFEAF3FF),
    onBackground = Color(0xFF0F172A),

    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF0F172A),

    surfaceVariant = Color(0xFFE2ECFF),
    onSurfaceVariant = Color(0xFF475569),

    outline = Color(0xFF7EA7E8),
    outlineVariant = Color(0x663B82F6),

    error = Color(0xFFE11D48),
    onError = Color.White
)

private val SidiaPremiumDarkColorScheme = darkColorScheme(
    primary = Color(0xFF3B82F6),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF0F2F66),
    onPrimaryContainer = Color(0xFFDCEAFF),

    secondary = Color(0xFF22C55E),
    onSecondary = Color(0xFF052E16),
    secondaryContainer = Color(0xFF064E3B),
    onSecondaryContainer = Color(0xFFD1FAE5),

    tertiary = Color(0xFFA855F7),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF4C1D95),
    onTertiaryContainer = Color(0xFFF3E8FF),

    background = Color(0xFF020814),
    onBackground = Color(0xFFEAF2FF),

    surface = Color(0xFF0B1A33),
    onSurface = Color(0xFFF8FAFC),

    surfaceVariant = Color(0xFF102A55),
    onSurfaceVariant = Color(0xFFB6C8E6),

    outline = Color(0xFF4F7DCC),
    outlineVariant = Color(0x663B82F6),

    error = Color(0xFFFF4D67),
    onError = Color.White
)

/**
 * Darkula inspirado em IDEs JetBrains:
 * fundo cinza escuro, texto azul acinzentado, amarelo como destaque,
 * verde para sucesso e laranja para ações secundárias.
 */
private val DarkulaColorScheme = darkColorScheme(
    primary = Color(0xFFFFC66D),
    onPrimary = Color(0xFF2B2B2B),
    primaryContainer = Color(0xFF4A3A24),
    onPrimaryContainer = Color(0xFFFFE3B0),

    secondary = Color(0xFF6A8759),
    onSecondary = Color(0xFF10140E),
    secondaryContainer = Color(0xFF34462C),
    onSecondaryContainer = Color(0xFFD6EAC8),

    tertiary = Color(0xFFCC7832),
    onTertiary = Color(0xFF261000),
    tertiaryContainer = Color(0xFF4B2A0F),
    onTertiaryContainer = Color(0xFFFFD2A6),

    background = Color(0xFF2B2B2B),
    onBackground = Color(0xFFA9B7C6),

    surface = Color(0xFF313335),
    onSurface = Color(0xFFA9B7C6),

    surfaceVariant = Color(0xFF3C3F41),
    onSurfaceVariant = Color(0xFFBBBBBB),

    error = Color(0xFFBC3F3C),
    onError = Color.White,

    outline = Color(0xFF606366),
    outlineVariant = Color(0xFF4E5254),

    inverseSurface = Color(0xFFA9B7C6),
    inverseOnSurface = Color(0xFF2B2B2B),
    inversePrimary = Color(0xFF805500)
)

private val AppTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    titleSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp
    )
)

@Composable
fun MeuPontoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    temaForcado: String = "system",
    content: @Composable () -> Unit
) {
    val context = LocalContext.current

    val colorScheme = when (temaForcado) {
        "light" -> SidiaLightColorScheme
        "dark" -> SidiaDarkColorScheme

        "light_premium" -> SidiaPremiumLightColorScheme
        "dark_premium" -> SidiaPremiumDarkColorScheme
        "darkula" -> DarkulaColorScheme

        // Compatibilidade com temas antigos
        "sidia" -> SidiaLightColorScheme
        "sidia_dark" -> SidiaDarkColorScheme
        "sidia_premium_dark" -> SidiaPremiumDarkColorScheme

        "system" -> {
            if (dynamicColor) {
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            } else {
                if (darkTheme) SidiaDarkColorScheme else SidiaLightColorScheme
            }
        }

        else -> {
            if (darkTheme) SidiaDarkColorScheme else SidiaLightColorScheme
        }
    }

    val premiumTokens = when (temaForcado) {
        "light_premium" -> LightPremiumTokens
        "dark_premium" -> DarkPremiumTokens
        else -> if (darkTheme) DarkPremiumTokens else LightPremiumTokens
    }

    val themeController = AppThemeController(
        themeKey = temaForcado,
        isPremium = temaForcado == "light_premium" || temaForcado == "dark_premium",
        isDarkula = temaForcado == "darkula"
    )

    CompositionLocalProvider(
        LocalPremiumTokens provides premiumTokens,
        LocalAppThemeController provides themeController
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            content = content
        )
    }
}