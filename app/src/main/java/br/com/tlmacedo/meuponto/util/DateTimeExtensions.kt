// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/util/DateTimeExtensions.kt
package br.com.tlmacedo.meuponto.util

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Extensões centralizadas para manipulação e formatação de data e hora.
 *
 * Todos os formatadores usam [Locale] `pt-BR` para garantir saída correta
 * independente do locale do dispositivo.
 *
 * @author Thiago
 * @since 1.0.0
 * @updated 12.0.0 - Adicionado KDoc completo em todas as funções públicas;
 *                   adicionadas funções de formatação por extenso ausentes
 */

// ============================================================================
// FORMATADORES (privados — evitar instanciar repetidamente)
// ============================================================================

private val formatadorDataCompleta =
    DateTimeFormatter.ofPattern("EEEE, dd 'de' MMMM", Locale("pt", "BR"))
private val formatadorDataCurta =
    DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale("pt", "BR"))
private val formatadorDataMesAno =
    DateTimeFormatter.ofPattern("MMMM 'de' yyyy", Locale("pt", "BR"))
private val formatadorHora =
    DateTimeFormatter.ofPattern("HH:mm", Locale("pt", "BR"))
private val formatadorHoraCompleta =
    DateTimeFormatter.ofPattern("HH:mm:ss", Locale("pt", "BR"))

// ============================================================================
// LocalDate EXTENSIONS
// ============================================================================

/**
 * Formata a data no padrão por extenso completo.
 *
 * Exemplo: `LocalDate.of(2026, 2, 12).formatarCompleto()` → "Quinta-feira, 12 de Fevereiro"
 *
 * @return String formatada com dia da semana e data por extenso em pt-BR
 */
fun LocalDate.formatarCompleto(): String {
    return this.format(formatadorDataCompleta).replaceFirstChar { it.uppercase() }
}

/**
 * Formata a data no padrão numérico curto.
 *
 * Exemplo: `LocalDate.of(2026, 2, 12).formatarCurto()` → "12/02/2026"
 *
 * @return String formatada no padrão dd/MM/yyyy
 */
fun LocalDate.formatarCurto(): String {
    return this.format(formatadorDataCurta)
}

/**
 * Formata a data no padrão mês/ano por extenso.
 *
 * Exemplo: `LocalDate.of(2026, 2, 12).formatarMesAno()` → "Fevereiro de 2026"
 *
 * @return String formatada com mês por extenso e ano em pt-BR
 */
fun LocalDate.formatarMesAno(): String {
    return this.format(formatadorDataMesAno).replaceFirstChar { it.uppercase() }
}

/**
 * Verifica se a data é hoje.
 *
 * @return true se a data for igual a [LocalDate.now]
 */
fun LocalDate.isHoje(): Boolean = this == LocalDate.now()

/**
 * Verifica se a data é ontem.
 *
 * @return true se a data for igual a [LocalDate.now] menos 1 dia
 */
fun LocalDate.isOntem(): Boolean = this == LocalDate.now().minusDays(1)

/**
 * Retorna o primeiro dia do mês desta data.
 *
 * Exemplo: `LocalDate.of(2026, 2, 15).primeiroDiaDoMes()` → 2026-02-01
 *
 * @return [LocalDate] com dia = 1 no mesmo mês e ano
 */
fun LocalDate.primeiroDiaDoMes(): LocalDate = this.withDayOfMonth(1)

/**
 * Retorna o último dia do mês desta data.
 *
 * Exemplo: `LocalDate.of(2026, 2, 1).ultimoDiaDoMes()` → 2026-02-28
 *
 * @return [LocalDate] com o último dia do mês atual
 */
fun LocalDate.ultimoDiaDoMes(): LocalDate = this.withDayOfMonth(this.lengthOfMonth())

/**
 * Converte [LocalDate] para milissegundos UTC para uso com DatePicker do Material 3.
 *
 * O DatePicker do Material 3 usa milissegundos em UTC para a data selecionada.
 * Esta função garante que a conversão não sofra deslocamento de fuso horário.
 *
 * @return Epoch millis em UTC representando meia-noite desta data
 */
fun LocalDate.toDatePickerMillis(): Long {
    return this.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
}

// ============================================================================
// LocalTime EXTENSIONS
// ============================================================================

/**
 * Formata a hora no padrão "HH:mm".
 *
 * Exemplo: `LocalTime.of(8, 5).formatarHora()` → "08:05"
 *
 * @return String formatada no padrão HH:mm
 */
fun LocalTime.formatarHora(): String = this.format(formatadorHora)

/**
 * Formata a hora no padrão completo "HH:mm:ss".
 *
 * Exemplo: `LocalTime.of(8, 5, 30).formatarHoraCompleta()` → "08:05:30"
 *
 * @return String formatada no padrão HH:mm:ss
 */
fun LocalTime.formatarHoraCompleta(): String = this.format(formatadorHoraCompleta)

// ============================================================================
// LocalDateTime EXTENSIONS
// ============================================================================

/**
 * Formata a data e hora no padrão "dd/MM/yyyy HH:mm".
 *
 * Exemplo: `LocalDateTime.of(2026, 2, 12, 8, 30).formatarDataHora()` → "12/02/2026 08:30"
 *
 * @return String formatada com data e hora
 */
fun LocalDateTime.formatarDataHora(): String {
    return "${this.toLocalDate().formatarCurto()} ${this.toLocalTime().formatarHora()}"
}

/**
 * Formata apenas a parte de hora do [LocalDateTime].
 *
 * @return String formatada no padrão HH:mm
 */
fun LocalDateTime.formatarHora(): String = this.toLocalTime().formatarHora()

/**
 * Formata apenas a parte de data do [LocalDateTime].
 *
 * @return String formatada no padrão dd/MM/yyyy
 */
fun LocalDateTime.formatarData(): String = this.toLocalDate().formatarCurto()

/**
 * Retorna uma representação amigável de data e hora relativa (Hoje, Ontem ou data completa).
 *
 * @return String formatada (ex: "Hoje, 08:30", "Ontem, 19:15" ou "12/02/2026 08:30")
 */
fun LocalDateTime.toRelativeDateTime(): String {
    return when {
        this.toLocalDate().isHoje() -> "Hoje, ${this.formatarHora()}"
        this.toLocalDate().isOntem() -> "Ontem, ${this.formatarHora()}"
        else -> this.formatarDataHora()
    }
}

// ============================================================================
// CONVERSÕES PARA DATEPICKER (Material 3)
// ============================================================================

/**
 * Converte milissegundos UTC do DatePicker para [LocalDate].
 *
 * O DatePicker do Material 3 retorna milissegundos em UTC ao selecionar
 * uma data. Esta extensão garante que a conversão use [ZoneOffset.UTC]
 * para evitar deslocamento de fuso horário que mudaria o dia resultante.
 *
 * Uso: `datePickerState.selectedDateMillis?.toLocalDateFromDatePicker()`
 *
 * @return [LocalDate] correspondente aos milissegundos em UTC
 */
fun Long.toLocalDateFromDatePicker(): LocalDate {
    return java.time.Instant.ofEpochMilli(this)
        .atZone(ZoneOffset.UTC)
        .toLocalDate()
}