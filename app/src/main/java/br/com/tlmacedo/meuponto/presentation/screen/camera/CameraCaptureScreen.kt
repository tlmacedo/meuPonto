package br.com.tlmacedo.meuponto.presentation.screen.camera

import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.core.content.ContextCompat
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import br.com.tlmacedo.meuponto.util.ImageProcessor
import br.com.tlmacedo.meuponto.util.findActivity
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Composable
fun CameraCaptureScreen(
    onImageCaptured: (Uri) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    
    var previewUseCase by remember { mutableStateOf<Preview?>(null) }
    var imageCaptureUseCase by remember { mutableStateOf<ImageCapture?>(null) }
    var analysisUseCase by remember { mutableStateOf<ImageAnalysis?>(null) }
    var cameraSelector by remember { mutableStateOf(CameraSelector.DEFAULT_BACK_CAMERA) }
    var flashMode by remember { mutableStateOf(ImageCapture.FLASH_MODE_OFF) }
    var isCapturing by remember { mutableStateOf(false) }
    
    // Estados para o Auto-Capture
    var isDetected by remember { mutableStateOf(false) }
    var consecutiveDetections by remember { mutableIntStateOf(0) }
    var autoCaptureTriggered by remember { mutableStateOf(false) }

    val previewView = remember { PreviewView(context) }

    // Forçar orientação Retrato ao entrar nesta tela
    DisposableEffect(Unit) {
        val activity = context.findActivity()
        val originalOrientation = activity?.requestedOrientation ?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        onDispose {
            activity?.requestedOrientation = originalOrientation
        }
    }

    LaunchedEffect(cameraSelector) {
        val cameraProvider = context.getCameraProvider()
        
        previewUseCase = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }

        imageCaptureUseCase = ImageCapture.Builder()
            .setFlashMode(flashMode)
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
            .build()

        analysisUseCase = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build().also {
                it.setAnalyzer(cameraExecutor, ReceiptAnalyzer { detected ->
                    isDetected = detected
                    if (detected) {
                        consecutiveDetections++
                    } else {
                        consecutiveDetections = 0
                    }
                })
            }

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                previewUseCase,
                imageCaptureUseCase,
                analysisUseCase
            )
        } catch (e: Exception) {
            Log.e("CameraCaptureScreen", "Falha ao vincular câmera", e)
        }
    }

    // Lógica de Auto-Capture quando detectado por 3 quadros seguidos
    LaunchedEffect(consecutiveDetections) {
        if (consecutiveDetections >= 4 && !isCapturing && !autoCaptureTriggered) {
            autoCaptureTriggered = true
            isCapturing = true
            takePhoto(
                context = context,
                imageCapture = imageCaptureUseCase,
                executor = cameraExecutor,
                onImageCaptured = { uri ->
                    isCapturing = false
                    onImageCaptured(uri)
                },
                onError = {
                    isCapturing = false
                    autoCaptureTriggered = false
                    Log.e("CameraCaptureScreen", "Erro ao capturar foto automática", it)
                }
            )
        }
    }

    LaunchedEffect(flashMode) {
        imageCaptureUseCase?.flashMode = flashMode
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )

        // Overlay de Recibo (Estilo Google Pay)
        ReceiptOverlay(isDetected = isDetected)

        // Controles Superiores
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = Color.White)
            }

            IconButton(onClick = {
                flashMode = if (flashMode == ImageCapture.FLASH_MODE_ON) ImageCapture.FLASH_MODE_OFF 
                           else ImageCapture.FLASH_MODE_ON
            }) {
                Icon(
                    if (flashMode == ImageCapture.FLASH_MODE_ON) Icons.Default.FlashOn else Icons.Default.FlashOff,
                    contentDescription = "Flash",
                    tint = Color.White
                )
            }
        }

        // Rodapé de Ações
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 48.dp)
                .align(Alignment.BottomCenter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Mensagem de Status
            Box(
                modifier = Modifier
                    .background(
                        if (isDetected) Color(0xFF4CAF50).copy(alpha = 0.8f) 
                        else Color.Black.copy(alpha = 0.5f),
                        MaterialTheme.shapes.medium
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = if (autoCaptureTriggered) "Capturando..." 
                           else if (isDetected) "Comprovante detectado! Mantenha firme..."
                           else "Alinhe o comprovante no quadro",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Espaçador para centralizar o botão de captura
                Spacer(modifier = Modifier.size(48.dp))

                // Botão de Captura Manual
                IconButton(
                    onClick = {
                        if (!isCapturing) {
                            isCapturing = true
                            takePhoto(
                                context = context,
                                imageCapture = imageCaptureUseCase,
                                executor = cameraExecutor,
                                onImageCaptured = { uri ->
                                    isCapturing = false
                                    onImageCaptured(uri)
                                },
                                onError = {
                                    isCapturing = false
                                    Log.e("CameraCaptureScreen", "Erro ao capturar foto manual", it)
                                }
                            )
                        }
                    },
                    modifier = Modifier
                        .size(80.dp)
                        .background(Color.White.copy(alpha = 0.2f), CircleShape)
                        .border(4.dp, if (isDetected) Color(0xFF4CAF50) else Color.White, CircleShape)
                        .padding(4.dp)
                ) {
                    if (isCapturing) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(40.dp))
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.White, CircleShape)
                        )
                    }
                }

                // Alternar Câmera
                IconButton(
                    onClick = {
                        cameraSelector = if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA)
                            CameraSelector.DEFAULT_FRONT_CAMERA
                        else
                            CameraSelector.DEFAULT_BACK_CAMERA
                    },
                    modifier = Modifier.size(48.dp).background(Color.White.copy(alpha = 0.2f), CircleShape)
                ) {
                    Icon(Icons.Default.FlipCameraAndroid, contentDescription = "Alternar Câmera", tint = Color.White)
                }
            }
        }
    }
}

