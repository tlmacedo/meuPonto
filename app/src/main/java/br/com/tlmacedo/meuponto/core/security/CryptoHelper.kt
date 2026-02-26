// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/core/security/CryptoHelper.kt
package br.com.tlmacedo.meuponto.core.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import timber.log.Timber
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Helper para criptografia de dados sensíveis usando Android Keystore.
 * 
 * Usa AES-256-GCM para garantir confidencialidade e integridade.
 * A chave é armazenada no Android Keystore (hardware-backed quando disponível).
 *
 * @author Thiago
 * @since 6.1.0
 */
object CryptoHelper {

    private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
    private const val KEY_ALIAS = "MeuPonto_LocationKey"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val GCM_TAG_LENGTH = 128
    private const val GCM_IV_LENGTH = 12

    /**
     * Criptografa uma string sensível.
     * 
     * @param plainText Texto a ser criptografado
     * @return String criptografada em Base64 (IV + ciphertext) ou null em caso de erro
     */
    fun encrypt(plainText: String?): String? {
        if (plainText.isNullOrBlank()) return null

        return try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())

            val iv = cipher.iv
            val cipherText = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))

            // Concatena IV + ciphertext e codifica em Base64
            val combined = iv + cipherText
            Base64.encodeToString(combined, Base64.NO_WRAP)
        } catch (e: Exception) {
            Timber.e(e, "Erro ao criptografar dados")
            null
        }
    }

    /**
     * Descriptografa uma string criptografada.
     * 
     * @param encryptedText Texto criptografado em Base64
     * @return Texto original ou null em caso de erro
     */
    fun decrypt(encryptedText: String?): String? {
        if (encryptedText.isNullOrBlank()) return null

        return try {
            val combined = Base64.decode(encryptedText, Base64.NO_WRAP)

            // Extrai IV (primeiros 12 bytes) e ciphertext (restante)
            val iv = combined.sliceArray(0 until GCM_IV_LENGTH)
            val cipherText = combined.sliceArray(GCM_IV_LENGTH until combined.size)

            val cipher = Cipher.getInstance(TRANSFORMATION)
            val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
            cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), spec)

            String(cipher.doFinal(cipherText), Charsets.UTF_8)
        } catch (e: Exception) {
            Timber.e(e, "Erro ao descriptografar dados")
            null
        }
    }

    /**
     * Criptografa um valor Double (latitude/longitude).
     */
    fun encryptDouble(value: Double?): String? {
        return value?.let { encrypt(it.toString()) }
    }

    /**
     * Descriptografa para Double.
     */
    fun decryptDouble(encryptedValue: String?): Double? {
        return decrypt(encryptedValue)?.toDoubleOrNull()
    }

    /**
     * Obtém ou cria a chave AES no Android Keystore.
     */
    private fun getOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER).apply { load(null) }

        // Retorna chave existente se houver
        keyStore.getKey(KEY_ALIAS, null)?.let {
            return it as SecretKey
        }

        // Cria nova chave
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            KEYSTORE_PROVIDER
        )

        val keySpec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .setUserAuthenticationRequired(false) // Não exige biometria para acessar
            .build()

        keyGenerator.init(keySpec)
        return keyGenerator.generateKey()
    }
}
