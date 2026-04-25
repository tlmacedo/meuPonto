// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/data/local/database/converter/Converters.kt
package br.com.tlmacedo.meuponto.data.local.database.converter

import androidx.room.TypeConverter
import br.com.tlmacedo.meuponto.domain.model.AcaoAuditoria
import br.com.tlmacedo.meuponto.domain.model.DiaSemana
import br.com.tlmacedo.meuponto.domain.model.FotoFormato
import br.com.tlmacedo.meuponto.domain.model.FotoOrigem
import br.com.tlmacedo.meuponto.domain.model.TipoFechamento
import br.com.tlmacedo.meuponto.domain.model.TipoJornadaDia
import br.com.tlmacedo.meuponto.domain.model.TipoNsr
import br.com.tlmacedo.meuponto.domain.model.ausencia.TipoAusencia
import br.com.tlmacedo.meuponto.domain.model.ausencia.TipoFolga
import br.com.tlmacedo.meuponto.domain.model.chamado.AvaliacaoChamado
import br.com.tlmacedo.meuponto.domain.model.chamado.CategoriaChamado
import br.com.tlmacedo.meuponto.domain.model.chamado.PrioridadeChamado
import br.com.tlmacedo.meuponto.domain.model.chamado.StatusChamado
import br.com.tlmacedo.meuponto.domain.model.feriado.AbrangenciaFeriado
import br.com.tlmacedo.meuponto.domain.model.feriado.RecorrenciaFeriado
import br.com.tlmacedo.meuponto.domain.model.feriado.TipoFeriado
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Classe de conversores de tipos para o Room Database.
 *
 * @author Thiago
 * @since 1.0.0
 * @updated 12.0.0 - Adicionados conversores para Feriado (Tipo, Recorrencia, Abrangencia)
 */
class Converters {

    private val dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val timeFormatter = DateTimeFormatter.ISO_LOCAL_TIME

    // ════════════════════════════════════════════════════════════════════════
    // CONVERSORES DE DATA/HORA
    // ════════════════════════════════════════════════════════════════════════

    // LocalDateTime Converters
    @TypeConverter
    fun fromLocalDateTime(dateTime: LocalDateTime?): String? = dateTime?.format(dateTimeFormatter)

    @TypeConverter
    fun toLocalDateTime(value: String?): LocalDateTime? =
        value?.let { LocalDateTime.parse(it, dateTimeFormatter) }

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

    // Instant Converters (para timestamps UTC)
    @TypeConverter
    fun fromInstant(instant: Instant?): String? = instant?.toString()

    @TypeConverter
    fun toInstant(value: String?): Instant? = value?.let { Instant.parse(it) }

    // DayOfWeek Converters (para java.time.DayOfWeek)
    @TypeConverter
    fun fromDayOfWeek(dayOfWeek: DayOfWeek?): String? = dayOfWeek?.name

    @TypeConverter
    fun toDayOfWeek(value: String?): DayOfWeek? = value?.let { DayOfWeek.valueOf(it) }

    // ════════════════════════════════════════════════════════════════════════
    // CONVERSORES DE ENUMS EXISTENTES
    // ════════════════════════════════════════════════════════════════════════

    // TipoNsr Enum Converters
    @TypeConverter
    fun fromTipoNsr(tipo: TipoNsr?): String? = tipo?.name

    @TypeConverter
    fun toTipoNsr(value: String?): TipoNsr? = value?.let { TipoNsr.valueOf(it) }

    // TipoFechamento Enum Converters
    @TypeConverter
    fun fromTipoFechamento(tipo: TipoFechamento?): String? = tipo?.name

    @TypeConverter
    fun toTipoFechamento(value: String?): TipoFechamento? =
        value?.let { TipoFechamento.valueOf(it) }

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

    // TipoAusencia Enum Converters
    @TypeConverter
    fun fromTipoAusencia(value: TipoAusencia): String = value.name

    @TypeConverter
    fun toTipoAusencia(value: String): TipoAusencia = TipoAusencia.valueOf(value)

    @TypeConverter
    fun fromTipoFolga(value: TipoFolga?): String? = value?.name

