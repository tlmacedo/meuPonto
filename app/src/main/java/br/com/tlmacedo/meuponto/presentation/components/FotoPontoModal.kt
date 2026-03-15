// app/src/main/java/br/com/tlmacedo/meuponto/presentation/components/FotoPontoModal.kt

package br.com.tlmacedo.meuponto.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import br.com.tlmacedo.meuponto.domain.model.Ponto
import coil.compose.AsyncImage
import coil.request.ImageRequest
import java.io.File
import java.time.format.DateTimeFormatter

/**
 * Modal para visualização da foto de comprovante de um registro de ponto.
 *
 * Permite zoom e pan na imagem.
 *
 * @param ponto Ponto com foto a ser exibida
 * @param tipoDescricao Descrição do tipo (Entrada/Saída) - calculada dinamicamente pelo índice
 * @param fotoPath Caminho do arquivo da foto
 * @param onDismiss Callback ao fechar o modal
 *
 * @author Thiago
 * @since 7.2.0
 */
@Composable
fun FotoPontoModal(
    ponto: Ponto,
    tipoDescricao: String,
    fotoPath: String?,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy") }

    val temFoto = !fotoPath.isNullOrBlank() && File(fotoPath).exists()

    // Estados para zoom e pan
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

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
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f),
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // ══════════════════════════════════════════════════════════
                // HEADER
                // ══════════════════════════════════════════════════════════
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.secondaryContainer)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Photo,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "Comprovante - $tipoDescricao",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${ponto.hora.format(timeFormatter)} • ${ponto.dataHora.format(dateFormatter)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Fechar",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                HorizontalDivider()

                // ══════════════════════════════════════════════════════════
                // CONTEÚDO (FOTO OU MENSAGEM)
                // ══════════════════════════════════════════════════════════
                if (temFoto) {
                    // Controles de zoom
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                scale = (scale - 0.5f).coerceAtLeast(0.5f)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ZoomOut,
                                contentDescription = "Diminuir zoom"
                            )
                        }

                        Text(
                            text = "${(scale * 100).toInt()}%",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        IconButton(
                            onClick = {
                                scale = (scale + 0.5f).coerceAtMost(4f)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ZoomIn,
                                contentDescription = "Aumentar zoom"
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        IconButton(
                            onClick = {
                                scale = 1f
                                offset = Offset.Zero
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Resetar zoom"
                            )
                        }
                    }

                    // Imagem com zoom e pan
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                            .pointerInput(Unit) {
                                detectTransformGestures { _, pan, zoom, _ ->
                                    scale = (scale * zoom).coerceIn(0.5f, 4f)
                                    offset = Offset(
                                        x = offset.x + pan.x,
                                        y = offset.y + pan.y
                                    )
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(File(fotoPath!!))
                                .crossfade(true)
                                .build(),
                            contentDescription = "Foto do comprovante",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer(
                                    scaleX = scale,
                                    scaleY = scale,
                                    translationX = offset.x,
                                    translationY = offset.y
                                )
                        )
                    }
                } else {
                    // Sem foto
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.HideImage,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(64.dp)
                            )
                            Text(
                                text = "Foto não disponível",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Este registro não possui foto de comprovante\nou o arquivo foi removido.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                // ══════════════════════════════════════════════════════════
                // FOOTER
                // ══════════════════════════════════════════════════════════
                HorizontalDivider()

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(onClick = onDismiss) {
                        Text("Fechar")
                    }
                }
            }
        }
    }
}
