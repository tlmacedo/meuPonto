// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/model/IntervaloCalculado.kt
package br.com.tlmacedo.meuponto.domain.model

import java.time.Duration
import java.time.LocalTime

data class IntervaloCalculado(
    val saida: LocalTime,
    val retornoReal: LocalTime,
    val retornoConsiderado: LocalTime,
    val minutosReais: Int,
    val minutosParaCalculo: Int,
    val minutosPrevistos: Int,
    val toleranciaMinutos: Int,
    val toleranciaAplicada: Boolean
) {
    val excedeuTolerancia: Boolean
        get() = !toleranciaAplicada && minutosReais > minutosPrevistos

    val saldoMinutos: Int
        get() = minutosParaCalculo - minutosPrevistos

    val intervaloInsuficiente: Boolean
        get() = minutosReais < minutosPrevistos

    val estaDentroDoPrevisto: Boolean
        get() = !intervaloInsuficiente && !excedeuTolerancia

    val duracaoFormatada: String
        get() {
            val horas = minutosReais / 60
            val minutos = minutosReais % 60
            return String.format("%02d:%02d", horas, minutos)
        }

    companion object {
        fun calcular(
            saida: LocalTime,
            retorno: LocalTime,
            minutosPrevistos: Int,
            toleranciaMinutos: Int
        ): IntervaloCalculado {
            val minutosReais = Duration.between(saida, retorno)
                .toMinutes()
                .toInt()
                .coerceAtLeast(0)

            val limiteComTolerancia = minutosPrevistos + toleranciaMinutos

            val dentroDaTolerancia = minutosReais <= limiteComTolerancia

            val toleranciaAplicada = minutosReais > minutosPrevistos && dentroDaTolerancia

            val minutosParaCalculo = if (toleranciaAplicada) {
                minutosPrevistos
            } else {
                minutosReais
            }

            return IntervaloCalculado(
                saida = saida,
                retornoReal = retorno,
                retornoConsiderado = saida.plusMinutes(minutosParaCalculo.toLong()),
                minutosReais = minutosReais,
                minutosParaCalculo = minutosParaCalculo,
                minutosPrevistos = minutosPrevistos,
                toleranciaMinutos = toleranciaMinutos,
                toleranciaAplicada = toleranciaAplicada
            )
        }
    }
}
