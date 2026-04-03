// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/util/foto/ImageResizer.kt
package br.com.tlmacedo.meuponto.util.foto

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber // Importação adicionada
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt

/**
 * Utilitário para redimensionamento de imagens.
 *
 * Oferece múltiplas estratégias de redimensionamento sempre mantendo
 * o aspect ratio original, exceto quando [ResizeStrategy.EXACT] for
 * explicitamente solicitado.
 *
 * ## Estratégias disponíveis:
 * - [ResizeStrategy.FIT]: Redimensiona para caber dentro do limite (padrão)
 * - [ResizeStrategy.FILL]: Redimensiona para preencher, cortando o excesso
 * - [ResizeStrategy.EXACT]: Dimensões exatas sem preservar aspect ratio
 *
 * ## Correções aplicadas (12.0.0):
 * - [loadAndResize] (File): e.printStackTrace() substituído por Timber.e()
 * - [loadAndResize] (Uri): e.printStackTrace() substituído por Timber.e()
 *
 * @param context Contexto da aplicação para acesso ao ContentResolver
 *
 * @author Thiago
 * @since 10.0.0
 * @updated 12.0.0 - e.printStackTrace() substituído por Timber.e()
 */
@Singleton
class ImageResizer @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        /** Resolução máxima padrão em pixels (Full HD) */
        const val DEFAULT_MAX_DIMENSION = 1920

        /** Tamanho padrão para thumbnails em pixels */
        const val THUMBNAIL_SIZE = 200
    }

    /**
     * Estratégia de redimensionamento.
     */
    enum class ResizeStrategy {
        /** Cabe dentro das dimensões preservando aspect ratio (pode resultar menor) */
        FIT,
        /** Preenche as dimensões com crop central (pode cortar bordas) */
        FILL,
        /** Dimensões exatas sem preservar aspect ratio (pode distorcer) */
        EXACT
    }

    // ========================================================================
    // REDIMENSIONAMENTO PRINCIPAL
    // ========================================================================

    /**
     * Redimensiona um [Bitmap] para caber dentro de uma dimensão máxima.
     *
     * Mantém o aspect ratio. Se a imagem já estiver dentro do limite,
     * retorna o bitmap original sem criar cópia.
     *
     * @param bitmap Bitmap original
     * @param maxDimension Dimensão máxima em pixels (largura ou altura)
     * @return Bitmap redimensionado ou o original se já estiver no limite
     */
    fun resizeToFit(bitmap: Bitmap, maxDimension: Int = DEFAULT_MAX_DIMENSION): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        if (width <= maxDimension && height <= maxDimension) return bitmap

        val ratio = minOf(
            maxDimension.toFloat() / width,
            maxDimension.toFloat() / height
        )

        val newWidth = (width * ratio).roundToInt()
        val newHeight = (height * ratio).roundToInt()

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    /**
     * Redimensiona para largura específica mantendo aspect ratio.
     *
     * @param bitmap Bitmap original
     * @param targetWidth Largura desejada em pixels
     * @return Bitmap redimensionado
     */
    fun resizeToWidth(bitmap: Bitmap, targetWidth: Int): Bitmap {
        if (bitmap.width == targetWidth) return bitmap
        val ratio = targetWidth.toFloat() / bitmap.width
        val newHeight = (bitmap.height * ratio).roundToInt()
        return Bitmap.createScaledBitmap(bitmap, targetWidth, newHeight, true)
    }

    /**
     * Redimensiona para altura específica mantendo aspect ratio.
     *
     * @param bitmap Bitmap original
     * @param targetHeight Altura desejada em pixels
     * @return Bitmap redimensionado
     */
    fun resizeToHeight(bitmap: Bitmap, targetHeight: Int): Bitmap {
        if (bitmap.height == targetHeight) return bitmap
        val ratio = targetHeight.toFloat() / bitmap.height
        val newWidth = (bitmap.width * ratio).roundToInt()
        return Bitmap.createScaledBitmap(bitmap, newWidth, targetHeight, true)
    }

    /**
     * Redimensiona para dimensões exatas sem preservar aspect ratio.
     *
     * @param bitmap Bitmap original
     * @param width Largura desejada em pixels
     * @param height Altura desejada em pixels
     * @return Bitmap redimensionado nas dimensões exatas
     */
    fun resizeExact(bitmap: Bitmap, width: Int, height: Int): Bitmap {
        if (bitmap.width == width && bitmap.height == height) return bitmap
        return Bitmap.createScaledBitmap(bitmap, width, height, true)
    }

    /**
     * Redimensiona usando a estratégia especificada.
     *
     * @param bitmap Bitmap original
     * @param targetWidth Largura alvo em pixels
     * @param targetHeight Altura alvo em pixels
     * @param strategy Estratégia de redimensionamento (padrão: [ResizeStrategy.FIT])
     * @return Bitmap redimensionado conforme a estratégia
     */
    fun resize(
        bitmap: Bitmap,
        targetWidth: Int,
        targetHeight: Int,
        strategy: ResizeStrategy = ResizeStrategy.FIT
    ): Bitmap {
        return when (strategy) {
            ResizeStrategy.FIT   -> resizeToFit(bitmap, maxOf(targetWidth, targetHeight))
            ResizeStrategy.FILL  -> resizeToFill(bitmap, targetWidth, targetHeight)
            ResizeStrategy.EXACT -> resizeExact(bitmap, targetWidth, targetHeight)
        }
    }

    /**
     * Redimensiona para preencher área com crop central.
     *
     * Escala a imagem até preencher completamente as dimensões alvo e
     * corta o excesso centralizado.
     *
     * @param bitmap Bitmap original
     * @param targetWidth Largura alvo em pixels
     * @param targetHeight Altura alvo em pixels
     * @return Bitmap redimensionado e cortado nas dimensões exatas
     */
    fun resizeToFill(bitmap: Bitmap, targetWidth: Int, targetHeight: Int): Bitmap {
        val ratio = maxOf(
            targetWidth.toFloat() / bitmap.width,
            targetHeight.toFloat() / bitmap.height
        )

        val scaledWidth = (bitmap.width * ratio).roundToInt()
        val scaledHeight = (bitmap.height * ratio).roundToInt()

        val scaled = Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, true)

        val x = (scaledWidth - targetWidth) / 2
        val y = (scaledHeight - targetHeight) / 2

        return try {
            Bitmap.createBitmap(scaled, x, y, targetWidth, targetHeight)
        } finally {
            if (scaled !== bitmap) scaled.recycle()
        }
    }

    // ========================================================================
    // THUMBNAILS
    // ========================================================================

    /**
     * Cria uma thumbnail quadrada com crop central.
     *
     * @param bitmap Bitmap original
     * @param size Tamanho do lado da thumbnail em pixels (padrão: [THUMBNAIL_SIZE])
     * @return Bitmap quadrado com o tamanho especificado
     */
    fun createSquareThumbnail(bitmap: Bitmap, size: Int = THUMBNAIL_SIZE): Bitmap {
        return resizeToFill(bitmap, size, size)
    }

    /**
     * Cria uma thumbnail mantendo aspect ratio.
     *
     * @param bitmap Bitmap original
     * @param maxSize Dimensão máxima em pixels (padrão: [THUMBNAIL_SIZE])
     * @return Thumbnail proporcional dentro do tamanho especificado
     */
    fun createThumbnail(bitmap: Bitmap, maxSize: Int = THUMBNAIL_SIZE): Bitmap {
        return resizeToFit(bitmap, maxSize)
    }

    // ========================================================================
    // CARREGAR E REDIMENSIONAR
    // ========================================================================

    /**
     * Carrega e redimensiona uma imagem de arquivo com sampling otimizado.
     *
     * Usa [BitmapFactory.Options.inSampleSize] para eficiência de memória,
     * decodificando a imagem já em escala reduzida antes de qualquer
     * operação de bitmap.
     *
     * @param file Arquivo de imagem
     * @param maxDimension Dimensão máxima desejada em pixels (padrão: [DEFAULT_MAX_DIMENSION])
     * @return Bitmap redimensionado ou null em caso de erro
     */
    fun loadAndResize(file: File, maxDimension: Int = DEFAULT_MAX_DIMENSION): Bitmap? {
        return try {
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(file.absolutePath, options)

            options.inSampleSize = calculateInSampleSize(
                options.outWidth, options.outHeight, maxDimension, maxDimension
            )
            options.inJustDecodeBounds = false

            val bitmap = BitmapFactory.decodeFile(file.absolutePath, options) ?: return null

            if (bitmap.width > maxDimension || bitmap.height > maxDimension) {
                val resized = resizeToFit(bitmap, maxDimension)
                if (resized !== bitmap) bitmap.recycle()
                resized
            } else {
                bitmap
            }
        } catch (e: Exception) {
            Timber.e(e, "Falha ao carregar e redimensionar arquivo: ${file.name}") // Correção aqui
            null
        }
    }

    /**
     * Carrega e redimensiona uma imagem de URI com sampling otimizado.
     *
     * @param uri URI da imagem (content:// ou file://)
     * @param maxDimension Dimensão máxima desejada em pixels (padrão: [DEFAULT_MAX_DIMENSION])
     * @return Bitmap redimensionado ou null em caso de erro
     */
    fun loadAndResize(uri: Uri, maxDimension: Int = DEFAULT_MAX_DIMENSION): Bitmap? {
        return try {
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream, null, options)
            }

            options.inSampleSize = calculateInSampleSize(
                options.outWidth, options.outHeight, maxDimension, maxDimension
            )
            options.inJustDecodeBounds = false

            val bitmap = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream, null, options)
            } ?: return null

            if (bitmap.width > maxDimension || bitmap.height > maxDimension) {
                val resized = resizeToFit(bitmap, maxDimension)
                if (resized !== bitmap) bitmap.recycle()
                resized
            } else {
                bitmap
            }
        } catch (e: Exception) {
            Timber.e(e, "Falha ao carregar e redimensionar URI: $uri") // Correção aqui
            null
        }
    }

    // ========================================================================
    // UTILITÁRIOS
    // ========================================================================

    /**
     * Calcula o [BitmapFactory.Options.inSampleSize] ideal para carregar uma imagem.
     *
     * Um `inSampleSize` de 2 reduz cada dimensão pela metade, resultando em
     * 1/4 dos pixels totais e proporcional redução de uso de memória.
     * O valor retornado é sempre uma potência de 2.
     *
     * @param width Largura original da imagem
     * @param height Altura original da imagem
     * @param reqWidth Largura máxima desejada
     * @param reqHeight Altura máxima desejada
     * @return Valor de `inSampleSize` (mínimo: 1, sempre potência de 2)
     */
    fun calculateInSampleSize(
        width: Int,
        height: Int,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2

            while (halfHeight / inSampleSize >= reqHeight &&
                halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }

    /**
     * Obtém as dimensões de uma imagem sem carregá-la na memória.
     *
     * @param file Arquivo de imagem
     * @return Par (largura, altura) ou null em caso de erro
     */
    fun getImageDimensions(file: File): Pair<Int, Int>? {
        return try {
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(file.absolutePath, options)

            if (options.outWidth > 0 && options.outHeight > 0) {
                Pair(options.outWidth, options.outHeight)
            } else null
        } catch (e: Exception) {
            Timber.w(e, "Falha ao obter dimensões do arquivo: ${file.name}")
            null
        }
    }

    /**
     * Obtém as dimensões de uma imagem de URI sem carregá-la na memória.
     *
     * @param uri URI da imagem
     * @return Par (largura, altura) ou null em caso de erro
     */
    fun getImageDimensions(uri: Uri): Pair<Int, Int>? {
        return try {
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream, null, options)
            }

            if (options.outWidth > 0 && options.outHeight > 0) {
                Pair(options.outWidth, options.outHeight)
            } else null
        } catch (e: Exception) {
            Timber.w(e, "Falha ao obter dimensões do URI: $uri")
            null
        }
    }

    /**
     * Verifica se uma imagem precisa ser redimensionada.
     *
     * @param width Largura atual em pixels
     * @param height Altura atual em pixels
     * @param maxDimension Dimensão máxima permitida em pixels
     * @return true se largura ou altura excede [maxDimension]
     */
    fun needsResize(width: Int, height: Int, maxDimension: Int): Boolean {
        return width > maxDimension || height > maxDimension
    }
}

// ============================================================================
// DATA CLASSES DE SUPORTE
// ============================================================================

/**
 * Informações sobre dimensões de uma imagem.
 *
 * @property width Largura em pixels
 * @property height Altura em pixels
 */
data class ImageDimensions(
    val width: Int,
    val height: Int
) {
    /** Proporção largura/altura */
    val aspectRatio: Float get() = width.toFloat() / height

    /** true se a imagem está em modo retrato */
    val isPortrait: Boolean get() = height > width

    /** true se a imagem está em modo paisagem */
    val isLandscape: Boolean get() = width > height

    /** true se a imagem é perfeitamente quadrada */
    val isSquare: Boolean get() = width == height

    /** Total de pixels */
    val pixelCount: Long get() = width.toLong() * height

    /** Dimensões formatadas para exibição */
    val formatted: String get() = "${width}x${height}"
}