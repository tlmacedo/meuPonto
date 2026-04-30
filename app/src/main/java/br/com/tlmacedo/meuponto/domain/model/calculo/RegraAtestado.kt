// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/model/calculo/RegraAtestado.kt
package br.com.tlmacedo.meuponto.domain.model.calculo

import java.time.Duration

object RegraAtestado : RegraCalculoSaldo {

    override fun calcular(contexto: ContextoCalculo): Duration {
        if (contexto.isFuturo || !contexto.temRegistro) return Duration.ZERO

        if (!contexto.temPontos) {
            return Duration.ZERO
        }

        val restanteDaJornada = contexto.cargaHoraria
            .minus(contexto.horasTrabalhadas)
            .coerceAtLeast(Duration.ZERO)

        return contexto.horasTrabalhadas
            .plus(restanteDaJornada)
            .plus(contexto.tempoAbonado)
            .minus(contexto.cargaHoraria)
    }

    private fun Duration.coerceAtLeast(minimo: Duration): Duration {
        return if (this < minimo) minimo else this
    }
}