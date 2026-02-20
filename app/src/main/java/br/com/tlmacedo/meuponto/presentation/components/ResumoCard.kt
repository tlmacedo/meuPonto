// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/components/ResumoCard.kt
package br.com.tlmacedo.meuponto.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.WorkOff
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.tlmacedo.meuponto.domain.model.BancoHoras
import br.com.tlmacedo.meuponto.domain.model.ResumoDia
import br.com.tlmacedo.meuponto.domain.model.VersaoJornada
import br.com.tlmacedo.meuponto.presentation.theme.Error
import br.com.tlmacedo.meuponto.presentation.theme.ErrorLight
import br.com.tlmacedo.meuponto.presentation.theme.Info
import br.com.tlmacedo.meuponto.presentation.theme.InfoLight
import br.com.tlmacedo.meuponto.presentation.theme.Success
import br.com.tlmacedo.meuponto.presentation.theme.SuccessLight
import java.time.LocalDateTime
import java.time.LocalTime
import kotlin.math.abs

// Cores do gradiente moderno
private val GradientStart = Color(0xFF2D3748) // Cinza escuro azulado
private val GradientEnd = Color(0xFF1A202C)   // Cinza mais escuro
private val GradientAccent = Color(0xFF4A5568) // Cinza médio para detalhes

/**
 * Card compacto de resumo do dia com gradiente moderno e cores semânticas.
 *
 * DESIGN:
 * - Gradiente cinza escuro moderno com tons azulados
 * - Alto contraste para textos brancos e cores semânticas
 * - Visual sofisticado e profissional
 *
 * CÁLCULO EM TEMPO REAL:
 * - Quando há turno aberto, os valores são calculados incluindo o tempo em andamento
 * - O valor atualiza automaticamente junto com o relógio do HomeViewModel
 *
 * Cores dinâmicas:
 * - **Trabalhado**: branco (em andamento ou < jornada) → verde (>= jornada)
 * - **Saldo Dia**: verde/↑ (positivo) | branco/→ (zero) | vermelho/↓ (negativo)
 * - **Banco**: verde (positivo) | branco/cinza (zero) | vermelho (negativo)
 *
 * @author Thiago
 * @since 1.0.0
 * @updated 4.3.0 - Gradiente cinza moderno com melhor contraste
 */
