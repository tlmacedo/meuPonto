package br.com.tlmacedo.meuponto.util

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Extensões úteis para manipulação de data e hora.
 *
 * @author Thiago
 * @since 1.0.0
 */

// ============================================================================
// Formatadores
// ============================================================================

private val formatadorDataCompleta = DateTimeFormatter.ofPattern("EEEE, dd 'de' MMMM", Locale("pt", "BR"))
private val formatadorDataCurta = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale("pt", "BR"))
private val formatadorDataMesAno = DateTimeFormatter.ofPattern("MMMM 'de' yyyy", Locale("pt", "BR"))
private val formatadorHora = DateTimeFormatter.ofPattern("HH:mm", Locale("pt", "BR"))
private val formatadorHoraCompleta = DateTimeFormatter.ofPattern("HH:mm:ss", Locale("pt", "BR"))

// ============================================================================
// LocalDate Extensions
// ============================================================================

/**
 * Formata a data no padrão "Segunda-feira, 12 de Fevereiro".
 */
fun LocalDate.formatarCompleto(): String {
    return this.format(formatadorDataCompleta).replaceFirstChar { it.uppercase() }
}

/**
 * Formata a data no padrão "12/02/2026".
 */
fun LocalDate.formatarCurto(): String {
    return this.format(formatadorDataCurta)
}

/**
 * Formata a data no padrão "Fevereiro de 2026".
 */
fun LocalDate.formatarMesAno(): String {
    return this.format(formatadorDataMesAno).replaceFirstChar { it.uppercase() }
}

/**
 * Verifica se a data é hoje.
 */
fun LocalDate.isHoje(): Boolean {
    return this == LocalDate.now()
}

/**
 * Verifica se a data é ontem.
 */
fun LocalDate.isOntem(): Boolean {
    return this == LocalDate.now().minusDays(1)
}

/**
 * Retorna o primeiro dia do mês.
 */
fun LocalDate.primeiroDiaDoMes(): LocalDate {
    return this.withDayOfMonth(1)
}

/**
 * Retorna o último dia do mês.
 */
fun LocalDate.ultimoDiaDoMes(): LocalDate {
    return this.withDayOfMonth(this.lengthOfMonth())
}

// ============================================================================
// LocalTime Extensions
// ============================================================================

/**
 * Formata a hora no padrão "08:30".
 */
fun LocalTime.formatarHora(): String {
    return this.format(formatadorHora)
}

/**
 * Formata a hora no padrão "08:30:45".
 */
fun LocalTime.formatarHoraCompleta(): String {
    return this.format(formatadorHoraCompleta)
}

// ============================================================================
// LocalDateTime Extensions
// ============================================================================

/**
 * Formata a data/hora no padrão "12/02/2026 08:30".
 */
fun LocalDateTime.formatarDataHora(): String {
    return "${this.toLocalDate().formatarCurto()} ${this.toLocalTime().formatarHora()}"
}

/**
 * Retorna apenas a hora formatada.
 */
fun LocalDateTime.formatarHora(): String {
    return this.toLocalTime().formatarHora()
}

/**
 * Retorna apenas a data formatada.
 */
fun LocalDateTime.formatarData(): String {
    return this.toLocalDate().formatarCurto()
}
