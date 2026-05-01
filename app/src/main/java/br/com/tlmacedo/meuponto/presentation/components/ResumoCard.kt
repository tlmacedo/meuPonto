// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/components/ResumoCard.kt
package br.com.tlmacedo.meuponto.presentation.components

import br.com.tlmacedo.meuponto.util.helper.formatarComoHoraMinuto
import br.com.tlmacedo.meuponto.util.helper.formatarComoSaldoHoraMinuto
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingFlat
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import br.com.tlmacedo.meuponto.domain.extensions.isNormalOrTrue
import br.com.tlmacedo.meuponto.domain.model.BancoHoras
import br.com.tlmacedo.meuponto.domain.model.ResumoDia
import br.com.tlmacedo.meuponto.domain.model.VersaoJornada
import br.com.tlmacedo.meuponto.presentation.components.theme.ThemedCard
import br.com.tlmacedo.meuponto.presentation.theme.LocalAppThemeController
import br.com.tlmacedo.meuponto.presentation.theme.LocalPremiumTokens
import br.com.tlmacedo.meuponto.util.helper.minutosParaSaldoFormatado
import java.time.LocalDateTime
import java.time.LocalTime
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun ResumoCard(
    resumoDia: ResumoDia,
    bancoHoras: BancoHoras,
    horaAtual: LocalTime = LocalTime.now(),
    versaoJornada: VersaoJornada? = null,
    dataHoraInicioContador: LocalDateTime? = null,
    mostrarContador: Boolean = false,
    onEditarJornada: (() -> Unit)? = null,
    modifier: Modifier = Modifier,

    /**
     * Valores vindos do ResumoDiaCompleto.
     *
     * Usados principalmente quando existe declaração/abono.
     */
    horasTrabalhadasCalculadasMinutos: Int? = null,
    tempoAbonadoMinutos: Int = 0,
    saldoDiaCalculadoMinutos: Int? = null
) {

    val theme = LocalAppThemeController.current

    /**
     * Compatibilidade com o novo ResumoDia:
     * por enquanto usamos os valores já calculados no domínio.
     *
     * Depois podemos recriar cálculo "em andamento" em um use case separado.
     */

    val minutosTrabalhados = horasTrabalhadasCalculadasMinutos
        ?: resumoDia.horasTrabalhadasComAndamentoMinutos

    val temAbono = tempoAbonadoMinutos > 0
    val minutosComputados = minutosTrabalhados + tempoAbonadoMinutos

    val minutosTempoPrincipal = if (temAbono) {
        minutosComputados
    } else {
        minutosTrabalhados
    }

    val saldoDiaMinutos = saldoDiaCalculadoMinutos
        ?: resumoDia.saldoDiaComAndamentoMinutos

    /**
     * Se veio saldo calculado do ResumoDiaCompleto, o banco já deve estar correto.
     * Mantemos o comportamento antigo apenas quando não houver saldo calculado externo.
     */
    val bancoTotalMinutos = if (saldoDiaCalculadoMinutos != null) {
        bancoHoras.saldoTotalMinutos
    } else {
        bancoHoras.saldoTotalMinutos + saldoDiaMinutos - resumoDia.saldoDiaMinutos
    }

    val jornadaMinutos = resumoDia.cargaHorariaEfetivaMinutos

    val progressoTrabalhado = if (jornadaMinutos > 0) {
        (minutosTempoPrincipal.toFloat() / jornadaMinutos.toFloat()).coerceIn(0f, 1.15f)
    } else {
        0f
    }

    val status = resumoDiaStatus(resumoDia)

    ThemedCard(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onEditarJornada != null) {
                    Modifier.clickable { onEditarJornada() }
                } else {
                    Modifier
                }
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(if (theme.isPremium) 18.dp else 16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            ResumoHeader(
                resumoDia = resumoDia,
                status = status,
                jornadaMinutos = jornadaMinutos,
                onEditarJornada = onEditarJornada
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Top
            ) {
                ResumoMetricItem(
                    modifier = Modifier.weight(1f),
                    title = if (temAbono) {
                        "Computado no dia"
                    } else {
                        status.tituloTrabalhado
                    },
                    value = if (temAbono) {
                        minutosTempoPrincipal.formatarComoHoraMinuto()
                    } else {
                        status.valorTrabalhado ?: minutosTrabalhados.formatarComoHoraMinuto()
                    },
                    subtitle = if (temAbono) {
                        "${minutosTrabalhados.formatarComoHoraMinuto()} trabalhado + ${tempoAbonadoMinutos.formatarComoHoraMinuto()} abonado"
                    } else if (jornadaMinutos > 0) {
                        "${(progressoTrabalhado * 100f).roundToInt()}% da meta"
                    } else {
                        status.subtituloTrabalhado
                    },
                    icon = status.iconeTrabalhado,
                    color = status.corTrabalhado,
                    progress = if (jornadaMinutos > 0) {
                        progressoTrabalhado.coerceIn(0f, 1f)
                    } else {
                        null
                    }
                )

                ResumoMetricItem(
                    modifier = Modifier.weight(1f),
                    title = "Saldo do dia",
                    value = saldoDiaMinutos.formatarComoSaldoHoraMinuto(),
                    subtitle = when {
                        saldoDiaMinutos > 0 -> "Positivo"
                        saldoDiaMinutos < 0 -> "Negativo"
                        else -> "Neutro"
                    },
                    icon = when {
                        saldoDiaMinutos > 0 -> Icons.Default.TrendingUp
                        saldoDiaMinutos < 0 -> Icons.Default.TrendingDown
                        else -> Icons.Default.TrendingFlat
                    },
                    color = when {
                        saldoDiaMinutos > 0 -> Color(0xFF22C55E)
                        saldoDiaMinutos < 0 -> Color(0xFFFF4D67)
                        else -> MaterialTheme.colorScheme.primary
                    },
                    progress = saldoProgress(saldoDiaMinutos)
                )

                ResumoMetricItem(
                    modifier = Modifier.weight(1f),
                    title = "Banco de horas",
                    value = bancoTotalMinutos.formatarComoSaldoHoraMinuto(),
                    subtitle = "Acumulado",
                    icon = Icons.Default.AccountBalance,
                    color = when {
                        bancoTotalMinutos > 0 -> Color(0xFFA855F7)
                        bancoTotalMinutos < 0 -> Color(0xFFFF4D67)
                        else -> MaterialTheme.colorScheme.primary
                    },
                    progress = saldoProgress(bancoTotalMinutos)
                )
            }

            if (mostrarContador || versaoJornada != null) {
                ResumoFooter(
                    jornadaMinutos = jornadaMinutos,
                    mostrarContador = mostrarContador,
                    dataHoraInicioContador = dataHoraInicioContador
                )
            }
        }
    }
}

