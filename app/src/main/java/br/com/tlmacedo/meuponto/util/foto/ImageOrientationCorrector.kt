// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/util/foto/ImageOrientationCorrector.kt
package br.com.tlmacedo.meuponto.util.foto

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber // Importação adicionada
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Utilitário para corrigir a orientação de imagens baseada em metadados EXIF.
 *
 * Garante que as imagens sejam exibidas e processadas com a orientação correta,
 * evitando rotações indesejadas.
 *
 * ## Correções aplicadas (12.0.0):
 * - [getOrientation] (File): e.printStackTrace() substituído por Timber.e()
 * - [getOrientation] (Uri): e.printStackTrace() substituído por Timber.e()
 * - [loadBitmapWithCorrectOrientation] (File): e.printStackTrace() substituído por Timber.e()
 * - [loadBitmapWithCorrectOrientation] (Uri): e.printStackTrace() substituído por Timber.e()
 *
 * @param context Contexto da aplicação para acesso ao ContentResolver
 *
 * @author Thiago
 * @since 10.0.0
 * @updated 12.0.0 - e.printStackTrace() substituído por Timber.e()
 */
@Singleton
class ImageOrientationCorrector @Inject constructor(
    @ApplicationContext private val context: Context
) {

    /**
     * Retorna a orientação EXIF de um arquivo de imagem.
     *
     * @param file Arquivo de imagem
     * @return Constante de orientação EXIF (ex: [ExifInterface.ORIENTATION_NORMAL]),
     *         ou [ExifInterface.ORIENTATION_NORMAL] em caso de erro ou ausência de EXIF.
     */
    fun getOrientation(file: File): Int {
        return try {
            val exif = ExifInterface(file.absolutePath)
            exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
        } catch (e: Exception) {
            Timber.e(e, "Falha ao ler orientação EXIF do arquivo: ${file.name}") // Correção aqui
            ExifInterface.ORIENTATION_NORMAL
        }
    }

    /**
     * Retorna a orientação EXIF de uma imagem a partir de um URI.
     *
     * @param uri URI da imagem
     * @return Constante de orientação EXIF, ou [ExifInterface.ORIENTATION_NORMAL] em caso de erro.
     */
    fun getOrientation(uri: Uri): Int {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val exif = ExifInterface(inputStream)
                exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )
            } ?: ExifInterface.ORIENTATION_NORMAL
        } catch (e: Exception) {
            Timber.e(e, "Falha ao ler orientação EXIF do URI: $uri") // Correção aqui
            ExifInterface.ORIENTATION_NORMAL
        }
    }

    /**
     * Corrige a orientação de um [Bitmap] com base em sua orientação EXIF.
     *
     * Se a orientação for [ExifInterface.ORIENTATION_NORMAL], o bitmap original é retornado.
     * Caso contrário, uma nova cópia rotacionada é criada.
     *
     * @param bitmap Bitmap a ser corrigido
     * @param orientation Orientação EXIF (obtida via [getOrientation])
     * @return Bitmap com a orientação corrigida
     */
    fun correctOrientation(bitmap: Bitmap, orientation: Int): Bitmap {
        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.setRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.setRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.setRotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.setScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.setScale(1f, -1f)
            ExifInterface.ORIENTATION_TRANSPOSE -> {
                matrix.setRotate(90f)
                matrix.postScale(-1f, 1f)
            }
            ExifInterface.ORIENTATION_TRANSVERSE -> {
                matrix.setRotate(-90f)
                matrix.postScale(-1f, 1f)
            }
            else -> return bitmap // Nenhuma correção necessária
        }

        return try {
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        } catch (e: OutOfMemoryError) {
            Timber.e(e, "Out of memory ao corrigir orientação do bitmap.")
            bitmap // Retorna o original para evitar crash
        }
    }

    /**
     * Carrega um bitmap de um arquivo e aplica a correção de orientação EXIF.
     *
     * @param file Arquivo de imagem
     * @return Bitmap corrigido ou null em caso de erro
     */
    fun loadBitmapWithCorrectOrientation(file: File): Bitmap? {
        return try {
            val orientation = getOrientation(file)
            val bitmap = BitmapFactory.decodeFile(file.absolutePath) ?: run {
                Timber.w("Falha ao decodificar bitmap do arquivo: ${file.name}")
                return null
            }

            if (orientation == ExifInterface.ORIENTATION_NORMAL) {
                bitmap
            } else {
                val corrected = correctOrientation(bitmap, orientation)
                if (corrected !== bitmap) bitmap.recycle()
                corrected
            }
        } catch (e: Exception) {
            Timber.e(e, "Falha ao carregar bitmap com orientação corrigida: ${file.name}") // Correção aqui
            null
        }
    }

    /**
     * Carrega um bitmap de um URI e aplica a correção de orientação EXIF.
     *
     * @param uri URI da imagem
     * @return Bitmap corrigido ou null em caso de erro
     */
    fun loadBitmapWithCorrectOrientation(uri: Uri): Bitmap? {
        return try {
            val orientation = getOrientation(uri)
            val bitmap = context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it)
            } ?: run {
                Timber.w("Falha ao decodificar bitmap do URI: $uri")
                return null
            }

            if (orientation == ExifInterface.ORIENTATION_NORMAL) {
                bitmap
            } else {
                val corrected = correctOrientation(bitmap, orientation)
                if (corrected !== bitmap) bitmap.recycle()
                corrected
            }
        } catch (e: Exception) {
            Timber.e(e, "Falha ao carregar bitmap com orientação corrigida do URI: $uri") // Correção aqui
            null
        }
    }

    /**
     * Verifica se uma imagem precisa de correção de orientação.
     *
     * @param file Arquivo de imagem
     * @return true se a orientação EXIF não for [ExifInterface.ORIENTATION_NORMAL]
     */
    fun needsCorrection(file: File): Boolean =
        getOrientation(file) != ExifInterface.ORIENTATION_NORMAL

    /**
     * Verifica se uma imagem precisa de correção de orientação.
     *
     * @param uri URI da imagem
     * @return true se a orientação EXIF não for [ExifInterface.ORIENTATION_NORMAL]
     */
    fun needsCorrection(uri: Uri): Boolean =
        getOrientation(uri) != ExifInterface.ORIENTATION_NORMAL
}