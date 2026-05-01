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
     * Hora considerada SOMENTE pela tolerância de volta de intervalo.
     * Se for null, o cálculo usa a hora real do ponto.
     */
    val horaEntradaConsiderada: LocalDateTime? = null
) {
    private val dataHoraEntradaReal: LocalDateTime
        get() = LocalDateTime.of(entrada.data, entrada.hora)

    private val dataHoraSaidaReal: LocalDateTime?
        get() = saida?.let { LocalDateTime.of(it.data, it.hora) }

    val aberto: Boolean
        get() = saida == null

    val temPausaAntes: Boolean
        get() = pausaAntesMinutosReal != null && pausaAntesMinutosReal > 0

    val temPausaConsideradaDiferenteDaReal: Boolean
        get() = pausaAntesMinutosReal != null &&
                pausaAntesMinutosConsiderada != null &&
                pausaAntesMinutosReal != pausaAntesMinutosConsiderada

    val toleranciaAplicada: Boolean
        get() = temPausaConsideradaDiferenteDaReal

    val entradaReal: LocalDateTime
        get() = dataHoraEntradaReal

    val entradaParaCalculo: LocalDateTime
        get() = horaEntradaConsiderada ?: dataHoraEntradaReal

    val saidaParaCalculo: LocalDateTime
        get() = dataHoraSaidaReal ?: LocalDateTime.now()

    val temHoraEntradaConsideradaDiferenteDaReal: Boolean
        get() = horaEntradaConsiderada != null &&
                horaEntradaConsiderada != dataHoraEntradaReal

    val duracaoTurnoMinutos: Int
        get() = Duration.between(entradaParaCalculo, saidaParaCalculo)
            .toMinutes()
            .toInt()
            .coerceAtLeast(0)

    val duracaoTurnoRealMinutos: Int
        get() = Duration.between(dataHoraEntradaReal, saidaParaCalculo)
            .toMinutes()
            .toInt()
            .coerceAtLeast(0)

    fun formatarHoraEntradaReal(): String {
        return dataHoraEntradaReal.toLocalTime().format(HORA_FORMATTER)
    }

    fun formatarHoraEntradaConsiderada(): String? {
        return horaEntradaConsiderada
            ?.takeIf { it != dataHoraEntradaReal }
            ?.toLocalTime()
            ?.format(HORA_FORMATTER)
    }

    fun formatarHoraSaida(): String? {
        return dataHoraSaidaReal
            ?.toLocalTime()
            ?.format(HORA_FORMATTER)
    }

    fun formatarDuracaoCompacta(): String {
        return formatarMinutosPadrao(duracaoTurnoMinutos)
    }

    fun formatarPausaAntesCompacta(): String? {
        return pausaAntesMinutosReal?.let(::formatarMinutosPadrao)
    }

    fun formatarPausaConsideradaCompacta(): String? {
        return pausaAntesMinutosConsiderada?.let(::formatarMinutosPadrao)
    }

    companion object {
        private val HORA_FORMATTER = DateTimeFormatter.ofPattern("HH:mm")

        fun formatarMinutosPadrao(totalMinutos: Int): String {
            val minutosSeguros = totalMinutos.coerceAtLeast(0)
            val horas = minutosSeguros / 60
            val minutos = minutosSeguros % 60
            return "${horas.toString().padStart(2, '0')}h ${minutos.toString().padStart(2, '0')}min"
        }
    }
}