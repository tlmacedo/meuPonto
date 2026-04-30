package br.com.tlmacedo.meuponto.domain.model.assinatura

sealed interface GateRecurso {
    data object Liberado : GateRecurso

    data class Bloqueado(
        val recurso: RecursoPremium,
        val motivo: String,
        val planoNecessario: PlanoAssinatura = PlanoAssinatura.PREMIUM
    ) : GateRecurso
}

fun AssinaturaUsuario.validarAcesso(recurso: RecursoPremium): GateRecurso {
    return if (podeUsar(recurso)) {
        GateRecurso.Liberado
    } else {
        GateRecurso.Bloqueado(
            recurso = recurso,
            motivo = "Este recurso está disponível no plano Premium."
        )
    }
}