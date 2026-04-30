// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/model/calculo/RegraDiaNormal.kt
package br.com.tlmacedo.meuponto.domain.model.calculo

import java.time.Duration

object RegraDiaNormal : RegraCalculoSaldo {

    override fun calcular(contexto: ContextoCalculo): Duration {
        if (contexto.isFuturo || !contexto.temRegistro) return Duration.ZERO

        return contexto.horasTrabalhadas
            .plus(contexto.tempoAbonado)
            .minus(contexto.cargaHoraria)
    }
}