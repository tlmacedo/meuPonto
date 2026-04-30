// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/model/calculo/RegraJornadaZerada.kt
package br.com.tlmacedo.meuponto.domain.model.calculo

import java.time.Duration

object RegraJornadaZerada : RegraCalculoSaldo {

    override fun calcular(contexto: ContextoCalculo): Duration {
        if (contexto.isFuturo || !contexto.temRegistro) return Duration.ZERO

        return contexto.horasTrabalhadas
            .plus(contexto.tempoAbonado)
    }
}