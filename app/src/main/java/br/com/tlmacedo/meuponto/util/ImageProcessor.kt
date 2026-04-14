package br.com.tlmacedo.meuponto.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint

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
        val dest = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
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
        val dest = Bitmap.createBitmap(src.width, src.height, src.config ?: Bitmap.Config.ARGB_8888)
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
        return dest
    }

    /**
     * Aplica filtros de melhoria para OCR (Grayscale e Contraste) sem recortar.
     */
    fun applyOcrFilters(src: Bitmap, contrast: Float = 1.6f): Bitmap {
        val grayscale = toGrayscale(src)
        val final = adjustContrast(grayscale, contrast)
        
        if (grayscale != src && grayscale != final) grayscale.recycle()
        
        return final
    }

    /**
     * Recorta o bitmap conforme o overlay da câmera, aplicando margem de segurança.
     * Alinhado com a proporção de máscara 0.63.
     */
    fun cropForOcr(src: Bitmap): Bitmap {
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