@Composable
private fun ResumoHeader(
    resumoDia: ResumoDia,
    status: ResumoVisualStatus,
    jornadaMinutos: Int,
    onEditarJornada: (() -> Unit)?
) {
    val textPrimary = MaterialTheme.colorScheme.onSurface
    val textSecondary = MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
//                PremiumGlowIcon(
//                    icon = Icons.Default.Timer,
//                    color = MaterialTheme.colorScheme.primary,
//                    size = 34,
//                    iconSize = 18
//                )
//
//                Spacer(Modifier.width(8.dp))

                Text(
                    text = "Resumo do Dia",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = status.iconeStatus,
                    contentDescription = null,
                    tint = status.corStatus,
                    modifier = Modifier.size(16.dp)
                )

                Spacer(Modifier.width(4.dp))

                Text(
                    text = if (resumoDia.tipoAusencia.isNormalOrTrue) {
                        "Jornada esperada • ${jornadaMinutos.formatarComoHoraMinuto()}"
                    } else {
                        "${resumoDia.descricaoTipoDia} • Sem jornada obrigatória"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = textSecondary,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        if (onEditarJornada != null) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Editar jornada",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .padding(top = 4.dp)
                    .size(22.dp)
            )
        }
    }
}

@Composable
private fun ResumoMetricItem(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    subtitle: String?,
    icon: ImageVector,
    color: Color,
    progress: Float?
) {
    val theme = LocalAppThemeController.current

    Column(
        modifier = modifier.defaultMinSize(minHeight = if (theme.isPremium) 144.dp else 128.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        PremiumGlowIcon(
            icon = icon,
            color = color,
            size = if (theme.isPremium) 62 else 54,
            iconSize = if (theme.isPremium) 30 else 26
        )

        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            maxLines = 2,
            fontWeight = FontWeight.SemiBold
        )

        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = color,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1
        )

        if (progress != null) {
            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth(0.78f)
                    .height(5.dp),
                color = color,
                trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
            )
        }

        if (!subtitle.isNullOrBlank()) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 1,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun PremiumGlowIcon(
    icon: ImageVector,
    color: Color,
    size: Int,
    iconSize: Int
) {
    val theme = LocalAppThemeController.current
    val tokens = LocalPremiumTokens.current

    val backgroundBrush = when {
        theme.isPremium -> Brush.radialGradient(
            colors = listOf(
                color.copy(alpha = 0.34f),
                color.copy(alpha = 0.16f),
                Color.Transparent
            )
        )

        theme.isDarkula -> Brush.radialGradient(
            colors = listOf(
                color.copy(alpha = 0.26f),
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f)
            )
        )

        else -> Brush.radialGradient(
            colors = listOf(
                color.copy(alpha = 0.18f),
                color.copy(alpha = 0.10f)
            )
        )
    }

    Box(
        modifier = Modifier
            .size(size.dp)
            .then(
                if (theme.isPremium) {
                    Modifier.shadow(
                        elevation = 12.dp,
                        shape = CircleShape,
                        ambientColor = color.copy(alpha = 0.18f),
                        spotColor = tokens.primaryGlow.copy(alpha = 0.22f)
                    )
                } else {
                    Modifier
                }
            )
            .background(backgroundBrush, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(iconSize.dp)
        )
    }
}

