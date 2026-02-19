// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/core/util/LocationUtils.kt
package br.com.tlmacedo.meuponto.core.util

import kotlin.math.*

/**
 * Utilitários para manipulação de coordenadas geográficas.
 *
 * @author Thiago
 * @since 3.5.0
 */
object LocationUtils {

    /**
     * Calcula a distância entre duas coordenadas usando a fórmula de Haversine.
     *
     * @return Distância em metros
     */
    fun calcularDistancia(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {
        val raioTerra = 6371000.0 // Raio da Terra em metros

        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) *
                cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return raioTerra * c
    }

    /**
     * Formata coordenadas para exibição.
     */
    fun formatarCoordenadas(
        latitude: Double,
        longitude: Double,
        casasDecimais: Int = 6
    ): String {
        return "%.${casasDecimais}f, %.${casasDecimais}f".format(latitude, longitude)
    }

    /**
     * Formata coordenadas em graus, minutos e segundos.
     */
    fun formatarCoordenadasDMS(
        latitude: Double,
        longitude: Double
    ): String {
        fun toDMS(coord: Double, isLat: Boolean): String {
            val direction = when {
                isLat && coord >= 0 -> "N"
                isLat -> "S"
                coord >= 0 -> "E"
                else -> "W"
            }
            val absCoord = abs(coord)
            val degrees = absCoord.toInt()
            val minutes = ((absCoord - degrees) * 60).toInt()
            val seconds = ((absCoord - degrees - minutes / 60.0) * 3600)
            return "%d°%d'%.2f\"%s".format(degrees, minutes, seconds, direction)
        }

        return "${toDMS(latitude, true)} ${toDMS(longitude, false)}"
    }

    /**
     * Formata distância para exibição amigável.
     */
    fun formatarDistancia(metros: Double): String {
        return when {
            metros < 1000 -> "${metros.toInt()}m"
            else -> "%.1fkm".format(metros / 1000)
        }
    }

    /**
     * Verifica se as coordenadas são válidas.
     */
    fun coordenadasValidas(latitude: Double?, longitude: Double?): Boolean {
        if (latitude == null || longitude == null) return false
        return latitude in -90.0..90.0 && longitude in -180.0..180.0
    }
}
