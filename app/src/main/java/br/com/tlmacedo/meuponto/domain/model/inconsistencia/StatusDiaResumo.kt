package br.com.tlmacedo.meuponto.domain.model.inconsistencia

enum class StatusDiaResumo(val descricao: String, val isConsistente: Boolean) {
    COMPLETO("Completo", true),
    INCOMPLETO("Incompleto", false),
    EM_ANDAMENTO("Em andamento", true),
    FUTURO("Futuro", true),
    SEM_REGISTRO("Sem registro", true),
    COM_INFORMACOES("Com informações", true),
    PENDENTE_JUSTIFICATIVA("Pendente justificativa", false),
    BLOQUEADO("Bloqueado", false),
    DESCANSO("Descanso", true),
    DESCANSO_TRABALHADO("Descanso trabalhado", true),
    FOLGA("Folga", true),
    ABONADO("Abonado", true),
    FALTA("Falta", false),
    POSITIVO("Saldo positivo", true),
    NEGATIVO("Saldo negativo", true),
    NEUTRO("Neutro", true)
}