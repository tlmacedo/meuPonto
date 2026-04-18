package br.com.tlmacedo.meuponto.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import br.com.tlmacedo.meuponto.domain.model.ausencia.Ausencia
import br.com.tlmacedo.meuponto.domain.model.ausencia.TipoAusencia
import br.com.tlmacedo.meuponto.domain.model.feriado.Feriado
import br.com.tlmacedo.meuponto.presentation.theme.MeuPontoTheme
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun CalendarView(
    yearMonth: YearMonth,
    holidays: List<Feriado> = emptyList(),
    absences: List<Ausencia> = emptyList(),
    onDateClick: (LocalDate) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val daysInMonth = yearMonth.lengthOfMonth()
    val firstDayOfMonth = yearMonth.atDay(1)
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7 // 0 = Sunday, 1 = Monday, ...

    val locale = Locale.forLanguageTag("pt-BR")
    val daysOfWeek = remember {
        listOf(
            DayOfWeek.SUNDAY, DayOfWeek.MONDAY, DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY
        )
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // Cabeçalho dos dias da semana
        Row(modifier = Modifier.fillMaxWidth()) {
            daysOfWeek.forEach { day ->
                Text(
                    text = day.getDisplayName(TextStyle.NARROW, locale).uppercase(),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Grid de dias
        val totalCells = (firstDayOfWeek + daysInMonth + 6) / 7 * 7
        for (i in 0 until totalCells step 7) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (j in 0 until 7) {
                    val dayIndex = i + j
                    val dayOfMonth = dayIndex - firstDayOfWeek + 1
                    
                    Box(modifier = Modifier.weight(1f).aspectRatio(1f)) {
                        if (dayOfMonth in 1..daysInMonth) {
                            val date = yearMonth.atDay(dayOfMonth)
                            val holiday = holidays.firstOrNull { it.getDataParaAno(date.year) == date }
                            val dayAbsences = absences.filter { date in it.dataInicio..it.dataFim }
                            
                            CalendarDay(
                                date = date,
                                holiday = holiday,
                                absences = dayAbsences,
                                onClick = { onDateClick(date) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarDay(
    date: LocalDate,
    holiday: Feriado?,
    absences: List<Ausencia>,
    onClick: () -> Unit
) {
    val isToday = date == LocalDate.now()
    val hasHoliday = holiday != null
    val hasAbsence = absences.isNotEmpty()
    val isWeekend = date.dayOfWeek == DayOfWeek.SATURDAY || date.dayOfWeek == DayOfWeek.SUNDAY

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(2.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(
                when {
                    hasHoliday -> Color(0xFF9C27B0).copy(alpha = 0.1f)
                    hasAbsence -> getAbsenceColor(absences.first()).copy(alpha = 0.1f)
                    isToday -> MaterialTheme.colorScheme.primaryContainer
                    else -> Color.Transparent
                }
            )
            .border(
                width = if (isToday) 1.dp else 0.dp,
                color = if (isToday) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = RoundedCornerShape(4.dp)
            )
            .clickable(onClick = onClick)
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = date.dayOfMonth.toString(),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isToday || hasHoliday || hasAbsence) FontWeight.Bold else FontWeight.Normal,
            color = when {
                hasHoliday -> Color(0xFF9C27B0)
                isToday -> MaterialTheme.colorScheme.onPrimaryContainer
                isWeekend -> MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                else -> MaterialTheme.colorScheme.onSurface
            }
        )
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (hasHoliday) {
                Box(modifier = Modifier.size(4.dp).background(Color(0xFF9C27B0), CircleShape))
            }
            if (hasAbsence) {
                Box(modifier = Modifier.size(4.dp).background(getAbsenceColor(absences.first()), CircleShape))
            }
        }
    }
}

private fun getAbsenceColor(ausencia: Ausencia): Color {
    return when (ausencia.tipo) {
        TipoAusencia.FERIAS -> Color(0xFF00BCD4)
        TipoAusencia.ATESTADO -> Color(0xFFE91E63)
        TipoAusencia.FALTA_JUSTIFICADA -> Color(0xFF4CAF50)
        TipoAusencia.FOLGA -> Color(0xFFFF9800)
        TipoAusencia.FALTA_INJUSTIFICADA -> Color(0xFFF44336)
        else -> Color.Gray
    }
}

@Preview(showBackground = true)
@Composable
private fun CalendarViewPreview() {
    MeuPontoTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            CalendarView(
                yearMonth = YearMonth.now()
            )
        }
    }
}
