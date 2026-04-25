// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/model/HorarioDiaSemana.kt
package br.com.tlmacedo.meuponto.domain.model

import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Modelo de domínio que representa a configuração de horários para um dia da semana.
 *
 * @author Thiago
 * @since 2.0.0
 * @updated 2.7.0 - Adicionado versaoJornadaId para versionamento
 * @updated 7.2.0 - Removidos toleranciaEntradaMinutos e toleranciaSaidaMinutos
 */
data class HorarioDiaSemana(
    val id: Long = 0,
    val empregoId: Long,
    val versaoJornadaId: Long? = null,
    val diaSemana: DiaSemana,
    val ativo: Boolean = true,
    val cargaHorariaMinutos: Int = 492,
    val entradaIdeal: LocalTime? = null,
    val saidaIntervaloIdeal: LocalTime? = null,
    val voltaIntervaloIdeal: LocalTime? = null,
    val saidaIdeal: LocalTime? = null,
    val intervaloMinimoMinutos: Int = 60,
    val toleranciaIntervaloMaisMinutos: Int = 0,
    val criadoEm: LocalDateTime = LocalDateTime.now(),
    val atualizadoEm: LocalDateTime = LocalDateTime.now()
) {
    companion object {
        private val FORMATTER_HORA = DateTimeFormatter.ofPattern("HH:mm")

        /** Tolerância padrão de entrada em minutos */
        const val TOLERANCIA_ENTRADA_PADRAO = 10

        fun criarPadrao(
            empregoId: Long,
            diaSemana: DiaSemana,
            versaoJornadaId: Long? = null
        ): HorarioDiaSemana {
            val ehDiaUtil = diaSemana.isDiaUtil
            return HorarioDiaSemana(
                empregoId = empregoId,
                versaoJornadaId = versaoJornadaId,
                diaSemana = diaSemana,
                ativo = ehDiaUtil,
                cargaHorariaMinutos = if (ehDiaUtil) 492 else 0
            )
        }

        fun criarTodosPadrao(
            empregoId: Long,
            versaoJornadaId: Long? = null
        ): List<HorarioDiaSemana> {
            return DiaSemana.entries.map { dia -> criarPadrao(empregoId, dia, versaoJornadaId) }
        }
    }

    val temHorarioIdeal: Boolean get() = entradaIdeal != null
    val temHorariosIdeais: Boolean get() = entradaIdeal != null || saidaIntervaloIdeal != null || voltaIntervaloIdeal != null || saidaIdeal != null
    val temHorariosCompletos: Boolean get() = entradaIdeal != null && saidaIntervaloIdeal != null && voltaIntervaloIdeal != null && saidaIdeal != null
    val isDiaUtil: Boolean get() = ativo

    val cargaHorariaFormatada: String
        get() {
            val horas = cargaHorariaMinutos / 60
            val minutos = cargaHorariaMinutos % 60
            return String.format("%02d:%02d", horas, minutos)
        }

    val intervaloMinimoFormatado: String
        get() {
            val horas = intervaloMinimoMinutos / 60
            val minutos = intervaloMinimoMinutos % 60
            return String.format("%02d:%02d", horas, minutos)
        }

    val duracaoIntervaloIdealMinutos: Int?
        get() {
            if (saidaIntervaloIdeal != null && voltaIntervaloIdeal != null) {
                return Duration.between(saidaIntervaloIdeal, voltaIntervaloIdeal).toMinutes()
                    .toInt()
            }
            return null
        }

    val resumoHorariosIdeais: String
        get() = buildString {
            append(entradaIdeal?.format(FORMATTER_HORA) ?: "--:--")
            append(" → ")
            append(saidaIdeal?.format(FORMATTER_HORA) ?: "--:--")
        }

    val descricaoCompleta: String
        get() = buildString {
            append(diaSemana.descricao)
            if (!ativo) {
                append(" (Folga)")
            } else {
                append(" - $cargaHorariaFormatada")
                if (temHorarioIdeal) {
                    append(" ($resumoHorariosIdeais)")
                }
            }
        }
}
