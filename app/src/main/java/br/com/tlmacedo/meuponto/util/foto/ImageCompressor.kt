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

@Singleton
class ImageCompressor @Inject constructor() {

    companion object {
        const val MIN_QUALITY = 30
        const val MAX_QUALITY = 100
        const val QUALITY_STEP = 5
        const val DEFAULT_MAX_SIZE_BYTES = 1024 * 1024L
    }

    fun compressToJpeg(bitmap: Bitmap, quality: Int = 85): ByteArray {
        val validQuality = quality.coerceIn(MIN_QUALITY, MAX_QUALITY)
        return ByteArrayOutputStream().use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, validQuality, outputStream)
            outputStream.toByteArray()
        }
    }

    fun compressToPng(bitmap: Bitmap): ByteArray {
        return ByteArrayOutputStream().use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.toByteArray()
        }
    }

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

data class AdaptiveCompressionResult(
    val data: ByteArray,
    val finalQuality: Int,
    val sizeBytes: Long,
    val targetAchieved: Boolean
) {
    val sizeFormatted: String get() = sizeBytes.formatarTamanho()

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
