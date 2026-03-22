// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/editponto/EditPontoComponents.kt
package br.com.tlmacedo.meuponto.presentation.screen.editponto

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import br.com.tlmacedo.meuponto.R
import br.com.tlmacedo.meuponto.util.formatarCurto
import br.com.tlmacedo.meuponto.util.formatarHora
import br.com.tlmacedo.meuponto.util.toLocalDateFromDatePicker
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset

/**
 * Componentes reutilizáveis da tela de edição de ponto.
 *
 * Criado em 12.0.0 para resolver "Unresolved reference" na [EditPontoScreen]:
 * - DatePickerField (linha 47)
 * - DropdownField (linha 48)
 * - ObservacaoField (linha 49)
 * - TimePickerField (linha 50)
 *
 * Os composables estão no mesmo pacote da Screen para evitar imports extras.
 *
 * @author Thiago
 * @since 12.0.0
 */

// ============================================================================
// DATE PICKER FIELD
// ============================================================================

/**
 * Campo de texto somente leitura que abre um DatePicker do Material 3
 * ao ser clicado.
 *
 * @param label Rótulo do campo
 * @param selectedDate Data atualmente selecionada
 * @param onDateSelected Callback com a nova [LocalDate] confirmada
 * @param modifier Modifier opcional
 * @param enabled Controla se o campo responde a cliques (padrão: true)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerField(
    label: String,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    var mostrarDialog by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = selectedDate.formatarCurto(),
        onValueChange = {},
        label = { Text(label) },
        readOnly = true,
        enabled = enabled,
        modifier = modifier.then(
            if (enabled) Modifier.clickable { mostrarDialog = true } else Modifier
        )
    )

    if (mostrarDialog) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate
                .atStartOfDay(ZoneOffset.UTC)
                .toInstant()
                .toEpochMilli()
        )

        DatePickerDialog(
            onDismissRequest = { mostrarDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis
                            ?.toLocalDateFromDatePicker()
                            ?.let { onDateSelected(it) }
                        mostrarDialog = false
                    }
                ) {
                    Text(stringResource(R.string.btn_confirmar))
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialog = false }) {
                    Text(stringResource(R.string.btn_cancelar))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

// ============================================================================
// TIME PICKER FIELD
// ============================================================================

/**
 * Campo de texto somente leitura que abre um TimePicker do Material 3
 * ao ser clicado.
 *
 * @param label Rótulo do campo
 * @param selectedTime Hora atualmente selecionada
 * @param onTimeSelected Callback com a nova [LocalTime] confirmada
 * @param modifier Modifier opcional
 * @param enabled Controla se o campo responde a cliques (padrão: true)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerField(
    label: String,
    selectedTime: LocalTime,
    onTimeSelected: (LocalTime) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    var mostrarDialog by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = selectedTime.formatarHora(),
        onValueChange = {},
        label = { Text(label) },
        readOnly = true,
        enabled = enabled,
        modifier = modifier.then(
            if (enabled) Modifier.clickable { mostrarDialog = true } else Modifier
        )
    )

    if (mostrarDialog) {
        val timePickerState = rememberTimePickerState(
            initialHour = selectedTime.hour,
            initialMinute = selectedTime.minute,
            is24Hour = true
        )

        AlertDialog(
            onDismissRequest = { mostrarDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        onTimeSelected(
                            LocalTime.of(timePickerState.hour, timePickerState.minute)
                        )
                        mostrarDialog = false
                    }
                ) {
                    Text(stringResource(R.string.btn_confirmar))
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialog = false }) {
                    Text(stringResource(R.string.btn_cancelar))
                }
            },
            text = { TimePicker(state = timePickerState) }
        )
    }
}

// ============================================================================
// DROPDOWN FIELD
// ============================================================================

/**
 * Campo dropdown usando ExposedDropdownMenuBox do Material 3.
 *
 * @param label Rótulo do campo
 * @param options Lista de opções disponíveis
 * @param selectedOption Opção atualmente selecionada
 * @param onOptionSelected Callback com a string da opção escolhida
 * @param modifier Modifier opcional
 * @param enabled Controla se o dropdown responde a interações (padrão: true)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownField(
    label: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    var expandido by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expandido && enabled,
        onExpandedChange = { if (enabled) expandido = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedOption,
            onValueChange = {},
            label = { Text(label) },
            readOnly = true,
            enabled = enabled,
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandido && enabled)
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expandido && enabled,
            onDismissRequest = { expandido = false }
        ) {
            options.forEach { opcao ->
                DropdownMenuItem(
                    text = { Text(opcao) },
                    onClick = {
                        onOptionSelected(opcao)
                        expandido = false
                    }
                )
            }
        }
    }
}

// ============================================================================
// OBSERVACAO FIELD
// ============================================================================

/**
 * Campo de texto multilinha para observação livre do ponto.
 *
 * @param value Texto atual
 * @param onValueChange Callback com o novo texto
 * @param modifier Modifier opcional
 * @param minLines Número mínimo de linhas visíveis (padrão: 2)
 * @param maxLines Número máximo de linhas visíveis (padrão: 4)
 * @param enabled Controla se o campo é editável (padrão: true)
 */
@Composable
fun ObservacaoField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    minLines: Int = 2,
    maxLines: Int = 4,
    enabled: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(stringResource(R.string.lancamento_observacao)) },
        minLines = minLines,
        maxLines = maxLines,
        enabled = enabled,
        modifier = modifier.fillMaxWidth()
    )
}