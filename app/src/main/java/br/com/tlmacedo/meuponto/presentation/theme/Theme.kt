// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/theme/Theme.kt
package br.com.tlmacedo.meuponto.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val SidiaLightColorScheme = lightColorScheme(
    primary = SidiaBlue,
    onPrimary = Color.White,
    primaryContainer = SidiaBlue.copy(alpha = 0.1f),
    onPrimaryContainer = SidiaBlue,
    secondary = SidiaGreen,
    onSecondary = Color.White,
    secondaryContainer = SidiaGreen.copy(alpha = 0.1f),
    onSecondaryContainer = SidiaGreen,
    tertiary = SidiaMediumGray,
    onTertiary = Color.White,
    background = SidiaLightGray,
    onBackground = SidiaNavy,
    surface = SidiaLightGray,
    onSurface = SidiaNavy,
    surfaceVariant = Color.White,
    onSurfaceVariant = SidiaMediumGray,
    error = Error,
    onError = OnError,
    outline = SidiaMediumGray
)

private val SidiaDarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = Color.White,
    primaryContainer = DarkPrimary.copy(alpha = 0.2f),
    onPrimaryContainer = DarkPrimary,
    secondary = DarkSecondary,
    onSecondary = Color.Black,
    secondaryContainer = DarkSecondary.copy(alpha = 0.2f),
    onSecondaryContainer = DarkSecondary,
    tertiary = DarkOnSurfaceVariant,
    onTertiary = Color.Black,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    error = DarkError,
    onError = Color.White,
    outline = DarkOnSurfaceVariant
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
    dynamicColor: Boolean = true, // Ativado por padrão se disponível
    temaForcado: String = "system",
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = when (temaForcado) {
        "light" -> SidiaLightColorScheme
        "dark" -> SidiaDarkColorScheme
        "sidia" -> SidiaLightColorScheme
        "sidia_dark" -> SidiaDarkColorScheme
        "system" -> {
            if (dynamicColor) {
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            } else {
                if (darkTheme) SidiaDarkColorScheme else SidiaLightColorScheme
            }
        }

        else -> if (darkTheme) SidiaDarkColorScheme else SidiaLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
