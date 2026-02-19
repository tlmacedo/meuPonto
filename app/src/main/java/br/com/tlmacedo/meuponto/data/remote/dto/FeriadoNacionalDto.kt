// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/data/remote/dto/FeriadoNacionalDto.kt
package br.com.tlmacedo.meuponto.data.remote.dto

import br.com.tlmacedo.meuponto.domain.model.feriado.AbrangenciaFeriado
import br.com.tlmacedo.meuponto.domain.model.feriado.Feriado
import br.com.tlmacedo.meuponto.domain.model.feriado.RecorrenciaFeriado
import br.com.tlmacedo.meuponto.domain.model.feriado.TipoFeriado
import com.google.gson.annotations.SerializedName
import java.time.LocalDate
import java.time.MonthDay
import java.time.format.DateTimeFormatter

/**
 * DTO para resposta da Brasil API de feriados nacionais.
 *
 * Endpoint: https://brasilapi.com.br/api/feriados/v1/{ano}
 *
 * Exemplo de resposta:
 * ```json
 * [
 *   {
 *     "date": "2025-01-01",
 *     "name": "Confraternização Universal",
 *     "type": "national"
 *   }
 * ]
 * ```
 *
 * @author Thiago
 * @since 3.0.0
 */
data class FeriadoNacionalDto(
    @SerializedName("date")
    val date: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("type")
    val type: String
) {
    /**
     * Converte o DTO para o modelo de domínio.
     *
     * @param ano Ano de referência para feriados únicos (móveis)
     * @return Feriado do domínio
     */
    fun toDomain(ano: Int): Feriado {
        val dataFeriado = LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE)

        // Determina se é um feriado com data fixa (recorrente) ou móvel (único por ano)
        val (recorrencia, diaMes, dataEspecifica, anoRef) = determinarRecorrencia(dataFeriado, ano)

        return Feriado(
            nome = name,
            tipo = mapearTipo(type),
            recorrencia = recorrencia,
            abrangencia = AbrangenciaFeriado.GLOBAL,
            diaMes = diaMes,
            dataEspecifica = dataEspecifica,
            anoReferencia = anoRef,
            observacao = "Importado da Brasil API"
        )
    }

    /**
     * Mapeia o tipo da API para o enum do domínio.
     */
    private fun mapearTipo(type: String): TipoFeriado {
        return when (type.lowercase()) {
            "national" -> TipoFeriado.NACIONAL
            "state" -> TipoFeriado.ESTADUAL
            "municipal" -> TipoFeriado.MUNICIPAL
            else -> TipoFeriado.FACULTATIVO
        }
    }

    /**
     * Determina se o feriado é recorrente (data fixa) ou único (móvel).
     *
     * Feriados móveis no Brasil:
     * - Carnaval
     * - Sexta-feira Santa
     * - Páscoa
     * - Corpus Christi
     */
    private fun determinarRecorrencia(
        data: LocalDate,
        ano: Int
    ): RecorrenciaInfo {
        val feriadosMoveis = listOf(
            "carnaval",
            "sexta-feira santa",
            "paixão de cristo",
            "páscoa",
            "corpus christi"
        )

        val isMovel = feriadosMoveis.any { name.lowercase().contains(it) }

        return if (isMovel) {
            // Feriado móvel - único por ano
            RecorrenciaInfo(
                recorrencia = RecorrenciaFeriado.UNICO,
                diaMes = null,
                dataEspecifica = data,
                anoReferencia = ano
            )
        } else {
            // Feriado fixo - recorrente
            RecorrenciaInfo(
                recorrencia = RecorrenciaFeriado.ANUAL,
                diaMes = MonthDay.of(data.monthValue, data.dayOfMonth),
                dataEspecifica = null,
                anoReferencia = null
            )
        }
    }

    private data class RecorrenciaInfo(
        val recorrencia: RecorrenciaFeriado,
        val diaMes: MonthDay?,
        val dataEspecifica: LocalDate?,
        val anoReferencia: Int?
    )
}
