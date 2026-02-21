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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.BeachAccess
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.WorkOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.tlmacedo.meuponto.domain.model.BancoHoras
import br.com.tlmacedo.meuponto.domain.model.ResumoDia
import br.com.tlmacedo.meuponto.domain.model.TipoDiaEspecial
import br.com.tlmacedo.meuponto.domain.model.VersaoJornada
import br.com.tlmacedo.meuponto.presentation.theme.Error
import br.com.tlmacedo.meuponto.presentation.theme.Success
import java.time.LocalDateTime
import java.time.LocalTime
import kotlin.math.abs

// Cores do gradiente moderno
private val GradientStart = Color(0xFF2D3748)
private val GradientEnd = Color(0xFF1A202C)

/**
 * Card compacto de resumo do dia.
 *
 * @author Thiago
 * @since 1.0.0
 * @updated 5.0.0 - Layout compactado
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
    val textoTerciario = Color.White.copy(alpha = 0.7f)

    val minutosTrabalhados = resumoDia.horasTrabalhadasComAndamentoMinutos(horaAtual)
    val saldoDiaMinutos = resumoDia.saldoDiaComAndamentoMinutos(horaAtual)
    val bancoTotalMinutos = bancoHoras.saldoTotalMinutos + saldoDiaMinutos - resumoDia.saldoDiaMinutos

    val gradientBrush = Brush.linearGradient(
        colors = listOf(GradientStart, GradientEnd)
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
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
                    .padding(14.dp)
            ) {
                // Cabeçalho compacto
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            text = "Resumo do Dia",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = textoPrincipal
                        )
                        // Info jornada
                        when {
                            resumoDia.tipoDiaEspecial != TipoDiaEspecial.NORMAL -> {
                                DiaEspecialJornadaInfo(
                                    tipoDiaEspecial = resumoDia.tipoDiaEspecial,
                                    corTexto = textoTerciario
                                )
                            }
                            resumoDia.isFeriado -> {
                                FeriadoJornadaInfo(corTexto = textoTerciario)
                            }
                            else -> {
                                JornadaVersaoInfoCompact(
                                    cargaHorariaFormatada = resumoDia.cargaHorariaDiariaFormatada,
                                    versaoJornada = versaoJornada,
                                    corTexto = textoTerciario
                                )
                            }
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
                        resumoDia.tipoDiaEspecial != TipoDiaEspecial.NORMAL -> StatusBadgeCompact(
                            texto = resumoDia.tipoDiaEspecial.descricaoCurta,
                            icone = resumoDia.tipoDiaEspecial.getIcon(),
                            corIcone = resumoDia.tipoDiaEspecial.getCor()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Divisor
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color.White.copy(alpha = 0.1f))
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Resumo em três colunas (compacto)
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ResumoItemPrincipal(
                        tipoDiaEspecial = resumoDia.tipoDiaEspecial,
                        minutosTrabalhados = minutosTrabalhados,
                        minutosJornada = resumoDia.cargaHorariaEfetivaMinutos,
                        isFeriado = resumoDia.isFeriado,
                        emAndamento = resumoDia.temTurnoAberto,
                        corTitulo = textoTerciario,
                        modifier = Modifier.weight(1f)
                    )

                    ResumoItemSaldo(
                        titulo = "Saldo do dia",
                        saldoMinutos = saldoDiaMinutos,
                        isFeriado = resumoDia.isFeriado,
                        corTitulo = textoTerciario,
                        modifier = Modifier.weight(1f)
                    )

                    ResumoItemBanco(
                        titulo = "Banco de horas",
                        saldoMinutos = bancoTotalMinutos,
                        corTitulo = textoTerciario,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

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
        Text(
            text = "Jornada esperada:",
            style = MaterialTheme.typography.labelSmall,
            fontSize = 11.sp,
            color = corTexto
        )
        Spacer(modifier = Modifier.width(3.dp))
        Icon(
            imageVector = Icons.Default.Schedule,
            contentDescription = null,
            tint = corTexto,
            modifier = Modifier.size(12.dp)
        )
        Spacer(modifier = Modifier.width(3.dp))
        Text(
            text = cargaHorariaFormatada,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 11.sp,
            color = corTexto
        )

        versaoJornada?.let { versao ->
            Text(
                text = " • ",
                style = MaterialTheme.typography.labelSmall,
                fontSize = 11.sp,
                color = corTexto.copy(alpha = 0.5f)
            )
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = null,
                tint = corTexto,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = versao.periodoFormatado,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 11.sp,
                color = corTexto,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

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
            modifier = Modifier.size(12.dp)
        )
        Spacer(modifier = Modifier.width(3.dp))
        Text(
            text = "Feriado • Sem jornada obrigatória",
            style = MaterialTheme.typography.labelSmall,
            fontSize = 11.sp,
            color = corTexto
        )
    }
}

@Composable
private fun DiaEspecialJornadaInfo(
    tipoDiaEspecial: TipoDiaEspecial,
    corTexto: Color,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Icon(
            imageVector = tipoDiaEspecial.getIcon(),
            contentDescription = null,
            tint = tipoDiaEspecial.getCor(),
            modifier = Modifier.size(12.dp)
        )
        Spacer(modifier = Modifier.width(3.dp))
        Text(
            text = "${tipoDiaEspecial.descricao} • Sem jornada obrigatória",
            style = MaterialTheme.typography.labelSmall,
            fontSize = 11.sp,
            color = corTexto
        )
    }
}

@Composable
private fun StatusBadgeCompact(
    texto: String,
    icone: ImageVector = Icons.Default.PlayCircle,
    corIcone: Color = Success,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White.copy(alpha = 0.1f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Icon(
            imageVector = icone,
            contentDescription = null,
            tint = corIcone,
            modifier = Modifier.size(12.dp)
        )
        Text(
            text = texto,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )
    }
}

@Composable
private fun ResumoItemPrincipal(
    tipoDiaEspecial: TipoDiaEspecial,
    minutosTrabalhados: Int,
    minutosJornada: Int,
    isFeriado: Boolean = false,
    emAndamento: Boolean = false,
    corTitulo: Color,
    modifier: Modifier = Modifier
) {
    val (titulo, icone, corIconeEspecial) = when (tipoDiaEspecial) {
        TipoDiaEspecial.FERIAS -> Triple("Férias", Icons.Default.BeachAccess, Color(0xFF00BCD4))
        TipoDiaEspecial.ATESTADO -> Triple("Atestado", Icons.Default.LocalHospital, Color(0xFFEF5350))
        TipoDiaEspecial.FOLGA -> Triple("Folga", Icons.Default.Home, Color(0xFF66BB6A))
        TipoDiaEspecial.FALTA_JUSTIFICADA -> Triple("Falta Just.", Icons.Default.EventBusy, Color(0xFF42A5F5))
        TipoDiaEspecial.FALTA_INJUSTIFICADA -> Triple("Falta", Icons.Default.EventBusy, Color(0xFFEF5350))
        TipoDiaEspecial.FERIADO -> Triple("Feriado", Icons.Outlined.WorkOff, Color(0xFF4CAF50))
        TipoDiaEspecial.PONTE -> Triple("Ponte", Icons.Outlined.WorkOff, Color(0xFF9C27B0))
        TipoDiaEspecial.FACULTATIVO -> Triple("Facultativo", Icons.Outlined.WorkOff, Color(0xFFE91E63))
        TipoDiaEspecial.NORMAL -> Triple("Trabalhado no dia", Icons.Default.AccessTime, null)
    }

    val isDiaEspecialNaoTrabalhado = tipoDiaEspecial != TipoDiaEspecial.NORMAL
    val atingiuJornada = minutosTrabalhados >= minutosJornada
    val temTrabalho = minutosTrabalhados > 0

    val corValor: Color
    val corIcone: Color
    val corFundoIcone: Color

    when {
        isDiaEspecialNaoTrabalhado -> {
            corValor = corIconeEspecial ?: Color.White
            corIcone = corIconeEspecial ?: Color.White.copy(alpha = 0.8f)
            corFundoIcone = (corIconeEspecial ?: Color.White).copy(alpha = 0.15f)
        }
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

    val valorFormatado = if (isDiaEspecialNaoTrabalhado && minutosTrabalhados == 0) {
        "—"
    } else {
        formatarDuracaoCompacta(minutosTrabalhados)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(horizontal = 2.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(corFundoIcone)
        ) {
            Icon(
                imageVector = icone,
                contentDescription = titulo,
                tint = corIcone,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = titulo,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = corTitulo,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(2.dp))

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(Color.White.copy(alpha = 0.08f))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = valorFormatado,
                style = MaterialTheme.typography.bodySmall,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = corValor,
                textAlign = TextAlign.Center
            )
        }
    }
}

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
        modifier = modifier.padding(horizontal = 2.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(corFundoIcone)
        ) {
            Icon(
                imageVector = icone,
                contentDescription = titulo,
                tint = corIcone,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = titulo,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = corTitulo,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(2.dp))

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(Color.White.copy(alpha = 0.08f))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = valorFormatado,
                style = MaterialTheme.typography.bodySmall,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = corValor,
                textAlign = TextAlign.Center
            )
        }
    }
}

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
        modifier = modifier.padding(horizontal = 2.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(corFundoIcone)
        ) {
            Icon(
                imageVector = Icons.Default.AccountBalance,
                contentDescription = titulo,
                tint = corIcone,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = titulo,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = corTitulo,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(2.dp))

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(Color.White.copy(alpha = 0.08f))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = valorFormatado,
                style = MaterialTheme.typography.bodySmall,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = corValor,
                textAlign = TextAlign.Center
            )
        }
    }
}

// Extensões
private fun TipoDiaEspecial.getIcon(): ImageVector = when (this) {
    TipoDiaEspecial.FERIAS -> Icons.Default.BeachAccess
    TipoDiaEspecial.ATESTADO -> Icons.Default.LocalHospital
    TipoDiaEspecial.FOLGA -> Icons.Default.Home
    TipoDiaEspecial.FALTA_JUSTIFICADA -> Icons.Default.EventBusy
    TipoDiaEspecial.FALTA_INJUSTIFICADA -> Icons.Default.EventBusy
    TipoDiaEspecial.FERIADO -> Icons.Outlined.WorkOff
    TipoDiaEspecial.PONTE -> Icons.Outlined.WorkOff
    TipoDiaEspecial.FACULTATIVO -> Icons.Outlined.WorkOff
    TipoDiaEspecial.NORMAL -> Icons.Default.AccessTime
}

private fun TipoDiaEspecial.getCor(): Color = when (this) {
    TipoDiaEspecial.FERIAS -> Color(0xFF00BCD4)
    TipoDiaEspecial.ATESTADO -> Color(0xFFEF5350)
    TipoDiaEspecial.FOLGA -> Color(0xFF66BB6A)
    TipoDiaEspecial.FALTA_JUSTIFICADA -> Color(0xFF42A5F5)
    TipoDiaEspecial.FALTA_INJUSTIFICADA -> Color(0xFFEF5350)
    TipoDiaEspecial.FERIADO -> Color(0xFF4CAF50)
    TipoDiaEspecial.PONTE -> Color(0xFF9C27B0)
    TipoDiaEspecial.FACULTATIVO -> Color(0xFFE91E63)
    TipoDiaEspecial.NORMAL -> Color.White
}

private val TipoDiaEspecial.descricaoCurta: String
    get() = when (this) {
        TipoDiaEspecial.FERIAS -> "Férias"
        TipoDiaEspecial.ATESTADO -> "Atestado"
        TipoDiaEspecial.FOLGA -> "Folga"
        TipoDiaEspecial.FALTA_JUSTIFICADA -> "Falta Just."
        TipoDiaEspecial.FALTA_INJUSTIFICADA -> "Falta"
        TipoDiaEspecial.FERIADO -> "Feriado"
        TipoDiaEspecial.PONTE -> "Ponte"
        TipoDiaEspecial.FACULTATIVO -> "Facultativo"
        TipoDiaEspecial.NORMAL -> ""
    }

private fun formatarDuracaoCompacta(minutos: Int): String {
    val minutosAbs = abs(minutos)
    val horas = minutosAbs / 60
    val mins = minutosAbs % 60
    return if (horas > 0) "%02dh %02dmin".format(horas, mins) else "%02dmin".format(mins)
}

private fun formatarSaldoCompacto(minutos: Int): String {
    if (minutos == 0) return "00min"
    val minutosAbs = abs(minutos)
    val horas = minutosAbs / 60
    val mins = minutosAbs % 60
    val sinal = if (minutos > 0) "+ " else "- "
    return if (horas > 0) "$sinal%02dh %02dmin".format(horas, mins) else "$sinal%02dmin".format(mins)
}
