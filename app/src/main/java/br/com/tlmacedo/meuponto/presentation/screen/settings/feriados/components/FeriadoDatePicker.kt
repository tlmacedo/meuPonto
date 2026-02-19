// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/settings/feriados/components/FeriadoDatePicker.kt
package br.com.tlmacedo.meuponto.presentation.screen.settings.feriados.components

import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import br.com.tlmacedo.meuponto.domain.model.feriado.RecorrenciaFeriado
import java.time.Instant
import java.time.LocalDate
import java.time.MonthDay
import java.time.ZoneId

/**
 * DatePicker para seleção de data de feriado.
 * Suporta tanto dia/mês (anual) quanto data completa (único).
 *
 * @author Thiago
 * @since 3.0.0
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeriadoDatePicker(
    recorrencia: RecorrenciaFeriado,
    diaMesAtual: MonthDay?,
    dataAtual: LocalDate?,
    onDiaMesSelected: (MonthDay) -> Unit,
    onDataSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val initialMillis = when (recorrencia) {
        RecorrenciaFeriado.ANUAL -> {
            diaMesAtual?.let {
                LocalDate.now().withMonth(it.monthValue).withDayOfMonth(it.dayOfMonth)
            } ?: LocalDate.now()
        }
        RecorrenciaFeriado.UNICO -> dataAtual ?: LocalDate.now()
    }.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)

    val confirmEnabled by remember {
        derivedStateOf { datePickerState.selectedDateMillis != null }
    }

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val selectedDate = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()

                        when (recorrencia) {
                            RecorrenciaFeriado.ANUAL -> {
                                onDiaMesSelected(
                                    MonthDay.of(selectedDate.monthValue, selectedDate.dayOfMonth)
                                )
                            }
                            RecorrenciaFeriado.UNICO -> {
                                onDataSelected(selectedDate)
                            }
                        }
                    }
                },
                enabled = confirmEnabled
            ) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    ) {
        DatePicker(
            state = datePickerState,
            title = {
                Text(
                    text = when (recorrencia) {
                        RecorrenciaFeriado.ANUAL -> "Selecione o dia e mês"
                        RecorrenciaFeriado.UNICO -> "Selecione a data"
                    }
                )
            }
        )
    }
}
