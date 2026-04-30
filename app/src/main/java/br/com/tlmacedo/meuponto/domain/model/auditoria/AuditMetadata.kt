package br.com.tlmacedo.meuponto.domain.model.auditoria

import java.time.LocalDateTime

data class AuditMetadata(
    val criadoEm: LocalDateTime = LocalDateTime.now(),
    val atualizadoEm: LocalDateTime = LocalDateTime.now(),
    val criadoPor: String? = null,
    val atualizadoPor: String? = null,
    val origemRegistro: OrigemRegistro = OrigemRegistro.MANUAL,
    val versaoApp: String? = null,
    val deviceIdHash: String? = null
) {
    fun atualizadoPor(usuarioId: String?): AuditMetadata =
        copy(
            atualizadoEm = LocalDateTime.now(),
            atualizadoPor = usuarioId
        )
}