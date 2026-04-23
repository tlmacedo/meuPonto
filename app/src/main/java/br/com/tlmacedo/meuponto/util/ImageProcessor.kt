// path: app/src/main/java/br/com/tlmacedo/meuponto/util/ImageProcessor.kt
package br.com.tlmacedo.meuponto.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.Rect
import androidx.core.graphics.createBitmap

/**
 * Utilitário para processamento de imagem voltado a OCR.
 *
 * Melhorias:
 * - Parâmetro opcional blurBeforeBinary para suavizar ruído antes da binarização.
 * - Mantém responsabilidade de reciclar APENAS bitmaps criados aqui,
 *   quem chamou continua dono do bitmap original.
 */
object ImageProcessor {

    /**
     * Converte o bitmap para grayscale via ColorMatrix (rápido e estável).
     */
    fun toGrayscale(src: Bitmap): Bitmap {
        val width = src.width
        val height = src.height
        val dest = createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(dest)
        val paint = Paint()
        val colorMatrix = ColorMatrix().apply { setSaturation(0f) }
        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(src, 0f, 0f, paint)
        return dest
    }

    /**
     * Ajusta o contraste do bitmap.
     *
     * @param contrast 1.0 = neutro; > 1 aumenta contraste; < 1 reduz.
     */
    fun adjustContrast(src: Bitmap, contrast: Float): Bitmap {
        val dest = createBitmap(
            src.width,
            src.height,
            src.config ?: Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(dest)
        val paint = Paint()

        // T' = T * contrast + offset (offset para manter brilho médio)
        val offset = 128f * (1f - contrast)
        val matrix = ColorMatrix(
            floatArrayOf(
                contrast, 0f, 0f, 0f, offset,
                0f, contrast, 0f, 0f, offset,
                0f, 0f, contrast, 0f, offset,
                0f, 0f, 0f, 1f, 0f
            )
        )

        paint.colorFilter = ColorMatrixColorFilter(matrix)
        canvas.drawBitmap(src, 0f, 0f, paint)
        return dest
    }

    /**
     * Aplica um blur simples (box blur 3x3) para suavizar serrilhado/ruído.
     * Útil antes da binarização em recibos impressos.
     */
    fun applyBoxBlur(src: Bitmap): Bitmap {
        val width = src.width
        val height = src.height
        val dest = createBitmap(width, height, Bitmap.Config.ARGB_8888)

        val inPixels = IntArray(width * height)
        val outPixels = IntArray(width * height)
        src.getPixels(inPixels, 0, width, 0, 0, width, height)

        // Kernel 3x3 com peso 1/9
        for (y in 0 until height) {
            for (x in 0 until width) {
                var rSum = 0
                var gSum = 0
                var bSum = 0
                var count = 0

                for (dy in -1..1) {
                    val ny = y + dy
                    if (ny !in 0 until height) continue
                    for (dx in -1..1) {
                        val nx = x + dx
                        if (nx !in 0 until width) continue
                        val color = inPixels[ny * width + nx]
                        rSum += (color shr 16) and 0xFF
                        gSum += (color shr 8) and 0xFF
                        bSum += color and 0xFF
                        count++
                    }
                }

                val r = (rSum / count)
                val g = (gSum / count)
                val b = (bSum / count)
                outPixels[y * width + x] =
                    (0xFF shl 24) or (r shl 16) or (g shl 8) or b
            }
        }

        dest.setPixels(outPixels, 0, width, 0, 0, width, height)
        return dest
    }

    /**
     * Binarização simples (preto e branco) baseada em limiar fixo.
     *
     * Limitação: threshold fixo pode falhar em iluminação muito desigual.
     * Por isso, usamos blur opcional + fallback no OcrService.
     */
    fun toBinary(src: Bitmap, threshold: Int = 128): Bitmap {
        val width = src.width
        val height = src.height
        val dest = createBitmap(width, height, Bitmap.Config.ARGB_8888)

        val pixels = IntArray(width * height)
        src.getPixels(pixels, 0, width, 0, 0, width, height)

        for (i in pixels.indices) {
            val color = pixels[i]
            val r = (color shr 16) and 0xFF
            val g = (color shr 8) and 0xFF
            val b = color and 0xFF
            val gray = (r * 0.299 + g * 0.587 + b * 0.114).toInt()
            val binary = if (gray > threshold) {
                0xFFFFFFFF.toInt()
            } else {
                0xFF000000.toInt()
            }
            pixels[i] = binary
        }

        dest.setPixels(pixels, 0, width, 0, 0, width, height)
        return dest
    }

    /**
     * Aplica filtros de OCR:
     *
     * 1. Grayscale
     * 2. Contraste
     * 3. (Opcional) blurBeforeBinary
     * 4. (Opcional) binarização
     *
     * Responsabilidade de reciclagem:
     * - Bitmaps criados aqui (grayscale, contrasted, blur, binary) são geridos aqui.
     * - O bitmap src continua sendo responsabilidade do chamador.
     */
    fun applyOcrFilters(
        src: Bitmap,
        contrast: Float = 1.6f,
        binarize: Boolean = false,
        blurBeforeBinary: Boolean = false
    ): Bitmap {
        val grayscale = toGrayscale(src)
        val contrasted = adjustContrast(grayscale, contrast)

        val preBinary = if (binarize && blurBeforeBinary) {
            val blurred = applyBoxBlur(contrasted)
            blurred
        } else {
            contrasted
        }

        val final = if (binarize) {
            val binary = toBinary(preBinary)
            if (preBinary !== contrasted) preBinary.recycle()
            if (contrasted !== grayscale && contrasted !== preBinary) contrasted.recycle()
            binary
        } else {
            preBinary
        }

        if (grayscale !== src && grayscale !== contrasted && grayscale !== final) {
            grayscale.recycle()
        }

        return final
    }

    /**
     * Recorte padrão usado quando se processa a foto inteira em vez do recorte
     * calculado pelo OcrService.
     */
    private fun cropForOcr(src: Bitmap): Bitmap {
        val relWidth = 0.85f
        val bitmapRatio = src.width.toFloat() / src.height.toFloat()
        val relHeight = 0.63f * relWidth * bitmapRatio

        val relLeft = (1f - relWidth) / 2f
        val relTop = (1f - relHeight) * 0.30f

        val marginFactor = 0.50f
        val marginW = relWidth * marginFactor
        val marginH = relHeight * marginFactor

        val cropLeft = (relLeft - marginW).coerceAtLeast(0f)
        val cropTop = (relTop - marginH).coerceAtLeast(0f)
        val cropWidth = (relWidth + 2 * marginW).coerceAtMost(1f - cropLeft)
        val cropHeight = (relHeight + 2 * marginH).coerceAtMost(1f - cropTop)

        return crop(src, cropLeft, cropTop, cropWidth, cropHeight)
    }

    /**
     * Pipeline compacto: recorta e aplica filtros.
     */
    fun processForOcr(src: Bitmap, contrast: Float = 1.6f): Bitmap {
        val cropped = cropForOcr(src)
        val final = applyOcrFilters(cropped, contrast)

        if (cropped !== src && cropped !== final) {
            cropped.recycle()
        }

        return final
    }

    /**
     * Desenha retângulos semi‑transparentes para destacar regiões.
     */
    fun drawHighlights(src: Bitmap, rects: List<Rect>, color: Int = 0x50FFEB3B.toInt()): Bitmap {
        val dest = src.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(dest)

        val fillPaint = Paint().apply {
            this.color = color
            style = Paint.Style.FILL
        }

        val borderPaint = Paint().apply {
            this.color = color or 0xFF000000.toInt()
            style = Paint.Style.STROKE
            strokeWidth = 3f
        }

        for (rect in rects) {
            canvas.drawRect(rect, fillPaint)
            canvas.drawRect(rect, borderPaint)
        }

        return dest
    }

    /**
     * Corta o bitmap usando coordenadas relativas (0.0 a 1.0).
     */
    fun crop(src: Bitmap, left: Float, top: Float, width: Float, height: Float): Bitmap {
        val x = (src.width * left).toInt().coerceIn(0, src.width - 1)
        val y = (src.height * top).toInt().coerceIn(0, src.height - 1)
        val w = (src.width * width).toInt().coerceAtLeast(10).coerceAtMost(src.width - x)
        val h = (src.height * height).toInt().coerceAtLeast(10).coerceAtMost(src.height - y)

        return Bitmap.createBitmap(src, x, y, w, h)
    }

    /**
     * Detector simples de “documento” por luminosidade relativa.
     * Retorna um RectF com proporções do documento na imagem.
     */
    fun detectDocumentBounds(src: Bitmap): android.graphics.RectF {
        val width = src.width
        val height = src.height

        val scale = 0.2f
        val small = Bitmap.createScaledBitmap(
            src,
            (width * scale).toInt(),
            (height * scale).toInt(),
            false
        )

        val w = small.width
        val h = small.height
        val pixels = IntArray(w * h)
        small.getPixels(pixels, 0, w, 0, 0, w, h)

        var minX = w
        var minY = h
        var maxX = 0
        var maxY = 0

        val luminances = FloatArray(w * h)
        var avgLuminance = 0f
        for (i in pixels.indices) {
            val color = pixels[i]
            val r = (color shr 16) and 0xFF
            val g = (color shr 8) and 0xFF
            val b = color and 0xFF
            val lum = (0.299f * r + 0.587f * g + 0.114f * b)
            luminances[i] = lum
            avgLuminance += lum
        }
        avgLuminance /= (w * h)

        val threshold = if (avgLuminance > 128) avgLuminance * 0.8f else avgLuminance * 1.2f
        val isDarkBackground = avgLuminance < 128

        for (y in 0 until h) {
            for (x in 0 until w) {
                val lum = luminances[y * w + x]
                val isContent = if (isDarkBackground) {
                    lum > threshold
                } else {
                    lum < threshold
                }

                if (isContent) {
                    if (x < minX) minX = x
                    if (x > maxX) maxX = x
                    if (y < minY) minY = y
                    if (y > maxY) maxY = y
                }
            }
        }

        small.recycle()

        if (maxX <= minX || maxY <= minY) {
            return android.graphics.RectF(0.1f, 0.1f, 0.9f, 0.9f)
        }

        val padding = 0.05f
        return android.graphics.RectF(
            (minX.toFloat() / w - padding).coerceAtLeast(0f),
            (minY.toFloat() / h - padding).coerceAtLeast(0f),
            (maxX.toFloat() / w + padding).coerceAtMost(1f),
            (maxY.toFloat() / h + padding).coerceAtMost(1f)
        )
    }
}