@Composable
private fun ResumoFooter(
    jornadaMinutos: Int,
    mostrarContador: Boolean,
    dataHoraInicioContador: LocalDateTime?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.34f),
                shape = MaterialTheme.shapes.large
            )
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Schedule,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
        )

        Spacer(Modifier.width(8.dp))

        Text(
            text = when {
                mostrarContador && dataHoraInicioContador != null -> "Expediente em andamento"
                else -> "Jornada esperada"
            },
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = jornadaMinutos.formatarComoHoraMinuto(),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )
    }
}

@Stable
private data class ResumoVisualStatus(
    val tituloTrabalhado: String,
    val valorTrabalhado: String?,
    val subtituloTrabalhado: String?,
    val iconeTrabalhado: ImageVector,
    val corTrabalhado: Color,
    val iconeStatus: ImageVector,
    val corStatus: Color
)

private fun resumoDiaStatus(resumoDia: ResumoDia): ResumoVisualStatus {
    return when {
        !resumoDia.tipoAusencia.isNormalOrTrue -> ResumoVisualStatus(
            tituloTrabalhado = resumoDia.descricaoTipoDia,
            valorTrabalhado = "—",
            subtituloTrabalhado = "Sem meta",
            iconeTrabalhado = Icons.Default.CalendarMonth,
            corTrabalhado = Color(0xFF22C55E),
            iconeStatus = Icons.Default.CalendarMonth,
            corStatus = Color(0xFF22C55E)
        )

        resumoDia.temProblemas -> ResumoVisualStatus(
            tituloTrabalhado = "Trabalhado",
            valorTrabalhado = null,
            subtituloTrabalhado = "Atenção",
            iconeTrabalhado = Icons.Default.Timer,
            corTrabalhado = Color(0xFFF59E0B),
            iconeStatus = Icons.Default.TrendingDown,
            corStatus = Color(0xFFF59E0B)
        )

        else -> ResumoVisualStatus(
            tituloTrabalhado = "Trabalhado no dia",
            valorTrabalhado = null,
            subtituloTrabalhado = null,
            iconeTrabalhado = Icons.Default.Schedule,
            corTrabalhado = Color(0xFF3B82F6),
            iconeStatus = Icons.Default.Timer,
            corStatus = Color(0xFF3B82F6)
        )
    }
}

private fun saldoProgress(minutos: Int): Float {
    if (minutos == 0) return 0f
    return (abs(minutos).coerceAtMost(480).toFloat() / 480f).coerceIn(0f, 1f)
}