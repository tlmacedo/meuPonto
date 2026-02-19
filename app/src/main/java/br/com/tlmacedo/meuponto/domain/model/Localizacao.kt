// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/model/Localizacao.kt
package br.com.tlmacedo.meuponto.domain.model

/**
 * Modelo que representa uma localização geográfica.
 *
 * @author Thiago
 * @since 3.5.0
 */
data class Localizacao(
    val latitude: Double,
    val longitude: Double,
    val endereco: String? = null,
    val precisao: Float? = null,
    val fonte: FonteLocalizacao = FonteLocalizacao.MANUAL
) {
    val coordenadasFormatadas: String
        get() = "%.6f, %.6f".format(latitude, longitude)

    val enderecoOuCoordenadas: String
        get() = endereco?.takeIf { it.isNotBlank() } ?: coordenadasFormatadas

    val isValida: Boolean
        get() = latitude in -90.0..90.0 && longitude in -180.0..180.0
}

/**
 * Fonte da localização capturada.
 */
enum class FonteLocalizacao {
    GPS,
    REDE,
    MANUAL,
    DIGITADA
}
