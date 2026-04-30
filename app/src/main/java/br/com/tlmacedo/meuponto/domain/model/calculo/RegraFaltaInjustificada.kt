// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/model/calculo/RegraFaltaInjustificada.kt
package br.com.tlmacedo.meuponto.domain.model.calculo

import java.time.Duration

object RegraFaltaInjustificada : RegraCalculoSaldo {

    override fun calcular(contexto: ContextoCalculo): Duration {
        if (contexto.isFuturo) return Duration.ZERO

        return contexto.horasTrabalhadas
            .plus(contexto.tempoAbonado)
            .minus(contexto.cargaHoraria)
    }
}