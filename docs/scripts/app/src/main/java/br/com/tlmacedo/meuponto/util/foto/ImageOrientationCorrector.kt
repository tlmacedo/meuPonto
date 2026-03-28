// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/util/foto/ImageOrientationCorrector.kt
package br.com.tlmacedo.meuponto.util.foto

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Corretor de orientação de imagens baseado em metadados EXIF.
 *
 * Câmeras fotográficas e smartphones salvam imagens em orientação física
 * do sensor e registram a orientação correta nos metadados EXIF. Sem
 * essa correção, imagens retrato aparecem como paisagem rotacionada.
 *
 * @param context Contexto da aplicação para acesso ao ContentResolver
 *
 * @author Thiago
 * @since 10.0.0
 * @updated 12.0.0 - e.printStackTrace() substituído por Timber.e/w();
 *                   adicionado KDoc completo em todas as funções públicas
 */
@Singleton
class ImageOrientationCorrector @Inject constructor(
    @ApplicationContext private val context: Context
) {

    /**
     * Obtém a orientação EXIF de um arquivo de imagem.
     *
     * @param file Arquivo de imagem JPEG
     * @return Constante de orientação do [ExifInterface] (ex: ORIENTATION_ROTATE_90)
     */
    fun getOrientation(file: File): Int {
        return try {
            val exif = ExifInterface(file)
            exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
        } catch (e: Exception) {
            Timber.w(e, "Falha ao ler orientação EXIF do arquivo: ${file.name}")
            ExifInterface.ORIENTATION_NORMAL
        }
    }

    /**
     * Obtém a orientação EXIF de um URI (galeria ou câmera).
     *
     * @param uri URI da imagem (content:// ou file://)
     * @return Constante de orientação do [ExifInterface]
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
            Timber.w(e, "Falha ao ler orientação EXIF do URI: $uri")
            ExifInterface.ORIENTATION_NORMAL
        }
    }

    /**
     * Corrige a orientação de um [Bitmap] com base na orientação EXIF.
     *
     * Se a orientação já for [ExifInterface.ORIENTATION_NORMAL], retorna
     * o bitmap original sem criar cópia.
     *
     * @param bitmap Bitmap a ser corrigido
     * @param orientation Orientação EXIF lida via [getOrientation]
     * @return Bitmap com orientação corrigida ou o original se não necessário
     */
    fun correctOrientation(bitmap: Bitmap, orientation: Int): Bitmap {
        val matrix = Matrix()

        when (orientation) {
            ExifInterface.ORIENTATION_NORMAL     -> return bitmap
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.setScale(-1f, 1f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.setRotate(180f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.setScale(1f, -1f)
            ExifInterface.ORIENTATION_TRANSPOSE  -> {
                matrix.setRotate(90f)
                matrix.postScale(-1f, 1f)
            }
            ExifInterface.ORIENTATION_ROTATE_90  -> matrix.setRotate(90f)
            ExifInterface.ORIENTATION_TRANSVERSE -> {
                matrix.setRotate(-90f)
                matrix.postScale(-1f, 1f)
            }
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.setRotate(-90f)
            else -> return bitmap
        }

        return try {
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        } catch (e: Exception) {
            Timber.e(e, "Falha ao aplicar matriz de rotação ao bitmap")
            bitmap
        }
    }

    /**
     * Carrega um [Bitmap] de arquivo já com orientação corrigida.
     *
     * Combina [getOrientation] e [correctOrientation] em uma única chamada.
     * Se a orientação for normal, nenhuma cópia é criada.
     *
     * @param file Arquivo de imagem JPEG
     * @return Bitmap com orientação correta ou null em caso de erro
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
            Timber.e(e, "Falha ao carregar bitmap com orientação corrigida: ${file.name}")
            null
        }
    }

    /**
     * Carrega um [Bitmap] de URI já com orientação corrigida.
     *
     * @param uri URI da imagem
     * @return Bitmap com orientação correta ou null em caso de erro
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
            Timber.e(e, "Falha ao carregar bitmap com orientação corrigida do URI: $uri")
            null
        }
    }

    /**
     * Verifica se uma imagem precisa de correção de orientação.
     *
     * @param file Arquivo de imagem
     * @return true se a orientação EXIF for diferente de [ExifInterface.ORIENTATION_NORMAL]
     */
    fun needsCorrection(file: File): Boolean =
        getOrientation(file) != ExifInterface.ORIENTATION_NORMAL

    /**
     * Verifica se uma imagem precisa de correção de orientação.
     *
     * @param uri URI da imagem
     * @return true se a orientação EXIF for diferente de [ExifInterface.ORIENTATION_NORMAL]
     */
    fun needsCorrection(uri: Uri): Boolean =
        getOrientation(uri) != ExifInterface.ORIENTATION_NORMAL
}
