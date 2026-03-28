// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/util/foto/ExifDataWriter.kt
package br.com.tlmacedo.meuponto.util.foto

import androidx.exifinterface.media.ExifInterface
import timber.log.Timber
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

/**
 * Gravador de metadados EXIF em imagens JPEG.
 *
 * Responsável por escrever informações de data/hora, localização GPS
 * e comentários nos metadados EXIF de arquivos JPEG processados.
 *
 * ATENÇÃO: EXIF só é suportado em arquivos JPEG. Não usar com PNG.
 *
 * @author Thiago
 * @since 10.0.0
 * @updated 12.0.0 - e.printStackTrace() substituído por Timber.e/w();
 *                   adicionado KDoc completo em todas as funções públicas
 */
@Singleton
class ExifDataWriter @Inject constructor() {

    companion object {
        /** Tag de software gravada em todas as imagens do app */
        const val SOFTWARE_TAG = "MeuPonto App"

        /** Formato de data/hora exigido pelo padrão EXIF */
        private val EXIF_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss")
    }

    /**
     * Escreve metadados completos em uma imagem JPEG.
     *
     * Grava: data/hora, localização GPS (se disponível), comentário
     * personalizado, descrição e tag de software.
     *
     * @param file Arquivo JPEG de destino
     * @param metadata Metadados a serem gravados
     * @return true se gravado com sucesso, false em caso de erro
     */
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

    /**
     * Escreve apenas dados de localização GPS em um arquivo JPEG existente.
     *
     * @param file Arquivo JPEG de destino
     * @param latitude Latitude em graus decimais
     * @param longitude Longitude em graus decimais
     * @param altitude Altitude em metros (opcional)
     * @return true se gravado com sucesso
     */
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

    /**
     * Escreve apenas data e hora em um arquivo JPEG existente.
     *
     * @param file Arquivo JPEG de destino
     * @param dateTime Data e hora a serem gravadas
     * @return true se gravado com sucesso
     */
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

    /**
     * Escreve comentário personalizado em um arquivo JPEG existente.
     *
     * @param file Arquivo JPEG de destino
     * @param comment Texto do comentário
     * @return true se gravado com sucesso
     */
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

    /**
     * Lê metadados EXIF de uma imagem.
     *
     * @param file Arquivo JPEG a ser lido
     * @return [FotoExifMetadata] com os dados lidos ou null em caso de erro
     */
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

            val latLong = FloatArray(2)
            val hasGps = exif.getLatLong(latLong)

            FotoExifMetadata(
                dateTime = dateTime,
                latitude = if (hasGps) latLong[0].toDouble() else null,
                longitude = if (hasGps) latLong[1].toDouble() else null,
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

    // ========================================================================
    // HELPERS PRIVADOS
    // ========================================================================

    /**
     * Grava os dados GPS no [ExifInterface] fornecido.
     * Deve ser seguido por [ExifInterface.saveAttributes] pelo chamador.
     *
     * @param exif Interface EXIF já aberta para o arquivo
     * @param latitude Latitude em graus decimais
     * @param longitude Longitude em graus decimais
     * @param altitude Altitude em metros (opcional)
     */
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

    /**
     * Converte coordenada decimal para formato DMS exigido pelo padrão EXIF.
     *
     * Formato: "graus/1,minutos/1,segundos*1000/1000"
     *
     * @param decimal Coordenada em graus decimais (valor absoluto)
     * @return String no formato DMS para EXIF
     */
    private fun decimalToDMS(decimal: Double): String {
        val degrees = decimal.toInt()
        val minutesDecimal = (decimal - degrees) * 60
        val minutes = minutesDecimal.toInt()
        val seconds = ((minutesDecimal - minutes) * 60 * 1000).toInt()
        return "$degrees/1,$minutes/1,$seconds/1000"
    }
}

/**
 * Metadados EXIF para foto de comprovante de ponto.
 *
 * @property dateTime Data e hora da captura
 * @property latitude Latitude GPS em graus decimais (opcional)
 * @property longitude Longitude GPS em graus decimais (opcional)
 * @property altitude Altitude em metros (opcional)
 * @property userComment Comentário com identificadores do ponto/emprego
 * @property description Descrição legível do comprovante
 * @property software Tag de software (preenchida automaticamente pelo [ExifDataWriter])
 */
data class FotoExifMetadata(
    val dateTime: LocalDateTime? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val altitude: Double? = null,
    val userComment: String? = null,
    val description: String? = null,
    val software: String? = null
) {
    /** true se há dados de localização GPS disponíveis */
    val hasGpsData: Boolean get() = latitude != null && longitude != null

    /** true se há data/hora registrada */
    val hasDateTime: Boolean get() = dateTime != null
}
