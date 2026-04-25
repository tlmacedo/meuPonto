// app/src/main/java/br/com/tlmacedo/meuponto/presentation/components/EdicaoPontoModal.kt

package br.com.tlmacedo.meuponto.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import br.com.tlmacedo.meuponto.domain.model.MotivoEdicao
import br.com.tlmacedo.meuponto.domain.model.Ponto
import br.com.tlmacedo.meuponto.domain.model.TipoNsr
import br.com.tlmacedo.meuponto.presentation.theme.EntradaColor
import br.com.tlmacedo.meuponto.presentation.theme.SaidaColor
import coil.compose.AsyncImage
import java.io.File
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
 * @param onConfirmar Callback ao confirmar a edição
 * @param isSaving Se está processando a gravação
 * @param mostrarNsr Se deve exibir o campo NSR
 *
 * @author Thiago
 * @since 7.1.0
 * @updated 7.2.0 - Adicionado parâmetro tipoDescricao para determinação dinâmica do tipo
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EdicaoModal(
    ponto: Ponto,
    tipoDescricao: String,
    onDismiss: () -> Unit,
    onConfirmar: (hora: LocalTime, nsr: String?, motivo: MotivoEdicao, detalhes: String?, observacao: String?) -> Unit,
    isSaving: Boolean = false,
    mostrarNsr: Boolean = false,
    nsrHabilitado: Boolean = false,
    tipoNsr: TipoNsr = TipoNsr.NUMERICO,
    fotoHabilitada: Boolean = false,
    fotoUri: android.net.Uri? = null,
    fotoPathAbsoluto: String? = null,
    fotoRemovida: Boolean = false,
    isProcessingOcr: Boolean = false,
    comentarioHabilitado: Boolean = true,
    onCapturarFoto: () -> Unit = {},
    onRemoverFoto: () -> Unit = {},
    onReprocessarOcr: () -> Unit = {}
) {
    var hora by remember { mutableStateOf(ponto.hora) }
    var nsr by remember { mutableStateOf(ponto.nsr ?: "") }
    var observacao by remember { mutableStateOf(ponto.observacao ?: "") }
    var motivoSelecionado by remember { mutableStateOf(MotivoEdicao.NENHUM) }
    var detalhes by remember { mutableStateOf("") }
    var mostrarTimePicker by remember { mutableStateOf(false) }
    var expandedDropdown by remember { mutableStateOf(false) }

    var nsrAutoFilled by remember { mutableStateOf(ponto.nsrAutoFilled) }
    var horaAutoFilled by remember { mutableStateOf(ponto.horaAutoFilled) }
    var dataAutoFilled by remember { mutableStateOf(ponto.dataAutoFilled) }

    val corPrincipal =
        if (tipoDescricao.contains("Entrada", ignoreCase = true)) EntradaColor else SaidaColor
    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy") }

    // Validações
    val motivoValido = when {
        motivoSelecionado == MotivoEdicao.NENHUM -> false
        motivoSelecionado == MotivoEdicao.OUTRO -> detalhes.trim().length >= 5
        motivoSelecionado.requerDetalhes -> detalhes.trim().length >= 5
        else -> true
    }
    val podeSalvar = motivoValido && !isSaving && (!nsrHabilitado || nsr.isNotBlank())

    // Dialog do TimePicker
    if (mostrarTimePicker) {
        TimePickerDialog(
            titulo = "Alterar Horário",
            horaInicial = hora,
            onConfirm = { novaHora ->
                if (novaHora != hora) {
                    hora = novaHora
                    horaAutoFilled = false
                }
                mostrarTimePicker = false
            },
            onDismiss = {
                mostrarTimePicker = false
            }
        )
    }

    Dialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        properties = DialogProperties(
            dismissOnBackPress = !isSaving,
            dismissOnClickOutside = !isSaving,
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
                .padding(vertical = 24.dp),
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
            ) {
                // HEADER
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
                            color = corPrincipal
                        )
                        Text(
                            text = ponto.dataHora.format(dateFormatter),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (dataAutoFilled) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Extraído do comprovante",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    if (!isSaving) {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Fechar")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // HORA (Igual ao RegistrarPontoModal)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(!isSaving) { mostrarTimePicker = true }
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = hora.format(timeFormatter),
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Editar horário",
                            tint = corPrincipal.copy(alpha = 0.6f),
                            modifier = Modifier.size(24.dp)
                        )
                        if (horaAutoFilled) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Extraído do comprovante",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Text(
                        text = "Toque para ajustar o horário",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // FOTO (Igual ao RegistrarPontoModal)
                if (fotoHabilitada) {
                    Text(
                        text = "Foto do Comprovante",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    val temNovaFoto = fotoUri != null
                    val temFotoExistente =
                        !fotoRemovida && !fotoPathAbsoluto.isNullOrBlank() && File(fotoPathAbsoluto).exists()

                    if (temNovaFoto || temFotoExistente) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(12.dp))
                        ) {
                            AsyncImage(
                                model = if (temNovaFoto) fotoUri else File(fotoPathAbsoluto!!),
                                contentDescription = "Comprovante",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            Surface(
                                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f),
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable { onRemoverFoto() },
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Remover foto",
                                    tint = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.padding(6.dp)
                                )
                            }

                            // Botão Tirar Outra Foto (Câmera) - Adicionado
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(top = 8.dp, end = 88.dp)
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable(!isProcessingOcr) { onCapturarFoto() },
                            ) {
                                Icon(
                                    Icons.Default.AddAPhoto,
                                    contentDescription = "Substituir foto",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(6.dp)
                                )
                            }

                            // Botão Reprocessar OCR - Adicionado
                            Surface(
                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f),
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(top = 8.dp, end = 48.dp)
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable(!isProcessingOcr) { onReprocessarOcr() },
                            ) {
                                Icon(
                                    Icons.Default.Refresh,
                                    contentDescription = "Reprocessar OCR",
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(6.dp)
                                )
                            }

                            if (isProcessingOcr) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(32.dp),
                                            color = corPrincipal
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "Analisando comprovante...",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = corPrincipal,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        OutlinedCard(
                            onClick = onCapturarFoto,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.outlinedCardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                            )
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    Icons.Default.AddAPhoto,
                                    contentDescription = null,
                                    tint = corPrincipal
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Tirar ou selecionar foto",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = corPrincipal
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }

                // NSR
                if (nsrHabilitado) {
                    OutlinedTextField(
                        value = nsr,
                        onValueChange = {
                            nsr = it
                            nsrAutoFilled = false
                        },
                        label = { Text("NSR *") },
                        placeholder = { Text("Número Sequencial de Registro") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = if (tipoNsr == TipoNsr.NUMERICO) KeyboardType.Number else KeyboardType.Text
                        ),
                        leadingIcon = { Icon(Icons.Default.Pin, contentDescription = null) },
                        trailingIcon = if (nsrAutoFilled) {
                            {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "Extraído do comprovante",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        } else null,
                        enabled = !isSaving
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                }

                // OBSERVAÇÃO (se habilitada)
                if (comentarioHabilitado) {
                    OutlinedTextField(
                        value = observacao,
                        onValueChange = { observacao = it },
                        label = { Text("Observação") },
                        placeholder = { Text("Adicione um comentário opcional") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 4,
                        leadingIcon = {
                            Icon(
                                Icons.AutoMirrored.Filled.Comment,
                                contentDescription = "Campo Observação"
                            )
                        },
                        enabled = !isSaving
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                }

                // MOTIVO (Essencial para edição)
                Text(
                    text = "Motivo da Alteração *",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

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
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        isError = motivoSelecionado == MotivoEdicao.NENHUM,
                        enabled = !isSaving
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
                        },
                        enabled = !isSaving
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // AÇÕES
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        enabled = !isSaving
                    ) {
                        Text("Cancelar")
                    }

                    Button(
                        onClick = {
                            onConfirmar(
                                hora,
                                nsr.ifBlank { null },
                                motivoSelecionado,
                                if (detalhes.isBlank()) null else detalhes.trim(),
                                observacao.ifBlank { null }
                            )
                        },
                        modifier = Modifier.weight(1f),
                        enabled = podeSalvar,
                        colors = ButtonDefaults.buttonColors(containerColor = corPrincipal)
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
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