    @TypeConverter
    fun toTipoFolga(value: String?): TipoFolga? = value?.let {
        try {
            TipoFolga.valueOf(it)
        } catch (e: Exception) {
            null
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    // CONVERSORES DE ENUMS DE FOTO DE COMPROVANTE
    // ════════════════════════════════════════════════════════════════════════

    // FotoOrigem Enum Converters
    @TypeConverter
    fun fromFotoOrigem(origem: FotoOrigem?): String? = origem?.name

    @TypeConverter
    fun toFotoOrigem(value: String?): FotoOrigem? = value?.let {
        try {
            FotoOrigem.valueOf(it)
        } catch (e: Exception) {
            FotoOrigem.CAMERA
        }
    }

    // FotoFormato Enum Converters
    @TypeConverter
    fun fromFotoFormato(formato: FotoFormato?): String? = formato?.name

    @TypeConverter
    fun toFotoFormato(value: String?): FotoFormato? = value?.let {
        try {
            FotoFormato.valueOf(it)
        } catch (e: Exception) {
            FotoFormato.JPEG
        }
    }

    // TipoJornadaDia Enum Converters
    @TypeConverter
    fun fromTipoJornadaDia(tipo: TipoJornadaDia?): String? = tipo?.name

    @TypeConverter
    fun toTipoJornadaDia(value: String?): TipoJornadaDia? = value?.let {
        try {
            TipoJornadaDia.valueOf(it)
        } catch (e: Exception) {
            TipoJornadaDia.NORMAL
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    // CONVERSORES DE ENUMS DE FERIADO
    // ════════════════════════════════════════════════════════════════════════

    @TypeConverter
    fun fromTipoFeriado(tipo: TipoFeriado?): String? = tipo?.name

    @TypeConverter
    fun toTipoFeriado(value: String?): TipoFeriado? = value?.let {
        try {
            TipoFeriado.valueOf(it)
        } catch (e: Exception) {
            TipoFeriado.NACIONAL
        }
    }

    @TypeConverter
    fun fromRecorrenciaFeriado(recorrencia: RecorrenciaFeriado?): String? = recorrencia?.name

    @TypeConverter
    fun toRecorrenciaFeriado(value: String?): RecorrenciaFeriado? = value?.let {
        try {
            RecorrenciaFeriado.valueOf(it)
        } catch (e: Exception) {
            RecorrenciaFeriado.ANUAL
        }
    }

    @TypeConverter
    fun fromAbrangenciaFeriado(abrangencia: AbrangenciaFeriado?): String? = abrangencia?.name

    @TypeConverter
    fun toAbrangenciaFeriado(value: String?): AbrangenciaFeriado? = value?.let {
        try {
            AbrangenciaFeriado.valueOf(it)
        } catch (e: Exception) {
            AbrangenciaFeriado.GLOBAL
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    // CONVERSORES DE CHAMADOS
    // ════════════════════════════════════════════════════════════════════════

    @TypeConverter
    fun fromCategoriaChamado(categoria: CategoriaChamado?): String? = categoria?.name

    @TypeConverter
    fun toCategoriaChamado(value: String?): CategoriaChamado? = value?.let {
        try {
            CategoriaChamado.valueOf(it)
        } catch (e: Exception) {
            null
        }
    }

    @TypeConverter
    fun fromPrioridadeChamado(prioridade: PrioridadeChamado?): String? = prioridade?.name

    @TypeConverter
    fun toPrioridadeChamado(value: String?): PrioridadeChamado? = value?.let {
        try {
            PrioridadeChamado.valueOf(it)
        } catch (e: Exception) {
            null
        }
    }

    @TypeConverter
    fun fromStatusChamado(status: StatusChamado?): String? = status?.name

    @TypeConverter
    fun toStatusChamado(value: String?): StatusChamado? = value?.let {
        try {
            StatusChamado.valueOf(it)
        } catch (e: Exception) {
            null
        }
    }

    @TypeConverter
    fun fromAvaliacaoChamado(avaliacao: AvaliacaoChamado?): String? =
        avaliacao?.let { Gson().toJson(it) }

    @TypeConverter
    fun toAvaliacaoChamado(value: String?): AvaliacaoChamado? = value?.let {
        Gson().fromJson(it, AvaliacaoChamado::class.java)
    }

    @TypeConverter
    fun fromArrayListString(list: ArrayList<String>?): String? =
        list?.let { Gson().toJson(it) }

    @TypeConverter
    fun toArrayListString(value: String?): ArrayList<String>? = value?.let {
        val listType = object : TypeToken<ArrayList<String>?>() {}.type
        Gson().fromJson(it, listType)
    }
}
