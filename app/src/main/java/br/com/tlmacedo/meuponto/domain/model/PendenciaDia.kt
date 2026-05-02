// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/model/PendenciaDia.kt
package br.com.tlmacedo.meuponto.domain.model

import java.time.LocalDate

enum class StatusPendencia(val label: String, val ordem: Int) {
    BLOQUEANTE("Bloqueante", 0),
    PENDENTE("Pendente", 1),
    INFORMATIVO("Informativo", 2),
}

data class PendenciaDia(
    val data: LocalDate,
    val inconsistencias: List<InconsistenciaDetectada>,
    val quantidadePontos: Int,
    val temJustificativa: Boolean
) {
    val status: StatusPendencia
        get() = when {
            inconsistencias.any { it.isBloqueante } -> StatusPendencia.BLOQUEANTE
            !temJustificativa && inconsistencias.any {
                it.inconsistencia == Inconsistencia.FALTA_SEM_JUSTIFICATIVA ||
                        it.inconsistencia == Inconsistencia.REGISTRO_EDITADO
            } -> StatusPendencia.PENDENTE
            else -> StatusPendencia.INFORMATIVO
        }

    val totalBloqueantes: Int
        get() = inconsistencias.count { it.isBloqueante }

    val totalInformativos: Int
        get() = inconsistencias.count { !it.isBloqueante }
}
