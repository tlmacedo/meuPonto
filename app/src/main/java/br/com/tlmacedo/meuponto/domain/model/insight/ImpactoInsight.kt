package br.com.tlmacedo.meuponto.domain.model.insight

enum class ImpactoInsight(
    val prioridade: Int
) {
    BAIXO(1),
    MEDIO(2),
    ALTO(3),
    CRITICO(4)
}