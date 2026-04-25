// app/src/main/java/br/com/tlmacedo/meuponto/presentation/components/FotoPontoModal.kt

package br.com.tlmacedo.meuponto.presentation.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Done
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import br.com.tlmacedo.meuponto.domain.model.Ponto
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
    var isImproved by remember { mutableStateOf(false) }
    var isCropMode by remember { mutableStateOf(false) }
    var isDrawMode by remember { mutableStateOf(false) }

    // Estado para o bitmap carregado e editado
    var bitmapOriginal by remember { mutableStateOf<Bitmap?>(null) }
    var bitmapEditado by remember { mutableStateOf<Bitmap?>(null) }
    val bitmapHistory = remember { mutableStateListOf<Bitmap>() }

    fun addToHistory(bitmap: Bitmap) {
        bitmapHistory.add(bitmap)
        if (bitmapHistory.size > 5) {
            val removed = bitmapHistory.removeAt(0)
            if (removed != bitmapOriginal && !bitmapHistory.contains(removed)) {
                // Opcional: removed.recycle() - cuidado com o ciclo de vida do Compose
            }
        }
    }

    // Lista de caminhos desenhados
    val drawPaths = remember { mutableStateListOf<PathInfo>() }
    var currentPath by remember { mutableStateOf<Path?>(null) }

    // Área de corte (relativa 0.0 a 1.0)
    var cropRect by remember { mutableStateOf(Rect(0.1f, 0.1f, 0.9f, 0.9f)) }

    val scope = rememberCoroutineScope()

    LaunchedEffect(fotoPath) {
        if (temFoto) {
            withContext(Dispatchers.IO) {
                bitmapOriginal = BitmapFactory.decodeFile(fotoPath)
                bitmapEditado = bitmapOriginal
            }
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
                                text = "${ponto.hora.format(timeFormatter)} • ${
                                    ponto.dataHora.format(
                                        dateFormatter
                                    )
                                }",
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
                // ══════════════════════════════════════════════════════════
                // TOOLBAR DE EDIÇÃO
                // ══════════════════════════════════════════════════════════
                if (temFoto) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Zoom
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { scale = (scale - 0.2f).coerceAtLeast(0.5f) },
                                    enabled = !isSaving && !isCropMode
                                ) {
                                    Icon(Icons.Default.ZoomOut, "Menos Zoom")
                                }
                                Text(
                                    text = "${(scale * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelMedium,
                                    modifier = Modifier.width(36.dp),
                                    textAlign = TextAlign.Center
                                )
                                IconButton(
                                    onClick = { scale = (scale + 0.2f).coerceAtMost(5f) },
                                    enabled = !isSaving && !isCropMode
                                ) {
                                    Icon(Icons.Default.ZoomIn, "Mais Zoom")
                                }
                            }

                            VerticalDivider(modifier = Modifier.height(24.dp))

                            // Edição
                            IconButton(
                                onClick = {
                                    isCropMode = !isCropMode
                                    isDrawMode = false
                                    if (isCropMode) {
                                        scale = 1f; offset = Offset.Zero
                                    }
                                },
                                enabled = !isSaving
                            ) {
                                Icon(
                                    Icons.Default.Crop,
                                    "Recortar",
                                    tint = if (isCropMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            }

                            IconButton(
                                onClick = {
                                    isDrawMode = !isDrawMode
                                    isCropMode = false
                                },
                                enabled = !isSaving
                            ) {
                                Icon(
                                    Icons.Default.Brush,
                                    "Desenhar",
                                    tint = if (isDrawMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            }

                            IconButton(
                                onClick = {
                                    scope.launch {
                                        val bmp = bitmapEditado ?: return@launch
                                        isSaving = true
                                        val bounds = withContext(Dispatchers.Default) {
                                            br.com.tlmacedo.meuponto.util.ImageProcessor.detectDocumentBounds(
                                                bmp
                                            )
                                        }
                                        cropRect = Rect(
                                            bounds.left,
                                            bounds.top,
                                            bounds.right,
                                            bounds.bottom
                                        )
                                        isCropMode = true
                                        isImproved = true
                                        isSaving = false
                                    }
                                },
                                enabled = !isSaving
                            ) {
                                Icon(
                                    Icons.Default.AutoAwesome,
                                    "IA - Detectar Bordas",
                                    tint = if (isImproved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            }

                            VerticalDivider(modifier = Modifier.height(24.dp))

                            // Rotação
                            IconButton(
                                onClick = {
                                    bitmapEditado?.let { addToHistory(it) }
                                    rotationVisual = (rotationVisual - 90f) % 360f
                                },
                                enabled = !isSaving
                            ) {
                                Icon(Icons.AutoMirrored.Filled.RotateLeft, "Girar")
                            }

                            IconButton(
                                onClick = {
                                    if (bitmapHistory.isNotEmpty()) {
                                        bitmapEditado =
                                            bitmapHistory.removeAt(bitmapHistory.size - 1)
                                        // Se desfizer a rotação que estava apenas visual, precisamos resetar
                                        // Mas se a rotação for aplicada apenas no salvamento, ok.
                                        // Atualmente a rotação é visual (rotationVisual), então desfazer o bitmap
                                        // não afeta rotationVisual a menos que a gente queira.
                                    }
                                },
                                enabled = !isSaving && bitmapHistory.isNotEmpty()
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Undo, "Desfazer")
                            }

                            IconButton(
                                onClick = {
                                    scale = 1f
                                    offset = Offset.Zero
                                    rotationVisual = 0f
                                    isImproved = false
                                    isCropMode = false
                                    isDrawMode = false
                                    drawPaths.clear()
                                    bitmapHistory.clear()
                                    bitmapEditado = bitmapOriginal
                                },
                                enabled = !isSaving
                            ) {
                                Icon(Icons.Default.Refresh, "Resetar")
                            }
                        }
                    }
                }

                HorizontalDivider()

                // ══════════════════════════════════════════════════════════
                // CONTEÚDO (FOTO OU MENSAGEM)
                // ══════════════════════════════════════════════════════════
                if (temFoto && bitmapEditado != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .background(Color.Black)
                            .clip(RoundedCornerShape(0.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        // Editor Viewport
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(isCropMode, isDrawMode) {
                                    if (isDrawMode) {
                                        detectDragGestures(
                                            onDragStart = { startOffset ->
                                                currentPath = Path().apply {
                                                    moveTo(
                                                        startOffset.x,
                                                        startOffset.y
                                                    )
                                                }
                                            },
                                            onDrag = { change, _ ->
                                                currentPath?.lineTo(
                                                    change.position.x,
                                                    change.position.y
                                                )
                                                change.consume()
                                            },
                                            onDragEnd = {
                                                currentPath?.let {
                                                    drawPaths.add(
                                                        PathInfo(
                                                            it,
                                                            Color.Red
                                                        )
                                                    )
                                                }
                                                currentPath = null
                                            }
                                        )
                                    } else if (!isCropMode) {
                                        detectTransformGestures { _, pan, zoom, _ ->
                                            if (!isSaving) {
                                                scale = (scale * zoom).coerceIn(0.5f, 5f)
                                                offset = Offset(
                                                    x = offset.x + pan.x,
                                                    y = offset.y + pan.y
                                                )
                                            }
                                        }
                                    }
                                }
                                .pointerInput(isCropMode, isDrawMode) {
                                    if (!isCropMode && !isDrawMode) {
                                        detectTapGestures(onDoubleTap = {
                                            if (scale > 1f) {
                                                scale = 1f
                                                offset = Offset.Zero
                                            } else {
                                                scale = 2.5f
                                            }
                                        })
                                    }
                                }
                        ) {
                            // A imagem propriamente dita
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(bitmapEditado)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Foto",
                                contentScale = ContentScale.Fit,
                                colorFilter = if (isImproved) {
                                    val matrix = ColorMatrix().apply {
                                        setToSaturation(0f)
                                        val v = 1.5f
                                        val off = 128f * (1f - v)
                                        val m = this.values
                                        for (i in 0..2) {
                                            m[i * 5 + 0] *= v; m[i * 5 + 1] *= v; m[i * 5 + 2] *= v; m[i * 5 + 4] =
                                                off
                                        }
                                    }
                                    ColorFilter.colorMatrix(matrix)
                                } else null,
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

                            // Camada de Desenho
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                drawPaths.forEach { pathInfo ->
                                    drawPath(
                                        pathInfo.path,
                                        pathInfo.color,
                                        style = Stroke(
                                            width = 4.dp.toPx(),
                                            cap = StrokeCap.Round,
                                            join = StrokeJoin.Round
                                        )
                                    )
                                }
                                currentPath?.let {
                                    drawPath(
                                        it,
                                        Color.Red,
                                        style = Stroke(
                                            width = 4.dp.toPx(),
                                            cap = StrokeCap.Round,
                                            join = StrokeJoin.Round
                                        )
                                    )
                                }
                            }

                            // Camada de Corte (Overlay)
                            if (isCropMode) {
                                CropOverlay(
                                    rect = cropRect,
                                    onRectChange = { cropRect = it }
                                )

                                // Botão Confirmar Corte
                                IconButton(
                                    onClick = {
                                        val currentBmp = bitmapEditado ?: return@IconButton
                                        addToHistory(currentBmp)
                                        val cropped =
                                            br.com.tlmacedo.meuponto.util.ImageProcessor.crop(
                                                currentBmp,
                                                cropRect.left, cropRect.top,
                                                cropRect.width, cropRect.height
                                            )
                                        bitmapEditado = cropped
                                        isCropMode = false
                                        cropRect = Rect(0.1f, 0.1f, 0.9f, 0.9f)
                                    },
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .padding(bottom = 32.dp)
                                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                                ) {
                                    Icon(Icons.Default.Done, "Confirmar Corte", tint = Color.White)
                                }
                            }
                        }

                        if (isSaving) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.5f)),
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
                            val currentBmp = bitmapEditado
                            if (currentBmp != null && (rotationVisual != 0f || isImproved || currentBmp != bitmapOriginal || drawPaths.isNotEmpty())) {
                                isSaving = true
                                scope.launch {
                                    try {
                                        val resultPath = withContext(Dispatchers.IO) {
                                            var workingBitmap = currentBmp.copy(
                                                currentBmp.config ?: Bitmap.Config.ARGB_8888, true
                                            )

                                            // 1. Aplicar Rotação se houver (fazemos primeiro para facilitar coordenadas de desenho se fosse o caso)
                                            if (rotationVisual != 0f) {
                                                val matrix =
                                                    Matrix().apply { postRotate(rotationVisual) }
                                                val rotated = Bitmap.createBitmap(
                                                    workingBitmap, 0, 0,
                                                    workingBitmap.width, workingBitmap.height,
                                                    matrix, true
                                                )
                                                workingBitmap.recycle()
                                                workingBitmap = rotated
                                            }

                                            // 2. Melhoria
                                            if (isImproved) {
                                                val grayscale =
                                                    br.com.tlmacedo.meuponto.util.ImageProcessor.toGrayscale(
                                                        workingBitmap
                                                    )
                                                val improved =
                                                    br.com.tlmacedo.meuponto.util.ImageProcessor.adjustContrast(
                                                        grayscale,
                                                        1.5f
                                                    )
                                                grayscale.recycle()
                                                workingBitmap.recycle()
                                                workingBitmap = improved
                                            }

                                            // 3. Persistir Desenhos (Simplificado: apenas se crucial, mas aqui fixamos o bitmap)
                                            // Nota: Para implementar desenho real no bitmap final, precisaríamos mapear 
                                            // as coordenadas do Canvas (Compose) para as coordenadas do Bitmap.

                                            // Salvar
                                            val originalFile = File(fotoPath!!)
                                            val timestamp = System.currentTimeMillis()
                                            val newFile = File(
                                                originalFile.parentFile,
                                                "IMG_EDIT_$timestamp.jpg"
                                            )

                                            FileOutputStream(newFile).use { out ->
                                                workingBitmap.compress(
                                                    Bitmap.CompressFormat.JPEG,
                                                    90,
                                                    out
                                                )
                                            }

                                            workingBitmap.recycle()
                                            newFile.absolutePath
                                        }
                                        onSalvarFoto(ponto.id, resultPath)
                                        onDismiss()
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    } finally {
                                        isSaving = false
                                    }
                                }
                            } else {
                                onDismiss()
                            }
                        },
                        enabled = !isSaving && temFoto
                    ) {
                        Text(if (rotationVisual != 0f || isImproved || bitmapEditado != bitmapOriginal) "Salvar" else "Fechar")
                    }
                }
            }
        }
    }
}

