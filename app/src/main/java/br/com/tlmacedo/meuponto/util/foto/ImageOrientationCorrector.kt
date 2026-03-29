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

@Singleton
class ImageOrientationCorrector @Inject constructor(
    @ApplicationContext private val context: Context
) {

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

    fun needsCorrection(file: File): Boolean =
        getOrientation(file) != ExifInterface.ORIENTATION_NORMAL

    fun needsCorrection(uri: Uri): Boolean =
        getOrientation(uri) != ExifInterface.ORIENTATION_NORMAL
}