@Composable
fun ResumoCard(
    resumoDia: ResumoDia,
    bancoHoras: BancoHoras,
    horaAtual: LocalTime = LocalTime.now(),
    versaoJornada: VersaoJornada? = null,
    dataHoraInicioContador: LocalDateTime? = null,
    mostrarContador: Boolean = false,
    modifier: Modifier = Modifier
) {
    val textoPrincipal = Color.White
    val textoSecundario = Color.White.copy(alpha = 0.9f)
    val textoTerciario = Color.White.copy(alpha = 0.7f)

    // Calcular valores com tempo em andamento
    val minutosTrabalhados = resumoDia.horasTrabalhadasComAndamentoMinutos(horaAtual)
    val saldoDiaMinutos = resumoDia.saldoDiaComAndamentoMinutos(horaAtual)

    // Banco de horas: saldo acumulado + saldo do dia atual (com andamento)
    val bancoTotalMinutos = bancoHoras.saldoTotalMinutos + saldoDiaMinutos - resumoDia.saldoDiaMinutos

    // Gradiente diagonal moderno
    val gradientBrush = Brush.linearGradient(
        colors = listOf(GradientStart, GradientEnd)
    )

    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(gradientBrush)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Cabeçalho
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            text = "Resumo do Dia",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = textoPrincipal
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        // Info jornada
                        if (resumoDia.isFeriado) {
                            FeriadoJornadaInfo(corTexto = textoTerciario)
                        } else {
                            JornadaVersaoInfoCompact(
                                cargaHorariaFormatada = resumoDia.cargaHorariaDiariaFormatada,
                                versaoJornada = versaoJornada,
                                corTexto = textoTerciario
                            )
                        }
                    }

                    // Badge de status
                    when {
                        mostrarContador -> StatusBadgeCompact(texto = "Em andamento")
                        resumoDia.isFeriado && resumoDia.pontos.isNotEmpty() -> StatusBadgeCompact(
                            texto = "Hora extra",
                            icone = Icons.Default.Star,
                            corIcone = Success
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Divisor sutil
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color.White.copy(alpha = 0.1f))
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Resumo em três colunas
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ResumoItemTrabalhado(
                        titulo = "Trabalhado",
                        minutosTrabalhados = minutosTrabalhados,
                        minutosJornada = resumoDia.cargaHorariaEfetivaMinutos,
                        isFeriado = resumoDia.isFeriado,
                        emAndamento = resumoDia.temTurnoAberto,
                        corTitulo = textoTerciario,
                        modifier = Modifier.weight(1f)
                    )

                    ResumoItemSaldo(
                        titulo = "Saldo",
                        saldoMinutos = saldoDiaMinutos,
                        isFeriado = resumoDia.isFeriado,
                        corTitulo = textoTerciario,
                        modifier = Modifier.weight(1f)
                    )

                    ResumoItemBanco(
                        titulo = "Banco",
                        saldoMinutos = bancoTotalMinutos,
                        corTitulo = textoTerciario,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

/**
 * Info compacta de jornada e versão.
 */
@Composable
private fun JornadaVersaoInfoCompact(
    cargaHorariaFormatada: String,
    versaoJornada: VersaoJornada?,
    corTexto: Color,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Icon(
            imageVector = Icons.Default.Schedule,
            contentDescription = null,
            tint = corTexto,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = cargaHorariaFormatada,
            style = MaterialTheme.typography.bodySmall,
            fontSize = 12.sp,
            color = corTexto
        )

        versaoJornada?.let { versao ->
            Text(
                text = " • ",
                style = MaterialTheme.typography.bodySmall,
                fontSize = 12.sp,
                color = corTexto.copy(alpha = 0.5f)
            )
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = null,
                tint = corTexto,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = versao.periodoFormatado,
                style = MaterialTheme.typography.bodySmall,
                fontSize = 12.sp,
                color = corTexto,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Info de jornada para dias de feriado.
 */
@Composable
private fun FeriadoJornadaInfo(
    corTexto: Color,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Icon(
            imageVector = Icons.Outlined.WorkOff,
            contentDescription = null,
            tint = corTexto,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "Feriado • Sem jornada obrigatória",
            style = MaterialTheme.typography.bodySmall,
            fontSize = 12.sp,
            color = corTexto
        )
    }
}

/**
 * Badge de status compacto.
 */
@Composable
private fun StatusBadgeCompact(
    texto: String,
    icone: androidx.compose.ui.graphics.vector.ImageVector = Icons.Default.PlayCircle,
    corIcone: Color = Success,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.1f))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Icon(
            imageVector = icone,
            contentDescription = null,
            tint = corIcone,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = texto,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )
    }
}

/**
 * Item de resumo para Trabalhado.
 */
