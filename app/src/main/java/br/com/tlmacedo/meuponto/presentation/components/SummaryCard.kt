// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/components/SummaryCard.kt
package br.com.tlmacedo.meuponto.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.com.tlmacedo.meuponto.domain.model.SaldoHoras
import br.com.tlmacedo.meuponto.domain.model.StatusDia

/**
 * Card que exibe o resumo do dia de trabalho.
 *
 * Mostra informações como horas trabalhadas, saldo do dia
 * e status de consistência dos registros.
 *
 * @param horasTrabalhadas Total de minutos trabalhados no dia
 * @param saldo Saldo de horas do dia (pode ser null se não calculável)
 * @param statusDia Status de consistência dos registros
 * @param modifier Modificador opcional para customização do layout
 *
 * @author Thiago
 * @since 1.0.0
 */
@Composable
fun SummaryCard(
    horasTrabalhadas: Int?,
    saldo: SaldoHoras?,
    statusDia: StatusDia,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Título
            Text(
                text = "Resumo do Dia",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
            ) {
                // Horas trabalhadas
                SummaryItem(
                    label = "Trabalhado no dia",
                    value = horasTrabalhadas?.let { formatarMinutos(it) } ?: "--:--"
                )

                // Saldo
                SummaryItem(
                    label = "Saldo do dia",
                    value = saldo?.formatado ?: "--:--",
                    valueColor = when {
                        saldo == null -> MaterialTheme.colorScheme.onSecondaryContainer
                        saldo.isPositivo -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.error
                    }
                )

                // Status
                SummaryItem(
                    label = "Status",
                    value = statusDia.descricao,
                    valueColor = if (statusDia.isConsistente) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    }
                )
            }
        }
    }
}

/**
 * Item individual do resumo.
 *
 * @param label Rótulo descritivo
 * @param value Valor a ser exibido
 * @param valueColor Cor do valor (opcional)
 */
@Composable
private fun SummaryItem(
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSecondaryContainer
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
    }
}

/**
 * Formata minutos para o padrão HH:mm.
 *
 * @param minutos Total de minutos
 * @return String formatada
 */
private fun formatarMinutos(minutos: Int): String {
    val horas = minutos / 60
    val mins = minutos % 60
    return String.format("%02d:%02d", horas, mins)
}
