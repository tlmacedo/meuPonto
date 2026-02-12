package br.com.tlmacedo.meuponto.domain.model

import kotlin.math.abs

/**
 * Modelo que representa um saldo de horas formatado.
 *
 * Encapsula a lógica de formatação e exibição de saldos de banco de horas.
 *
 * @property totalMinutos Total de minutos (positivo = crédito, negativo = débito)
 *
 * @author Thiago
 * @since 1.0.0
 */
data class SaldoHoras(
    val totalMinutos: Int
) {
    /**
     * Indica se o saldo é positivo (crédito).
     */
    val isPositivo: Boolean
        get() = totalMinutos >= 0

    /**
     * Indica se o saldo é negativo (débito).
     */
    val isNegativo: Boolean
        get() = totalMinutos < 0

    /**
     * Retorna as horas absolutas.
     */
    val horas: Int
        get() = abs(totalMinutos) / 60

    /**
     * Retorna os minutos restantes (após extrair as horas).
     */
    val minutos: Int
        get() = abs(totalMinutos) % 60

    /**
     * Retorna o saldo formatado no padrão "+HH:mm" ou "-HH:mm".
     */
    val formatado: String
        get() {
            val sinal = if (isPositivo) "+" else "-"
            return String.format("%s%02d:%02d", sinal, horas, minutos)
        }

    /**
     * Retorna o saldo formatado sem o sinal.
     */
    val formatadoSemSinal: String
        get() = String.format("%02d:%02d", horas, minutos)

    companion object {
        /**
         * Cria um SaldoHoras zerado.
         */
        fun zero(): SaldoHoras = SaldoHoras(0)

        /**
         * Cria um SaldoHoras a partir de horas e minutos.
         *
         * @param horas Quantidade de horas
         * @param minutos Quantidade de minutos
         * @param positivo Se true, saldo positivo; se false, negativo
         */
        fun of(horas: Int, minutos: Int, positivo: Boolean = true): SaldoHoras {
            val total = (horas * 60) + minutos
            return SaldoHoras(if (positivo) total else -total)
        }
    }
}
