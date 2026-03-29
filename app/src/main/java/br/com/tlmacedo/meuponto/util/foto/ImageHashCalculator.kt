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

@Singleton
class ImageHashCalculator @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        private const val HASH_ALGORITHM = "MD5"
        private const val BUFFER_SIZE = 8192
    }

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

    fun calculateMd5(data: ByteArray): String? {
        return try {
            val digest = MessageDigest.getInstance(HASH_ALGORITHM)
            bytesToHex(digest.digest(data))
        } catch (e: Exception) {
            Timber.e(e, "Falha ao calcular MD5 do ByteArray")
            null
        }
    }

    fun verifyHash(file: File, expectedHash: String): Boolean {
        val calculatedHash = calculateMd5(file) ?: return false
        return calculatedHash.equals(expectedHash, ignoreCase = true)
    }

    fun verifyHash(uri: Uri, expectedHash: String): Boolean {
        val calculatedHash = calculateMd5(uri) ?: return false
        return calculatedHash.equals(expectedHash, ignoreCase = true)
    }

    fun areFilesIdentical(file1: File, file2: File): Boolean {
        val hash1 = calculateMd5(file1) ?: return false
        val hash2 = calculateMd5(file2) ?: return false
        return hash1.equals(hash2, ignoreCase = true)
    }

    fun isValidMd5Format(hash: String): Boolean {
        return hash.length == 32 &&
                hash.all { it in '0'..'9' || it in 'a'..'f' || it in 'A'..'F' }
    }

    private fun bytesToHex(bytes: ByteArray): String {
        return bytes.joinToString("") { byte -> "%02x".format(byte) }
    }
}

data class IntegrityCheckResult(
    val isValid: Boolean,
    val expectedHash: String,
    val actualHash: String?,
    val errorMessage: String? = null
) {
    val hasError: Boolean get() = errorMessage != null
}
