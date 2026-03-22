// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/util/foto/ImageCompressor.kt
package br.com.tlmacedo.meuponto.util.foto

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import br.com.tlmacedo.meuponto.util.formatarTamanho
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Utilitário para compressão inteligente de imagens.
 *
 * Oferece compressão com qualidade fixa e compressão adaptativa que reduz
 * automaticamente a qualidade JPEG até atingir o tamanho alvo, respeitando
 * um limite mínimo de qualidade aceitável.
 *
 * ## Estratégia de compressão adaptativa:
 * 1. Comprime com qualidade inicial ([initialQuality])
 * 2. Se o resultado for maior que [maxSizeBytes], reduz em [QUALITY_STEP]
 * 3. Repete até atingir o tamanho ou a qualidade mínima [MIN_QUALITY]
 *
 * ## Correções aplicadas (12.0.0):
 * - [saveToFile] e [saveToFileAdaptive]: e.printStackTrace() substituído por Timber.e()
 * - [recompressFile]: e.printStackTrace() substituído por Timber.e() e Timber.w()
 * - [AdaptiveCompressionResult.sizeFormatted]: substituída lógica inline duplicada
 *   por [Long.formatarTamanho] centralizado em FileExtensions.kt
 *
 * @author Thiago
 * @since 10.0.0
 * @updated 12.0.0 - e.printStackTrace() substituído por Timber.e();
 *                   sizeFormatted usa Long.formatarTamanho() centralizado
 */
@Singleton
class ImageCompressor @Inject constructor() {

    companion object {
        /** Qualidade mínima aceitável para JPEG (abaixo disso o artefato é visível) */
        const val MIN_QUALITY = 30

        /** Qualidade máxima para JPEG */
        const val MAX_QUALITY = 100

        /** Passo de redução de qualidade na compressão adaptativa */
        const val QUALITY_STEP = 5

        /** Tamanho padrão máximo em bytes (1 MB) */
        const val DEFAULT_MAX_SIZE_BYTES = 1024 * 1024L
    }

    // ========================================================================
    // COMPRESSÃO PARA BYTEARRAY
    // ========================================================================

