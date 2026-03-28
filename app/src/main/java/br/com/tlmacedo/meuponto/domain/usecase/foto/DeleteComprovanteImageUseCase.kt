package br.com.tlmacedo.meuponto.domain.usecase.foto

import br.com.tlmacedo.meuponto.domain.model.Ponto
import br.com.tlmacedo.meuponto.domain.repository.PontoRepository
import br.com.tlmacedo.meuponto.util.foto.FotoStorageManager
import javax.inject.Inject

class DeleteComprovanteImageUseCase @Inject constructor(
    private val storageManager: FotoStorageManager,
    private val pontoRepository: PontoRepository
) {
    suspend operator fun invoke(ponto: Ponto, updatePonto: Boolean = true): Boolean {
        val filePath = ponto.fotoComprovantePath ?: return true
        val deleted = storageManager.deletePhoto(filePath)
        if (deleted && updatePonto) {
            val pontoAtualizado = ponto.comFotoComprovante(null)
            pontoRepository.atualizar(pontoAtualizado)
        }
        return deleted
    }

    suspend fun deleteFileOnly(filePath: String): Boolean {
        return storageManager.deletePhoto(filePath)
    }
}
