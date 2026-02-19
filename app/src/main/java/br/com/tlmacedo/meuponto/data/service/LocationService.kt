// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/data/service/LocationService.kt
package br.com.tlmacedo.meuponto.data.service

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import androidx.core.content.ContextCompat
import br.com.tlmacedo.meuponto.domain.model.FonteLocalizacao
import br.com.tlmacedo.meuponto.domain.model.Localizacao
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Serviço para obtenção de localização do dispositivo.
 *
 * @author Thiago
 * @since 3.5.0
 */
@Singleton
class LocationService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val geocoder: Geocoder by lazy {
        Geocoder(context, Locale("pt", "BR"))
    }

    /**
     * Verifica se as permissões de localização estão concedidas.
     */
    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Obtém a última localização conhecida.
     */
    suspend fun getLastLocation(): Localizacao? {
        if (!hasLocationPermission()) return null

        return suspendCancellableCoroutine { continuation ->
            try {
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location ->
                        if (location != null) {
                            val localizacao = Localizacao(
                                latitude = location.latitude,
                                longitude = location.longitude,
                                precisao = location.accuracy,
                                fonte = FonteLocalizacao.GPS
                            )
                            continuation.resume(localizacao)
                        } else {
                            continuation.resume(null)
                        }
                    }
                    .addOnFailureListener { e ->
                        continuation.resumeWithException(e)
                    }
            } catch (e: SecurityException) {
                continuation.resumeWithException(e)
            }
        }
    }

    /**
     * Obtém a localização atual com alta precisão.
     */
    suspend fun getCurrentLocation(): Localizacao? {
        if (!hasLocationPermission()) return null

        return suspendCancellableCoroutine { continuation ->
            val locationRequest = CurrentLocationRequest.Builder()
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .setMaxUpdateAgeMillis(10000)
                .build()

            try {
                fusedLocationClient.getCurrentLocation(locationRequest, null)
                    .addOnSuccessListener { location ->
                        if (location != null) {
                            val localizacao = Localizacao(
                                latitude = location.latitude,
                                longitude = location.longitude,
                                precisao = location.accuracy,
                                fonte = if (location.accuracy <= 20f) FonteLocalizacao.GPS else FonteLocalizacao.REDE
                            )
                            continuation.resume(localizacao)
                        } else {
                            continuation.resume(null)
                        }
                    }
                    .addOnFailureListener { e ->
                        continuation.resumeWithException(e)
                    }
            } catch (e: SecurityException) {
                continuation.resumeWithException(e)
            }
        }
    }

    /**
     * Obtém o endereço a partir das coordenadas (geocodificação reversa).
     */
    @Suppress("DEPRECATION")
    suspend fun getAddressFromLocation(latitude: Double, longitude: Double): String? {
        return try {
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                buildString {
                    address.thoroughfare?.let { append(it) }
                    address.subThoroughfare?.let { append(", $it") }
                    address.subLocality?.let {
                        if (isNotEmpty()) append(" - ")
                        append(it)
                    }
                    address.locality?.let {
                        if (isNotEmpty()) append(", ")
                        append(it)
                    }
                    address.adminArea?.let {
                        if (address.locality != null) append(" - ")
                        else if (isNotEmpty()) append(", ")
                        append(it)
                    }
                }
            } else null
        } catch (e: Exception) {
            null
        }
    }
}
