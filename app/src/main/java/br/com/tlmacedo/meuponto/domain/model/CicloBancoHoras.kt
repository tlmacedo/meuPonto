// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/model/CicloBancoHoras.kt
package br.com.tlmacedo.meuponto.domain.model

import java.time.LocalDate
import kotlin.math.abs

/**
 * Modelo que representa um ciclo do banco de horas.
 * 
 * Encapsula todas as informações relevantes de um ciclo específico,
 * seja o atual ou um histórico.
 *
 * @property dataInicio Data de início do ciclo
 * @property dataFim Data de fim do ciclo (inclusive)
 * @property saldoInicialMinutos Saldo no início do ciclo (normalmente 0 se zerado)
 * @property saldoAtualMinutos Saldo acumulado até o momento ou fim do ciclo
 * @property fechamento Fechamento associado ao fim do ciclo (se existir)
 * @property isCicloAtual Indica se é o ciclo vigente
 *
 * @author Thiago
 * @since 3.0.0
 */
data class CicloBancoHoras(
    val dataInicio: LocalDate,
    val dataFim: LocalDate,
    val saldoInicialMinutos: Int = 0,
    val saldoAtualMinutos: Int = 0,
    val fechamento: FechamentoPeriodo? = null,
    val isCicloAtual: Boolean = false
) {
    /**
     * Duração do ciclo em dias.
     */
    val duracaoDias: Int
        get() = java.time.temporal.ChronoUnit.DAYS.between(dataInicio, dataFim).toInt() + 1

    /**
     * Verifica se o ciclo foi fechado.
     */
    val isFechado: Boolean
        get() = fechamento != null

    /**
     * Saldo final formatado.
     */
    val saldoAtualFormatado: String
        get() {
            val sinal = if (saldoAtualMinutos >= 0) "+" else "-"
            val totalMinutos = abs(saldoAtualMinutos)
            val horas = totalMinutos / 60
            val mins = totalMinutos % 60
            return "$sinal${String.format("%02d:%02d", horas, mins)}"
        }

    /**
     * Verifica se uma data está dentro deste ciclo.
     */
    fun contemData(data: LocalDate): Boolean =
        !data.isBefore(dataInicio) && !data.isAfter(dataFim)

    /**
     * Descrição do período para exibição.
     */
    val periodoDescricao: String
        get() {
            val formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")
            return "${dataInicio.format(formatter)} ~ ${dataFim.format(formatter)}"
        }
}
