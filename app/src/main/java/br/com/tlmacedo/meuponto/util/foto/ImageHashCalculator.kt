// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/util/foto/ImageHashCalculator.kt
package br.com.tlmacedo.meuponto.util.foto

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Calculador de hash MD5 para verificação de integridade de imagens.
 *
 * O hash MD5 é usado para:
 * - Verificar integridade do arquivo após transferências ou backups
 * - Detectar modificações não autorizadas no comprovante
 * - Identificar imagens duplicadas
 * - Rastreabilidade e auditoria
 *
 * O hash é calculado no momento do salvamento e armazenado em
 * [br.com.tlmacedo.meuponto.data.local.database.entity.FotoComprovanteEntity.hashMd5].
 *
 * @param context Contexto da aplicação para acesso ao ContentResolver
 *
 * @author Thiago
 * @since 10.0.0
 * @updated 12.0.0 - e.printStackTrace() substituído por Timber.e();
 *                   adicionado KDoc completo
 */
@Singleton
class ImageHashCalculator @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        /** Algoritmo de hash utilizado — MD5 é suficiente para integridade de arquivo */
        private const val HASH_ALGORITHM = "MD5"

        /** Tamanho do buffer para leitura em chunks (8 KB) */
        private const val BUFFER_SIZE = 8192
    }

    /**
     * Calcula o hash MD5 de um arquivo.
     *
     * @param file Arquivo para calcular o hash
     * @return Hash MD5 em hexadecimal minúsculo (32 caracteres) ou null em caso de erro
     */
    fun calculateMd5(file: File): String? {
        return try {
            FileInputStream(file).use { inputStream ->
                calculateMd5(inputStream)
            }
        } catch (e: Exception) {
            Timber.e(e, "Falha ao calcular MD5 do arquivo: ${file.name}")
            null
        }
    }

    /**
     * Calcula o hash MD5 de um URI.
     *
     * @param uri URI do arquivo (content:// ou file://)
     * @return Hash MD5 em hexadecimal minúsculo (32 caracteres) ou null em caso de erro
     */
    fun calculateMd5(uri: Uri): String? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                calculateMd5(inputStream)
            }
        } catch (e: Exception) {
            Timber.e(e, "Falha ao calcular MD5 do URI: $uri")
            null
        }
    }

    /**
     * Calcula o hash MD5 de um [InputStream].
     *
     * O stream é lido em chunks de [BUFFER_SIZE] bytes para eficiência
     * de memória com arquivos grandes.
     *
     * @param inputStream Stream de dados (não fechado por esta função)
     * @return Hash MD5 em hexadecimal minúsculo (32 caracteres) ou null em caso de erro
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
            Timber.e(e, "Falha ao calcular MD5 do InputStream")
            null
        }
    }

    /**
     * Calcula o hash MD5 de um [ByteArray].
     *
     * @param data Dados para calcular o hash
     * @return Hash MD5 em hexadecimal minúsculo (32 caracteres) ou null em caso de erro
     */
    fun calculateMd5(data: ByteArray): String? {
        return try {
            val digest = MessageDigest.getInstance(HASH_ALGORITHM)
            bytesToHex(digest.digest(data))
        } catch (e: Exception) {
            Timber.e(e, "Falha ao calcular MD5 do ByteArray")
            null
        }
    }

    /**
     * Verifica se o hash MD5 de um arquivo corresponde ao esperado.
     *
     * @param file Arquivo a verificar
     * @param expectedHash Hash MD5 esperado (case-insensitive)
     * @return true se os hashes são iguais
     */
    fun verifyHash(file: File, expectedHash: String): Boolean {
        val calculatedHash = calculateMd5(file) ?: return false
        return calculatedHash.equals(expectedHash, ignoreCase = true)
    }

    /**
     * Verifica se o hash MD5 de um URI corresponde ao esperado.
     *
     * @param uri URI do arquivo a verificar
     * @param expectedHash Hash MD5 esperado (case-insensitive)
     * @return true se os hashes são iguais
     */
    fun verifyHash(uri: Uri, expectedHash: String): Boolean {
        val calculatedHash = calculateMd5(uri) ?: return false
        return calculatedHash.equals(expectedHash, ignoreCase = true)
    }

    /**
     * Verifica se dois arquivos são idênticos comparando os hashes MD5.
     *
     * @param file1 Primeiro arquivo
     * @param file2 Segundo arquivo
     * @return true se os arquivos têm conteúdo idêntico
     */
    fun areFilesIdentical(file1: File, file2: File): Boolean {
        val hash1 = calculateMd5(file1) ?: return false
        val hash2 = calculateMd5(file2) ?: return false
        return hash1.equals(hash2, ignoreCase = true)
    }

    /**
     * Valida se uma string está no formato de hash MD5 válido.
     *
     * Um MD5 válido tem exatamente 32 caracteres hexadecimais (0-9, a-f, A-F).
     *
     * @param hash String a validar
     * @return true se é um hash MD5 válido
     */
    fun isValidMd5Format(hash: String): Boolean {
        return hash.length == 32 &&
                hash.all { it in '0'..'9' || it in 'a'..'f' || it in 'A'..'F' }
    }

    // ========================================================================
    // HELPERS PRIVADOS
    // ========================================================================

    /**
     * Converte array de bytes para string hexadecimal minúscula.
     *
     * @param bytes Array de bytes do digest
     * @return String hexadecimal com 2 caracteres por byte
     */
    private fun bytesToHex(bytes: ByteArray): String {
        return bytes.joinToString("") { byte -> "%02x".format(byte) }
    }
}

/**
 * Resultado da verificação de integridade de uma imagem.
 *
 * @property isValid true se o hash calculado corresponde ao esperado
 * @property expectedHash Hash MD5 armazenado no banco de dados
 * @property actualHash Hash MD5 calculado no arquivo atual (null se erro)
 * @property errorMessage Mensagem de erro se o cálculo falhou (null se sucesso)
 */
data class IntegrityCheckResult(
    val isValid: Boolean,
    val expectedHash: String,
    val actualHash: String?,
    val errorMessage: String? = null
) {
    /** true se ocorreu erro durante o cálculo do hash */
    val hasError: Boolean get() = errorMessage != null
}