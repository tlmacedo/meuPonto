// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/extensions/TipoAusenciaExtensions.kt
package br.com.tlmacedo.meuponto.domain.extensions

import br.com.tlmacedo.meuponto.domain.model.TipoJornadaDia
import br.com.tlmacedo.meuponto.domain.model.ausencia.TipoAusencia
import br.com.tlmacedo.meuponto.domain.model.dia.ClassificacaoDia

// ============================================================================
// CLASSIFICAÇÃO MACRO
// ============================================================================

val TipoAusencia.isFolga: Boolean
    get() = classificacaoDia == ClassificacaoDia.FOLGA

val TipoAusencia.isDescanso: Boolean
    get() = classificacaoDia == ClassificacaoDia.DESCANSO

val TipoAusencia.isAusencia: Boolean
    get() = classificacaoDia == ClassificacaoDia.AUSENCIA

val TipoAusencia?.isNormalOrTrue: Boolean
    get() = this == null

val TipoAusencia?.isFolgaOrFalse: Boolean
    get() = this?.isFolga == true

val TipoAusencia?.isDescansoOrFalse: Boolean
    get() = this?.isDescanso == true

val TipoAusencia?.isAusenciaOrFalse: Boolean
    get() = this?.isAusencia == true

// ============================================================================
// TIPOS ESPECÍFICOS
// ============================================================================

val TipoAusencia.isFerias: Boolean
    get() = this is TipoAusencia.Ferias

val TipoAusencia.isFeriado: Boolean
    get() = this is TipoAusencia.Feriado

val TipoAusencia.isFeriadoOficial: Boolean
    get() = this is TipoAusencia.Feriado.Oficial

val TipoAusencia.isDiaPonte: Boolean
    get() = this is TipoAusencia.Feriado.DiaPonte

val TipoAusencia.isFacultativo: Boolean
    get() = this is TipoAusencia.Feriado.Facultativo

val TipoAusencia.isAtestado: Boolean
    get() = this is TipoAusencia.Atestado

val TipoAusencia.isDeclaracao: Boolean
    get() = this is TipoAusencia.Declaracao

val TipoAusencia.isDayOff: Boolean
    get() = this is TipoAusencia.DayOff

val TipoAusencia.isDiminuirBanco: Boolean
    get() = this is TipoAusencia.DiminuirBanco

val TipoAusencia.isFaltaJustificada: Boolean
    get() = this is TipoAusencia.Falta.Justificada

val TipoAusencia.isFaltaInjustificada: Boolean
    get() = this is TipoAusencia.Falta.Injustificada

val TipoAusencia?.isFeriasOrFalse: Boolean
    get() = this?.isFerias == true

val TipoAusencia?.isFeriadoOrFalse: Boolean
    get() = this?.isFeriado == true

val TipoAusencia?.isAtestadoOrFalse: Boolean
    get() = this?.isAtestado == true

val TipoAusencia?.isDeclaracaoOrFalse: Boolean
    get() = this?.isDeclaracao == true

val TipoAusencia?.isDayOffOrFalse: Boolean
    get() = this?.isDayOff == true

val TipoAusencia?.isDiminuirBancoOrFalse: Boolean
    get() = this?.isDiminuirBanco == true

val TipoAusencia?.isFaltaJustificadaOrFalse: Boolean
    get() = this?.isFaltaJustificada == true

val TipoAusencia?.isFaltaInjustificadaOrFalse: Boolean
    get() = this?.isFaltaInjustificada == true

// ============================================================================
// REGRAS DERIVADAS
// ============================================================================

val TipoAusencia.isAbonado: Boolean
    get() = when {
        isFerias -> true
        isFeriado -> true
        isFolga -> true
        isDayOff -> true
        isFaltaJustificada -> true
        isAtestado -> true
        else -> false
    }

val TipoAusencia.isDebito: Boolean
    get() = when {
        isFaltaInjustificada -> true
        isDiminuirBanco -> true
        else -> false
    }

val TipoAusencia.isParcial: Boolean
    get() = isDeclaracao

val TipoAusencia.isIntegral: Boolean
    get() = !isParcial

val TipoAusencia?.zeraJornadaOrFalse: Boolean
    get() = this?.zeraJornada == true

val TipoAusencia.bloqueiaPonto: Boolean
    get() = when {
        isFerias -> true
        isDayOff -> true
        else -> bloqueiaRegistroPonto
    }

val TipoAusencia?.bloqueiaPontoOrFalse: Boolean
    get() = this?.bloqueiaPonto == true

// ============================================================================
// PRIORIDADE PARA CALENDÁRIO / RESUMO DO DIA
// ============================================================================

val TipoAusencia.prioridade: Int
    get() = when (this) {
        TipoAusencia.Falta.Injustificada -> 100
        TipoAusencia.Falta.Justificada -> 90
        TipoAusencia.Atestado -> 80
        TipoAusencia.Declaracao -> 70
        TipoAusencia.DayOff -> 60
        TipoAusencia.DiminuirBanco -> 50
        TipoAusencia.Ferias -> 40
        TipoAusencia.Feriado.Oficial -> 30
        TipoAusencia.Feriado.DiaPonte -> 20
        TipoAusencia.Feriado.Facultativo -> 10
        TipoAusencia.Folga -> 5
    }

// ============================================================================
// CONVERSÃO PARA AUDITORIA / FOTO / JORNADA
// ============================================================================

fun TipoAusencia.toTipoJornadaDia(): TipoJornadaDia {
    return when (this) {
        TipoAusencia.Folga -> TipoJornadaDia.FOLGA

        TipoAusencia.Ferias -> TipoJornadaDia.FERIAS

        TipoAusencia.Feriado.Oficial,
        TipoAusencia.Feriado.DiaPonte -> TipoJornadaDia.FERIADO

        TipoAusencia.Feriado.Facultativo -> TipoJornadaDia.PONTO_FACULTATIVO

        TipoAusencia.Atestado -> TipoJornadaDia.LICENCA

        TipoAusencia.DayOff,
        TipoAusencia.Falta.Justificada,
        TipoAusencia.Declaracao -> TipoJornadaDia.COMPENSACAO

        TipoAusencia.Falta.Injustificada,
        TipoAusencia.DiminuirBanco -> TipoJornadaDia.NORMAL
    }
}