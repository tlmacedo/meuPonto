// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/model/TipoDiaEspecial.kt
package br.com.tlmacedo.meuponto.domain.model

import br.com.tlmacedo.meuponto.domain.model.ausencia.TipoAusenciaCor

sealed class TipoDiaEspecial(
    open val descricao: String,
    open val emoji: String,
    open val zeraJornada: Boolean
) {

    open val isNormal: Boolean get() = false
    open val isFolga: Boolean get() = false
    open val isDescanso: Boolean get() = false
    open val isAusencia: Boolean get() = false
    open val isFerias: Boolean get() = false
    open val isFeriado: Boolean get() = false
    open val isFalta: Boolean get() = false
    open val isFaltaJustificada: Boolean get() = false
    open val isFaltaInjustificada: Boolean get() = false
    open val isAtestado: Boolean get() = false
    open val isDayOff: Boolean get() = false
    open val isDiminuirBanco: Boolean get() = false
    open val isDeclaracao: Boolean get() = false

    open val descricaoCurta: String get() = descricao

    /**
     * Cor semântica indicativa do tipo de dia.
     */
    val corIndicativa: TipoAusenciaCor
        get() = when (this) {
            is Normal -> TipoAusenciaCor.AZUL
            is Folga -> TipoAusenciaCor.VERDE
            is Descanso.Ferias -> TipoAusenciaCor.AZUL
            is Descanso.Feriado -> TipoAusenciaCor.VERDE
            is Ausencia.Atestado -> TipoAusenciaCor.VERMELHO
            is Ausencia.DayOff -> TipoAusenciaCor.VERDE
            is Ausencia.DiminuirBanco -> TipoAusenciaCor.AZUL
            is Ausencia.Declaracao -> TipoAusenciaCor.AZUL
            is Ausencia.Falta.Justificada -> TipoAusenciaCor.VERDE
            is Ausencia.Falta.Injustificada -> TipoAusenciaCor.VERMELHO
        }

    object Normal : TipoDiaEspecial(
        descricao = "Dia normal",
        emoji = "📅",
        zeraJornada = false
    ) {
        override val isNormal: Boolean = true
        override val descricaoCurta: String = ""
    }

    object Folga : TipoDiaEspecial(
        descricao = "Folga",
        emoji = "😴",
        zeraJornada = false
    ) {
        override val isFolga: Boolean = true
    }

    sealed class Descanso(
        override val descricao: String,
        override val emoji: String
    ) : TipoDiaEspecial(
        descricao = descricao,
        emoji = emoji,
        zeraJornada = true
    ) {
        override val isDescanso: Boolean = true

        object Ferias : Descanso(
            descricao = "Férias",
            emoji = "🏖️"
        ) {
            override val isFerias: Boolean = true
        }

        sealed class Feriado(
            override val descricao: String,
            override val emoji: String
        ) : Descanso(
            descricao = descricao,
            emoji = emoji
        ) {
            override val isFeriado: Boolean = true

            object Oficial : Feriado(
                descricao = "Feriado",
                emoji = "🎉"
            )

            object DiaPonte : Feriado(
                descricao = "Dia ponte",
                emoji = "🌉"
            ) {
                override val descricaoCurta: String = "Ponte"
            }

            object Facultativo : Feriado(
                descricao = "Facultativo",
                emoji = "📌"
            )
        }
    }

    sealed class Ausencia(
        override val descricao: String,
        override val emoji: String,
        override val zeraJornada: Boolean
    ) : TipoDiaEspecial(
        descricao = descricao,
        emoji = emoji,
        zeraJornada = zeraJornada
    ) {
        override val isAusencia: Boolean = true

        object Atestado : Ausencia(
            descricao = "Atestado",
            emoji = "🏥",
            zeraJornada = false
        ) {
            override val isAtestado: Boolean = true
        }

        object DayOff : Ausencia(
            descricao = "Day off",
            emoji = "🏝️",
            zeraJornada = true
        ) {
            override val isDayOff: Boolean = true
        }

        object DiminuirBanco : Ausencia(
            descricao = "Banco de horas",
            emoji = "📄",
            zeraJornada = false
        ) {
            override val isDiminuirBanco: Boolean = true
        }

        object Declaracao : Ausencia(
            descricao = "Declaração",
            emoji = "📄",
            zeraJornada = false
        ) {
            override val isDeclaracao: Boolean = true
        }

        sealed class Falta(
            override val descricao: String,
            override val emoji: String,
            override val zeraJornada: Boolean
        ) : Ausencia(
            descricao = descricao,
            emoji = emoji,
            zeraJornada = zeraJornada
        ) {
            override val isFalta: Boolean = true

            object Justificada : Falta(
                descricao = "Falta justificada",
                emoji = "🩺",
                zeraJornada = true
            ) {
                override val isFaltaJustificada: Boolean = true
                override val descricaoCurta: String = "Falta Just."
            }

            object Injustificada : Falta(
                descricao = "Falta injustificada",
                emoji = "❌",
                zeraJornada = false
            ) {
                override val isFaltaInjustificada: Boolean = true
                override val descricaoCurta: String = "Falta"
            }
        }
    }

    /**
     * Converte para o modelo de jornada usado na auditoria.
     */
    fun toTipoJornadaDia(): TipoJornadaDia = when (this) {
        Normal, Ausencia.DiminuirBanco,
        Ausencia.Falta.Injustificada -> TipoJornadaDia.NORMAL

        Descanso.Feriado.Oficial,
        Descanso.Feriado.DiaPonte -> TipoJornadaDia.FERIADO

        Descanso.Feriado.Facultativo -> TipoJornadaDia.PONTO_FACULTATIVO
        Descanso.Ferias -> TipoJornadaDia.FERIAS
        Ausencia.Atestado -> TipoJornadaDia.LICENCA
        Ausencia.DayOff, Ausencia.Falta.Justificada,
        Ausencia.Declaracao -> TipoJornadaDia.COMPENSACAO

        Folga -> TipoJornadaDia.FOLGA
    }
}

inline fun <reified T : TipoDiaEspecial> TipoDiaEspecial.isTipo(): Boolean {
    return this is T
}
