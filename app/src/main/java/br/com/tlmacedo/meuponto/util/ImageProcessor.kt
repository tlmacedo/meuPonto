package br.com.tlmacedo.meuponto.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
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
     * Corta o bitmap baseado em coordenadas relativas (0.0 a 1.0).
     */
    fun crop(src: Bitmap, left: Float, top: Float, width: Float, height: Float): Bitmap {
        val x = (src.width * left).toInt().coerceIn(0, src.width - 1)
        val y = (src.height * top).toInt().coerceIn(0, src.height - 1)
        val w = (src.width * width).toInt().coerceAtMost(src.width - x)
        val h = (src.height * height).toInt().coerceAtMost(src.height - y)
        
        return Bitmap.createBitmap(src, x, y, w, h)
    }
}
