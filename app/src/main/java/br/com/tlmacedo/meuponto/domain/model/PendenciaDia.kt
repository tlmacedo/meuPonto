// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/model/PendenciaDia.kt
package br.com.tlmacedo.meuponto.domain.model

import java.time.LocalDate

data class PendenciaDia(
    val data: LocalDate,
    val inconsistencias: List<InconsistenciaDetectada>,
    val quantidadePontos: Int,
    val temJustificativa: Boolean = false,
    val isHoje: Boolean = false
) {
    val status: StatusDiaPonto
        get() = when {
            inconsistencias.any { it.isBloqueante } -> StatusDiaPonto.BLOQUEADO
            inconsistencias.any { it.isPendente } -> StatusDiaPonto.PENDENTE_JUSTIFICATIVA
            isHoje && quantidadePontos % 2 != 0 -> StatusDiaPonto.EM_ANDAMENTO
            inconsistencias.any { it.isInfo } -> StatusDiaPonto.INFO
            else -> StatusDiaPonto.NORMAL
        }

    val totalBloqueantes: Int
        get() = inconsistencias.count { it.isBloqueante }

    val totalPendentes: Int
        get() = inconsistencias.count { it.isPendente }

    val totalInformativos: Int
        get() = inconsistencias.count { it.isInfo }

    val temPendencias: Boolean get() = inconsistencias.isNotEmpty()
}
