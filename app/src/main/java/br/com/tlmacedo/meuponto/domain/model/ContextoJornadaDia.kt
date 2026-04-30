// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/model/ContextoJornadaDia.kt
package br.com.tlmacedo.meuponto.domain.model

import java.time.LocalDate
import java.time.LocalTime

/**
 * Contexto completo da jornada de um emprego em uma data específica.
 *
 * Este model existe para evitar mistura de regras entre empregos e versões diferentes.
 *
 * Exemplo:
 * - empregoId = emprego atualmente carregado no app
 * - data = dia que está sendo calculado
 * - versaoJornada = versão vigente desse emprego nessa data
 * - horarioDiaSemana = horário do dia da semana dentro dessa versão
 */
data class ContextoJornadaDia(
    val empregoId: Long,
    val data: LocalDate,
    val diaSemana: DiaSemana,

    val emprego: Emprego,
    val configuracaoEmprego: ConfiguracaoEmprego,
    val versaoJornada: VersaoJornada,
    val horarioDiaSemana: HorarioDiaSemana?,

    /**
     * Jornada esperada do dia.
     *
     * Regra:
     * jornadaDoDia = cargaHorariaMinutos do horário do dia
     *              + acrescimoMinutosDiasPontes da versão vigente
     *
     * Se o dia não tiver jornada, o valor é 0.
     */
    val jornadaDoDiaMinutos: Int,

    val acrescimoDiasPontesMinutos: Int,

    /**
     * Regras gerais da versão vigente.
     * Mesmo se o dia for feriado, descanso ou folga, se houver ponto,
     * essas regras continuam sendo úteis para validações.
     */
    val intervaloMinimoMinutos: Int,
    val toleranciaVoltaIntervaloMinutos: Int,
    val turnoMaximoMinutos: Int,
    val jornadaMaximaDiariaMinutos: Int,
    val intervaloMinimoInterjornadaMinutos: Int,

    /**
     * Horários ideais do dia.
     * Usados para escolher qual intervalo entre turnos recebe a tolerância.
     */
    val entradaIdeal: LocalTime?,
    val saidaIntervaloIdeal: LocalTime?,
    val voltaIntervaloIdeal: LocalTime?,
    val saidaIdeal: LocalTime?
) {
    val isDiaSemJornada: Boolean
        get() = jornadaDoDiaMinutos == 0

    val temHorarioPlanejado: Boolean
        get() = horarioDiaSemana?.ativo == true && jornadaDoDiaMinutos > 0
}