// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/util/MinutosExtensions.kt
package br.com.tlmacedo.meuponto.util

import java.time.Duration
import kotlin.math.abs

/**
 * Extensões centralizadas para manipulação de minutos e formatação de tempo.
 *
 * PADRÃO DE FORMATAÇÃO:
 * - Duração: "00h 00min" (sempre dois dígitos, com espaço)
 * - Saldo: "+00h 00min" ou "-00h 00min" (com sinal)
 *
 * @author Thiago
 * @since 1.0.0
 * @updated 2.11.0 - Centralização de todos os formatadores com padrão único
 */

// ============================================================================
// EXTENSÕES PARA INT (minutos)
// ============================================================================

/**
 * Converte minutos para o formato "00h 00min".
 */
fun Int.minutosParaHoraMinuto(): String {
    val horas = abs(this) / 60
    val minutos = abs(this) % 60
    return String.format("%02dh %02dmin", horas, minutos)
}

/**
 * Converte minutos para o formato "+00h 00min" ou "-00h 00min".
 */
fun Int.minutosParaSaldoFormatado(): String {
    val sinal = if (this >= 0) "+" else "-"
    val horas = abs(this) / 60
    val minutos = abs(this) % 60
    return String.format("%s%02dh %02dmin", sinal, horas, minutos)
}

/**
 * Converte minutos para descrição por extenso.
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
 * Converte minutos para formato de duração compacta (alias).
 */
fun Int.minutosParaDuracaoCompacta(): String = this.minutosParaHoraMinuto()

/**
 * Converte minutos para descrição de turno.
 */
fun Int.minutosParaTurno(): String = "Turno de ${this.minutosParaHoraMinuto()}"

/**
 * Converte minutos para descrição de intervalo.
 */
fun Int.minutosParaIntervalo(): String = "Intervalo de ${this.minutosParaHoraMinuto()}"

// ============================================================================
// EXTENSÕES PARA LONG (minutos)
// ============================================================================

/**
 * Converte minutos (Long) para o formato "00h 00min".
 */
fun Long.minutosParaHoraMinuto(): String {
    val horas = abs(this) / 60
    val minutos = abs(this) % 60
    return String.format("%02dh %02dmin", horas, minutos)
}

/**
 * Converte minutos (Long) para o formato "+00h 00min" ou "-00h 00min".
 */
fun Long.minutosParaSaldoFormatado(): String {
    val sinal = if (this > 0) "+" else { if (this <0) "-" else ""}
    val horas = abs(this) / 60
    val minutos = abs(this) % 60
    return String.format("%s%02dh %02dmin", sinal, horas, minutos)
}

// ============================================================================
// EXTENSÕES PARA DURATION
// ============================================================================

/**
 * Converte Duration para o formato "00h 00min".
 */
fun Duration.formatarDuracao(): String {
    val totalMinutos = abs(this.toMinutes())
    val horas = totalMinutos / 60
    val minutos = totalMinutos % 60
    return String.format("%02dh %02dmin", horas, minutos)
}

/**
 * Converte Duration para o formato "+00h 00min" ou "-00h 00min".
 */
fun Duration.formatarSaldo(): String {
    val sinal = if (!this.isNegative) "+" else "-"
    val totalMinutos = abs(this.toMinutes())
    val horas = totalMinutos / 60
    val minutos = totalMinutos % 60
    return String.format("%s%02dh %02dmin", sinal, horas, minutos)
}

/**
 * Converte Duration para total de minutos (Int).
 */
fun Duration.toMinutosInt(): Int = this.toMinutes().toInt()

// ============================================================================
// FUNÇÕES UTILITÁRIAS
// ============================================================================

/**
 * Converte horas e minutos para total de minutos.
 */
fun horasParaMinutos(horas: Int, minutos: Int = 0): Int = (horas * 60) + minutos
