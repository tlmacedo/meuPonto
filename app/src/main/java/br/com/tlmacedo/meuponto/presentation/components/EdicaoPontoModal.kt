// app/src/main/java/br/com/tlmacedo/meuponto/presentation/components/EdicaoPontoModal.kt

package br.com.tlmacedo.meuponto.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import br.com.tlmacedo.meuponto.domain.model.MotivoEdicao
import br.com.tlmacedo.meuponto.domain.model.Ponto
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Modal/Popup para edição de um registro de ponto.
 *
 * Exibe um dialog sobreposto à tela ativa, permitindo:
 * - Alterar a hora do ponto
 * - Informar/alterar o NSR
 * - Selecionar o motivo da alteração (obrigatório)
 * - Detalhar o motivo (quando exigido)
 *
 * @param ponto Ponto a ser editado
 * @param tipoDescricao Descrição do tipo (Entrada/Saída) - calculada dinamicamente pelo índice
 * @param onDismiss Callback ao fechar o dialog
 * @param onSalvar Callback ao confirmar a edição
 * @param mostrarNsr Se deve exibir o campo NSR
 *
 * @author Thiago
 * @since 7.1.0
 * @updated 7.2.0 - Adicionado parâmetro tipoDescricao para determinação dinâmica do tipo
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EdicaoPontoModal(
    ponto: Ponto,
    tipoDescricao: String,
    onDismiss: () -> Unit,
    onSalvar: (hora: LocalTime, nsr: String?, motivo: MotivoEdicao, detalhes: String?) -> Unit,
    mostrarNsr: Boolean = false
) {
    var hora by remember { mutableStateOf(ponto.hora) }
    var nsr by remember { mutableStateOf(ponto.nsr ?: "") }
    var motivoSelecionado by remember { mutableStateOf(MotivoEdicao.NENHUM) }
    var detalhes by remember { mutableStateOf("") }
    var mostrarTimePicker by remember { mutableStateOf(false) }
    var expandedDropdown by remember { mutableStateOf(false) }

    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy") }

    // Validações
    val motivoValido = when {
        motivoSelecionado == MotivoEdicao.NENHUM -> false
        motivoSelecionado == MotivoEdicao.OUTRO -> detalhes.trim().length >= 5
        motivoSelecionado.requerDetalhes -> detalhes.trim().length >= 5
        else -> true
    }
    val podeSalvar = motivoValido

    // Dialog do TimePicker - usando o componente existente do projeto
    if (mostrarTimePicker) {
        TimePickerDialog(
            titulo = "Alterar Horário",
            horaInicial = hora,
            onConfirm = { novaHora ->
                hora = novaHora
                mostrarTimePicker = false
            },
            onDismiss = {
                mostrarTimePicker = false
            }
        )
    }

    // Dialog principal
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight(),
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
            ) {
                // ══════════════════════════════════════════════════════════
                // HEADER
                // ══════════════════════════════════════════════════════════
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Editar $tipoDescricao",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = ponto.dataHora.format(dateFormatter),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Fechar",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ══════════════════════════════════════════════════════════
                // CAMPO DE HORA
                // ══════════════════════════════════════════════════════════
                Text(
                    text = "Hora",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { mostrarTimePicker = true },
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = hora.format(timeFormatter),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = "Selecionar hora",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                // ══════════════════════════════════════════════════════════
                // CAMPO NSR (condicional)
                // ══════════════════════════════════════════════════════════
                if (mostrarNsr) {
                    Spacer(modifier = Modifier.height(20.dp))

                    OutlinedTextField(
                        value = nsr,
                        onValueChange = { nsr = it },
                        label = { Text("NSR (opcional)") },
                        placeholder = { Text("Número do registro") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // ══════════════════════════════════════════════════════════
                // DROPDOWN DE MOTIVO (obrigatório)
                // ══════════════════════════════════════════════════════════
                Text(
                    text = "Motivo da Alteração *",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))

                ExposedDropdownMenuBox(
                    expanded = expandedDropdown,
                    onExpandedChange = { expandedDropdown = it }
                ) {
                    OutlinedTextField(
                        value = if (motivoSelecionado == MotivoEdicao.NENHUM) "" else motivoSelecionado.descricao,
                        onValueChange = {},
                        readOnly = true,
                        placeholder = { Text("Selecione o motivo") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDropdown)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        isError = motivoSelecionado == MotivoEdicao.NENHUM
                    )

                    ExposedDropdownMenu(
                        expanded = expandedDropdown,
                        onDismissRequest = { expandedDropdown = false }
                    ) {
                        MotivoEdicao.entries
                            .filter { it != MotivoEdicao.NENHUM }
                            .forEach { motivo ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(
                                                text = motivo.descricao,
                                                style = MaterialTheme.typography.bodyLarge
                                            )
                                            if (motivo.requerDetalhes) {
                                                Text(
                                                    text = "Requer detalhamento",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    },
                                    onClick = {
                                        motivoSelecionado = motivo
                                        expandedDropdown = false
                                    }
                                )
                            }
                    }
                }

                // ══════════════════════════════════════════════════════════
                // CAMPO DE DETALHES (condicional)
                // ══════════════════════════════════════════════════════════
                if (motivoSelecionado.requerDetalhes || motivoSelecionado == MotivoEdicao.OUTRO) {
                    Spacer(modifier = Modifier.height(20.dp))

                    OutlinedTextField(
                        value = detalhes,
                        onValueChange = { detalhes = it },
                        label = { Text("Detalhes *") },
                        placeholder = { Text("Descreva o motivo (mín. 5 caracteres)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 100.dp),
                        maxLines = 4,
                        isError = detalhes.trim().length < 5,
                        supportingText = {
                            Text("${detalhes.length}/5 caracteres mínimos")
                        }
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // ══════════════════════════════════════════════════════════
                // BOTÕES DE AÇÃO
                // ══════════════════════════════════════════════════════════
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancelar")
                    }

                    Button(
                        onClick = {
                            onSalvar(
                                hora,
                                nsr.ifBlank { null },
                                motivoSelecionado,
                                if (detalhes.isBlank()) null else detalhes.trim()
                            )
                        },
                        modifier = Modifier.weight(1f),
                        enabled = podeSalvar
                    ) {
                        Text("Salvar")
                    }
                }
            }
        }
    }
}
