// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/model/ausencia/TipoAusencia.kt
package br.com.tlmacedo.meuponto.domain.model.ausencia

import br.com.tlmacedo.meuponto.domain.model.calculo.RegraAtestado
import br.com.tlmacedo.meuponto.domain.model.calculo.RegraCalculoSaldo
import br.com.tlmacedo.meuponto.domain.model.calculo.RegraDiaNormal
import br.com.tlmacedo.meuponto.domain.model.calculo.RegraFaltaInjustificada
import br.com.tlmacedo.meuponto.domain.model.calculo.RegraJornadaZerada
import br.com.tlmacedo.meuponto.domain.model.dia.ClassificacaoDia

sealed class TipoAusencia(
    open val descricao: String,
    open val emoji: String,
    open val classificacaoDia: ClassificacaoDia,
    open val requerDocumento: Boolean = false,
    open val permiteAnexo: Boolean = false,
    open val isPlanejada: Boolean = false,
    open val explicacaoImpacto: String,
    open val exemploUso: String,
    open val labelObservacao: String,
    open val placeholderObservacao: String
) {

    abstract val regraCalculo: RegraCalculoSaldo

    open val bloqueiaRegistroPonto: Boolean = false
    open val usaIntervaloHoras: Boolean = false
    open val abonoCondicional: Boolean = false
    open val descontaDoBanco: Boolean = false
    open val limiteAnual: Int? = null
    open val exigeRegraAniversario: Boolean = false

    val usaPeriodo: Boolean
        get() = !usaIntervaloHoras

    val zeraJornada: Boolean
        get() = regraCalculo == RegraJornadaZerada

    val isDescanso: Boolean
        get() = classificacaoDia == ClassificacaoDia.DESCANSO

    val isAusencia: Boolean
        get() = classificacaoDia == ClassificacaoDia.AUSENCIA

    val isFolga: Boolean
        get() = classificacaoDia == ClassificacaoDia.FOLGA

    val isJustificada: Boolean
        get() = zeraJornada || this is Atestado || this is Falta.Justificada

    val impactoResumido: String
        get() = when {
            zeraJornada -> "✅ Abonado"
            descontaDoBanco -> "❌ Débito"
            else -> "⏱️ Normal"
        }

    object Folga : TipoAusencia(
        descricao = "Folga",
        emoji = "😴",
        classificacaoDia = ClassificacaoDia.FOLGA,
        isPlanejada = true,
        explicacaoImpacto = "Dia sem jornada prevista. Se houver trabalho, pode gerar hora extra.",
        exemploUso = "Sábado, domingo ou folga semanal sem expediente.",
        labelObservacao = "Observação",
        placeholderObservacao = "Ex: Folga semanal"
    ) {
        override val regraCalculo = RegraJornadaZerada
    }

    object Ferias : TipoAusencia(
        descricao = "Férias",
        emoji = "🏖️",
        classificacaoDia = ClassificacaoDia.DESCANSO,
        isPlanejada = true,
        explicacaoImpacto = "Jornada zerada durante o período de férias.",
        exemploUso = "Férias anuais, férias coletivas ou recesso remunerado.",
        labelObservacao = "Observação",
        placeholderObservacao = "Ex: Período aquisitivo 2024/2025"
    ) {
        override val regraCalculo = RegraJornadaZerada
        override val bloqueiaRegistroPonto = true
    }

    sealed class Feriado(
        override val descricao: String,
        override val emoji: String,
        override val exemploUso: String
    ) : TipoAusencia(
        descricao = descricao,
        emoji = emoji,
        classificacaoDia = ClassificacaoDia.DESCANSO,
        isPlanejada = true,
        explicacaoImpacto = "Jornada zerada. Se houver trabalho, pode gerar hora extra.",
        exemploUso = exemploUso,
        labelObservacao = "Observação",
        placeholderObservacao = "Ex: Nome do feriado ou observação"
    ) {
        override val regraCalculo = RegraJornadaZerada

        object Oficial : Feriado(
            descricao = "Feriado",
            emoji = "🎉",
            exemploUso = "Feriado nacional, estadual ou municipal."
        )

        object DiaPonte : Feriado(
            descricao = "Dia ponte",
            emoji = "🌉",
            exemploUso = "Emenda entre feriado e fim de semana."
        )

        object Facultativo : Feriado(
            descricao = "Facultativo",
            emoji = "📌",
            exemploUso = "Ponto facultativo definido pela empresa ou calendário."
        )
    }

    object Atestado : TipoAusencia(
        descricao = "Atestado",
        emoji = "🏥",
        classificacaoDia = ClassificacaoDia.AUSENCIA,
        requerDocumento = true,
        permiteAnexo = true,
        isPlanejada = false,
        explicacaoImpacto = "Usado para repouso médico de dia inteiro. Pode abonar a jornada.",
        exemploUso = "Doença, emergência médica ou afastamento médico.",
        labelObservacao = "Motivo do atestado",
        placeholderObservacao = "Ex: Repouso médico"
    ) {
        override val regraCalculo = RegraAtestado
        override val abonoCondicional = true
    }

    object Declaracao : TipoAusencia(
        descricao = "Declaração",
        emoji = "📄",
        classificacaoDia = ClassificacaoDia.AUSENCIA,
        requerDocumento = true,
        permiteAnexo = true,
        isPlanejada = false,
        explicacaoImpacto = "Abona apenas minutos ou horas da jornada, nunca acima da jornada total.",
        exemploUso = "Consulta médica, exame, audiência, cartório ou reunião escolar.",
        labelObservacao = "Motivo da declaração",
        placeholderObservacao = "Ex: Consulta médica das 14h às 16h"
    ) {
        override val regraCalculo = RegraDiaNormal
        override val usaIntervaloHoras = true
    }

    object DayOff : TipoAusencia(
        descricao = "Day off",
        emoji = "🏝️",
        classificacaoDia = ClassificacaoDia.AUSENCIA,
        requerDocumento = false,
        permiteAnexo = true,
        isPlanejada = true,
        explicacaoImpacto = "Dia abonado concedido pela empresa. Jornada zerada, ponto bloqueado e banco não altera.",
        exemploUso = "Day off de aniversário.",
        labelObservacao = "Motivo do day off",
        placeholderObservacao = "Ex: Day off de aniversário"
    ) {
        override val regraCalculo = RegraJornadaZerada
        override val bloqueiaRegistroPonto = true
        override val limiteAnual = 1
        override val exigeRegraAniversario = true
    }

    object CompensacaoBanco : TipoAusencia(
        descricao = "Diminuir banco de horas",
        emoji = "⏳",
        classificacaoDia = ClassificacaoDia.AUSENCIA,
        requerDocumento = false,
        permiteAnexo = true,
        isPlanejada = true,
        explicacaoImpacto = "Compensação planejada que usa saldo positivo do banco de horas e abate a jornada do dia.",
        exemploUso = "Compensação usando saldo positivo do banco.",
        labelObservacao = "Observação",
        placeholderObservacao = "Ex: Compensação de banco"
    ) {
        override val regraCalculo = RegraDiaNormal
        override val descontaDoBanco = true
        override val bloqueiaRegistroPonto = true
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
        classificacaoDia = ClassificacaoDia.AUSENCIA,
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
            requerDocumento = true,
            permiteAnexo = true,
            isPlanejada = true,
            explicacaoImpacto = "Falta aceita mediante documento obrigatório. Jornada zerada e banco não altera.",
            exemploUso = "Casamento, doação de sangue, nascimento de filho ou justificativa aceita.",
            labelObservacao = "Motivo da falta",
            placeholderObservacao = "Ex: Casamento, doação de sangue"
        ) {
            override val regraCalculo = RegraJornadaZerada
            override val bloqueiaRegistroPonto = true
        }

        object Injustificada : Falta(
            descricao = "Falta injustificada",
            emoji = "❌",
            requerDocumento = false,
            permiteAnexo = true,
            isPlanejada = false,
            explicacaoImpacto = "Falta sem justificativa. Mantém a jornada normal e gera débito integral no banco de horas.",
            exemploUso = "Falta sem aviso ou justificativa recusada.",
            labelObservacao = "Motivo",
            placeholderObservacao = "Informe o motivo, se desejar"
        ) {
            override val regraCalculo = RegraFaltaInjustificada
            override val descontaDoBanco = true
            override val bloqueiaRegistroPonto = true
        }
    }

    companion object {
        val todos: List<TipoAusencia> by lazy {
            listOf(
                Folga,
                Ferias,
                Feriado.Oficial,
                Feriado.DiaPonte,
                Feriado.Facultativo,
                Atestado,
                Declaracao,
                DayOff,
                CompensacaoBanco,
                Falta.Justificada,
                Falta.Injustificada
            )
        }

        val descansos: List<TipoAusencia> by lazy {
            listOf(
                Ferias,
                Feriado.Oficial,
                Feriado.DiaPonte,
                Feriado.Facultativo
            )
        }

        val ausencias: List<TipoAusencia> by lazy {
            listOf(
                Atestado,
                Declaracao,
                DayOff,
                CompensacaoBanco,
                Falta.Justificada,
                Falta.Injustificada
            )
        }

        fun valueOf(value: String): TipoAusencia {
            return todos.find { it::class.simpleName == value }
                ?: throw IllegalArgumentException("No TipoAusencia with name $value")
        }
    }
}