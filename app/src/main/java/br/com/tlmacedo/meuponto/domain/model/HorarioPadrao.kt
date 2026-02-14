package br.com.tlmacedo.meuponto.domain.model

import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Modelo de domínio que representa o horário padrão de trabalho
 * para um dia da semana específico.
 *
 * @property id Identificador único
 * @property empregoId FK para o emprego associado
 * @property diaSemana Dia da semana (1=Segunda ... 7=Domingo)
 * @property horaEntrada Horário de entrada padrão
 * @property horaSaidaAlmoco Horário de saída para almoço (opcional)
 * @property horaRetornoAlmoco Horário de retorno do almoço (opcional)
 * @property horaSaida Horário de saída padrão
 * @property jornadaMinutos Jornada esperada em minutos para o dia
 * @property isDiaUtil Se o dia é considerado útil para trabalho
 * @property criadoEm Timestamp de criação
 * @property atualizadoEm Timestamp da última atualização
 *
 * @author Thiago
 * @since 2.0.0
 */
data class HorarioPadrao(
    val id: Long = 0,
    val empregoId: Long,
    val diaSemana: Int,
    val horaEntrada: LocalTime? = null,
    val horaSaidaAlmoco: LocalTime? = null,
    val horaRetornoAlmoco: LocalTime? = null,
    val horaSaida: LocalTime? = null,
    val jornadaMinutos: Int = 480,
    val isDiaUtil: Boolean = true,
    val criadoEm: LocalDateTime = LocalDateTime.now(),
    val atualizadoEm: LocalDateTime = LocalDateTime.now()
) {
    /**
     * Retorna a jornada formatada (ex: "08:00").
     */
    val jornadaFormatada: String
        get() {
            val h = jornadaMinutos / 60
            val m = jornadaMinutos % 60
            return String.format("%02d:%02d", h, m)
        }

    /**
     * Verifica se tem intervalo de almoço configurado.
     */
    val temIntervaloAlmoco: Boolean
        get() = horaSaidaAlmoco != null && horaRetornoAlmoco != null

    /**
     * Calcula a duração do intervalo de almoço em minutos.
     */
    val intervaloAlmocoMinutos: Long?
        get() {
            if (!temIntervaloAlmoco) return null
            return java.time.Duration.between(horaSaidaAlmoco, horaRetornoAlmoco).toMinutes()
        }

    /**
     * Retorna o nome do dia da semana.
     */
    val nomeDiaSemana: String
        get() = when (diaSemana) {
            1 -> "Segunda-feira"
            2 -> "Terça-feira"
            3 -> "Quarta-feira"
            4 -> "Quinta-feira"
            5 -> "Sexta-feira"
            6 -> "Sábado"
            7 -> "Domingo"
            else -> "Desconhecido"
        }

    /**
     * Retorna o nome abreviado do dia da semana.
     */
    val nomeDiaSemanaAbreviado: String
        get() = when (diaSemana) {
            1 -> "Seg"
            2 -> "Ter"
            3 -> "Qua"
            4 -> "Qui"
            5 -> "Sex"
            6 -> "Sáb"
            7 -> "Dom"
            else -> "???"
        }

    init {
        require(diaSemana in 1..7) { "Dia da semana deve estar entre 1 (Segunda) e 7 (Domingo)" }
        require(jornadaMinutos >= 0) { "Jornada não pode ser negativa" }
    }
}
