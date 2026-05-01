// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/util/helper/MinutosFormatters.kt
package br.com.tlmacedo.meuponto.util.helper

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs


val horaFormatter = DateTimeFormatter.ofPattern("HH:mm")
val dateFormatterCompleto =
    DateTimeFormatter.ofPattern("dd/MM/yyyy (EEE)", Locale.forLanguageTag("pt-BR"))
val dateFormatterSimples = DateTimeFormatter.ofPattern("dd/MM/yyyy")


/**
 * Formata uma duração em minutos no padrão visual principal do app.
 *
 * Ex:
 * 0   -> 00h 00min
 * 56  -> 00h 56min
 * 120 -> 02h 00min
 */
fun Int.formatarComoHoraMinuto(): String {
    val total = coerceAtLeast(0)
    val horas = total / 60
    val minutos = total % 60

    return "${horas.toString().padStart(2, '0')}h ${minutos.toString().padStart(2, '0')}min"
}

/**
 * Formata saldo em minutos com sinal.
 *
 * Ex:
 *  56  -> +00h 56min
 * -56  -> -00h 56min
 *  0   -> 00h 00min
 */
fun Int.formatarComoSaldoHoraMinuto(): String {
    val sinal = when {
        this > 0 -> "+"
        this < 0 -> "-"
        else -> ""
    }

    return "$sinal${abs(this).formatarComoHoraMinuto()}"
}

/**
 * Formato compacto para mensagens curtas.
 *
 * Ex:
 *  56  -> +00:56
 * -56  -> -00:56
 *  0   -> +00:00
 *
 * Use apenas onde o app já espera o padrão HH:mm compacto.
 */
fun Int.formatarComoSaldoCompacto(): String {
    val sinal = if (this >= 0) "+" else "-"
    val total = abs(this)
    val horas = total / 60
    val minutos = total % 60

    return "$sinal${horas.toString().padStart(2, '0')}:${minutos.toString().padStart(2, '0')}"
}

/**
 * Formato mais natural para textos de detalhes.
 *
 * Ex:
 * 30  -> 30min
 * 60  -> 1h
 * 90  -> 1h 30min
 * 120 -> 2h
 */
fun Int.formatarComoDuracaoCurta(): String {
    val total = coerceAtLeast(0)
    val horas = total / 60
    val minutos = total % 60

    return when {
        horas > 0 && minutos > 0 -> "${horas}h ${minutos.toString().padStart(2, '0')}min"
        horas > 0 -> "${horas}h"
        else -> "${minutos}min"
    }
}

/**
 * Formata período de gozo de férias no formato completo.
 */
fun formatarGozoFerias(dataInicio: LocalDate, dataFim: LocalDate): String {
    return "${dataInicio.format(dateFormatterCompleto)} ~ ${dataFim.format(dateFormatterCompleto)}"
}

/**
 * Formata período aquisitivo no formato simples.
 */
fun formatarAquisitivoFerias(inicio: LocalDate?, fim: LocalDate?): String? {
    if (inicio == null || fim == null) return null
    return "${inicio.format(dateFormatterSimples)} ~ ${fim.format(dateFormatterSimples)}"
}
