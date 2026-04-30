// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/model/calculo/RegraCalculoSaldo.kt
package br.com.tlmacedo.meuponto.domain.model.calculo

import java.time.Duration

sealed interface RegraCalculoSaldo {
    fun calcular(contexto: ContextoCalculo): Duration
}