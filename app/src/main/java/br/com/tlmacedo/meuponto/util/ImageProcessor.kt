package br.com.tlmacedo.meuponto.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.Rect
import androidx.core.graphics.createBitmap

/**
 * Utilitário para processamento de imagem visando melhorar o OCR.
 */
object ImageProcessor {

    /**
     * Converte o bitmap para tons de cinza.
     */
    fun toGrayscale(src: Bitmap): Bitmap {
        val height = src.height
        val width = src.width
        val dest = createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(dest)
        val paint = Paint()
        val colorMatrix = ColorMatrix()
        colorMatrix.setSaturation(0f)
        val filter = ColorMatrixColorFilter(colorMatrix)
        paint.colorFilter = filter
        canvas.drawBitmap(src, 0f, 0f, paint)
        return dest
    }

    /**
     * Aumenta o contraste do bitmap.
     * @param contrast 1.0 é normal, > 1.0 aumenta o contraste.
     */
    fun adjustContrast(src: Bitmap, contrast: Float): Bitmap {
        val dest = createBitmap(src.width, src.height, src.config ?: Bitmap.Config.ARGB_8888)
        val canvas = Canvas(dest)
        val paint = Paint()
        
        // Ajuste de contraste: T' = T * contrast + offset
        // Offset para manter o brilho médio: 128 * (1 - contrast)
        val colorMatrix = ColorMatrix(floatArrayOf(
            contrast, 0f, 0f, 0f, 128f * (1f - contrast),
            0f, contrast, 0f, 0f, 128f * (1f - contrast),
            0f, 0f, contrast, 0f, 128f * (1f - contrast),
            0f, 0f, 0f, 1f, 0f
        ))
        
        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(src, 0f, 0f, paint)
        // Libera a memória do bitmap original se não for mais necessário
        // src.recycle() // Removido para evitar reciclar bitmaps ainda em uso por outros métodos
        return dest
    }

    /**
     * Aplica binarização (Otsu-like simplified) para melhorar OCR em textos claros/escuros.
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
            val binary = if (gray > threshold) 0xFFFFFFFF.toInt() else 0xFF000000.toInt()
            pixels[i] = binary
        }

        dest.setPixels(pixels, 0, width, 0, 0, width, height)
        return dest
    }

    /**
     * Aplica filtros de melhoria para OCR (Grayscale, Contraste e opcionalmente Binarização).
     */
    fun applyOcrFilters(src: Bitmap, contrast: Float = 1.6f, binarize: Boolean = false): Bitmap {
        val grayscale = toGrayscale(src)
        val contrasted = adjustContrast(grayscale, contrast)
        
        val final = if (binarize) {
            val binary = toBinary(contrasted)
            if (contrasted != grayscale) contrasted.recycle()
            binary
        } else {
            contrasted
        }

        if (grayscale != src && grayscale != contrasted && grayscale != final) grayscale.recycle()
        
        return final
    }

    /**
     * Recorta o bitmap conforme o overlay da câmera, aplicando margem de segurança.
     * Alinhado com a proporção de máscara 0.63.
     */
    private fun cropForOcr(src: Bitmap): Bitmap {
        val relWidth = 0.85f
        val bitmapRatio = src.width.toFloat() / src.height.toFloat()
        // Proporção da máscara ajustada para 0.63 conforme requisito
        val relHeight = 0.63f * relWidth * bitmapRatio
        
        val relLeft = (1f - relWidth) / 2f
        val relTop = (1f - relHeight) * 0.30f
        
        // Margem de segurança de 50% sobre as dimensões da máscara
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
     * Aplica processamento otimizado para OCR: Corta conforme o overlay
     * e aplica filtros de imagem.
     */
    fun processForOcr(src: Bitmap, contrast: Float = 1.6f): Bitmap {
        val cropped = cropForOcr(src)
        val final = applyOcrFilters(cropped, contrast)
        
        if (cropped != src && cropped != final) cropped.recycle()
        
        return final
    }

    /**
     * Desenha destaques (retângulos) no bitmap para indicar áreas detectadas pelo OCR.
     */
    fun drawHighlights(src: Bitmap, rects: List<Rect>, color: Int = 0x50FFEB3B.toInt()): Bitmap {
        val dest = src.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(dest)
        val paint = Paint().apply {
            this.color = color
            style = Paint.Style.FILL
        }
        val borderPaint = Paint().apply {
            this.color = color or 0xFF000000.toInt() // Torna opaco para a borda
            style = Paint.Style.STROKE
            strokeWidth = 3f
        }

        for (rect in rects) {
            canvas.drawRect(rect, paint)
            canvas.drawRect(rect, borderPaint)
        }
        return dest
    }

    /**
     * Corta o bitmap baseado em coordenadas relativas (0.0 a 1.0).
     */
    fun crop(src: Bitmap, left: Float, top: Float, width: Float, height: Float): Bitmap {
        val x = (src.width * left).toInt().coerceIn(0, src.width - 1)
        val y = (src.height * top).toInt().coerceIn(0, src.height - 1)
        val w = (src.width * width).toInt().coerceAtLeast(10).coerceAtMost(src.width - x)
        val h = (src.height * height).toInt().coerceAtLeast(10).coerceAtMost(src.height - y)
        
        return Bitmap.createBitmap(src, x, y, w, h)
    }

    /**
     * Tenta detectar as bordas de um documento (comprovante) no bitmap.
     * Retorna um Rect com coordenadas relativas (0.0 a 1.0).
     */
    fun detectDocumentBounds(src: Bitmap): android.graphics.RectF {
        val width = src.width
        val height = src.height
        
        // Redimensionar para processamento mais rápido
        val scale = 0.2f
        val small = Bitmap.createScaledBitmap(src, (width * scale).toInt(), (height * scale).toInt(), false)
        
        val w = small.width
        val h = small.height
        val pixels = IntArray(w * h)
        small.getPixels(pixels, 0, w, 0, 0, w, h)
        
        var minX = w
        var minY = h
        var maxX = 0
        var maxY = 0
        
        // Converter para luminosidade e encontrar pixels que não são "fundo" (simplificado)
        // Assume-se que o documento é mais claro que o fundo ou vice-versa
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
        
        // Threshold dinâmico baseado na média
        val threshold = if (avgLuminance > 128) avgLuminance * 0.8f else avgLuminance * 1.2f
        val isDarkBackground = avgLuminance < 128
        
        for (y in 0 until h) {
            for (x in 0 until w) {
                val lum = luminances[y * w + x]
                val isContent = if (isDarkBackground) lum > threshold else lum < threshold
                
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
        
        // Adicionar uma pequena margem (5%)
        val padding = 0.05f
        return android.graphics.RectF(
            (minX.toFloat() / w - padding).coerceAtLeast(0f),
            (minY.toFloat() / h - padding).coerceAtLeast(0f),
            (maxX.toFloat() / w + padding).coerceAtMost(1f),
            (maxY.toFloat() / h + padding).coerceAtMost(1f)
        )
    }
}
