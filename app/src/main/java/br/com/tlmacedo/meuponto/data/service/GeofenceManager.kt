package br.com.tlmacedo.meuponto.data.service

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import br.com.tlmacedo.meuponto.data.receiver.GeofenceBroadcastReceiver
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeofenceManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val geofencingClient = LocationServices.getGeofencingClient(context)

    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
        PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
    }

    @SuppressLint("MissingPermission")
    fun monitorarTrabalho(latitude: Double, longitude: Double, raioMetros: Float) {
        val geofence = Geofence.Builder()
            .setRequestId(GEOFENCE_ID_TRABALHO)
            .setCircularRegion(latitude, longitude, raioMetros)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
            .build()

        val request = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()

        geofencingClient.addGeofences(request, geofencePendingIntent).run {
            addOnSuccessListener {
                Timber.d("Geofence monitorando local de trabalho: $latitude, $longitude (Raio: ${raioMetros}m)")
            }
            addOnFailureListener { e ->
                Timber.e(e, "Erro ao registrar Geofence")
            }
        }
    }

    fun pararMonitoramento() {
        geofencingClient.removeGeofences(geofencePendingIntent).run {
            addOnSuccessListener {
                Timber.d("Monitoramento de Geofence removido")
            }
            addOnFailureListener { e ->
                Timber.e(e, "Erro ao remover Geofence")
            }
        }
    }

    companion object {
        const val GEOFENCE_ID_TRABALHO = "TRABALHO_PADRAO"
    }
}
