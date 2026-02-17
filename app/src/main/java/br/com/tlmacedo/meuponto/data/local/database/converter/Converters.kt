// Arquivo: Converters.kt
package br.com.tlmacedo.meuponto.data.local.database.converter

import androidx.room.TypeConverter
import br.com.tlmacedo.meuponto.domain.model.AcaoAuditoria
import br.com.tlmacedo.meuponto.domain.model.DiaSemana
import br.com.tlmacedo.meuponto.domain.model.TipoFechamento
import br.com.tlmacedo.meuponto.domain.model.TipoNsr
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Classe de conversores de tipos para o Room Database.
 *
 * @author Thiago
 * @since 1.0.0
 * @updated 2.1.0 - Removido conversor de TipoPonto (tipo calculado em runtime)
 */
class Converters {

    private val dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val timeFormatter = DateTimeFormatter.ISO_LOCAL_TIME

    // LocalDateTime Converters
    @TypeConverter
    fun fromLocalDateTime(dateTime: LocalDateTime?): String? = dateTime?.format(dateTimeFormatter)

    @TypeConverter
    fun toLocalDateTime(value: String?): LocalDateTime? = value?.let { LocalDateTime.parse(it, dateTimeFormatter) }

    // LocalDate Converters
    @TypeConverter
    fun fromLocalDate(date: LocalDate?): String? = date?.format(dateFormatter)

    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? = value?.let { LocalDate.parse(it, dateFormatter) }

    // LocalTime Converters
    @TypeConverter
    fun fromLocalTime(time: LocalTime?): String? = time?.format(timeFormatter)

    @TypeConverter
    fun toLocalTime(value: String?): LocalTime? = value?.let { LocalTime.parse(it, timeFormatter) }

    // TipoNsr Enum Converters
    @TypeConverter
    fun fromTipoNsr(tipo: TipoNsr?): String? = tipo?.name

    @TypeConverter
    fun toTipoNsr(value: String?): TipoNsr? = value?.let { TipoNsr.valueOf(it) }

    // TipoFechamento Enum Converters
    @TypeConverter
    fun fromTipoFechamento(tipo: TipoFechamento?): String? = tipo?.name

    @TypeConverter
    fun toTipoFechamento(value: String?): TipoFechamento? = value?.let { TipoFechamento.valueOf(it) }

    // DiaSemana Enum Converters
    @TypeConverter
    fun fromDiaSemana(dia: DiaSemana?): String? = dia?.name

    @TypeConverter
    fun toDiaSemana(value: String?): DiaSemana? = value?.let { DiaSemana.valueOf(it) }

    // AcaoAuditoria Enum Converters
    @TypeConverter
    fun fromAcaoAuditoria(acao: AcaoAuditoria?): String? = acao?.name

    @TypeConverter
    fun toAcaoAuditoria(value: String?): AcaoAuditoria? = value?.let { AcaoAuditoria.valueOf(it) }
}
