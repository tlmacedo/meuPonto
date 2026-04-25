// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/model/SaldoHoras.kt
package br.com.tlmacedo.meuponto.domain.model

import kotlin.math.abs

/**
 * Modelo que representa um saldo de horas formatado.
 *
 * Encapsula a lógica de formatação e exibição de saldos de banco de horas,
 * fornecendo métodos utilitários para diferentes formatos de apresentação.
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
     * Indica se o saldo é positivo ou zero (crédito).
     */
    val isPositivo: Boolean
        get() = totalMinutos >= 0

    /**
     * Indica se o saldo é negativo (débito).
     */
    val isNegativo: Boolean
        get() = totalMinutos < 0

    /**
     * Indica se o saldo está zerado.
     */
    val isZerado: Boolean
        get() = totalMinutos == 0

    /**
     * Retorna as horas absolutas do saldo.
     */
    val horas: Int
        get() = abs(totalMinutos) / 60

    /**
     * Retorna os minutos restantes após extrair as horas.
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
     * Retorna o saldo formatado sem o sinal (apenas "HH:mm").
     */
    val formatadoSemSinal: String
        get() = String.format("%02d:%02d", horas, minutos)

    /**
     * Retorna uma descrição textual do saldo.
     *
     * Exemplo: "2 horas e 30 minutos de crédito"
     */
    val descricao: String
        get() {
            val tipo = if (isPositivo) "crédito" else "débito"
            return when {
                isZerado -> "Saldo zerado"
                horas == 0 -> "$minutos minuto${if (minutos > 1) "s" else ""} de $tipo"
                minutos == 0 -> "$horas hora${if (horas > 1) "s" else ""} de $tipo"
                else -> "$horas hora${if (horas > 1) "s" else ""} e $minutos minuto${if (minutos > 1) "s" else ""} de $tipo"
            }
        }

    /**
     * Soma este saldo com outro.
     *
     * @param outro Saldo a ser somado
     * @return Novo SaldoHoras com a soma
     */
    operator fun plus(outro: SaldoHoras): SaldoHoras {
        return SaldoHoras(this.totalMinutos + outro.totalMinutos)
    }

    /**
     * Subtrai outro saldo deste.
     *
     * @param outro Saldo a ser subtraído
     * @return Novo SaldoHoras com a diferença
     */
    operator fun minus(outro: SaldoHoras): SaldoHoras {
        return SaldoHoras(this.totalMinutos - outro.totalMinutos)
    }

    companion object {
        /**
         * Cria um SaldoHoras zerado.
         *
         * @return SaldoHoras com valor zero
         */
        fun zero(): SaldoHoras = SaldoHoras(0)

        /**
         * Cria um SaldoHoras a partir de horas e minutos.
         *
         * @param horas Quantidade de horas
         * @param minutos Quantidade de minutos (padrão: 0)
         * @param isPositivo Se true, saldo positivo; se false, negativo (padrão: true)
         * @return SaldoHoras calculado
         */
        fun of(horas: Int, minutos: Int = 0, isPositivo: Boolean = true): SaldoHoras {
            val total = (horas * 60) + minutos
            return SaldoHoras(if (isPositivo) total else -total)
        }

        /**
         * Cria um SaldoHoras a partir de uma string formatada.
         *
         * @param formatado String no formato "+HH:mm" ou "-HH:mm"
         * @return SaldoHoras ou null se formato inválido
         */
        fun fromString(formatado: String): SaldoHoras? {
            return try {
                val isPositivo = !formatado.startsWith("-")
                val limpo = formatado.removePrefix("+").removePrefix("-")
                val partes = limpo.split(":")
                if (partes.size != 2) return null

                val horas = partes[0].toInt()
                val minutos = partes[1].toInt()
                of(horas, minutos, isPositivo)
            } catch (e: Exception) {
                null
            }
        }
    }
}