@Composable
private fun ResumoItemTrabalhado(
    titulo: String,
    minutosTrabalhados: Int,
    minutosJornada: Int,
    isFeriado: Boolean = false,
    emAndamento: Boolean = false,
    corTitulo: Color,
    modifier: Modifier = Modifier
) {
    val atingiuJornada = minutosTrabalhados >= minutosJornada
    val temTrabalho = minutosTrabalhados > 0

    val corValor: Color
    val corIcone: Color
    val corFundoIcone: Color

    when {
        isFeriado && temTrabalho -> {
            corValor = Success
            corIcone = Success
            corFundoIcone = Success.copy(alpha = 0.15f)
        }
        atingiuJornada -> {
            corValor = Success
            corIcone = Success
            corFundoIcone = Success.copy(alpha = 0.15f)
        }
        else -> {
            corValor = Color.White
            corIcone = Color.White.copy(alpha = 0.8f)
            corFundoIcone = Color.White.copy(alpha = 0.1f)
        }
    }

    val valorFormatado = formatarDuracaoCompacta(minutosTrabalhados)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(horizontal = 4.dp)
    ) {
        // Ícone com fundo
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(corFundoIcone)
        ) {
            Icon(
                imageVector = Icons.Default.AccessTime,
                contentDescription = titulo,
                tint = corIcone,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Título
        Text(
            text = titulo,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = corTitulo,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Valor com fundo
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White.copy(alpha = 0.08f))
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Text(
                text = valorFormatado,
                style = MaterialTheme.typography.bodyLarge,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = corValor,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Item de resumo para Saldo do Dia.
 */
@Composable
private fun ResumoItemSaldo(
    titulo: String,
    saldoMinutos: Int,
    isFeriado: Boolean = false,
    corTitulo: Color,
    modifier: Modifier = Modifier
) {
    val isPositivo = saldoMinutos > 0
    val isNegativo = saldoMinutos < 0

    val icone = when {
        isPositivo -> Icons.AutoMirrored.Filled.TrendingUp
        isNegativo -> Icons.AutoMirrored.Filled.TrendingDown
        else -> Icons.Default.Remove
    }

    val corIcone = when {
        isPositivo -> Success
        isNegativo -> Error
        else -> Color.White.copy(alpha = 0.8f)
    }

    val corFundoIcone = when {
        isPositivo -> Success.copy(alpha = 0.15f)
        isNegativo -> Error.copy(alpha = 0.15f)
        else -> Color.White.copy(alpha = 0.1f)
    }

    val corValor = when {
        isPositivo -> Success
        isNegativo -> Error
        else -> Color.White
    }

    val valorFormatado = formatarSaldoCompacto(saldoMinutos)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(horizontal = 4.dp)
    ) {
        // Ícone com fundo
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(corFundoIcone)
        ) {
            Icon(
                imageVector = icone,
                contentDescription = titulo,
                tint = corIcone,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Título
        Text(
            text = titulo,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = corTitulo,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Valor com fundo
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White.copy(alpha = 0.08f))
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Text(
                text = valorFormatado,
                style = MaterialTheme.typography.bodyLarge,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = corValor,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Item de resumo para Banco de Horas.
 */
@Composable
private fun ResumoItemBanco(
    titulo: String,
    saldoMinutos: Int,
    corTitulo: Color,
    modifier: Modifier = Modifier
) {
    val isPositivo = saldoMinutos > 0
    val isNegativo = saldoMinutos < 0

    val corIcone = when {
        isPositivo -> Success
        isNegativo -> Error
        else -> Color.White.copy(alpha = 0.8f)
    }

    val corFundoIcone = when {
        isPositivo -> Success.copy(alpha = 0.15f)
        isNegativo -> Error.copy(alpha = 0.15f)
        else -> Color.White.copy(alpha = 0.1f)
    }

    val corValor = when {
        isPositivo -> Success
        isNegativo -> Error
        else -> Color.White
    }

    val valorFormatado = formatarSaldoCompacto(saldoMinutos)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(horizontal = 4.dp)
    ) {
        // Ícone com fundo
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(corFundoIcone)
        ) {
            Icon(
                imageVector = Icons.Default.AccountBalance,
                contentDescription = titulo,
                tint = corIcone,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Título
        Text(
            text = titulo,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = corTitulo,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Valor com fundo
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White.copy(alpha = 0.08f))
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Text(
                text = valorFormatado,
                style = MaterialTheme.typography.bodyLarge,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = corValor,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Formata duração (sem sinal) para exibição compacta.
 */
private fun formatarDuracaoCompacta(minutos: Int): String {
    val minutosAbs = abs(minutos)
    val horas = minutosAbs / 60
    val mins = minutosAbs % 60

    return if (horas > 0) {
        "%02dh %02dmin".format(horas, mins)
    } else {
        "%02dmin".format(mins)
    }
}

/**
 * Formata saldo com sinal para exibição compacta.
 */
private fun formatarSaldoCompacto(minutos: Int): String {
    if (minutos == 0) return "00min"

    val minutosAbs = abs(minutos)
    val horas = minutosAbs / 60
    val mins = minutosAbs % 60

    val sinal = if (minutos > 0) "+ " else "- "

    return if (horas > 0) {
        "$sinal%02dh %02dmin".format(horas, mins)
    } else {
        "$sinal%02dmin".format(mins)
    }
}
