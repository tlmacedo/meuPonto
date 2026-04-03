// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/util/foto/ImageHashCalculator.kt
package br.com.tlmacedo.meuponto.util.foto

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber // Importação adicionada
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Utilitário para cálculo de hash MD5 de imagens.
 *
 * Usado para verificar a integridade de arquivos e detectar duplicatas.
 *
 * ## Correções aplicadas (12.0.0):
 * - [calculateMd5] (File): e.printStackTrace() substituído por Timber.e()
 * - [calculateMd5] (Uri): e.printStackTrace() substituído por Timber.e()
 * - [calculateMd5] (InputStream): e.printStackTrace() substituído por Timber.e()
 * - [calculateMd5] (ByteArray): e.printStackTrace() substituído por Timber.e()
 *
 * @param context Contexto da aplicação para acesso ao ContentResolver
 *
 * @author Thiago
 * @since 10.0.0
 * @updated 12.0.0 - e.printStackTrace() substituído por Timber.e()
 */
@Singleton
class ImageHashCalculator @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        private const val HASH_ALGORITHM = "MD5"
        private const val BUFFER_SIZE = 8192
    }

    /**
     * Calcula o hash MD5 de um arquivo.
     *
     * @param file Arquivo para calcular o hash
     * @return Hash MD5 em formato hexadecimal ou null em caso de erro
     */
    fun calculateMd5(file: File): String? {
        return try {
            FileInputStream(file).use { inputStream ->
                calculateMd5(inputStream)
            }
        } catch (e: Exception) {
            Timber.e(e, "Falha ao calcular MD5 do arquivo: ${file.name}") // Correção aqui
            null
        }
    }

    /**
     * Calcula o hash MD5 de um URI.
     *
     * @param uri URI para calcular o hash
     * @return Hash MD5 em formato hexadecimal ou null em caso de erro
     */
    fun calculateMd5(uri: Uri): String? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                calculateMd5(inputStream)
            }
        } catch (e: Exception) {
            Timber.e(e, "Falha ao calcular MD5 do URI: $uri") // Correção aqui
            null
        }
    }

    /**
     * Calcula o hash MD5 de um [InputStream].
     *
     * @param inputStream InputStream para calcular o hash
     * @return Hash MD5 em formato hexadecimal ou null em caso de erro
     */
    fun calculateMd5(inputStream: InputStream): String? {
        return try {
            val digest = MessageDigest.getInstance(HASH_ALGORITHM)
            val buffer = ByteArray(BUFFER_SIZE)
            var bytesRead: Int

            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }

            bytesToHex(digest.digest())
        } catch (e: Exception) {
            Timber.e(e, "Falha ao calcular MD5 do InputStream") // Correção aqui
            null
        }
    }

    /**
     * Calcula o hash MD5 de um [ByteArray].
     *
     * @param data ByteArray para calcular o hash
     * @return Hash MD5 em formato hexadecimal ou null em caso de erro
     */
    fun calculateMd5(data: ByteArray): String? {
        return try {
            val digest = MessageDigest.getInstance(HASH_ALGORITHM)
            bytesToHex(digest.digest(data))
        } catch (e: Exception) {
            Timber.e(e, "Falha ao calcular MD5 do ByteArray") // Correção aqui
            null
        }
    }

    /**
     * Verifica se o hash MD5 de um arquivo corresponde a um hash esperado.
     *
     * @param file Arquivo para verificar
     * @param expectedHash Hash MD5 esperado
     * @return true se os hashes correspondem, false caso contrário ou em caso de erro
     */
    fun verifyHash(file: File, expectedHash: String): Boolean {
        val calculatedHash = calculateMd5(file) ?: return false
        return calculatedHash.equals(expectedHash, ignoreCase = true)
    }

    /**
     * Verifica se o hash MD5 de um URI corresponde a um hash esperado.
     *
     * @param uri URI para verificar
     * @param expectedHash Hash MD5 esperado
     * @return true se os hashes correspondem, false caso contrário ou em caso de erro
     */
    fun verifyHash(uri: Uri, expectedHash: String): Boolean {
        val calculatedHash = calculateMd5(uri) ?: return false
        return calculatedHash.equals(expectedHash, ignoreCase = true)
    }

    /**
     * Compara se dois arquivos são idênticos com base em seus hashes MD5.
     *
     * @param file1 Primeiro arquivo
     * @param file2 Segundo arquivo
     * @return true se os hashes são idênticos, false caso contrário ou em caso de erro
     */
    fun areFilesIdentical(file1: File, file2: File): Boolean {
        val hash1 = calculateMd5(file1) ?: return false
        val hash2 = calculateMd5(file2) ?: return false
        return hash1.equals(hash2, ignoreCase = true)
    }

    /**
     * Verifica se uma string está em um formato MD5 válido (32 caracteres hexadecimais).
     *
     * @param hash String a ser verificada
     * @return true se a string é um MD5 válido, false caso contrário
     */
    fun isValidMd5Format(hash: String): Boolean {
        return hash.length == 32 &&
                hash.all { it in '0'..'9' || it in 'a'..'f' || it in 'A'..'F' }
    }

    /**
     * Converte um array de bytes em uma string hexadecimal.
     *
     * @param bytes Array de bytes
     * @return String hexadecimal
     */
    private fun bytesToHex(bytes: ByteArray): String {
        return bytes.joinToString("") { byte -> "%02x".format(byte) }
    }
}

/**
 * Resultado da verificação de integridade de uma imagem.
 *
 * @property isValid true se o hash calculado corresponde ao esperado
 * @property expectedHash Hash MD5 que era esperado
 * @property actualHash Hash MD5 que foi calculado (pode ser null se houve erro)
 * @property errorMessage Mensagem de erro, se houver
 */
data class IntegrityCheckResult(
    val isValid: Boolean,
    val expectedHash: String,
    val actualHash: String?,
    val errorMessage: String? = null
) {
    /** true se houve algum erro durante a verificação */
    val hasError: Boolean get() = errorMessage != null
}