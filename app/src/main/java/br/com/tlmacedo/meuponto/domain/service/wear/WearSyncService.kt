package br.com.tlmacedo.meuponto.domain.service.wear

interface WearSyncService {
    suspend fun syncPontoStatus(
        saldoAtual: String,
        ultimoPonto: String?,
        emExpediente: Boolean
    )
}