@Composable
fun ReceiptOverlay(isDetected: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "scanning")
    val scanAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scanAlpha"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isDetected) Color(0xFF4CAF50) else Color(0xFFD32F2F),
        animationSpec = tween(300),
        label = "borderColor"
    )
    val borderWidth by animateDpAsState(
        targetValue = if (isDetected) 6.dp else 4.dp,
        animationSpec = tween(300),
        label = "borderWidth"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        
        // Retângulo central (Proporção de Cartão de Crédito)
        val rectWidth = width * 0.85f
        val rectHeight = rectWidth * 0.63f 
        val left = (width - rectWidth) / 2
        val top = (height - rectHeight) * 0.35f // Levemente acima do centro
        
        val rect = Rect(left, top, left + rectWidth, top + rectHeight)
        
        val holePath = Path().apply {
            addRoundRect(RoundRect(rect, CornerRadius(24.dp.toPx(), 24.dp.toPx())))
        }
        
        // Fundo escurecido
        clipPath(holePath, clipOp = ClipOp.Difference) {
            drawRect(Color.Black.copy(alpha = 0.7f))
        }
        
        // Borda do quadro
        drawRoundRect(
            color = borderColor,
            topLeft = Offset(left, top),
            size = Size(rectWidth, rectHeight),
            cornerRadius = CornerRadius(24.dp.toPx(), 24.dp.toPx()),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = borderWidth.toPx())
        )

        // Linha de Scan (só aparece se não estiver detectado)
        if (!isDetected) {
            val lineY = top + (rectHeight * scanAlpha)
            drawLine(
                color = Color.White.copy(alpha = 0.4f),
                start = Offset(left + 20.dp.toPx(), lineY),
                end = Offset(left + rectWidth - 20.dp.toPx(), lineY),
                strokeWidth = 2.dp.toPx()
            )
        }
    }
}

@OptIn(ExperimentalGetImage::class)
private class ReceiptAnalyzer(
    private val onReceiptDetected: (Boolean) -> Unit
) : ImageAnalysis.Analyzer {
    // Modelo Latin: Ideal para Português Brasileiro
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private var lastAnalysisTime = 0L

    override fun analyze(imageProxy: ImageProxy) {
        val currentTime = System.currentTimeMillis()
        // Limitar análise para cada 300ms para poupar bateria/CPU e melhorar resposta
        if (currentTime - lastAnalysisTime < 300) {
            imageProxy.close()
            return
        }
        lastAnalysisTime = currentTime

        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    val detected = checkReceiptPatterns(visionText.text)
                    onReceiptDetected(detected)
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }

    private fun checkReceiptPatterns(text: String): Boolean {
        if (text.isBlank()) return false
        val upperText = text.uppercase()
        
        // Palavras-chave típicas de comprovantes de ponto brasileiros (REP)
        val keywords = listOf(
            "COMPROVANTE", "PONTO", "NSR", "DATA", "HORA", 
            "TRABALHADOR", "PIS", "EMPREGADOR", "CNPJ", "SEQ"
        )
        
        // Se encontrar "COMPROVANTE" ou "NSR", já é um forte indício
        if (upperText.contains("COMPROVANTE") || upperText.contains("NSR")) return true
        
        var matches = 0
        for (key in keywords) {
            if (upperText.contains(key)) matches++
        }
        
        // Se encontrar 2 ou mais termos, consideramos um comprovante detectado
        return matches >= 2
    }
}

