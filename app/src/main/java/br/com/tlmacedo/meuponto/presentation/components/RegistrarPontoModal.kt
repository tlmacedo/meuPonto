// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/components/RegistrarPontoModal.kt
package br.com.tlmacedo.meuponto.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import java.time.LocalTime
import br.com.tlmacedo.meuponto.domain.model.TipoNsr
import br.com.tlmacedo.meuponto.domain.usecase.ponto.ProximoPonto
import br.com.tlmacedo.meuponto.presentation.screen.home.RegistrarPontoModalState
import br.com.tlmacedo.meuponto.presentation.theme.EntradaColor
import br.com.tlmacedo.meuponto.presentation.theme.SaidaColor
import coil.compose.AsyncImage

/**
 * Modal unificado para registro de ponto.
 * Centraliza Hora, NSR, Foto e Localização em uma única interface.
 *
 * @author Thiago
 * @since 12.0.0
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrarPontoModal(
    state: RegistrarPontoModalState,
    proximoTipo: ProximoPonto,
    nsrHabilitado: Boolean,
    tipoNsr: TipoNsr,
    fotoHabilitada: Boolean,
    fotoObrigatoria: Boolean,
    configLocalizacaoHabilitada: Boolean,
    onNsrChange: (String) -> Unit,
    onCapturarFoto: () -> Unit,
    onRemoverFoto: () -> Unit,
    onCapturarLocalizacao: () -> Unit,
    onAbrirTimePicker: () -> Unit,
    onFecharTimePicker: () -> Unit,
    onHoraSelecionada: (LocalTime) -> Unit,
    onConfirmar: () -> Unit,
    onDismiss: () -> Unit
) {
    val corPrincipal = if (proximoTipo.isEntrada) EntradaColor else SaidaColor
    val temDadosLocalizacao = state.latitude != null || state.longitude != null || state.isCapturingLocation || state.erroLocalizacao != null

    val podeConfirmar = !state.isSaving && !state.isProcessingOcr &&
            (!fotoObrigatoria || state.fotoUri != null) &&
            (!nsrHabilitado || state.nsr.isNotBlank())

    if (state.showTimePicker) {
        TimePickerDialog(
            titulo = "Ajustar horário",
            horaInicial = state.dataHora.toLocalTime(),
            onConfirm = onHoraSelecionada,
            onDismiss = onFecharTimePicker
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = !state.isSaving,
            dismissOnClickOutside = !state.isSaving
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
                            text = "Registrar ${proximoTipo.descricao}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = corPrincipal
                        )
                        Text(
                            text = state.dataFormatada,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    if (!state.isSaving) {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Fechar")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // HORA
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(!state.isSaving) { onAbrirTimePicker() }
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = state.horaFormatada,
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
                    }
                    Text(
                        text = "Toque para ajustar o horário",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // FOTO (se habilitada)
                if (fotoHabilitada) {
                    Text(
                        text = "Foto do Comprovante" + if (fotoObrigatoria) " *" else "",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    if (state.fotoUri != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(12.dp))
                        ) {
                            AsyncImage(
                                model = state.fotoUri,
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
                                    .clickable(!state.isProcessingOcr) { onRemoverFoto() },
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Remover foto",
                                    tint = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.padding(6.dp)
                                )
                            }

                            if (state.isProcessingOcr) {
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

                // NSR (se habilitado)
                if (nsrHabilitado) {
                    OutlinedTextField(
                        value = state.nsr,
                        onValueChange = onNsrChange,
                        label = { Text("NSR *") },
                        placeholder = { Text("Número Sequencial de Registro") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = if (tipoNsr == TipoNsr.NUMERICO) KeyboardType.Number else KeyboardType.Text
                        ),
                        leadingIcon = { Icon(Icons.Default.Pin, contentDescription = null) }
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                }

                // LOCALIZAÇÃO (se houver dados ou estiver capturando)
                if (configLocalizacaoHabilitada || temDadosLocalizacao) {
                    Text(
                        text = "Localização",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.outlinedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (state.erroLocalizacao != null) Icons.Default.LocationOff
                                else Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = if (state.erroLocalizacao != null) MaterialTheme.colorScheme.error
                                else corPrincipal,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                if (state.isCapturingLocation) {
                                    LinearProgressIndicator(
                                        modifier = Modifier.fillMaxWidth().height(2.dp),
                                        color = corPrincipal
                                    )
                                    Text(
                                        text = "Obtendo localização...",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                } else if (state.erroLocalizacao != null) {
                                    Text(
                                        text = state.erroLocalizacao,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                } else if (state.endereco != null) {
                                    Text(
                                        text = state.endereco,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                } else {
                                    Text(
                                        text = "Localização não capturada",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            if (!state.isCapturingLocation && !state.isSaving) {
                                IconButton(onClick = onCapturarLocalizacao) {
                                    Icon(
                                        Icons.Default.Refresh,
                                        contentDescription = "Atualizar localização",
                                        modifier = Modifier.size(20.dp),
                                        tint = corPrincipal
                                    )
                                }
                            }
                        }
                    }
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
                        enabled = !state.isSaving
                    ) {
                        Text("Cancelar")
                    }

                    Button(
                        onClick = onConfirmar,
                        modifier = Modifier.weight(1f),
                        enabled = podeConfirmar,
                        colors = ButtonDefaults.buttonColors(containerColor = corPrincipal)
                    ) {
                        if (state.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Confirmar")
                        }
                    }
                }
            }
        }
    }
}
