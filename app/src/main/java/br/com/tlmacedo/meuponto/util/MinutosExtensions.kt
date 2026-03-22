// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/util/MinutosExtensions.kt
package br.com.tlmacedo.meuponto.util

import java.time.Duration
import kotlin.math.abs

/**
 * Extensões centralizadas para manipulação de minutos e formatação de tempo.
 *
 * PADRÃO DE FORMATAÇÃO:
 * - Duração:  "00h 00min" (sempre dois dígitos, com espaço)
 * - Saldo:   "+00h 00min" ou "-00h 00min" (com sinal, zero sempre positivo)
 *
 * As versões [Int] e [Long] são sempre consistentes entre si:
 * para qualquer valor, `valor.minutosParaSaldoFormatado()` ==
 * `valor.toLong().minutosParaSaldoFormatado()`.
 *
 * @author Thiago
 * @since 1.0.0
 * @updated 2.11.0 - Centralização de todos os formatadores com padrão único
 * @updated 12.0.0 - Corrigido sinal inconsistente na versão Long (zero retornava
 *                   string vazia em vez de "+"); padronizado com versão Int
 */

// ============================================================================
// EXTENSÕES PARA INT (minutos)
// ============================================================================

/**
 * Converte minutos para o formato "00h 00min".
 *
 * Exemplos:
 * - `0.minutosParaHoraMinuto()` → "00h 00min"
 * - `90.minutosParaHoraMinuto()` → "01h 30min"
 * - `(-90).minutosParaHoraMinuto()` → "01h 30min" (valor absoluto)
 *
 * @return String formatada com horas e minutos, sem sinal
 */
fun Int.minutosParaHoraMinuto(): String {
    val horas = abs(this) / 60
    val minutos = abs(this) % 60
    return String.format("%02dh %02dmin", horas, minutos)
}

/**
 * Converte minutos para o formato "+00h 00min" ou "-00h 00min".
 *
 * Zero retorna sempre com sinal positivo: "+00h 00min".
 *
 * Exemplos:
 * - `0.minutosParaSaldoFormatado()` → "+00h 00min"
 * - `90.minutosParaSaldoFormatado()` → "+01h 30min"
 * - `(-90).minutosParaSaldoFormatado()` → "-01h 30min"
 *
 * @return String formatada com sinal, horas e minutos
 */
fun Int.minutosParaSaldoFormatado(): String {
    val sinal = if (this >= 0) "+" else "-"
    val horas = abs(this) / 60
    val minutos = abs(this) % 60
    return String.format("%s%02dh %02dmin", sinal, horas, minutos)
}

/**
 * Converte minutos para descrição por extenso em português.
 *
 * Exemplos:
 * - `0.minutosParaDescricao()` → "0 minutos"
 * - `60.minutosParaDescricao()` → "1 hora"
 * - `90.minutosParaDescricao()` → "1 hora e 30 minutos"
 *
 * @return Descrição legível em pt-BR
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
 * Converte minutos para formato de duração compacta.
 * Alias para [minutosParaHoraMinuto].
 *
 * @return String no formato "00h 00min"
 */
fun Int.minutosParaDuracaoCompacta(): String = this.minutosParaHoraMinuto()

/**
 * Converte minutos para descrição de turno.
 *
 * @return String no formato "Turno de 00h 00min"
 */
fun Int.minutosParaTurno(): String = "Turno de ${this.minutosParaHoraMinuto()}"

/**
 * Converte minutos para descrição de intervalo.
 *
 * @return String no formato "Intervalo de 00h 00min"
 */
fun Int.minutosParaIntervalo(): String = "Intervalo de ${this.minutosParaHoraMinuto()}"

// ============================================================================
// EXTENSÕES PARA LONG (minutos)
// ============================================================================

/**
 * Converte minutos (Long) para o formato "00h 00min".
 *
 * Comportamento idêntico à versão [Int].
 *
 * @return String formatada com horas e minutos, sem sinal
 */
fun Long.minutosParaHoraMinuto(): String {
    val horas = abs(this) / 60
    val minutos = abs(this) % 60
    return String.format("%02dh %02dmin", horas, minutos)
}

/**
 * Converte minutos (Long) para o formato "+00h 00min" ou "-00h 00min".
 *
 * Comportamento idêntico à versão [Int]: zero retorna "+00h 00min".
 * Versões Int e Long são garantidamente consistentes para qualquer valor.
 *
 * Correção aplicada em 12.0.0: a versão anterior retornava string vazia
 * para zero, divergindo da versão Int.
 *
 * @return String formatada com sinal, horas e minutos
 */
fun Long.minutosParaSaldoFormatado(): String {
    val sinal = if (this >= 0) "+" else "-"
    val horas = abs(this) / 60
    val minutos = abs(this) % 60
    return String.format("%s%02dh %02dmin", sinal, horas, minutos)
}

// ============================================================================
// EXTENSÕES PARA DURATION
// ============================================================================

/**
 * Converte [Duration] para o formato "00h 00min".
 *
 * @return String formatada com horas e minutos, sem sinal
 */
fun Duration.formatarDuracao(): String {
    val totalMinutos = abs(this.toMinutes())
    val horas = totalMinutos / 60
    val minutos = totalMinutos % 60
    return String.format("%02dh %02dmin", horas, minutos)
}

/**
 * Converte [Duration] para o formato "+00h 00min" ou "-00h 00min".
 *
 * @return String formatada com sinal, horas e minutos
 */
fun Duration.formatarSaldo(): String {
    val sinal = if (!this.isNegative) "+" else "-"
    val totalMinutos = abs(this.toMinutes())
    val horas = totalMinutos / 60
    val minutos = totalMinutos % 60
    return String.format("%s%02dh %02dmin", sinal, horas, minutos)
}

/**
 * Converte [Duration] para total de minutos como [Int].
 *
 * @return Total de minutos
 */
fun Duration.toMinutosInt(): Int = this.toMinutes().toInt()

// ============================================================================
// FUNÇÕES UTILITÁRIAS
// ============================================================================

/**
 * Converte horas e minutos para total de minutos.
 *
 * @param horas Quantidade de horas
 * @param minutos Quantidade de minutos adicionais (padrão: 0)
 * @return Total em minutos
 */
fun horasParaMinutos(horas: Int, minutos: Int = 0): Int = (horas * 60) + minutos