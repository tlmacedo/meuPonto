package br.com.tlmacedo.meuponto.domain.model

/**
 * Representa o estado consolidado de um dia de registros de ponto.
 *
 * Mapeamento de regras:
 * - Sem problemas -> NORMAL
 * - Dia atual com ponto aberto -> EM_ANDAMENTO
 * - Somente INFO -> INFO
 * - Tem PENDENTE_JUSTIFICATIVA -> PENDENTE_JUSTIFICATIVA
 * - Tem BLOQUEANTE -> BLOQUEADO
 *
 * @author Thiago
 * @since 14.0.0
 */
enum class StatusDiaPonto(val label: String) {
    NORMAL("Normal"),
    EM_ANDAMENTO("Em andamento"),
    INFO("Informativo"),
    COM_PROBLEMAS("Com problemas"), // Reservado para inconsistências genéricas
    PENDENTE_JUSTIFICATIVA("Pendente de justificativa"),
    BLOQUEADO("Bloqueado")
}
