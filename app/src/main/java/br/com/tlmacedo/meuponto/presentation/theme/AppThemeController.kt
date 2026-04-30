// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/theme/AppThemeController.kt
package br.com.tlmacedo.meuponto.presentation.theme

import androidx.compose.runtime.staticCompositionLocalOf

data class AppThemeController(
    val themeKey: String,
    val isPremium: Boolean,
    val isDarkula: Boolean
)

val LocalAppThemeController = staticCompositionLocalOf {
    AppThemeController(
        themeKey = "system",
        isPremium = false,
        isDarkula = false
    )
}