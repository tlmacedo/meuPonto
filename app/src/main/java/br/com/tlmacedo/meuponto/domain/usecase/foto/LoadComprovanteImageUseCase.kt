package br.com.tlmacedo.meuponto.domain.usecase.foto

import android.graphics.Bitmap
import br.com.tlmacedo.meuponto.domain.model.Ponto
import br.com.tlmacedo.meuponto.util.foto.FotoStorageManager
import br.com.tlmacedo.meuponto.util.foto.IntegrityCheckResult
import java.io.File
import javax.inject.Inject

class LoadComprovanteImageUseCase @Inject constructor(
    private val storageManager: FotoStorageManager
) {
    suspend operator fun invoke(ponto: Ponto): Bitmap? {
        val filePath = ponto.fotoComprovantePath ?: return null
        return storageManager.loadPhoto(filePath)
    }

    suspend fun loadThumbnail(ponto: Ponto): Bitmap? {
        val filePath = ponto.fotoComprovantePath ?: return null
        return storageManager.loadThumbnail(filePath)
    }

    fun getFile(ponto: Ponto): File? {
        val filePath = ponto.fotoComprovantePath ?: return null
        val file = storageManager.getAbsoluteFile(filePath)
        return if (file.exists()) file else null
    }

    fun exists(ponto: Ponto): Boolean {
        val filePath = ponto.fotoComprovantePath ?: return false
        return storageManager.exists(filePath)
    }

    suspend fun verifyIntegrity(ponto: Ponto, expectedHash: String): IntegrityCheckResult {
        val filePath = ponto.fotoComprovantePath
            ?: return IntegrityCheckResult(
                isValid = false,
                expectedHash = expectedHash,
                actualHash = null,
                errorMessage = "Arquivo não encontrado"
            )
        return storageManager.verifyIntegrity(filePath, expectedHash)
    }
}
