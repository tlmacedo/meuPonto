// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/model/calculo/ContextoCalculo.kt
package br.com.tlmacedo.meuponto.domain.model.calculo

import java.time.Duration

data class ContextoCalculo(
    val horasTrabalhadas: Duration,
    val cargaHoraria: Duration,
    val tempoAbonado: Duration,
    val isFuturo: Boolean,
    val temRegistro: Boolean,
    val temPontos: Boolean
)