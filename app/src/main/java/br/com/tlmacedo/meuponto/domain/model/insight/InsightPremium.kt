package br.com.tlmacedo.meuponto.domain.model.insight

import java.time.LocalDate

data class InsightPremium(
    val id: String,
    val empregoId: Long,
    val tipo: TipoInsight,
    val titulo: String,
    val descricao: String,
    val impacto: ImpactoInsight,
    val dataReferencia: LocalDate,
    val acaoSugerida: String? = null
)