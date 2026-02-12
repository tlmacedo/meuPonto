package br.com.tlmacedo.meuponto.util

import kotlin.math.abs

/**
 * Extensões úteis para manipulação de minutos e formatação de tempo.
 *
 * @author Thiago
 * @since 1.0.0
 */

/**
 * Converte minutos para o formato "HH:mm".
 *
 * @return String formatada (ex: "08:30")
 */
fun Int.minutosParaHoraMinuto(): String {
    val horas = abs(this) / 60
    val minutos = abs(this) % 60
    return String.format("%02d:%02d", horas, minutos)
}

/**
 * Converte minutos para o formato "+HH:mm" ou "-HH:mm".
 *
 * @return String formatada com sinal (ex: "+02:30" ou "-01:15")
 */
fun Int.minutosParaSaldoFormatado(): String {
    val sinal = if (this >= 0) "+" else "-"
    val horas = abs(this) / 60
    val minutos = abs(this) % 60
    return String.format("%s%02d:%02d", sinal, horas, minutos)
}

/**
 * Converte minutos para descrição por extenso.
 *
 * @return String descritiva (ex: "2 horas e 30 minutos")
 */
fun Int.minutosParaDescricao(): String {
    val totalMinutos = abs(this)
    val horas = totalMinutos / 60
    val minutos = totalMinutos % 60

    return when {
        horas == 0 && minutos == 0 -> "0 minutos"
        horas == 0 -> "$minutos minuto${if (minutos > 1) "s" else ""}"
        minutos == 0 -> "$horas hora${if (horas > 1) "s" else ""}"
        else -> "$horas hora${if (horas > 1) "s" else ""} e $minutos minuto${if (minutos > 1) "s" else ""}"
    }
}

/**
 * Converte horas e minutos para total de minutos.
 *
 * @param horas Quantidade de horas
 * @param minutos Quantidade de minutos
 * @return Total em minutos
 */
fun horasParaMinutos(horas: Int, minutos: Int = 0): Int {
    return (horas * 60) + minutos
}
