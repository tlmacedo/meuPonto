// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/components/FechamentoCicloBanner.kt
package br.com.tlmacedo.meuponto.presentation.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.EventRepeat
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import br.com.tlmacedo.meuponto.domain.model.FechamentoPeriodo
import java.time.format.DateTimeFormatter
import java.util.Locale

private val localeBR = Locale("pt", "BR")
private val formatterData = DateTimeFormatter.ofPattern("dd/MM/yyyy", localeBR)
private val formatterDataCurta = DateTimeFormatter.ofPattern("dd/MM", localeBR)

/**
 * Banner que exibe informações sobre o fechamento de um ciclo de banco de horas anterior.
 * 
 * Exibido quando o usuário navega para o primeiro dia de um novo ciclo,
 * mostrando que o ciclo anterior foi fechado e o saldo foi zerado.
 *
 * @param fechamento Dados do fechamento do ciclo anterior
 * @param modifier Modifier opcional
 *
 * @author Thiago
 * @since 6.4.0
 */
@Composable
fun FechamentoCicloBanner(
    fechamento: FechamentoPeriodo,
    modifier: Modifier = Modifier
) {
    val backgroundColor = Color(0xFFE8EAF6) // Indigo claro
    val contentColor = Color(0xFF3949AB)    // Indigo escuro

    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Header: Ícone + Título
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.EventRepeat,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Início de Novo Ciclo",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = contentColor
                    )
                    Text(
                        text = "Ciclo anterior encerrado",
                        style = MaterialTheme.typography.bodySmall,
                        color = contentColor.copy(alpha = 0.7f)
                    )
                }

                // Badge com saldo zerado
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = contentColor.copy(alpha = 0.15f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Autorenew,
                            contentDescription = null,
                            tint = contentColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Saldo zerado",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = contentColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Informações do ciclo fechado
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Período do ciclo fechado
                InfoChipFechamento(
                    label = "Período",
                    value = "${fechamento.dataInicioPeriodo.format(formatterDataCurta)} a ${fechamento.dataFimPeriodo.format(formatterDataCurta)}",
                    contentColor = contentColor,
                    modifier = Modifier.weight(1f)
                )

                // Saldo que foi ajustado
                InfoChipFechamento(
                    label = "Ajuste",
                    value = fechamento.saldoAnteriorFormatado,
                    contentColor = contentColor,
                    isHighlight = fechamento.saldoAnteriorMinutos != 0
                )
            }

            // Observação (se houver)
            fechamento.observacao?.let { obs ->
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = null,
                        tint = contentColor.copy(alpha = 0.6f),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = obs,
                        style = MaterialTheme.typography.bodySmall,
                        color = contentColor.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

/**
 * Chip de informação para o banner de fechamento.
 */
@Composable
private fun InfoChipFechamento(
    label: String,
    value: String,
    contentColor: Color,
    modifier: Modifier = Modifier,
    isHighlight: Boolean = false
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isHighlight) contentColor.copy(alpha = 0.12f) else contentColor.copy(alpha = 0.08f),
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = contentColor.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isHighlight) FontWeight.Bold else FontWeight.Medium,
                color = contentColor,
                textAlign = TextAlign.Center
            )
        }
    }
}
