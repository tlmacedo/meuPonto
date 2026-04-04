// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/settings/horarios/HorariosDialogs.kt

package br.com.tlmacedo.meuponto.presentation.screen.settings.horarios

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import br.com.tlmacedo.meuponto.domain.model.DiaSemana
import br.com.tlmacedo.meuponto.domain.model.HorarioDiaSemana
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val FORMATTER_HORA = DateTimeFormatter.ofPattern("HH:mm")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarHorarioDialog(
    horario: HorarioDiaSemana,
    isSaving: Boolean,
    mostrarTimePicker: Boolean,
    campoTimePicker: CampoHorario?,
    onAlterarCargaHoraria: (Int) -> Unit,
    onAlterarIntervaloMinimo: (Int) -> Unit,
    onAbrirTimePicker: (CampoHorario) -> Unit,
    onSelecionarHorario: (LocalTime?) -> Unit,
    onFecharTimePicker: () -> Unit,
    onLimparHorariosIdeais: () -> Unit,
    onSalvar: () -> Unit,
    onDismiss: () -> Unit,
    avisoJornadaExcedida: String? = null,
    avisoTurnoMaximo: String? = null,
    avisoIntervaloMinimo: String? = null,
    canSave: Boolean = true
) {
    // Estado para TimePicker
    val horarioAtual = when (campoTimePicker) {
        CampoHorario.ENTRADA -> horario.entradaIdeal
        CampoHorario.SAIDA_INTERVALO -> horario.saidaIntervaloIdeal
        CampoHorario.VOLTA_INTERVALO -> horario.voltaIntervaloIdeal
        CampoHorario.SAIDA -> horario.saidaIdeal
        null -> null
    }

    val timePickerState = rememberTimePickerState(
        initialHour = horarioAtual?.hour ?: 8,
        initialMinute = horarioAtual?.minute ?: 0,
        is24Hour = true
    )

    if (mostrarTimePicker && campoTimePicker != null) {
        TimePickerDialog(
            title = when (campoTimePicker) {
                CampoHorario.ENTRADA -> "Horário de Entrada"
                CampoHorario.SAIDA_INTERVALO -> "Saída para Intervalo"
                CampoHorario.VOLTA_INTERVALO -> "Volta do Intervalo"
                CampoHorario.SAIDA -> "Horário de Saída"
            },
            state = timePickerState,
            onConfirm = {
                val selectedTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                onSelecionarHorario(selectedTime)
            },
            onDismiss = onFecharTimePicker,
            onClear = { onSelecionarHorario(null) }
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
            ) {
                // Título
                Text(
                    text = horario.diaSemana.descricao,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Carga horária
                Text(
                    text = "Carga Horária",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                DuracaoSelector(
                    valorMinutos = horario.cargaHorariaMinutos,
                    onValorChange = onAlterarCargaHoraria,
                    label = "Duração da jornada",
                    incrementos = listOf(-60, -30, -10, 10, 30, 60)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Intervalo mínimo
                Text(
                    text = "Intervalo Mínimo",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                DuracaoSelector(
                    valorMinutos = horario.intervaloMinimoMinutos,
                    onValorChange = onAlterarIntervaloMinimo,
                    label = "Intervalo mínimo para almoço",
                    incrementos = listOf(-30, -15, -5, 5, 15, 30)
                )

                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))

                // Horários ideais
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Horários Ideais",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    if (horario.temHorariosIdeais) {
                        TextButton(onClick = onLimparHorariosIdeais) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Limpar")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Opcional: Configure os horários ideais para sugestões automáticas",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Grid de horários ideais
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HorarioIdealField(
                        label = "Entrada",
                        valor = horario.entradaIdeal,
                        onClick = { onAbrirTimePicker(CampoHorario.ENTRADA) },
                        modifier = Modifier.weight(1f)
                    )
                    HorarioIdealField(
                        label = "Saída Int.",
                        valor = horario.saidaIntervaloIdeal,
                        onClick = { onAbrirTimePicker(CampoHorario.SAIDA_INTERVALO) },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HorarioIdealField(
                        label = "Volta Int.",
                        valor = horario.voltaIntervaloIdeal,
                        onClick = { onAbrirTimePicker(CampoHorario.VOLTA_INTERVALO) },
                        modifier = Modifier.weight(1f)
                    )
                    HorarioIdealField(
                        label = "Saída",
                        valor = horario.saidaIdeal,
                        onClick = { onAbrirTimePicker(CampoHorario.SAIDA) },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Mensagens de Validação
                avisoJornadaExcedida?.let { ValidationWarning(it, isError = true) }
                avisoTurnoMaximo?.let { ValidationWarning(it, isError = false) }
                avisoIntervaloMinimo?.let { ValidationWarning(it, isError = false) }

                Spacer(modifier = Modifier.height(24.dp))

                // Botões de ação
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onDismiss) {
                        Text("Cancelar")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onSalvar,
                        enabled = !isSaving && canSave
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Salvar")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ValidationWarning(
    message: String,
    isError: Boolean = false
) {
    val color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary
    val icon = Icons.Default.Info

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = color
        )
    }
}

@Composable
private fun HorarioIdealField(
    label: String,
    valor: LocalTime?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = valor?.format(FORMATTER_HORA) ?: "--:--",
        onValueChange = { },
        label = { Text(label) },
        readOnly = true,
        enabled = false,
        modifier = modifier.clickable { onClick() },
        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
            disabledTextColor = MaterialTheme.colorScheme.onSurface,
            disabledBorderColor = MaterialTheme.colorScheme.outline,
            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
}

@Composable
private fun DuracaoSelector(
    valorMinutos: Int,
    onValorChange: (Int) -> Unit,
    label: String,
    incrementos: List<Int>,
    modifier: Modifier = Modifier
) {
    val horas = valorMinutos / 60
    val minutos = valorMinutos % 60
    val formatado = String.format("%02d:%02d", horas, minutos)

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = formatado,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                incrementos.forEach { inc ->
                    val texto = if (inc > 0) "+$inc" else "$inc"
                    TextButton(
                        onClick = { onValorChange(valorMinutos + inc) }
                    ) {
                        Text(
                            text = texto,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    title: String,
    state: TimePickerState,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    onClear: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            TimePicker(state = state)
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("OK")
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onClear) {
                    Text("Limpar")
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancelar")
                }
            }
        }
    )
}

@Composable
fun CopiarHorarioDialog(
    diaOrigem: DiaSemana,
    diasDisponiveis: List<DiaSemana>,
    isSaving: Boolean,
    onConfirmar: (List<DiaSemana>) -> Unit,
    onDismiss: () -> Unit
) {
    val diasSelecionados = remember { mutableStateListOf<DiaSemana>() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Copiar ${diaOrigem.descricao}")
        },
        text = {
            Column {
                Text(
                    text = "Selecione os dias para copiar as configurações:",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(16.dp))

                diasDisponiveis
                    .filter { it != diaOrigem }
                    .forEach { dia ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (dia in diasSelecionados) {
                                        diasSelecionados.remove(dia)
                                    } else {
                                        diasSelecionados.add(dia)
                                    }
                                }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = dia in diasSelecionados,
                                onCheckedChange = { checked ->
                                    if (checked) {
                                        diasSelecionados.add(dia)
                                    } else {
                                        diasSelecionados.remove(dia)
                                    }
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(dia.descricao)
                        }
                    }

                Spacer(modifier = Modifier.height(8.dp))

                // Atalhos
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = {
                            diasSelecionados.clear()
                            diasSelecionados.addAll(
                                diasDisponiveis.filter { it != diaOrigem && it.isDiaUtil }
                            )
                        }
                    ) {
                        Text("Dias úteis")
                    }

                    TextButton(
                        onClick = {
                            diasSelecionados.clear()
                            diasSelecionados.addAll(
                                diasDisponiveis.filter { it != diaOrigem }
                            )
                        }
                    ) {
                        Text("Todos")
                    }

                    TextButton(
                        onClick = { diasSelecionados.clear() }
                    ) {
                        Text("Limpar")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirmar(diasSelecionados.toList()) },
                enabled = diasSelecionados.isNotEmpty() && !isSaving
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Copiar (${diasSelecionados.size})")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
