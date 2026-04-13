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
 */
enum class TipoJornadaDia {
    /**
     * Dia útil com jornada normal de trabalho.
     */
    NORMAL,

    /**
     * Feriado nacional, estadual ou municipal.
     */
    FERIADO,

    /**
     * Dia de folga configurado (ex: sábado, domingo).
     */
    FOLGA,

    /**
     * Dia de compensação de horas.
     */
    COMPENSACAO,

    /**
     * Dia dentro de período de férias.
     */
    FERIAS,

    /**
     * Dia de licença (médica, maternidade, etc).
     */
    LICENCA,

    /**
     * Ponto facultativo.
     */
    PONTO_FACULTATIVO
}

/**
 * Converte TipoDiaEspecial (usado nos cálculos) para TipoJornadaDia (usado na auditoria).
 */
fun TipoDiaEspecial.toTipoJornadaDia(): TipoJornadaDia = when (this) {
    TipoDiaEspecial.NORMAL,
    TipoDiaEspecial.FALTA_INJUSTIFICADA -> TipoJornadaDia.NORMAL
    TipoDiaEspecial.FERIADO,
    TipoDiaEspecial.PONTE -> TipoJornadaDia.FERIADO
    TipoDiaEspecial.FACULTATIVO -> TipoJornadaDia.PONTO_FACULTATIVO
    TipoDiaEspecial.FERIAS -> TipoJornadaDia.FERIAS
    TipoDiaEspecial.ATESTADO,
    TipoDiaEspecial.FALTA_JUSTIFICADA -> TipoJornadaDia.LICENCA
    TipoDiaEspecial.DESCANSO,
    TipoDiaEspecial.FOLGA -> TipoJornadaDia.FOLGA
}