private fun takePhoto(
    context: Context,
    imageCapture: ImageCapture?,
    executor: ExecutorService,
    onImageCaptured: (Uri) -> Unit,
    onError: (ImageCaptureException) -> Unit
) {
    val outputDirectory = File(context.cacheDir, "temp_camera").apply { if (!exists()) mkdirs() }
    val photoFile = File(
        outputDirectory,
        SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US).format(System.currentTimeMillis()) + ".jpg"
    )

    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

    imageCapture?.takePicture(
        outputOptions,
        executor,
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                // Processamento de imagem para melhorar OCR e Cortar exatamente conforme a máscara visual
                try {
                    val exif = ExifInterface(photoFile.absolutePath)
                    val orientation = exif.getAttributeInt(
                        ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_NORMAL
                    )

                    val matrix = Matrix()
                    when (orientation) {
                        ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                        ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                        ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
                    }

                    var bitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
                    if (bitmap != null) {
                        // 1. Corrigir orientação primeiro
                        if (orientation != ExifInterface.ORIENTATION_NORMAL) {
                            val rotated = Bitmap.createBitmap(
                                bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true
                            )
                            bitmap.recycle()
                            bitmap = rotated
                        }

                        // 2. Calcular as coordenadas da máscara visual (ReceiptOverlay)
                        // Agora que o bitmap está na orientação correta, usamos os mesmos parâmetros da UI
                        val relWidth = 0.85f
                        val bitmapRatio = bitmap.width.toFloat() / bitmap.height.toFloat()
                        val relHeight = 0.63f * relWidth * bitmapRatio
                        
                        val relLeft = (1f - relWidth) / 2f
                        val relTop = (1f - relHeight) * 0.35f
                        
                        // Margem de segurança de 15% sobre as dimensões da máscara
                        val marginFactor = 0.15f
                        val marginW = relWidth * marginFactor
                        val marginH = relHeight * marginFactor

                        val cropLeft = (relLeft - marginW).coerceAtLeast(0f)
                        val cropTop = (relTop - marginH).coerceAtLeast(0f)
                        val cropWidth = (relWidth + 2 * marginW).coerceAtMost(1f - cropLeft)
                        val cropHeight = (relHeight + 2 * marginH).coerceAtMost(1f - cropTop)

                        // 3. Cortar o bitmap original (mais eficiente antes do processamento)
                        val cropped = ImageProcessor.crop(bitmap, cropLeft, cropTop, cropWidth, cropHeight)
                        
                        // 4. Melhorar imagem (Cinza + Contraste 1.6x) para facilitar o OCR
                        val grayscale = ImageProcessor.toGrayscale(cropped)
                        val finalBitmap = ImageProcessor.adjustContrast(grayscale, 1.6f)
                        
                        FileOutputStream(photoFile).use { out ->
                            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
                        }
                        
                        bitmap.recycle()
                        cropped.recycle()
                        grayscale.recycle()
                        finalBitmap.recycle()
                    }
                } catch (e: Exception) {
                    Log.e("CameraCaptureScreen", "Erro ao processar imagem para recorte", e)
                }

                onImageCaptured(Uri.fromFile(photoFile))
            }

            override fun onError(exception: ImageCaptureException) {
                onError(exception)
            }
        }
    )
}

private suspend fun Context.getCameraProvider(): ProcessCameraProvider = suspendCoroutine { continuation ->
    ProcessCameraProvider.getInstance(this).also { cameraProvider ->
        cameraProvider.addListener({
            continuation.resume(cameraProvider.get())
        }, ContextCompat.getMainExecutor(this))
    }
}
