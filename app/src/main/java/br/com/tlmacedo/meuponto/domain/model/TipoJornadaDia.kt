// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/model/TipoJornadaDia.kt
package br.com.tlmacedo.meuponto.domain.model

/**
 * Tipo de jornada para um dia específico.
 *
 * Determina como o dia deve ser tratado nos cálculos de
 * horas trabalhadas, saldos e banco de horas.
 *
 * @author Thiago
 * @since 10.0.0
 * @updated 12.0.0 - Centralizada lógica de descrição e emojis
 */
enum class TipoJornadaDia(
    val descricao: String,
    val emoji: String
) {
    /**
     * Dia útil com jornada normal de trabalho.
     */
    NORMAL("Normal", "📅"),

    /**
     * Feriado nacional, estadual ou municipal.
     */
    FERIADO("Feriado", "🎉"),

    /**
     * Dia de folga configurado (ex: sábado, domingo).
     */
    FOLGA("Folga", "😴"),

    /**
     * Dia de compensação de horas.
     */
    COMPENSACAO("Compensação", "⚖️"),

    /**
     * Dia dentro de período de férias.
     */
    FERIAS("Férias", "🏖️"),

    /**
     * Dia de licença (médica, maternidade, etc).
     */
    LICENCA("Licença", "🏥"),

    /**
     * Ponto facultativo.
     */
    PONTO_FACULTATIVO("Ponto Facultativo", "📌");

    /**
     * Retorna a descrição formatada com emoji.
     */
    val descricaoCompleta: String
        get() = "$emoji $descricao"
}
