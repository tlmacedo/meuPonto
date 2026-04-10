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
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Utilitário para gerenciar o armazenamento físico de logotipos de empresas.
 *
 * @param context Contexto da aplicação para acesso ao ContentResolver
 *
 * @author Thiago
 * @since 12.0.0
 */
@Singleton
class LogoImageStorage @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val ROOT_DIR = "logos"
        private const val IMAGE_QUALITY = 90
        private const val MAX_IMAGE_DIMENSION = 512
    }

    suspend fun saveFromUri(
        uri: Uri,
        empregoId: Long
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
            val result = saveBitmap(resizedBitmap, empregoId)

            if (resizedBitmap !== originalBitmap) resizedBitmap.recycle()
            originalBitmap.recycle()

            result
        } catch (e: Exception) {
            Timber.e(e, "Falha ao salvar logo do URI: $uri, empregoId=$empregoId")
            null
        }
    }

    private fun saveBitmap(
        bitmap: Bitmap,
        empregoId: Long
    ): String? {
        return try {
            val directory = getOrCreateDirectory()
            val fileName = "logo_emprego_$empregoId.jpg"
            val file = File(directory, fileName)

            FileOutputStream(file).use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, IMAGE_QUALITY, outputStream)
                outputStream.flush()
            }

            getRelativePath(fileName)
        } catch (e: Exception) {
            Timber.e(e, "Falha ao gravar arquivo de logo no disco")
            null
        }
    }

    fun getAbsoluteFile(relativePath: String): File {
        return File(getRootDirectory(), relativePath)
    }

    suspend fun delete(relativePath: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val file = getAbsoluteFile(relativePath)
            if (file.exists()) {
                val deleted = file.delete()
                if (!deleted) Timber.w("Não foi possível deletar logo: $relativePath")
                deleted
            } else {
                true
            }
        } catch (e: Exception) {
            Timber.e(e, "Falha ao deletar logo: $relativePath")
            false
        }
    }

    private fun getRootDirectory(): File {
        return File(context.filesDir, ROOT_DIR).apply {
            if (!exists()) mkdirs()
        }
    }

    private fun getOrCreateDirectory(): File {
        return getRootDirectory()
    }

    private fun getRelativePath(fileName: String): String {
        return fileName
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
