package br.com.tlmacedo.meuponto.domain.model.sync

import java.time.LocalDateTime

data class SyncMetadata(
    val remotoId: String? = null,
    val sincronizado: Boolean = false,
    val pendenteUpload: Boolean = false,
    val pendenteDelete: Boolean = false,
    val ultimaSincronizacaoEm: LocalDateTime? = null,
    val versaoLocal: Long = 1,
    val versaoRemota: Long? = null,
    val conflito: Boolean = false
) {
    val precisaSincronizar: Boolean
        get() = pendenteUpload || pendenteDelete || !sincronizado || conflito

    fun marcarPendenteUpload(): SyncMetadata =
        copy(
            sincronizado = false,
            pendenteUpload = true,
            versaoLocal = versaoLocal + 1
        )

    fun marcarSincronizado(remotoId: String?): SyncMetadata =
        copy(
            remotoId = remotoId ?: this.remotoId,
            sincronizado = true,
            pendenteUpload = false,
            pendenteDelete = false,
            ultimaSincronizacaoEm = LocalDateTime.now(),
            conflito = false
        )

    fun marcarConflito(): SyncMetadata =
        copy(
            conflito = true,
            sincronizado = false
        )
}