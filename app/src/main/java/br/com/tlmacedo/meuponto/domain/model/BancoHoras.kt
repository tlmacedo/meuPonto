// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/model/BancoHoras.kt
package br.com.tlmacedo.meuponto.domain.model

import java.time.Duration

/**
 * Modelo que representa o banco de horas acumulado.
 *
 * Contém o saldo total de horas extras ou horas devidas
 * acumuladas ao longo do tempo.
 *
 * @property saldoTotal Duração total do banco de horas
 *
 * @author Thiago
 * @since 1.0.0
 */
data class BancoHoras(
    val saldoTotal: Duration = Duration.ZERO
) {
    /**
     * Verifica se o banco de horas é positivo.
     */
    val positivo: Boolean
        get() = !saldoTotal.isNegative && !saldoTotal.isZero

    /**
     * Verifica se o banco de horas é negativo.
     */
    val negativo: Boolean
        get() = saldoTotal.isNegative

    /**
     * Formata o saldo total do banco de horas.
     */
    fun formatarSaldo(): String {
        val totalMinutos = saldoTotal.toMinutes()
        val horas = kotlin.math.abs(totalMinutos / 60)
        val minutos = kotlin.math.abs(totalMinutos % 60)
        val sinal = if (saldoTotal.isNegative) "-" else "+"
        return "$sinal${horas}h${minutos.toString().padStart(2, '0')}min"
    }
}
