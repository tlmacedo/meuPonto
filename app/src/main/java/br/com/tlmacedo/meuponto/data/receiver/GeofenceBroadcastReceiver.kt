package br.com.tlmacedo.meuponto.data.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import br.com.tlmacedo.meuponto.worker.GeofenceWorker
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import timber.log.Timber

/**
 * Receiver para capturar transições de Geofence (Entrada/Saída do Trabalho).
 */
class GeofenceBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent) ?: return

        if (geofencingEvent.hasError()) {
            Timber.e("Erro no evento de Geofence: ${geofencingEvent.errorCode}")
            return
        }

        val transitionType = geofencingEvent.geofenceTransition
        if (transitionType == Geofence.GEOFENCE_TRANSITION_ENTER ||
            transitionType == Geofence.GEOFENCE_TRANSITION_EXIT
        ) {
            // Dispara um Worker para processar o registro do ponto em background
            val workRequest = OneTimeWorkRequestBuilder<GeofenceWorker>()
                .setInputData(workDataOf(GeofenceWorker.KEY_TRANSITION_TYPE to transitionType))
                .build()

            WorkManager.getInstance(context).enqueue(workRequest)
        }
    }
}
