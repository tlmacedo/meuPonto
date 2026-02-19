// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/data/local/database/converter/FeriadoConverters.kt
package br.com.tlmacedo.meuponto.data.local.database.converter

import androidx.room.TypeConverter
import br.com.tlmacedo.meuponto.domain.model.feriado.AbrangenciaFeriado
import br.com.tlmacedo.meuponto.domain.model.feriado.RecorrenciaFeriado
import br.com.tlmacedo.meuponto.domain.model.feriado.TipoFeriado

/**
 * TypeConverters para enums relacionados a Feriados.
 *
 * @author Thiago
 * @since 3.0.0
 */
class FeriadoConverters {

    // ========================================================================
    // TipoFeriado
    // ========================================================================

    @TypeConverter
    fun fromTipoFeriado(tipo: TipoFeriado?): String? = tipo?.name

    @TypeConverter
    fun toTipoFeriado(value: String?): TipoFeriado? = value?.let {
        try {
            TipoFeriado.valueOf(it)
        } catch (e: IllegalArgumentException) {
            TipoFeriado.NACIONAL // Fallback
        }
    }

    // ========================================================================
    // RecorrenciaFeriado
    // ========================================================================

    @TypeConverter
    fun fromRecorrenciaFeriado(recorrencia: RecorrenciaFeriado?): String? = recorrencia?.name

    @TypeConverter
    fun toRecorrenciaFeriado(value: String?): RecorrenciaFeriado? = value?.let {
        try {
            RecorrenciaFeriado.valueOf(it)
        } catch (e: IllegalArgumentException) {
            RecorrenciaFeriado.ANUAL // Fallback
        }
    }

    // ========================================================================
    // AbrangenciaFeriado
    // ========================================================================

    @TypeConverter
    fun fromAbrangenciaFeriado(abrangencia: AbrangenciaFeriado?): String? = abrangencia?.name

    @TypeConverter
    fun toAbrangenciaFeriado(value: String?): AbrangenciaFeriado? = value?.let {
        try {
            AbrangenciaFeriado.valueOf(it)
        } catch (e: IllegalArgumentException) {
            AbrangenciaFeriado.GLOBAL // Fallback
        }
    }
}