    /**
     * Comprime um [Bitmap] para JPEG com qualidade especificada.
     *
     * @param bitmap Bitmap a ser comprimido
     * @param quality Qualidade JPEG de 1 a 100 (padrão: 85)
     * @return [ByteArray] com os dados JPEG comprimidos
     */
    fun compressToJpeg(bitmap: Bitmap, quality: Int = 85): ByteArray {
        val validQuality = quality.coerceIn(MIN_QUALITY, MAX_QUALITY)
        return ByteArrayOutputStream().use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, validQuality, outputStream)
            outputStream.toByteArray()
        }
    }

    /**
     * Comprime um [Bitmap] para PNG (lossless — sem perdas de qualidade).
     *
     * O parâmetro de qualidade é ignorado pelo Android para PNG.
     *
     * @param bitmap Bitmap a ser comprimido
     * @return [ByteArray] com os dados PNG
     */
    fun compressToPng(bitmap: Bitmap): ByteArray {
        return ByteArrayOutputStream().use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.toByteArray()
        }
    }

    /**
     * Comprime um [Bitmap] no formato especificado.
     *
     * @param bitmap Bitmap a ser comprimido
     * @param format Formato de compressão ([Bitmap.CompressFormat.JPEG] ou [Bitmap.CompressFormat.PNG])
     * @param quality Qualidade para JPEG de 1 a 100 (ignorado para PNG)
     * @return [ByteArray] com os dados comprimidos
     */
    fun compress(
        bitmap: Bitmap,
        format: Bitmap.CompressFormat,
        quality: Int = 85
    ): ByteArray {
        return when (format) {
            Bitmap.CompressFormat.PNG -> compressToPng(bitmap)
            else -> compressToJpeg(bitmap, quality)
        }
    }

    // ========================================================================
    // COMPRESSÃO ADAPTATIVA
    // ========================================================================

    /**
     * Compressão adaptativa que tenta atingir o tamanho máximo especificado.
     *
     * Reduz a qualidade JPEG em passos de [QUALITY_STEP] até atingir
     * [maxSizeBytes] ou alcançar [minQuality]. O resultado inclui a
     * qualidade final aplicada e se o alvo foi atingido.
     *
     * @param bitmap Bitmap a ser comprimido
     * @param maxSizeBytes Tamanho máximo desejado em bytes (padrão: 1 MB)
     * @param initialQuality Qualidade inicial de 1 a 100 (padrão: 85)
     * @param minQuality Qualidade mínima aceitável (padrão: [MIN_QUALITY])
     * @return [AdaptiveCompressionResult] com dados e metadados da compressão
     */
    fun compressAdaptive(
        bitmap: Bitmap,
        maxSizeBytes: Long = DEFAULT_MAX_SIZE_BYTES,
        initialQuality: Int = 85,
        minQuality: Int = MIN_QUALITY
    ): AdaptiveCompressionResult {
        var currentQuality = initialQuality.coerceIn(minQuality, MAX_QUALITY)
        var compressedData = compressToJpeg(bitmap, currentQuality)

        while (compressedData.size > maxSizeBytes && currentQuality > minQuality) {
            currentQuality = (currentQuality - QUALITY_STEP).coerceAtLeast(minQuality)
            compressedData = compressToJpeg(bitmap, currentQuality)
        }

        return AdaptiveCompressionResult(
            data = compressedData,
            finalQuality = currentQuality,
            sizeBytes = compressedData.size.toLong(),
            targetAchieved = compressedData.size <= maxSizeBytes
        )
    }

    // ========================================================================
    // SALVAR EM ARQUIVO
    // ========================================================================

    /**
     * Salva um [Bitmap] comprimido diretamente em um arquivo.
     *
     * @param bitmap Bitmap a ser salvo
     * @param outputFile Arquivo de destino (deve ser gravável)
     * @param format Formato de compressão (padrão: JPEG)
     * @param quality Qualidade para JPEG de 1 a 100 (padrão: 85)
     * @return true se salvou com sucesso, false em caso de erro
     */
    fun saveToFile(
        bitmap: Bitmap,
        outputFile: File,
        format: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG,
        quality: Int = 85
    ): Boolean {
        return try {
            FileOutputStream(outputFile).use { outputStream ->
                bitmap.compress(format, quality.coerceIn(MIN_QUALITY, MAX_QUALITY), outputStream)
                outputStream.flush()
            }
            true
        } catch (e: Exception) {
            Timber.e(e, "Falha ao salvar bitmap no arquivo: ${outputFile.name}")
            false
        }
    }

    /**
     * Salva com compressão adaptativa diretamente em arquivo.
     *
     * Combina [compressAdaptive] com gravação direta em disco, evitando
     * manter o [ByteArray] inteiro em memória por tempo desnecessário.
     *
     * @param bitmap Bitmap a ser salvo
     * @param outputFile Arquivo de destino
     * @param maxSizeBytes Tamanho máximo em bytes (padrão: 1 MB)
     * @param initialQuality Qualidade inicial (padrão: 85)
     * @return [AdaptiveCompressionResult] com metadados ou null em caso de erro
     */
    fun saveToFileAdaptive(
        bitmap: Bitmap,
        outputFile: File,
        maxSizeBytes: Long = DEFAULT_MAX_SIZE_BYTES,
        initialQuality: Int = 85
    ): AdaptiveCompressionResult? {
        return try {
            val result = compressAdaptive(bitmap, maxSizeBytes, initialQuality)
            FileOutputStream(outputFile).use { outputStream ->
                outputStream.write(result.data)
                outputStream.flush()
            }
            result
        } catch (e: Exception) {
            Timber.e(e, "Falha ao salvar bitmap com compressão adaptativa: ${outputFile.name}")
            null
        }
    }

    // ========================================================================
    // UTILITÁRIOS
    // ========================================================================

    /**
     * Estima o tamanho em bytes após compressão JPEG sem comprimir de fato.
     *
     * ATENÇÃO: Esta é uma estimativa de ordem de grandeza baseada em constantes
     * empíricas. O erro pode ser de até 50% dependendo do conteúdo da imagem
     * (imagens de documentos com textura complexa tendem a ser maiores que o
     * estimado). Use apenas para decisões de pré-filtragem, nunca para
     * validações de tamanho exato.
     *
     * @param bitmap Bitmap para estimar
     * @param quality Qualidade JPEG esperada de 1 a 100 (padrão: 85)
     * @return Estimativa de tamanho em bytes
     */
    fun estimateCompressedSize(bitmap: Bitmap, quality: Int = 85): Long {
        val pixelCount = bitmap.width.toLong() * bitmap.height
        val bytesPerPixel = when {
            quality >= 90 -> 0.8
            quality >= 80 -> 0.5
            quality >= 70 -> 0.35
            quality >= 60 -> 0.25
            else          -> 0.15
        }
        return (pixelCount * bytesPerPixel).toLong()
    }

    /**
     * Recomprime um arquivo de imagem existente com nova qualidade.
     *
     * Útil para reduzir o tamanho de imagens já salvas sem reprocessamento
     * completo pelo pipeline do [ImageProcessor].
     *
     * @param inputFile Arquivo de entrada
     * @param outputFile Arquivo de saída (pode ser o mesmo que [inputFile])
     * @param quality Qualidade JPEG desejada de 1 a 100 (padrão: 85)
     * @return true se recomprimido com sucesso
     */
    fun recompressFile(
        inputFile: File,
        outputFile: File,
        quality: Int = 85
    ): Boolean {
        return try {
            val bitmap = BitmapFactory.decodeFile(inputFile.absolutePath) ?: run {
                Timber.w("Falha ao decodificar arquivo para recompressão: ${inputFile.name}")
                return false
            }
            val result = saveToFile(bitmap, outputFile, Bitmap.CompressFormat.JPEG, quality)
            bitmap.recycle()
            result
        } catch (e: Exception) {
            Timber.e(e, "Falha ao recomprimir arquivo: ${inputFile.name}")
            false
        }
    }
}

