package br.com.tlmacedo.meuponto.domain.usecase.seguranca

import timber.log.Timber
import java.io.File
import java.security.MessageDigest
import javax.inject.Inject

/**
 * Calcula o hash SHA-256 de um arquivo de comprovante para garantir integridade e evitar duplicidade.
 */
class CalcularHashComprovanteUseCase @Inject constructor() {

    operator fun invoke(filePath: String): String? {
        return try {
            val file = File(filePath)
            if (!file.exists()) return null

            val bytes = file.readBytes()
            val md = MessageDigest.getInstance("SHA-256")
            val digest = md.digest(bytes)
            
            digest.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            Timber.e(e, "Erro ao calcular hash SHA-256 do arquivo: $filePath")
            null
        }
    }
}
