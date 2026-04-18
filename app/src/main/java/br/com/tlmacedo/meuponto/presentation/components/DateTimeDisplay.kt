// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/components/DateTimeDisplay.kt
package br.com.tlmacedo.meuponto.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Componente que exibe a data e hora atual formatadas.
 *
 * Mostra a data por extenso (ex: "Quinta-feira, 12 de Fevereiro")
 * e a hora em formato grande (ex: "15:30:45").
 *
 * @param dataHora Data e hora a serem exibidas
 * @param modifier Modificador opcional para customização do layout
 *
 * @author Thiago
 * @since 1.0.0
 */
@Composable
fun DateTimeDisplay(
    dataHora: LocalDateTime,
    modifier: Modifier = Modifier
) {
    // Formatadores
    val formatadorData = DateTimeFormatter.ofPattern(
        "EEEE, dd 'de' MMMM",
        Locale.forLanguageTag("pt-BR")
    )
    val formatadorHora = DateTimeFormatter.ofPattern("HH:mm:ss")

    // Formata data com primeira letra maiúscula
    val dataFormatada = dataHora.format(formatadorData)
        .replaceFirstChar { it.uppercase() }
    val horaFormatada = dataHora.format(formatadorHora)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth()
    ) {
        // Data por extenso
        Text(
            text = dataFormatada,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        // Hora grande
        Text(
            text = horaFormatada,
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
