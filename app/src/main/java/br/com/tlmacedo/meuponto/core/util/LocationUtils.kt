// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/core/util/LocationUtils.kt
package br.com.tlmacedo.meuponto.core.util

import br.com.tlmacedo.meuponto.core.util.LocationUtils.toRad
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Utilitários para manipulação de coordenadas geográficas.
 *
 * Todas as operações trigonométricas usam exclusivamente [kotlin.math],
 * sem mistura com APIs Java ([Math.toRadians] foi substituído por
 * extensão privada [Double.toRad]).
 *
 * @author Thiago
 * @since 3.5.0
 * @updated 12.0.0 - Substituído Math.toRadians() por extensão Kotlin pura;
 *                   adicionado KDoc completo em todas as funções públicas
 */
object LocationUtils {

    /** Raio médio da Terra em metros (WGS-84) */
    private const val RAIO_TERRA_METROS = 6_371_000.0

    /**
     * Calcula a distância entre duas coordenadas geográficas usando
     * a fórmula de Haversine.
     *
     * A fórmula de Haversine é adequada para distâncias curtas a médias
     * (até algumas centenas de km), com erro inferior a 0.5% para a
     * maioria dos casos de uso deste app.
     *
     * @param lat1 Latitude do ponto de origem em graus decimais
     * @param lon1 Longitude do ponto de origem em graus decimais
     * @param lat2 Latitude do ponto de destino em graus decimais
     * @param lon2 Longitude do ponto de destino em graus decimais
     * @return Distância em metros entre os dois pontos
     */
    fun calcularDistancia(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {
        // ✅ Correção aplicada: Uso de extensão .toRad() para Kotlin puro
        val dLat = (lat2 - lat1).toRad()
        val dLon = (lon2 - lon1).toRad()

        val a = sin(dLat / 2).pow(2) +
                cos(lat1.toRad()) *
                cos(lat2.toRad()) *
                sin(dLon / 2).pow(2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return RAIO_TERRA_METROS * c
    }

    /**
     * Formata coordenadas decimais para exibição simples.
     *
     * Exemplo: `formatarCoordenadas(-3.1190, -60.0217)` → "-3.119000, -60.021700"
     *
     * @param latitude Latitude em graus decimais
     * @param longitude Longitude em graus decimais
     * @param casasDecimais Número de casas decimais (padrão: 6)
     * @return String formatada no padrão "lat, lon"
     */
    fun formatarCoordenadas(
        latitude: Double,
        longitude: Double,
        casasDecimais: Int = 6
    ): String {
        return "%.${casasDecimais}f, %.${casasDecimais}f".format(latitude, longitude)
    }

    /**
     * Formata coordenadas em Graus, Minutos e Segundos (DMS).
     *
     * Exemplo: `formatarCoordenadasDMS(-3.119, -60.022)` →
     * "3°7'8.40\"S 60°1'19.20\"W"
     *
     * @param latitude Latitude em graus decimais
     * @param longitude Longitude em graus decimais
     * @return String formatada no padrão DMS internacional
     */
    fun formatarCoordenadasDMS(
        latitude: Double,
        longitude: Double
    ): String {
        return "${toDMS(latitude, isLat = true)} ${toDMS(longitude, isLat = false)}"
    }

    /**
     * Formata uma distância em metros para exibição amigável.
     *
     * - Abaixo de 1 km: exibe em metros (ex: "250m")
     * - 1 km ou mais: exibe em quilômetros com 1 casa decimal (ex: "1.5km")
     *
     * @param metros Distância em metros
     * @return String formatada com unidade
     */
    fun formatarDistancia(metros: Double): String {
        return when {
            metros < 1_000 -> "${metros.toInt()}m"
            else -> "%.1fkm".format(metros / 1_000)
        }
    }

    /**
     * Verifica se um par de coordenadas está dentro dos limites válidos.
     *
     * Latitude válida: -90.0 a +90.0
     * Longitude válida: -180.0 a +180.0
     *
     * @param latitude Latitude a verificar (pode ser null)
     * @param longitude Longitude a verificar (pode ser null)
     * @return true se ambas as coordenadas são não-nulas e estão dentro dos limites
     */
    fun coordenadasValidas(latitude: Double?, longitude: Double?): Boolean {
        if (latitude == null || longitude == null) return false
        return latitude in -90.0..90.0 && longitude in -180.0..180.0
    }

    // ════════════════════════════════════════════════════════════════════════
    // HELPERS PRIVADOS
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Converte graus decimais para radianos usando Kotlin puro.
     * Substitui [Math.toRadians] da API Java.
     */
    private fun Double.toRad(): Double = this * PI / 180.0

    /**
     * Converte coordenada decimal para formato DMS com direção cardinal.
     *
     * @param coord Coordenada em graus decimais
     * @param isLat true para latitude (N/S), false para longitude (E/W)
     * @return String no formato "G°M'S.ss\"D"
     */
    private fun toDMS(coord: Double, isLat: Boolean): String {
        val direction = when {
            isLat && coord >= 0 -> "N"
            isLat -> "S"
            coord >= 0 -> "E"
            else -> "W"
        }
        val absCoord = abs(coord)
        val degrees = absCoord.toInt()
        val minutes = ((absCoord - degrees) * 60).toInt()
        val seconds = (absCoord - degrees - minutes / 60.0) * 3_600

        return "%d°%d'%.2f\"%s".format(degrees, minutes, seconds, direction)
    }
}