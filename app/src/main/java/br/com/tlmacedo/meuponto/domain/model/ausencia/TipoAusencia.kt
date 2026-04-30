// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/model/ausencia/TipoAusencia.kt
package br.com.tlmacedo.meuponto.domain.model.ausencia

import br.com.tlmacedo.meuponto.domain.model.TipoDiaEspecial
import br.com.tlmacedo.meuponto.domain.model.calculo.RegraAtestado
import br.com.tlmacedo.meuponto.domain.model.calculo.RegraCalculoSaldo
import br.com.tlmacedo.meuponto.domain.model.calculo.RegraDiaNormal
import br.com.tlmacedo.meuponto.domain.model.calculo.RegraFaltaInjustificada
import br.com.tlmacedo.meuponto.domain.model.calculo.RegraJornadaZerada

sealed class TipoAusencia(
    open val descricao: String,
    open val emoji: String,
    open val requerDocumento: Boolean = false,
    open val permiteAnexo: Boolean = false,
    open val isPlanejada: Boolean = false,
    open val explicacaoImpacto: String,
    open val exemploUso: String,
    open val labelObservacao: String,
    open val placeholderObservacao: String
) {

    abstract val tipoDiaEspecial: TipoDiaEspecial
    abstract val regraCalculo: RegraCalculoSaldo

    open val bloqueiaRegistroPonto: Boolean = false
    open val usaIntervaloHoras: Boolean = false
    open val abonoCondicional: Boolean = false
    open val descontaDoBanco: Boolean = false

    val usaPeriodo: Boolean
        get() = !usaIntervaloHoras

    val isJustificada: Boolean
        get() = tipoDiaEspecial.zeraJornada || this is Atestado

    // =========================================================================
    // UI PROPERTIES (v6.0.0)
    // =========================================================================

    open val corIndicativa: TipoAusenciaCor
        get() = when {
            zeraJornada -> TipoAusenciaCor.VERDE
            descontaDoBanco -> TipoAusenciaCor.VERMELHO
            else -> TipoAusenciaCor.AZUL
        }

    // =========================================================================
    // DESCANSO
    // =========================================================================

    object Folga : TipoAusencia(
        descricao = "Folga",
        emoji = "🏖️",
        requerDocumento = false,
        permiteAnexo = false,
        isPlanejada = true,
        explicacaoImpacto = "Jornada zerada. Se trabalhar, as horas serão contadas como extra.",
        exemploUso = "Folga semanal remunerada.",
        labelObservacao = "Observação / comentário",
        placeholderObservacao = "Ex: sábado e domingo"
    ) {
        override val tipoDiaEspecial = TipoDiaEspecial.Folga
        override val regraCalculo = RegraJornadaZerada
        override val bloqueiaRegistroPonto = false
    }

    object Ferias : TipoAusencia(
        descricao = "Férias",
        emoji = "🏖️",
        requerDocumento = false,
        permiteAnexo = false,
        isPlanejada = true,
        explicacaoImpacto = "Jornada zerada. Se trabalhar, as horas serão contadas como extra.",
        exemploUso = "Férias anuais, férias coletivas, recesso remunerado.",
        labelObservacao = "Observação / comentário",
        placeholderObservacao = "Ex: Período aquisitivo 2024/2025"
    ) {
        override val tipoDiaEspecial = TipoDiaEspecial.Descanso.Ferias
        override val regraCalculo = RegraJornadaZerada
        override val bloqueiaRegistroPonto = true
    }

    object Feriado : TipoAusencia(
        descricao = "Feriado",
        emoji = "🎉",
        requerDocumento = false,
        permiteAnexo = false,
        isPlanejada = true,
        explicacaoImpacto = "Jornada zerada. Se trabalhar, as horas serão contadas como extra.",
        exemploUso = "Feriado nacional, estadual ou municipal.",
        labelObservacao = "Nome do feriado",
        placeholderObservacao = "Ex: Tiradentes, Natal, Ano Novo..."
    ) {
        override val tipoDiaEspecial = TipoDiaEspecial.Descanso.Feriado.Oficial
        override val regraCalculo = RegraJornadaZerada
        override val bloqueiaRegistroPonto = false
    }

    object DiaPonte : TipoAusencia(
        descricao = "Dia ponte",
        emoji = "🌉",
        requerDocumento = false,
        permiteAnexo = false,
        isPlanejada = true,
        explicacaoImpacto = "Jornada zerada por emenda de feriado. Se trabalhar, vira hora extra.",
        exemploUso = "Emenda entre feriado e fim de semana.",
        labelObservacao = "Observação",
        placeholderObservacao = "Ex: Ponte do feriado de quinta-feira..."
    ) {
        override val tipoDiaEspecial = TipoDiaEspecial.Descanso.Feriado.DiaPonte
        override val regraCalculo = RegraJornadaZerada
        override val bloqueiaRegistroPonto = false
    }

    object Facultativo : TipoAusencia(
        descricao = "Facultativo",
        emoji = "📌",
        requerDocumento = false,
        permiteAnexo = false,
        isPlanejada = true,
        explicacaoImpacto = "Jornada zerada por ponto facultativo. Se trabalhar, vira hora extra.",
        exemploUso = "Ponto facultativo definido pela empresa ou calendário.",
        labelObservacao = "Observação",
        placeholderObservacao = "Ex: Ponto facultativo de carnaval..."
    ) {
        override val tipoDiaEspecial = TipoDiaEspecial.Descanso.Feriado.Facultativo
        override val regraCalculo = RegraJornadaZerada
        override val bloqueiaRegistroPonto = false
    }

    // =========================================================================
    // AUSÊNCIA
    // =========================================================================

    object Atestado : TipoAusencia(
        descricao = "Atestado",
        emoji = "🏥",
        requerDocumento = true,
        permiteAnexo = true,
        isPlanejada = false,
        explicacaoImpacto = "Sem ponto no dia: saldo neutro. Com ponto registrado: abona o restante da jornada.",
        exemploUso = "Doença, emergência médica, exame urgente ou afastamento médico.",
        labelObservacao = "Motivo do atestado",
        placeholderObservacao = "Ex: Gripe, emergência, procedimento médico..."
    ) {
        override val tipoDiaEspecial = TipoDiaEspecial.Ausencia.Atestado
        override val regraCalculo = RegraAtestado
        override val abonoCondicional = true
    }

    object DayOff : TipoAusencia(
        descricao = "Day off",
        emoji = "🏝️",
        requerDocumento = false,
        permiteAnexo = false,
        isPlanejada = true,
        explicacaoImpacto = "Jornada zerada. Não gera débito no banco.",
        exemploUso = "Aniversário, benefício da empresa, premiação ou folga abonada.",
        labelObservacao = "Motivo do day off",
        placeholderObservacao = "Ex: Day off de aniversário..."
    ) {
        override val tipoDiaEspecial = TipoDiaEspecial.Ausencia.DayOff
        override val regraCalculo = RegraJornadaZerada
        override val bloqueiaRegistroPonto = true
    }

    object DiminuirBanco : TipoAusencia(
        descricao = "Diminuir banco de horas",
        emoji = "📄",
        requerDocumento = false,
        permiteAnexo = false,
        isPlanejada = true,
        explicacaoImpacto = "Jornada normal. O saldo negativo do dia reduz o banco positivo.",
        exemploUso = "Compensação de banco de horas ou saída planejada para reduzir saldo.",
        labelObservacao = "Observação",
        placeholderObservacao = "Ex: Compensação de banco..."
    ) {
        override val tipoDiaEspecial = TipoDiaEspecial.Ausencia.DiminuirBanco
        override val regraCalculo = RegraDiaNormal
        override val descontaDoBanco = true
    }

    object Declaracao : TipoAusencia(
        descricao = "Declaração",
        emoji = "📄",
        requerDocumento = true,
        permiteAnexo = true,
        isPlanejada = false,
        explicacaoImpacto = "Abona apenas o tempo informado. O restante da jornada deve ser cumprido.",
        exemploUso = "Consulta médica, audiência, prova, cartório ou reunião escolar.",
        labelObservacao = "Motivo da declaração",
        placeholderObservacao = "Ex: Consulta médica das 14h às 16h..."
    ) {
        override val tipoDiaEspecial = TipoDiaEspecial.Ausencia.Declaracao
        override val regraCalculo = RegraDiaNormal
        override val usaIntervaloHoras = true
    }

    sealed class Falta(
        override val descricao: String,
        override val emoji: String,
        override val requerDocumento: Boolean,
        override val permiteAnexo: Boolean,
        override val isPlanejada: Boolean,
        override val explicacaoImpacto: String,
        override val exemploUso: String,
        override val labelObservacao: String,
        override val placeholderObservacao: String
    ) : TipoAusencia(
        descricao = descricao,
        emoji = emoji,
        requerDocumento = requerDocumento,
        permiteAnexo = permiteAnexo,
        isPlanejada = isPlanejada,
        explicacaoImpacto = explicacaoImpacto,
        exemploUso = exemploUso,
        labelObservacao = labelObservacao,
        placeholderObservacao = placeholderObservacao
    ) {

        object Justificada : Falta(
            descricao = "Falta justificada",
            emoji = "🩺",
            requerDocumento = false,
            permiteAnexo = true,
            isPlanejada = true,
            explicacaoImpacto = "Jornada zerada. Não gera débito no banco.",
            exemploUso = "Casamento, doação de sangue, nascimento de filho, alistamento ou justificativa aceita.",
            labelObservacao = "Motivo da falta",
            placeholderObservacao = "Ex: Casamento, doação de sangue..."
        ) {
            override val tipoDiaEspecial = TipoDiaEspecial.Ausencia.Falta.Justificada
            override val regraCalculo = RegraJornadaZerada
        }

        object Injustificada : Falta(
            descricao = "Falta injustificada",
            emoji = "❌",
            requerDocumento = false,
            permiteAnexo = false,
            isPlanejada = false,
            explicacaoImpacto = "Jornada normal. Gera débito no banco de horas.",
            exemploUso = "Falta sem aviso, justificativa recusada ou abandono.",
            labelObservacao = "Motivo",
            placeholderObservacao = "Informe o motivo, se desejar..."
        ) {
            override val tipoDiaEspecial = TipoDiaEspecial.Ausencia.Falta.Injustificada
            override val regraCalculo = RegraFaltaInjustificada
            override val descontaDoBanco = true
        }
    }

    companion object {
        fun valueOf(value: String): TipoAusencia {
            return todos.find { it::class.simpleName == value }
                ?: throw IllegalArgumentException("No TipoAusencia with name $value")
        }

        val todos: List<TipoAusencia> = listOf(
            Folga,
            Ferias,
            Feriado,
            DiaPonte,
            Facultativo,
            Atestado,
            DayOff,
            DiminuirBanco,
            Declaracao,
            Falta.Justificada,
            Falta.Injustificada
        )

        val descansos: List<TipoAusencia> = listOf(
            Ferias,
            Feriado,
            DiaPonte,
            Facultativo
        )

        val ausencias: List<TipoAusencia> = listOf(
            Atestado,
            DayOff,
            DiminuirBanco,
            Declaracao,
            Falta.Justificada,
            Falta.Injustificada
        )
    }

    val zeraJornada: Boolean
        get() = tipoDiaEspecial.zeraJornada

    val impactoResumido: String
        get() = when {
            zeraJornada -> "✅ Abonado"
            descontaDoBanco -> "❌ Débito"
            else -> "⏱️ Normal"
        }

    fun toTipoDiaEspecial(): TipoDiaEspecial = tipoDiaEspecial
}
