// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/components/ResumoCard.kt
package br.com.tlmacedo.meuponto.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.com.tlmacedo.meuponto.domain.model.BancoHoras
import br.com.tlmacedo.meuponto.domain.model.ResumoDia
import br.com.tlmacedo.meuponto.presentation.theme.Error
import br.com.tlmacedo.meuponto.presentation.theme.ErrorLight
import br.com.tlmacedo.meuponto.presentation.theme.Info
import br.com.tlmacedo.meuponto.presentation.theme.InfoLight
import br.com.tlmacedo.meuponto.presentation.theme.Success
import br.com.tlmacedo.meuponto.presentation.theme.SuccessLight
import java.time.Duration
import kotlin.math.abs

/**
 * Card de resumo do dia com horas trabalhadas, saldo e banco de horas.
 *
 * Apresenta as informações principais de forma visual e intuitiva,
 * com indicadores coloridos para facilitar a compreensão.
 *
 * @param resumoDia Resumo do dia atual
 * @param bancoHoras Banco de horas acumulado
 * @param modifier Modificador opcional
 *
 * @author Thiago
 * @since 1.0.0
 */
@Composable
fun ResumoCard(
    resumoDia: ResumoDia,
    bancoHoras: BancoHoras,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(20.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Resumo do Dia",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Horas Trabalhadas
                ResumoItem(
                    icone = Icons.Default.AccessTime,
                    titulo = "Trabalhado",
                    valor = formatarDuracao(resumoDia.horasTrabalhadas),
                    corIcone = Info,
                    corFundo = InfoLight,
                    modifier = Modifier.weight(1f)
                )

                // Saldo do Dia - usando ícones AutoMirrored
                ResumoItem(
                    icone = if (resumoDia.temSaldoPositivo) {
                        Icons.AutoMirrored.Filled.TrendingUp
                    } else {
                        Icons.AutoMirrored.Filled.TrendingDown
                    },
                    titulo = "Saldo Dia",
                    valor = formatarSaldo(resumoDia.saldoDia),
                    corIcone = if (resumoDia.temSaldoNegativo) Error else Success,
                    corFundo = if (resumoDia.temSaldoNegativo) ErrorLight else SuccessLight,
                    modifier = Modifier.weight(1f)
                )

                // Banco de Horas
                ResumoItem(
                    icone = Icons.Default.AccountBalance,
                    titulo = "Banco",
                    valor = bancoHoras.formatarSaldo(),
                    corIcone = if (bancoHoras.negativo) Error else Success,
                    corFundo = if (bancoHoras.negativo) ErrorLight else SuccessLight,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * Item individual do resumo.
 */
@Composable
private fun ResumoItem(
    icone: ImageVector,
    titulo: String,
    valor: String,
    corIcone: androidx.compose.ui.graphics.Color,
    corFundo: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(4.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(corFundo)
        ) {
            Icon(
                imageVector = icone,
                contentDescription = titulo,
                tint = corIcone,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = titulo,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
        )
        Text(
            text = valor,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

/**
 * Formata uma duração para exibição (compatível com API 26+).
 */
private fun formatarDuracao(duracao: Duration): String {
    val totalMinutos = duracao.toMinutes()
    val horas = totalMinutos / 60
    val minutos = abs(totalMinutos % 60)
    return "${horas}h${minutos.toString().padStart(2, '0')}"
}

/**
 * Formata o saldo com sinal (compatível com API 26+).
 */
private fun formatarSaldo(duracao: Duration): String {
    val totalMinutos = duracao.toMinutes()
    val horas = abs(totalMinutos / 60)
    val minutos = abs(totalMinutos % 60)
    val sinal = when {
        totalMinutos > 0 -> "+"
        totalMinutos < 0 -> "-"
        else -> ""
    }
    return "$sinal${horas}h${minutos.toString().padStart(2, '0')}"
}
