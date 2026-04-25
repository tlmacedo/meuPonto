package br.com.tlmacedo.meuponto.util.foto

import androidx.exifinterface.media.ExifInterface
import timber.log.Timber
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

@Singleton
class ExifDataWriter @Inject constructor() {

    companion object {
        const val SOFTWARE_TAG = "MeuPonto App"
        private val EXIF_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss")
    }

    fun writeMetadata(file: File, metadata: FotoExifMetadata): Boolean {
        return try {
            val exif = ExifInterface(file)

            metadata.dateTime?.let { dateTime ->
                val formattedDate = dateTime.format(EXIF_DATE_FORMAT)
                exif.setAttribute(ExifInterface.TAG_DATETIME, formattedDate)
                exif.setAttribute(ExifInterface.TAG_DATETIME_ORIGINAL, formattedDate)
                exif.setAttribute(ExifInterface.TAG_DATETIME_DIGITIZED, formattedDate)
            }

            if (metadata.latitude != null && metadata.longitude != null) {
                writeGpsData(exif, metadata.latitude, metadata.longitude, metadata.altitude)
            }

            metadata.userComment?.let {
                exif.setAttribute(ExifInterface.TAG_USER_COMMENT, it)
            }
            metadata.description?.let {
                exif.setAttribute(ExifInterface.TAG_IMAGE_DESCRIPTION, it)
            }

            exif.setAttribute(ExifInterface.TAG_SOFTWARE, SOFTWARE_TAG)
            exif.saveAttributes()
            true
        } catch (e: Exception) {
            Timber.e(e, "Falha ao gravar metadados EXIF no arquivo: ${file.name}")
            false
        }
    }

    fun writeGpsLocation(
        file: File,
        latitude: Double,
        longitude: Double,
        altitude: Double? = null
    ): Boolean {
        return try {
            val exif = ExifInterface(file)
            writeGpsData(exif, latitude, longitude, altitude)
            exif.saveAttributes()
            true
        } catch (e: Exception) {
            Timber.e(e, "Falha ao gravar dados GPS no arquivo: ${file.name}")
            false
        }
    }

    fun writeDateTime(file: File, dateTime: LocalDateTime): Boolean {
        return try {
            val exif = ExifInterface(file)
            val formattedDate = dateTime.format(EXIF_DATE_FORMAT)
            exif.setAttribute(ExifInterface.TAG_DATETIME, formattedDate)
            exif.setAttribute(ExifInterface.TAG_DATETIME_ORIGINAL, formattedDate)
            exif.setAttribute(ExifInterface.TAG_DATETIME_DIGITIZED, formattedDate)
            exif.saveAttributes()
            true
        } catch (e: Exception) {
            Timber.e(e, "Falha ao gravar data/hora EXIF no arquivo: ${file.name}")
            false
        }
    }

    fun writeUserComment(file: File, comment: String): Boolean {
        return try {
            val exif = ExifInterface(file)
            exif.setAttribute(ExifInterface.TAG_USER_COMMENT, comment)
            exif.saveAttributes()
            true
        } catch (e: Exception) {
            Timber.e(e, "Falha ao gravar comentário EXIF no arquivo: ${file.name}")
            false
        }
    }

    fun readMetadata(file: File): FotoExifMetadata? {
        return try {
            val exif = ExifInterface(file)

            val dateTimeStr = exif.getAttribute(ExifInterface.TAG_DATETIME)
            val dateTime = dateTimeStr?.let {
                try {
                    LocalDateTime.parse(it, EXIF_DATE_FORMAT)
                } catch (e: Exception) {
                    Timber.w("Formato de data EXIF inválido: $it")
                    null
                }
            }

            val latLong = exif.latLong
            latLong != null

            FotoExifMetadata(
                dateTime = dateTime,
                latitude = latLong?.get(0),
                longitude = latLong?.get(1),
                altitude = exif.getAltitude(Double.NaN).takeIf { !it.isNaN() },
                userComment = exif.getAttribute(ExifInterface.TAG_USER_COMMENT),
                description = exif.getAttribute(ExifInterface.TAG_IMAGE_DESCRIPTION),
                software = exif.getAttribute(ExifInterface.TAG_SOFTWARE)
            )
        } catch (e: Exception) {
            Timber.e(e, "Falha ao ler metadados EXIF do arquivo: ${file.name}")
            null
        }
    }

    private fun writeGpsData(
        exif: ExifInterface,
        latitude: Double,
        longitude: Double,
        altitude: Double?
    ) {
        exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, if (latitude >= 0) "N" else "S")
        exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE, decimalToDMS(abs(latitude)))

        exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, if (longitude >= 0) "E" else "W")
        exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, decimalToDMS(abs(longitude)))

        altitude?.let { alt ->
            exif.setAttribute(ExifInterface.TAG_GPS_ALTITUDE_REF, if (alt >= 0) "0" else "1")
            exif.setAttribute(ExifInterface.TAG_GPS_ALTITUDE, "${abs(alt).toLong()}/1")
        }
    }

    private fun decimalToDMS(decimal: Double): String {
        val degrees = decimal.toInt()
        val minutesDecimal = (decimal - degrees) * 60
        val minutes = minutesDecimal.toInt()
        val seconds = ((minutesDecimal - minutes) * 60 * 1000).toInt()
        return "$degrees/1,$minutes/1,$seconds/1000"
    }
}

data class FotoExifMetadata(
    val dateTime: LocalDateTime? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val altitude: Double? = null,
    val userComment: String? = null,
    val description: String? = null,
    val software: String? = null
) {
    val hasGpsData: Boolean get() = latitude != null && longitude != null
    val hasDateTime: Boolean get() = dateTime != null
}
