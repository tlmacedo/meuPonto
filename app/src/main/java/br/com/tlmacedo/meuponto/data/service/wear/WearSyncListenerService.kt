package br.com.tlmacedo.meuponto.data.service.wear

import br.com.tlmacedo.meuponto.domain.usecase.ponto.RegistrarPontoUseCase
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class WearSyncListenerService : WearableListenerService() {

    @Inject
    lateinit var registrarPontoUseCase: RegistrarPontoUseCase

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onMessageReceived(messageEvent: MessageEvent) {
        super.onMessageReceived(messageEvent)
        
        if (messageEvent.path == WearSyncConstants.PATH_PONTO_COMMAND) {
            val command = String(messageEvent.data)
            if (command == WearSyncConstants.KEY_COMMAND_REGISTER) {
                Timber.d("Comando de registro de ponto recebido do Wear OS")
                scope.launch {
                    val result = registrarPontoUseCase.registrarAgora()
                    Timber.d("Resultado do registro via Wear OS: $result")
                    // Opcional: Notificar o relógio do sucesso/erro via mensagem ou DataLayer
                }
            }
        }
    }
}
