// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/model/LocalizacaoSegura.kt
package br.com.tlmacedo.meuponto.domain.model

import br.com.tlmacedo.meuponto.core.security.CryptoHelper

/**
 * Wrapper para dados de localização com criptografia transparente.
 * 
 * Armazena latitude, longitude e endereço de forma criptografada,
 * descriptografando apenas quando necessário para exibição.
 *
 * @author Thiago
 * @since 6.1.0
 */
data class LocalizacaoSegura(
    private val latitudeCriptografada: String?,
    private val longitudeCriptografada: String?,
    private val enderecoCriptografado: String?
) {
    /**
     * Latitude descriptografada (lazy).
     */
    val latitude: Double?
        get() = CryptoHelper.decryptDouble(latitudeCriptografada)

    /**
     * Longitude descriptografada (lazy).
     */
    val longitude: Double?
        get() = CryptoHelper.decryptDouble(longitudeCriptografada)

    /**
     * Endereço descriptografado (lazy).
     */
    val endereco: String?
        get() = CryptoHelper.decrypt(enderecoCriptografado)

    /**
     * Verifica se há localização disponível.
     */
    val disponivel: Boolean
        get() = latitudeCriptografada != null && longitudeCriptografada != null

    companion object {
        /**
         * Cria LocalizacaoSegura a partir de valores em texto plano.
         */
        fun fromPlainText(
            latitude: Double?,
            longitude: Double?,
            endereco: String?
        ): LocalizacaoSegura {
            return LocalizacaoSegura(
                latitudeCriptografada = CryptoHelper.encryptDouble(latitude),
                longitudeCriptografada = CryptoHelper.encryptDouble(longitude),
                enderecoCriptografado = CryptoHelper.encrypt(endereco)
            )
        }

        /**
         * Cria LocalizacaoSegura a partir de valores já criptografados (do banco).
         */
        fun fromEncrypted(
            latitudeCriptografada: String?,
            longitudeCriptografada: String?,
            enderecoCriptografado: String?
        ): LocalizacaoSegura {
            return LocalizacaoSegura(
                latitudeCriptografada = latitudeCriptografada,
                longitudeCriptografada = longitudeCriptografada,
                enderecoCriptografado = enderecoCriptografado
            )
        }

        /**
         * Instância vazia (sem localização).
         */
        val VAZIA = LocalizacaoSegura(null, null, null)
    }
}
