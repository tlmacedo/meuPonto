// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/theme/AppTheme.kt
package br.com.tlmacedo.meuponto.presentation.theme

enum class AppTheme(
    val key: String,
    val titulo: String,
    val subtitulo: String,
    val isPremium: Boolean = false
) {
    LIGHT(
        key = "light",
        titulo = "Claro",
        subtitulo = "Tema claro padrão"
    ),

    DARK(
        key = "dark",
        titulo = "Escuro",
        subtitulo = "Tema escuro padrão"
    ),

    LIGHT_PREMIUM(
        key = "light_premium",
        titulo = "Claro Premium",
        subtitulo = "Visual claro refinado, elegante e com mais contraste",
        isPremium = true
    ),

    DARK_PREMIUM(
        key = "dark_premium",
        titulo = "Escuro Premium",
        subtitulo = "Visual escuro profundo, moderno e confortável",
        isPremium = true
    ),

    DARKULA(
        key = "darkula",
        titulo = "Darkula",
        subtitulo = "Tema inspirado em IDEs: escuro, técnico e confortável"
    ),

    SYSTEM(
        key = "system",
        titulo = "Sistema",
        subtitulo = "Segue o tema do Android"
    );

    companion object {
        val todos get() = entries

        fun fromKey(key: String?): AppTheme {
            return when (key) {

                // 🔁 Compatibilidade com versões antigas
                "sidia" -> LIGHT
                "sidia_dark" -> DARK
                "sidia_premium_dark" -> DARK_PREMIUM

                // ✅ Novos temas
                else -> entries.firstOrNull { it.key == key } ?: SYSTEM
            }
        }
    }
}