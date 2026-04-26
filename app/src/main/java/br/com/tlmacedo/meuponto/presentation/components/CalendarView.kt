package br.com.tlmacedo.meuponto.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.tlmacedo.meuponto.domain.model.StatusDiaResumo
import br.com.tlmacedo.meuponto.domain.model.ausencia.Ausencia
import br.com.tlmacedo.meuponto.domain.model.ausencia.TipoAusencia
import br.com.tlmacedo.meuponto.domain.model.ausencia.TipoFolga
import br.com.tlmacedo.meuponto.presentation.screen.history.FiltroHistorico
import br.com.tlmacedo.meuponto.presentation.screen.history.InfoDiaHistorico
import br.com.tlmacedo.meuponto.presentation.theme.Error
import br.com.tlmacedo.meuponto.presentation.theme.Info
import br.com.tlmacedo.meuponto.presentation.theme.MeuPontoTheme
import br.com.tlmacedo.meuponto.presentation.theme.SidiaMediumGray
import br.com.tlmacedo.meuponto.presentation.theme.Success
import br.com.tlmacedo.meuponto.presentation.theme.Warning
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun CalendarView(
    yearMonth: YearMonth,
    diasHistorico: List<InfoDiaHistorico>,
    modifier: Modifier = Modifier,
    filtrosAtivos: Set<FiltroHistorico> = emptySet(),
    onDateClick: (LocalDate) -> Unit = {}
) {
    val daysInMonth = yearMonth.lengthOfMonth()
    val firstDayOfMonth = yearMonth.atDay(1)
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7

    val locale = Locale.forLanguageTag("pt-BR")
    val daysOfWeek = remember {
        listOf(
            DayOfWeek.SUNDAY, DayOfWeek.MONDAY, DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY
        )
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // Cabeçalho dos dias da semana
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        ) {
            daysOfWeek.forEach { day ->
                Text(
                    text = day.getDisplayName(TextStyle.SHORT, locale).uppercase(),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }

        // Grid de dias
        val calendarDays = remember(yearMonth, diasHistorico) {
            val list = mutableListOf<CalendarDayData>()

            // Dias do mês anterior
            val prevMonth = yearMonth.minusMonths(1)
            val daysInPrevMonth = prevMonth.lengthOfMonth()
            for (i in firstDayOfWeek - 1 downTo 0) {
                val date = prevMonth.atDay(daysInPrevMonth - i)
                val info = diasHistorico.find { it.data == date }
                list.add(CalendarDayData.Empty(date, info))
            }

            // Dias do mês atual
            for (day in 1..daysInMonth) {
                val date = yearMonth.atDay(day)
                val info = diasHistorico.find { it.data == date }
                list.add(CalendarDayData.Day(date, info))
            }

            // Dias do próximo mês para fechar a grade (6 semanas = 42 dias)
            val totalCellsNeeded = 42
            val remaining = totalCellsNeeded - list.size
            val nextMonth = yearMonth.plusMonths(1)
            for (day in 1..remaining) {
                val date = nextMonth.atDay(day)
                val info = diasHistorico.find { it.data == date }
                list.add(CalendarDayData.Empty(date, info))
            }
            list
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            calendarDays.chunked(7).forEach { week ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    week.forEach { dayData ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                        ) {
                            val info = when (dayData) {
                                is CalendarDayData.Day -> dayData.info
                                is CalendarDayData.Empty -> dayData.info
                            }

                            val matchesFilter = if (filtrosAtivos.isEmpty()) true else {
                                info?.let { dia ->
                                    filtrosAtivos.any { filtro ->
                                        when (filtro) {
                                            FiltroHistorico.TODOS, FiltroHistorico.CALENDARIO, FiltroHistorico.LISTA -> true
                                            FiltroHistorico.COMPLETOS -> dia.jornadaCompleta
                                            FiltroHistorico.INCOMPLETOS -> !dia.jornadaCompleta && dia.pontos.isNotEmpty() && !dia.resumoDia.isFuturo
                                            FiltroHistorico.COM_PROBLEMAS -> dia.temProblemas
                                            FiltroHistorico.FUTUROS -> dia.resumoDia.isFuturo
                                            FiltroHistorico.DESCANSO -> dia.isDescanso
                                            FiltroHistorico.FERIADOS -> dia.temFeriado
                                            FiltroHistorico.FERIAS -> dia.ausencias.any { it.tipo == TipoAusencia.FERIAS }
                                            FiltroHistorico.FOLGAS -> dia.ausencias.any { it.tipo == TipoAusencia.FOLGA && it.tipoFolga != TipoFolga.DAY_OFF }
                                            FiltroHistorico.DAY_OFF -> dia.ausencias.any { it.tipo == TipoAusencia.FOLGA && it.tipoFolga == TipoFolga.DAY_OFF }
                                            FiltroHistorico.ATESTADOS -> dia.ausencias.any { it.tipo == TipoAusencia.ATESTADO }
                                            FiltroHistorico.DECLARACOES -> dia.declaracoes.isNotEmpty()
                                            FiltroHistorico.FALTAS -> dia.ausencias.any { it.tipo == TipoAusencia.FALTA_JUSTIFICADA || it.tipo == TipoAusencia.FALTA_INJUSTIFICADA }
                                        }
                                    }
                                } ?: false
                            }

                            CalendarDay(
                                date = dayData.date,
                                infoDia = info,
                                isMuted = !matchesFilter,
                                isCurrentMonth = dayData is CalendarDayData.Day,
                                onClick = { onDateClick(dayData.date) }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        CalendarLegend()
    }
}

sealed class CalendarDayData {
    abstract val date: LocalDate

    data class Day(override val date: LocalDate, val info: InfoDiaHistorico?) : CalendarDayData()
    data class Empty(override val date: LocalDate, val info: InfoDiaHistorico? = null) :
        CalendarDayData()
}

@Composable
private fun CalendarDay(
    date: LocalDate,
    infoDia: InfoDiaHistorico?,
    isMuted: Boolean,
    isCurrentMonth: Boolean,
    onClick: () -> Unit
) {
    val isToday = date == LocalDate.now()
    val contentAlpha = if (isMuted) 0.2f else 1f

    val backgroundColor = when {
        !isCurrentMonth -> Color.Transparent
        isToday -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
        infoDia?.temFeriado == true -> Color(0xFF9C27B0).copy(alpha = 0.1f)
        infoDia?.temAusencia == true -> getAbsenceColor(infoDia.ausenciaPrincipal!!).copy(alpha = 0.1f)
        date.dayOfWeek == DayOfWeek.SATURDAY || date.dayOfWeek == DayOfWeek.SUNDAY -> MaterialTheme.colorScheme.error.copy(
            alpha = 0.05f
        )

        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    }

    val borderColor = if (isToday) MaterialTheme.colorScheme.primary else Color.Transparent

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .border(
                width = if (isToday) 1.dp else 0.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(enabled = infoDia != null, onClick = onClick)
            .padding(4.dp)
            .alpha(contentAlpha)
    ) {
        // Número do dia
        Text(
            text = date.dayOfMonth.toString(),
            style = MaterialTheme.typography.labelSmall,
            fontSize = 11.sp,
            fontWeight = if (isToday) FontWeight.ExtraBold else FontWeight.Normal,
            color = if (isCurrentMonth) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
            modifier = Modifier.align(Alignment.TopStart)
        )

        // Ícone de status (Utilizando o Emoji padrão do dia)
        infoDia?.let { info ->
            Text(
                text = info.emoji,
                fontSize = 10.sp,
                modifier = Modifier.align(Alignment.TopEnd)
            )
        }

        // Conteúdo central
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(12.dp))
            if (infoDia != null) {
                when {
                    infoDia.pontos.isNotEmpty() -> {
                        Text(
                            text = infoDia.resumoDia.horasTrabalhadasFormatadas,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = getStatusColor(infoDia.statusDia)
                        )
                        if (infoDia.resumoDia.saldoDiaMinutos != 0) {
                            Text(
                                text = infoDia.resumoDia.saldoDiaFormatado,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 7.sp,
                                color = if (infoDia.resumoDia.saldoDiaMinutos > 0) Success else Error
                            )
                        }
                    }

                    infoDia.temAusencia -> {
                        val principal = infoDia.ausenciaPrincipal!!
                        Text(
                            text = principal.tipo.descricao,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 7.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 8.sp,
                            color = getAbsenceColor(principal),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (!principal.observacao.isNullOrBlank()) {
                            Text(
                                text = principal.observacao,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 6.sp,
                                textAlign = TextAlign.Center,
                                lineHeight = 7.sp,
                                color = getAbsenceColor(principal).copy(alpha = 0.7f),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    infoDia.temFeriado -> {
                        Text(
                            text = infoDia.feriado?.nome ?: "Feriado",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 7.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 8.sp,
                            color = Color(0xFF9C27B0),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    infoDia.isDescanso -> {
                        Text(
                            text = "Descanso",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 8.sp,
                            color = Color(0xFF9C27B0).copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CalendarLegend() {
    val items = listOf(
        LegendItem("Completo", "✅", Success),
        LegendItem("Férias", "🏖️", Info),
        LegendItem("Feriado", "🎉", Color(0xFF9C27B0)),
        LegendItem("Descanso", "🛋️", Color(0xFF9C27B0)),
        LegendItem("Falta", "❌", Error),
        LegendItem("F. Justificada", "📝", Warning)
    )

    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items.forEach { item ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Text(text = item.emoji, fontSize = 12.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = item.label,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

data class LegendItem(val label: String, val emoji: String, val color: Color)

private fun getStatusColor(status: StatusDiaResumo): Color {
    return when (status) {
        StatusDiaResumo.DESCANSO, StatusDiaResumo.FERIADO -> Color(0xFF9C27B0)
        StatusDiaResumo.COMPLETO -> Success
        StatusDiaResumo.EM_ANDAMENTO -> Info
        StatusDiaResumo.INCOMPLETO, StatusDiaResumo.FERIADO_TRABALHADO -> Warning
        StatusDiaResumo.COM_PROBLEMAS -> Error
        else -> SidiaMediumGray
    }
}

private fun getAbsenceColor(ausencia: Ausencia): Color {
    return when (ausencia.tipo) {
        TipoAusencia.FERIAS -> Info
        TipoAusencia.ATESTADO, TipoAusencia.FALTA_JUSTIFICADA, TipoAusencia.FOLGA -> Warning
        TipoAusencia.FALTA_INJUSTIFICADA -> Error
        else -> Color.Gray
    }
}

@Preview(showBackground = true)
@Composable
private fun CalendarViewPreview() {
    MeuPontoTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            CalendarView(
                yearMonth = YearMonth.now(),
                diasHistorico = emptyList()
            )
        }
    }
}
