// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/components/IntervaloCard.kt
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import br.com.tlmacedo.meuponto.domain.model.IntervaloPonto
import br.com.tlmacedo.meuponto.presentation.theme.EntradaBg
import br.com.tlmacedo.meuponto.presentation.theme.EntradaColor
import br.com.tlmacedo.meuponto.presentation.theme.SaidaBg
import br.com.tlmacedo.meuponto.presentation.theme.SaidaColor
import br.com.tlmacedo.meuponto.presentation.theme.Success
import br.com.tlmacedo.meuponto.presentation.theme.Warning
import br.com.tlmacedo.meuponto.presentation.theme.WarningLight
import java.time.format.DateTimeFormatter

/**
 * Card que exibe um intervalo de trabalho (entrada -> saída).
 *
 * Mostra visualmente o período trabalhado com entrada, saída
 * e duração do turno de forma clara e intuitiva.
 * Quando o intervalo está aberto, exibe um contador em tempo real.
 * Exibe também o tempo de intervalo/pausa antes do turno (quando houver),
 * incluindo o tempo real e o tempo considerado (com tolerância).
 *
 * @param intervalo Intervalo a ser exibido
 * @param mostrarContadorTempoReal Se deve exibir contador em tempo real para intervalos abertos
 * @param modifier Modificador opcional
 *
 * @author Thiago
 * @since 1.0.0
 * @updated 2.4.0 - Adicionada exibição do intervalo/pausa antes do turno
 * @updated 2.8.0 - Exibição de tolerância de intervalo (real vs considerado)
 * @updated 2.9.0 - Exibição de hora de entrada considerada e ícones diferenciados
 */
@Composable
fun IntervaloCard(
    intervalo: IntervaloPonto,
    mostrarContadorTempoReal: Boolean = true,
    modifier: Modifier = Modifier
) {
    val formatadorHora = DateTimeFormatter.ofPattern("HH:mm")

    Column(modifier = modifier.fillMaxWidth()) {
        // Exibe o intervalo/pausa ANTES do card (se houver)
        if (intervalo.temPausaAntes) {
            PausaEntreIntervalos(
                textoReal = intervalo.formatarPausaAntesCompacta() ?: "",
                textoConsiderado = if (intervalo.toleranciaAplicada) {
                    intervalo.formatarPausaConsideradaCompacta()
                } else null,
                isAlmoco = intervalo.isIntervaloAlmoco
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Card principal do turno
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Linha de Entrada
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(EntradaBg)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Login,
                            contentDescription = "Entrada",
                            tint = EntradaColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Entrada",
                            style = MaterialTheme.typography.labelMedium,
                            color = EntradaColor
                        )

                        // Exibir hora real e hora considerada quando há tolerância
                        if (intervalo.temHoraEntradaConsiderada) {
                            // Hora real (cortada/riscada)
                            Text(
                                text = intervalo.entrada.hora.format(formatadorHora),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Normal,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                textDecoration = TextDecoration.LineThrough
                            )
                            // Hora considerada (destaque)
                            Text(
                                text = intervalo.horaEntradaConsiderada!!.toLocalTime().format(formatadorHora),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = EntradaColor
                            )
                        } else {
                            // Hora normal (sem tolerância)
                            Text(
                                text = intervalo.entrada.hora.format(formatadorHora),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Contador em tempo real para intervalos abertos
                    if (intervalo.aberto && mostrarContadorTempoReal) {
                        LiveCounterCompact(
                            dataHoraInicio = intervalo.entrada.dataHora
                        )
                    }
                }

                // Linha de conexão com duração do TURNO
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(2.dp)
                            .height(24.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant)
                    )
                    Spacer(modifier = Modifier.width(16.dp))

                    if (intervalo.aberto && mostrarContadorTempoReal) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                tint = Warning,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Em andamento",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Medium,
                                color = Warning
                            )
                        }
                    } else {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = if (intervalo.aberto) Warning else Success,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = intervalo.formatarDuracao(),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium,
                            color = if (intervalo.aberto) Warning else Success
                        )
                    }
                }

                // Linha de Saída
                if (intervalo.saida != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(SaidaBg)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Logout,
                                contentDescription = "Saída",
                                tint = SaidaColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Saída",
                                style = MaterialTheme.typography.labelMedium,
                                color = SaidaColor
                            )
                            Text(
                                text = intervalo.saida.hora.format(formatadorHora),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(WarningLight)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = "Aguardando saída",
                                tint = Warning,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Saída",
                                style = MaterialTheme.typography.labelMedium,
                                color = Warning
                            )
                            Text(
                                text = "Aguardando...",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium,
                                color = Warning
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Componente que exibe o tempo de pausa/intervalo entre turnos.
 *
 * Layout:
 * - Linha 1: Tempo real (se diferente do considerado, exibe menor/cortado)
 * - Linha 2: Ícone + Tempo considerado (principal)
 *
 * Ícones:
 * - 🍽️ Restaurant: Intervalo >= intervalo mínimo (almoço)
 * - ☕ Coffee: Intervalo < intervalo mínimo (café/pausa rápida)
 *
 * @param textoReal Texto do intervalo real (compacto, ex: "01h 18m")
 * @param textoConsiderado Texto do intervalo considerado (null se não houver tolerância)
 * @param isAlmoco Se é intervalo de almoço (para escolher ícone)
 */
@Composable
private fun PausaEntreIntervalos(
    textoReal: String,
    textoConsiderado: String? = null,
    isAlmoco: Boolean = true,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.outlineVariant
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            // Linha do intervalo real (se houver tolerância, mostra cortado)
            if (textoConsiderado != null) {
                Text(
                    text = "Intervalo de $textoReal",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    textDecoration = TextDecoration.LineThrough
                )
            }

            // Linha principal com ícone e tempo considerado (ou real se não houver tolerância)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = if (isAlmoco) Icons.Default.Restaurant else Icons.Default.Coffee,
                    contentDescription = if (isAlmoco) "Intervalo de almoço" else "Intervalo de café",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "Intervalo de ${textoConsiderado ?: textoReal}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.outlineVariant
        )
    }
}
