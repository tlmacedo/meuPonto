package br.com.tlmacedo.meuponto.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Utilitário para gerenciar o armazenamento físico de fotos de comprovantes.
 *
 * ## Correções aplicadas (12.0.0):
 * - e.printStackTrace() substituído por Timber.e() em todos os blocos catch.
 * - Métodos de I/O agora são `suspend` e executam em `withContext(Dispatchers.IO)`.
 * - `cleanupOrphanImages` usa `File.relativeTo()` para cálculo robusto de caminho relativo.
 * - `getTotalStorageSizeFormatted()` usa a extensão `Long.formatarTamanho()` centralizada.
 *
 * @param context Contexto da aplicação para acesso ao ContentResolver
 *
 * @author Thiago
 * @since 10.0.0
 * @updated 12.0.0 - e.printStackTrace() → Timber.e(); I/O em Dispatchers.IO;
 *                   cleanupOrphanImages robusto; formatarTamanho centralizado.
 */
@Singleton
class ComprovanteImageStorage @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val ROOT_DIR = "comprovantes"
        private const val IMAGE_QUALITY = 85
        private const val MAX_IMAGE_DIMENSION = 1920
        private const val THUMBNAIL_SIZE = 200
    }

    private val timestampFormatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")

    val appContext: Context get() = context

    fun getComprovantesDirectory(): File = getRootDirectory()

    fun createTempFileForCamera(empregoId: Long, data: LocalDate): File {
        val directory = getOrCreateDirectory(empregoId, data)
        val timestamp = LocalDateTime.now().format(timestampFormatter)
        val fileName = "temp_camera_$timestamp.jpg"
        return File(directory, fileName)
    }

    suspend fun saveFromUri(
        uri: Uri,
        empregoId: Long,
        pontoId: Long,
        dataHora: LocalDateTime
    ): String? = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return@withContext null
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()

            if (originalBitmap == null) {
                Timber.w("Falha ao decodificar bitmap do URI: $uri")
                return@withContext null
            }

            val resizedBitmap = resizeIfNeeded(originalBitmap)
            val result = saveBitmap(resizedBitmap, empregoId, pontoId, dataHora)

            if (resizedBitmap !== originalBitmap) resizedBitmap.recycle()
            originalBitmap.recycle()

            result
        } catch (e: Exception) {
            Timber.e(e, "Falha ao salvar imagem do URI: $uri, empregoId=$empregoId, pontoId=$pontoId")
            null
        }
    }

    suspend fun saveFromBitmap(
        bitmap: Bitmap,
        empregoId: Long,
        pontoId: Long,
        dataHora: LocalDateTime
    ): String? = withContext(Dispatchers.IO) {
        try {
            val resizedBitmap = resizeIfNeeded(bitmap)
            val result = saveBitmap(resizedBitmap, empregoId, pontoId, dataHora)
            if (resizedBitmap !== bitmap) resizedBitmap.recycle()
            result
        } catch (e: Exception) {
            Timber.e(e, "Falha ao salvar bitmap, empregoId=$empregoId, pontoId=$pontoId")
            null
        }
    }

    private fun saveBitmap(
        bitmap: Bitmap,
        empregoId: Long,
        pontoId: Long,
        dataHora: LocalDateTime
    ): String? {
        return try {
            val data = dataHora.toLocalDate()
            val directory = getOrCreateDirectory(empregoId, data)
            val fileName = generateFileName(pontoId, dataHora)
            val file = File(directory, fileName)

            FileOutputStream(file).use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, IMAGE_QUALITY, outputStream)
                outputStream.flush()
            }

            getRelativePath(empregoId, data, fileName)
        } catch (e: Exception) {
            Timber.e(e, "Falha ao gravar arquivo de imagem no disco")
            null
        }
    }

    suspend fun loadBitmap(relativePath: String): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val file = getFileFromRelativePath(relativePath)
            if (file.exists()) {
                BitmapFactory.decodeFile(file.absolutePath)
            } else {
                Timber.w("Arquivo de imagem não encontrado: $relativePath")
                null
            }
        } catch (e: Exception) {
            Timber.e(e, "Falha ao carregar bitmap: $relativePath")
            null
        }
    }

    suspend fun loadThumbnail(relativePath: String): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val file = getFileFromRelativePath(relativePath)
            if (!file.exists()) {
                Timber.w("Arquivo não encontrado para thumbnail: $relativePath")
                return@withContext null
            }

            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeFile(file.absolutePath, options)

            val scaleFactor = maxOf(
                options.outWidth / THUMBNAIL_SIZE,
                options.outHeight / THUMBNAIL_SIZE
            ).coerceAtLeast(1)

            options.apply {
                inJustDecodeBounds = false
                inSampleSize = scaleFactor
            }

            BitmapFactory.decodeFile(file.absolutePath, options)
        } catch (e: Exception) {
            Timber.e(e, "Falha ao carregar thumbnail: $relativePath")
            null
        }
    }

    fun getAbsoluteFile(relativePath: String): File {
        return getFileFromRelativePath(relativePath)
    }

    fun exists(relativePath: String): Boolean {
        return try {
            getFileFromRelativePath(relativePath).exists()
        } catch (e: Exception) {
            Timber.w(e, "Erro ao verificar existência: $relativePath")
            false
        }
    }

    suspend fun delete(relativePath: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val file = getFileFromRelativePath(relativePath)
            if (file.exists()) {
                val deleted = file.delete()
                if (!deleted) Timber.w("Não foi possível deletar: $relativePath")
                deleted
            } else {
                true
            }
        } catch (e: Exception) {
            Timber.e(e, "Falha ao deletar imagem: $relativePath")
            false
        }
    }

    suspend fun deleteAllForEmprego(empregoId: Long): Int = withContext(Dispatchers.IO) {
        try {
            val empregoDir = File(getRootDirectory(), "emprego_$empregoId")
            if (empregoDir.exists()) {
                val count = empregoDir.walkTopDown().filter { it.isFile }.count()
                empregoDir.deleteRecursively()
                Timber.i("Deletados $count arquivos do empregoId=$empregoId")
                count
            } else {
                0
            }
        } catch (e: Exception) {
            Timber.e(e, "Falha ao deletar arquivos do empregoId=$empregoId")
            0
        }
    }

    suspend fun getTotalStorageSize(): Long = withContext(Dispatchers.IO) {
        try {
            getRootDirectory().walkTopDown()
                .filter { it.isFile }
                .sumOf { it.length() }
        } catch (e: Exception) {
            Timber.e(e, "Falha ao calcular tamanho total de armazenamento")
            0L
        }
    }

    suspend fun getTotalStorageSizeFormatted(): String = getTotalStorageSize().formatarTamanho()

    suspend fun getTotalImageCount(): Int = withContext(Dispatchers.IO) {
        try {
            getRootDirectory().walkTopDown()
                .filter { it.isFile && it.extension.lowercase() == "jpg" }
                .count()
        } catch (e: Exception) {
            Timber.e(e, "Falha ao contar imagens")
            0
        }
    }

    suspend fun cleanupOrphanImages(validPaths: Set<String>): Int = withContext(Dispatchers.IO) {
        var removedCount = 0
        val rootDir = getRootDirectory()
        try {
            rootDir.walkTopDown()
                .filter { it.isFile && it.extension.lowercase() == "jpg" }
                .forEach { file ->
                    // Usa File.relativeTo para obter o caminho relativo de forma robusta
                    val relativePath = file.relativeTo(rootDir).path
                    if (relativePath !in validPaths) {
                        if (file.delete()) {
                            removedCount++
                            Timber.d("Arquivo órfão removido: $relativePath")
                        } else {
                            Timber.w("Não foi possível remover arquivo órfão: $relativePath")
                        }
                    }
                }
            Timber.i("Limpeza de órfãos concluída: $removedCount arquivo(s) removido(s)")
        } catch (e: Exception) {
            Timber.e(e, "Falha na limpeza de imagens órfãs")
        }
        removedCount
    }

    private fun getRootDirectory(): File {
        return File(context.filesDir, ROOT_DIR).apply {
            if (!exists()) mkdirs()
        }
    }

    private fun getOrCreateDirectory(empregoId: Long, data: LocalDate): File {
        val path = "emprego_$empregoId/${data.year}/${String.format("%02d", data.monthValue)}"
        return File(getRootDirectory(), path).apply {
            if (!exists()) mkdirs()
        }
    }

    private fun generateFileName(pontoId: Long, dataHora: LocalDateTime): String {
        val timestamp = dataHora.format(timestampFormatter)
        return "ponto_${pontoId}_$timestamp.jpg"
    }

    private fun getRelativePath(empregoId: Long, data: LocalDate, fileName: String): String {
        return "emprego_$empregoId/${data.year}/${String.format("%02d", data.monthValue)}/$fileName"
    }

    private fun getFileFromRelativePath(relativePath: String): File {
        return File(getRootDirectory(), relativePath)
    }

    private fun resizeIfNeeded(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        if (width <= MAX_IMAGE_DIMENSION && height <= MAX_IMAGE_DIMENSION) {
            return bitmap
        }

        val ratio = minOf(
            MAX_IMAGE_DIMENSION.toFloat() / width,
            MAX_IMAGE_DIMENSION.toFloat() / height
        )

        val newWidth = (width * ratio).toInt()
        val newHeight = (height * ratio).toInt()

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }
}