// ============================================================================
// DATA CLASSES DE SUPORTE
// ============================================================================

/**
 * Resultado da compressão adaptativa de uma imagem.
 *
 * @property data Dados binários comprimidos prontos para gravação em disco
 * @property finalQuality Qualidade JPEG final efetivamente aplicada
 * @property sizeBytes Tamanho final em bytes após compressão
 * @property targetAchieved true se o tamanho [maxSizeBytes] foi atingido;
 *           false se parou no [ImageCompressor.MIN_QUALITY] sem atingir o alvo
 */
data class AdaptiveCompressionResult(
    val data: ByteArray,
    val finalQuality: Int,
    val sizeBytes: Long,
    val targetAchieved: Boolean
) {
    /**
     * Tamanho formatado usando [Long.formatarTamanho] centralizado.
     *
     * Corrigido em 12.0.0: substituída lógica inline duplicada (que existia
     * em 6 lugares do projeto com implementações ligeiramente diferentes)
     * pelo utilitário centralizado em FileExtensions.kt.
     */
    val sizeFormatted: String get() = sizeBytes.formatarTamanho()

    // ByteArray requer equals e hashCode customizados
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as AdaptiveCompressionResult
        return data.contentEquals(other.data) &&
                finalQuality == other.finalQuality &&
                sizeBytes == other.sizeBytes &&
                targetAchieved == other.targetAchieved
    }

    override fun hashCode(): Int {
        var result = data.contentHashCode()
        result = 31 * result + finalQuality
        result = 31 * result + sizeBytes.hashCode()
        result = 31 * result + targetAchieved.hashCode()
        return result
    }
}