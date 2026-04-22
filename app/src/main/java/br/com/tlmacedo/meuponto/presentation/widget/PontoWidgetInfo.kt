package br.com.tlmacedo.meuponto.presentation.widget

import kotlinx.serialization.Serializable

/**
 * Representa o estado do widget para persistência e exibição.
 */
@Serializable
data class PontoWidgetInfo(
    val apelidoEmprego: String? = null,
    val logoEmprego: String? = null,
    val horasTrabalhadasHoje: String = "00:00",
    val proximoPontoTipo: String = "Entrada",
    val statusTrabalho: String = "Não iniciado",
    val erro: String? = null,
    val ultimaAtualizacao: Long = System.currentTimeMillis()
)
