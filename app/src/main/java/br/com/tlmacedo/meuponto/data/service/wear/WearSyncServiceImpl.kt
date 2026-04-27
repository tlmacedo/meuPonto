package br.com.tlmacedo.meuponto.data.service.wear

import android.content.Context
import br.com.tlmacedo.meuponto.domain.service.wear.WearSyncService
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject

class WearSyncServiceImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : WearSyncService {

    private val dataClient by lazy { Wearable.getDataClient(context) }

    override suspend fun syncPontoStatus(
        saldoAtual: String,
        ultimoPonto: String?,
        emExpediente: Boolean
    ) {
        try {
            val request = PutDataMapRequest.create(WearSyncConstants.PATH_PONTO_STATUS).apply {
                dataMap.putString(WearSyncConstants.KEY_SALDO_ATUAL, saldoAtual)
                dataMap.putString(WearSyncConstants.KEY_ULTIMO_PONTO, ultimoPonto ?: "")
                dataMap.putBoolean(WearSyncConstants.KEY_EM_EXPEDIENTE, emExpediente)
                // Força o trigger do onDataChanged no relógio adicionando um timestamp
                dataMap.putLong("timestamp", System.currentTimeMillis())
            }
            
            dataClient.putDataItem(request.asPutDataRequest()).await()
            Timber.d("Status do ponto sincronizado com o Wear OS: $saldoAtual, exped: $emExpediente")
        } catch (e: Exception) {
            Timber.e(e, "Erro ao sincronizar dados com o Wear OS")
        }
    }
}
