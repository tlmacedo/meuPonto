// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/data/local/database/converter/Converters.kt
package br.com.tlmacedo.meuponto.data.local.database.converter

import androidx.room.TypeConverter
import br.com.tlmacedo.meuponto.domain.model.TipoPonto
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Classe de conversores de tipos para o Room Database.
 *
 * O Room não suporta nativamente tipos como LocalDateTime, LocalDate, LocalTime
 * e enums, então precisamos fornecer conversores para serialização/deserialização.
 *
 * @author Thiago
 * @since 1.0.0
 */
class Converters {

    // Formatadores imutáveis para garantir thread-safety
    private val dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val timeFormatter = DateTimeFormatter.ISO_LOCAL_TIME

    // ========================================================================
    // LocalDateTime Converters
    // ========================================================================

    /**
     * Converte LocalDateTime para String para armazenamento no banco.
     *
     * @param dateTime Data/hora a ser convertida
     * @return String no formato ISO ou null
     */
    @TypeConverter
    fun fromLocalDateTime(dateTime: LocalDateTime?): String? {
        return dateTime?.format(dateTimeFormatter)
    }

    /**
     * Converte String do banco para LocalDateTime.
     *
     * @param value String no formato ISO
     * @return LocalDateTime ou null
     */
    @TypeConverter
    fun toLocalDateTime(value: String?): LocalDateTime? {
        return value?.let { LocalDateTime.parse(it, dateTimeFormatter) }
    }

    // ========================================================================
    // LocalDate Converters
    // ========================================================================

    /**
     * Converte LocalDate para String para armazenamento no banco.
     *
     * @param date Data a ser convertida
     * @return String no formato ISO ou null
     */
    @TypeConverter
    fun fromLocalDate(date: LocalDate?): String? {
        return date?.format(dateFormatter)
    }

    /**
     * Converte String do banco para LocalDate.
     *
     * @param value String no formato ISO
     * @return LocalDate ou null
     */
    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? {
        return value?.let { LocalDate.parse(it, dateFormatter) }
    }

    // ========================================================================
    // LocalTime Converters
    // ========================================================================

    /**
     * Converte LocalTime para String para armazenamento no banco.
     *
     * @param time Hora a ser convertida
     * @return String no formato ISO ou null
     */
    @TypeConverter
    fun fromLocalTime(time: LocalTime?): String? {
        return time?.format(timeFormatter)
    }

    /**
     * Converte String do banco para LocalTime.
     *
     * @param value String no formato ISO
     * @return LocalTime ou null
     */
    @TypeConverter
    fun toLocalTime(value: String?): LocalTime? {
        return value?.let { LocalTime.parse(it, timeFormatter) }
    }

    // ========================================================================
    // TipoPonto Enum Converters
    // ========================================================================

    /**
     * Converte TipoPonto enum para String.
     *
     * @param tipo Enum TipoPonto
     * @return Nome do enum como String
     */
    @TypeConverter
    fun fromTipoPonto(tipo: TipoPonto?): String? {
        return tipo?.name
    }

    /**
     * Converte String para TipoPonto enum.
     *
     * @param value Nome do enum
     * @return TipoPonto ou null
     */
    @TypeConverter
    fun toTipoPonto(value: String?): TipoPonto? {
        return value?.let { TipoPonto.valueOf(it) }
    }
}
