// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/model/BancoHoras.kt
package br.com.tlmacedo.meuponto.domain.model

import br.com.tlmacedo.meuponto.util.formatarSaldo
import java.time.Duration

/**
 * Modelo que representa o banco de horas acumulado.
 *
 * @author Thiago
 * @since 1.0.0
 * @updated 2.11.0 - Usa formatadores padronizados de MinutosExtensions
 */
data class BancoHoras(
    val saldoTotal: Duration = Duration.ZERO
) {
    val positivo: Boolean
        get() = !saldoTotal.isNegative && !saldoTotal.isZero

    val negativo: Boolean
        get() = saldoTotal.isNegative

    val saldoTotalMinutos: Int
        get() = saldoTotal.toMinutes().toInt()

    /**
     * Formata o saldo: "+00h 00min" ou "-00h 00min"
     */
    fun formatarSaldo(): String = saldoTotal.formatarSaldo()
}
