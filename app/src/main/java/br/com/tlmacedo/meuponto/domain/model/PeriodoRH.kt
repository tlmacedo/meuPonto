// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/model/PeriodoRH.kt
package br.com.tlmacedo.meuponto.domain.model

import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs

/**
 * Modelo que representa um período de fechamento do RH (mensal).
 * 
 * O período RH é definido pelo dia de início configurado e vai até
 * o dia anterior do mês subsequente.
 *
 * @property dataInicio Data de início do período
 * @property dataFim Data de fim do período (inclusive)
 * @property mesReferencia Mês de referência para exibição (baseado na data de início)
 * @property saldoMinutos Saldo do período em minutos
 *
 * @author Thiago
 * @since 3.0.0
 */
data class PeriodoRH(
    val dataInicio: LocalDate,
    val dataFim: LocalDate,
    val mesReferencia: YearMonth = YearMonth.from(dataInicio),
    val saldoMinutos: Int = 0
) {
    /**
     * Nome do mês de referência para exibição.
     */
    val mesReferenciaDescricao: String
        get() {
            val formatter = DateTimeFormatter.ofPattern("MMMM/yyyy", Locale("pt", "BR"))
            return mesReferencia.atDay(1).format(formatter).replaceFirstChar { it.uppercase() }
        }

    /**
     * Descrição completa do período.
     */
    val periodoDescricao: String
        get() {
            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
            return "${dataInicio.format(formatter)} ~ ${dataFim.format(formatter)}"
        }

    /**
     * Saldo formatado.
     */
    val saldoFormatado: String
        get() {
            val sinal = if (saldoMinutos >= 0) "+" else "-"
            val totalMinutos = abs(saldoMinutos)
            val horas = totalMinutos / 60
            val mins = totalMinutos % 60
            return "$sinal${String.format("%02d:%02d", horas, mins)}"
        }

    /**
     * Verifica se uma data está dentro deste período.
     */
    fun contemData(data: LocalDate): Boolean =
        !data.isBefore(dataInicio) && !data.isAfter(dataFim)

    companion object {
        /**
         * Cria um PeriodoRH a partir de uma data de referência e dia de início.
         */
        fun criarPara(dataReferencia: LocalDate, diaInicioFechamento: Int): PeriodoRH {
            val dia = diaInicioFechamento.coerceIn(1, 28)

            val dataInicio = if (dataReferencia.dayOfMonth >= dia) {
                dataReferencia.withDayOfMonth(dia)
            } else {
                dataReferencia.minusMonths(1).withDayOfMonth(dia)
            }

            val dataFim = dataInicio.plusMonths(1).minusDays(1)

            return PeriodoRH(
                dataInicio = dataInicio,
                dataFim = dataFim,
                mesReferencia = YearMonth.from(dataInicio)
            )
        }

        /**
         * Lista todos os períodos entre duas datas.
         */
        fun listarPeriodos(
            dataInicio: LocalDate,
            dataFim: LocalDate,
            diaInicioFechamento: Int
        ): List<PeriodoRH> {
            val periodos = mutableListOf<PeriodoRH>()
            var periodoAtual = criarPara(dataInicio, diaInicioFechamento)

            while (periodoAtual.dataInicio <= dataFim) {
                periodos.add(periodoAtual)
                periodoAtual = criarPara(
                    periodoAtual.dataFim.plusDays(1),
                    diaInicioFechamento
                )
            }

            return periodos
        }
    }
}