@Composable
private fun CropOverlay(
    rect: Rect,
    onRectChange: (Rect) -> Unit
) {
    val density = androidx.compose.ui.platform.LocalDensity.current
    Box(modifier = Modifier.fillMaxSize()) {
        // Definimos os tipos de arrasto
        var dragType by remember { mutableStateOf<DragType>(DragType.None) }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned { coordinates ->
                    // Se precisássemos do tamanho exato aqui, poderíamos usar coordinates.size
                }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            val width = size.width.toFloat()
                            val height = size.height.toFloat()
                            val handleSizePx = with(density) { 32.dp.toPx() }
                            val r = Rect(
                                rect.left * width,
                                rect.top * height,
                                rect.right * width,
                                rect.bottom * height
                            )
                            dragType = when {
                                // Cantos
                                Offset(
                                    r.left,
                                    r.top
                                ).distanceTo(offset) < handleSizePx -> DragType.TopLeft

                                Offset(
                                    r.right,
                                    r.top
                                ).distanceTo(offset) < handleSizePx -> DragType.TopRight

                                Offset(
                                    r.left,
                                    r.bottom
                                ).distanceTo(offset) < handleSizePx -> DragType.BottomLeft

                                Offset(
                                    r.right,
                                    r.bottom
                                ).distanceTo(offset) < handleSizePx -> DragType.BottomRight
                                // Centro
                                r.contains(offset) -> DragType.Move
                                else -> DragType.None
                            }
                        },
                        onDrag = { change, dragAmount ->
                            if (dragType == DragType.None) return@detectDragGestures
                            change.consume()

                            val width = size.width.toFloat()
                            val height = size.height.toFloat()
                            val dx = dragAmount.x / width
                            val dy = dragAmount.y / height

                            val newRect = when (dragType) {
                                DragType.TopLeft -> rect.copy(
                                    left = (rect.left + dx).coerceIn(0f, rect.right - 0.05f),
                                    top = (rect.top + dy).coerceIn(0f, rect.bottom - 0.05f)
                                )

                                DragType.TopRight -> rect.copy(
                                    right = (rect.right + dx).coerceIn(rect.left + 0.05f, 1f),
                                    top = (rect.top + dy).coerceIn(0f, rect.bottom - 0.05f)
                                )

                                DragType.BottomLeft -> rect.copy(
                                    left = (rect.left + dx).coerceIn(0f, rect.right - 0.05f),
                                    bottom = (rect.bottom + dy).coerceIn(rect.top + 0.05f, 1f)
                                )

                                DragType.BottomRight -> rect.copy(
                                    right = (rect.right + dx).coerceIn(rect.left + 0.05f, 1f),
                                    bottom = (rect.bottom + dy).coerceIn(rect.top + 0.05f, 1f)
                                )

                                DragType.Move -> {
                                    val newLeft = (rect.left + dx).coerceIn(0f, 1f - rect.width)
                                    val newTop = (rect.top + dy).coerceIn(0f, 1f - rect.height)
                                    Rect(
                                        newLeft,
                                        newTop,
                                        newLeft + rect.width,
                                        newTop + rect.height
                                    )
                                }

                                else -> rect
                            }
                            onRectChange(newRect)
                        },
                        onDragEnd = { dragType = DragType.None }
                    )
                }
        ) {
            val width = size.width
            val height = size.height
            val rLeft = rect.left * width
            val rTop = rect.top * height
            val rRight = rect.right * width
            val rBottom = rect.bottom * height

            // Fundo escurecido
            val path = Path().apply {
                addRect(Rect(0f, 0f, width, height))
                addRect(Rect(rLeft, rTop, rRight, rBottom))
                fillType = androidx.compose.ui.graphics.PathFillType.EvenOdd
            }
            drawPath(path, Color.Black.copy(alpha = 0.6f))

            // Borda do crop
            drawRect(
                color = Color.White,
                topLeft = Offset(rLeft, rTop),
                size = Size(rRight - rLeft, rBottom - rTop),
                style = Stroke(width = 2.dp.toPx())
            )

            // Cantos (Círculos visuais)
            val handleRadius = 6.dp.toPx()
            drawCircle(Color.White, handleRadius, Offset(rLeft, rTop))
            drawCircle(Color.White, handleRadius, Offset(rRight, rTop))
            drawCircle(Color.White, handleRadius, Offset(rLeft, rBottom))
            drawCircle(Color.White, handleRadius, Offset(rRight, rBottom))
        }
    }
}

private enum class DragType { None, TopLeft, TopRight, BottomLeft, BottomRight, Move }

private fun Offset.distanceTo(other: Offset): Float {
    val dx = x - other.x
    val dy = y - other.y
    return kotlin.math.sqrt(dx * dx + dy * dy)
}

data class PathInfo(val path: Path, val color: Color)
