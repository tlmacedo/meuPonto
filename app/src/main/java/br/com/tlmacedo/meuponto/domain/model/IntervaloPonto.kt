// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/model/IntervaloPonto.kt
package br.com.tlmacedo.meuponto.domain.model

import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Representa um turno do dia.
 *
 * Turno:
 * - entrada + saída mais próxima
 * - entrada + agora, quando a saída ainda não existe
 *
 * A pausa/intervalo exibida neste turno representa o intervalo entre:
 * - saída do turno anterior
 * - entrada deste turno
 *
 * A tolerância de volta do intervalo, quando aplicada, altera somente a hora
 * considerada da entrada deste turno. A hora real nunca é perdida.
 */
data class IntervaloPonto(
    val entrada: Ponto,
    val saida: Ponto?,

    val pausaAntesMinutosReal: Int? = null,
    val pausaAntesMinutosConsiderada: Int? = null,
    val tipoPausa: TipoPausa? = null,

    /**
     * Hora considerada para cálculo do início do turno.
     *
     * Ex:
     * saída anterior: 12:35
     * entrada real: 13:54
     * intervalo mínimo: 60min
     * tolerância aplicada: sim
     *
     * horaEntradaConsiderada = 13:35
     */
    val horaEntradaConsiderada: LocalDateTime? = null
) {
    val aberto: Boolean
        get() = saida == null

    val temPausaAntes: Boolean
        get() = pausaAntesMinutosReal != null && pausaAntesMinutosReal > 0

    val toleranciaAplicada: Boolean
        get() = temPausaConsideradaDiferenteDaReal

    val temPausaConsideradaDiferenteDaReal: Boolean
        get() = pausaAntesMinutosReal != null &&
                pausaAntesMinutosConsiderada != null &&
                pausaAntesMinutosReal != pausaAntesMinutosConsiderada

    val temHoraEntradaConsiderada: Boolean
        get() = horaEntradaConsiderada != null

    val temHoraEntradaConsideradaDiferenteDaReal: Boolean
        get() = horaEntradaConsiderada != null &&
                horaEntradaConsiderada != entrada.dataHoraEfetiva

    val entradaReal: LocalDateTime
        get() = entrada.dataHoraEfetiva

    /**
     * Esta é a hora oficial para cálculo do turno.
     */
    val entradaParaCalculo: LocalDateTime
        get() = horaEntradaConsiderada ?: entrada.dataHoraEfetiva

    val saidaParaCalculo: LocalDateTime
        get() = saida?.dataHoraEfetiva ?: LocalDateTime.now()

    val duracaoTurnoMinutos: Int
        get() = Duration.between(entradaParaCalculo, saidaParaCalculo)
            .toMinutes()
            .toInt()
            .coerceAtLeast(0)

    val duracaoTurnoRealMinutos: Int
        get() = Duration.between(entrada.dataHoraEfetiva, saidaParaCalculo)
            .toMinutes()
            .toInt()
            .coerceAtLeast(0)

    fun formatarDuracaoCompacta(): String {
        return formatarMinutosPadrao(duracaoTurnoMinutos)
    }

    fun formatarDuracaoTurnoReal(): String {
        return formatarMinutosPadrao(duracaoTurnoRealMinutos)
    }

    fun formatarHoraEntradaReal(): String {
        return entrada.dataHoraEfetiva.toLocalTime().format(HORA_FORMATTER)
    }

    fun formatarHoraEntradaConsiderada(): String? {
        return horaEntradaConsiderada
            ?.toLocalTime()
            ?.format(HORA_FORMATTER)
    }

    fun formatarHoraEntradaParaCalculo(): String {
        return entradaParaCalculo.toLocalTime().format(HORA_FORMATTER)
    }

    fun formatarHoraSaida(): String? {
        return saida?.dataHoraEfetiva
            ?.toLocalTime()
            ?.format(HORA_FORMATTER)
    }

    fun formatarPausaAntesCompacta(): String? {
        return pausaAntesMinutosReal?.let(::formatarMinutosPadrao)
    }

    fun formatarPausaConsideradaCompacta(): String? {
        return pausaAntesMinutosConsiderada?.let(::formatarMinutosPadrao)
    }

    companion object {
        private val HORA_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

        fun formatarMinutosPadrao(totalMinutos: Int): String {
            val minutosSeguros = totalMinutos.coerceAtLeast(0)
            val horas = minutosSeguros / 60
            val minutos = minutosSeguros % 60

            return "${horas.toString().padStart(2, '0')}h ${minutos.toString().padStart(2, '0')}min"
        }
    }
}