// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/components/DurationInputField.kt
package br.com.tlmacedo.meuponto.presentation.components

import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * Unidade de incremento/decremento para duração.
 */
enum class DurationUnit(val minutes: Int, val label: String) {
    MINUTE_1(1, "1min"),
    MINUTE_5(5, "5min"),
    MINUTE_15(15, "15min"),
    MINUTE_30(30, "30min"),
    HOUR_1(60, "1h")
}

/**
 * Campo de entrada de duração com suporte a:
 * - Digitação direta no formato HH:MM
 * - Incremento/decremento por botões + e -
 * - Unidade configurável (1min, 5min, 15min, 30min, 1h)
 *
 * @param totalMinutos Valor atual em minutos
 * @param onValueChange Callback quando o valor muda (retorna minutos totais)
 * @param modifier Modificador do componente
 * @param label Label do campo
 * @param minValue Valor mínimo em minutos (default: 0)
 * @param maxValue Valor máximo em minutos (default: 24h = 1440)
 * @param enabled Se o campo está habilitado
 * @param showUnitSelector Se deve mostrar o seletor de unidade
 *
 * @author Thiago
 * @since 6.0.0
 */
@Composable
fun DurationInputField(
    totalMinutos: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    minValue: Int = 0,
    maxValue: Int = 1440, // 24 horas
    enabled: Boolean = true,
    showUnitSelector: Boolean = true
) {
    var unit by remember { mutableStateOf(DurationUnit.MINUTE_15) }
    var textValue by remember(totalMinutos) {
        mutableStateOf(formatMinutesToHHMM(totalMinutos))
    }
    var isFocused by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    Column(modifier = modifier) {
        // Label
        if (label != null) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // Campo principal com botões
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Botão de diminuir
            IconButton(
                onClick = {
                    val newValue = (totalMinutos - unit.minutes).coerceIn(minValue, maxValue)
                    onValueChange(newValue)
                },
                enabled = enabled && totalMinutos > minValue,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                ),
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "Diminuir ${unit.label}",
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Campo de texto editável
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
                    .border(
                        width = if (isFocused) 2.dp else 1.dp,
                        color = if (isFocused) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        },
                        shape = RoundedCornerShape(12.dp)
                    )
            ) {
                BasicTextField(
                    value = textValue,
                    onValueChange = { newText ->
                        // Auto-formatação: adiciona ":" automaticamente
                        val filtered = newText.filter { it.isDigit() }

                        textValue = when {
                            filtered.length <= 2 -> filtered
                            filtered.length <= 4 -> {
                                val hours = filtered.take(2)
                                val mins = filtered.drop(2)
                                "$hours:$mins"
                            }
                            else -> textValue // Não permite mais de 4 dígitos
                        }

                        // Tentar parsear e atualizar valor
                        parseHHMMToMinutes(textValue)?.let { minutes ->
                            val clamped = minutes.coerceIn(minValue, maxValue)
                            if (clamped != totalMinutos) {
                                onValueChange(clamped)
                            }
                        }
                    },
                    enabled = enabled,
                    textStyle = LocalTextStyle.current.copy(
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = MaterialTheme.typography.headlineSmall.fontSize,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                        }
                    ),
                    singleLine = true,
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .onFocusChanged { focusState ->
                            isFocused = focusState.isFocused
                            if (!focusState.isFocused) {
                                // Ao perder foco, reformatar o valor
                                textValue = formatMinutesToHHMM(totalMinutos)
                            }
                        },
                    decorationBox = { innerTextField ->
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (textValue.isEmpty()) {
                                Text(
                                    text = "00:00",
                                    style = LocalTextStyle.current.copy(
                                        textAlign = TextAlign.Center,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                        fontSize = MaterialTheme.typography.headlineSmall.fontSize
                                    )
                                )
                            }
                            innerTextField()
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Botão de aumentar
            IconButton(
                onClick = {
                    val newValue = (totalMinutos + unit.minutes).coerceIn(minValue, maxValue)
                    onValueChange(newValue)
                },
                enabled = enabled && totalMinutos < maxValue,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                ),
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Aumentar ${unit.label}",
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Seletor de unidade
        if (showUnitSelector) {
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                DurationUnit.entries.forEach { durationUnit ->
                    val isSelected = durationUnit == unit

                    FilterChip(
                        selected = isSelected,
                        onClick = { unit = durationUnit },
                        enabled = enabled,
                        label = {
                            Text(
                                text = durationUnit.label,
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Exibição do valor em texto legível
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = formatMinutesToReadable(totalMinutos),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Formata minutos para HH:MM.
 */
private fun formatMinutesToHHMM(minutes: Int): String {
    val hours = minutes / 60
    val mins = minutes % 60
    return "%02d:%02d".format(hours, mins)
}

/**
 * Parseia HH:MM para minutos.
 */
private fun parseHHMMToMinutes(text: String): Int? {
    return try {
        when {
            text.contains(":") -> {
                val parts = text.split(":")
                val hours = parts.getOrNull(0)?.toIntOrNull() ?: 0
                val minutes = parts.getOrNull(1)?.toIntOrNull() ?: 0
                if (hours in 0..23 && minutes in 0..59) {
                    hours * 60 + minutes
                } else null
            }
            text.length <= 2 -> {
                // Apenas horas ou minutos
                val value = text.toIntOrNull() ?: 0
                if (value <= 23) value * 60 else null
            }
            else -> null
        }
    } catch (e: Exception) {
        null
    }
}

/**
 * Formata minutos para texto legível.
 */
private fun formatMinutesToReadable(minutes: Int): String {
    val hours = minutes / 60
    val mins = minutes % 60

    return when {
        hours == 0 && mins == 0 -> "0 minutos"
        hours == 0 -> "$mins minuto${if (mins != 1) "s" else ""}"
        mins == 0 -> "$hours hora${if (hours != 1) "s" else ""}"
        else -> "$hours hora${if (hours != 1) "s" else ""} e $mins minuto${if (mins != 1) "s" else ""}"
    }
}

// ══════════════════════════════════════════════════════════════════════
// PREVIEW
// ══════════════════════════════════════════════════════════════════════

@Preview(showBackground = true)
@Composable
private fun DurationInputFieldPreview() {
    var value by remember { mutableStateOf(90) } // 1h30

    MaterialTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            DurationInputField(
                totalMinutos = value,
                onValueChange = { value = it },
                label = "Tempo de declaração",
                showUnitSelector = true
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DurationInputFieldSimplePreview() {
    var value by remember { mutableStateOf(30) }

    MaterialTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            DurationInputField(
                totalMinutos = value,
                onValueChange = { value = it },
                label = "Tempo de abono",
                showUnitSelector = false
            )
        }
    }
}
