// app/src/main/java/br/com/tlmacedo/meuponto/presentation/components/FotoPontoModal.kt

package br.com.tlmacedo.meuponto.presentation.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.RotateLeft
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HideImage
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
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
import java.io.FileOutputStream
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
 * @param onSalvarFoto Callback para salvar a foto editada (opcional)
 *
 * @author Thiago
 * @since 7.2.0
 */
@Composable
fun FotoPontoModal(
    ponto: Ponto,
    tipoDescricao: String,
    fotoPath: String?,
    onDismiss: () -> Unit,
    onSalvarFoto: (Long, String) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy") }

    val temFoto = !fotoPath.isNullOrBlank() && File(fotoPath).exists()

    // Estados para zoom, pan e rotação visual
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var rotationVisual by remember { mutableFloatStateOf(0f) }
    var isSaving by remember { mutableStateOf(false) }

    // Estado para o bitmap carregado (usado para salvar edições)
    var bitmapOriginal by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(fotoPath) {
        if (temFoto) {
            bitmapOriginal = BitmapFactory.decodeFile(fotoPath)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = !isSaving,
            dismissOnClickOutside = !isSaving,
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

                    IconButton(onClick = onDismiss, enabled = !isSaving) {
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
                    // Controles de zoom e rotação
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { scale = (scale - 0.2f).coerceAtLeast(0.5f) },
                                enabled = !isSaving
                            ) {
                                Icon(imageVector = Icons.Default.ZoomOut, contentDescription = "Diminuir zoom")
                            }

                            Text(
                                text = "${(scale * 100).toInt()}%",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.width(44.dp),
                                textAlign = TextAlign.Center
                            )

                            IconButton(
                                onClick = { scale = (scale + 0.2f).coerceAtMost(5f) },
                                enabled = !isSaving
                            ) {
                                Icon(imageVector = Icons.Default.ZoomIn, contentDescription = "Aumentar zoom")
                            }
                        }

                        VerticalDivider(modifier = Modifier.height(24.dp))

                        Row {
                            IconButton(
                                onClick = { rotationVisual = (rotationVisual - 90f) % 360f },
                                enabled = !isSaving
                            ) {
                                Icon(imageVector = Icons.AutoMirrored.Filled.RotateLeft, contentDescription = "Girar para esquerda")
                            }
                            IconButton(
                                onClick = { rotationVisual = (rotationVisual + 90f) % 360f },
                                enabled = !isSaving
                            ) {
                                Icon(imageVector = Icons.AutoMirrored.Filled.RotateRight, contentDescription = "Girar para direita")
                            }
                        }

                        VerticalDivider(modifier = Modifier.height(24.dp))

                        IconButton(
                            onClick = {
                                scale = 1f
                                offset = Offset.Zero
                                rotationVisual = 0f
                            },
                            enabled = !isSaving
                        ) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = "Resetar")
                        }
                    }

                    // Imagem com zoom, pan e rotação
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .background(Color.Black)
                            .clip(RoundedCornerShape(0.dp))
                            .pointerInput(Unit) {
                                detectTransformGestures { _, pan, zoom, _ ->
                                    if (!isSaving) {
                                        scale = (scale * zoom).coerceIn(0.5f, 5f)
                                        offset = Offset(x = offset.x + pan.x, y = offset.y + pan.y)
                                    }
                                }
                            }
                            .pointerInput(Unit) {
                                detectTapGestures(onDoubleTap = {
                                    if (scale > 1f) {
                                        scale = 1f
                                        offset = Offset.Zero
                                    } else {
                                        scale = 2.5f
                                    }
                                })
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(File(fotoPath!!))
                                .diskCacheKey("${fotoPath}_${File(fotoPath).lastModified()}")
                                .memoryCacheKey("${fotoPath}_${File(fotoPath).lastModified()}")
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
                                    translationY = offset.y,
                                    rotationZ = rotationVisual
                                )
                        )

                        if (isSaving) {
                            Box(
                                modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = Color.White)
                            }
                        }
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
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss, enabled = !isSaving) {
                        Text("Cancelar")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (rotationVisual != 0f && bitmapOriginal != null) {
                                isSaving = true
                                val matrix = Matrix().apply { postRotate(rotationVisual) }
                                val rotatedBitmap = Bitmap.createBitmap(
                                    bitmapOriginal!!, 0, 0,
                                    bitmapOriginal!!.width, bitmapOriginal!!.height,
                                    matrix, true
                                )
                                try {
                                    FileOutputStream(fotoPath!!).use { out ->
                                        rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                                    }
                                    onSalvarFoto(ponto.id, fotoPath!!)
                                    onDismiss()
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                } finally {
                                    isSaving = false
                                }
                            } else {
                                onDismiss()
                            }
                        },
                        enabled = !isSaving && temFoto
                    ) {
                        Text(if (rotationVisual != 0f) "Salvar" else "Fechar")
                    }
                }
            }
        }
    }
}
