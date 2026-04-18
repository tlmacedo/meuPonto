// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/data/service/LocationService.kt
package br.com.tlmacedo.meuponto.data.service

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import androidx.core.content.ContextCompat
import br.com.tlmacedo.meuponto.data.local.database.dao.GeocodificacaoCacheDao
import br.com.tlmacedo.meuponto.data.local.database.entity.GeocodificacaoCacheEntity
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

/**
 * Serviço para obtenção de localização do dispositivo.
 *
 * @author Thiago
 * @since 3.5.0
 */
@Singleton
class LocationService @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val geocodificacaoCacheDao: GeocodificacaoCacheDao
) {
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val geocoder: Geocoder by lazy {
        Geocoder(context, Locale.forLanguageTag("pt-BR"))
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
                    .addOnFailureListener {
                        continuation.resume(null)
                    }
            } catch (_: SecurityException) {
                continuation.resume(null)
            } catch (_: Exception) {
                continuation.resume(null)
            }
        }
    }

    /**
     * Obtém o endereço a partir das coordenadas (geocodificação reversa).
     * Utiliza cache local para evitar chamadas excessivas ao Geocoder.
     */
    @Suppress("DEPRECATION")
    suspend fun getAddressFromLocation(latitude: Double, longitude: Double): String? {
        // Arredonda para 4 casas decimais (~11 metros de precisão) para aumentar eficiência do cache
        val latRound = String.format(Locale.US, "%.4f", latitude).toDouble()
        val lngRound = String.format(Locale.US, "%.4f", longitude).toDouble()

        // 1. Tenta buscar no cache
        val cache = geocodificacaoCacheDao.buscarPorCoordenadas(latRound, lngRound)
        if (cache != null) return cache.endereco

        // 2. Se não estiver no cache, busca no Geocoder
        val endereco = try {
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
        } catch (_: Exception) {
            null
        }

        // 3. Se obteve o endereço, salva no cache
        if (endereco != null) {
            geocodificacaoCacheDao.inserir(
                GeocodificacaoCacheEntity(
                    latitude = latRound,
                    longitude = lngRound,
                    endereco = endereco
                )
            )
        }

        return endereco
    }
}
