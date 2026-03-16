// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/usecase/ponto/ExcluirPontoPermanenteUseCase.kt
package br.com.tlmacedo.meuponto.domain.usecase.ponto

import br.com.tlmacedo.meuponto.domain.model.AuditAction
import br.com.tlmacedo.meuponto.domain.repository.AuditLogRepository
import br.com.tlmacedo.meuponto.domain.repository.PontoRepository
import br.com.tlmacedo.meuponto.util.foto.ImageTrashManager
import timber.log.Timber
import javax.inject.Inject

/**
 * Use case para exclusão permanente de pontos.
 *
 * Remove definitivamente o ponto do banco de dados e a foto da lixeira.
 *
 * @author Thiago
 * @since 11.0.0
 */
class ExcluirPontoPermanenteUseCase @Inject constructor(
    private val pontoRepository: PontoRepository,
    private val auditLogRepository: AuditLogRepository,
    private val imageTrashManager: ImageTrashManager
) {
    /**
     * Exclui permanentemente um ponto.
     *
     * @param pontoId ID do ponto a ser excluído permanentemente
     * @return Result indicando sucesso ou falha
     */
    suspend operator fun invoke(pontoId: Long): Result<Unit> {
        return try {
            Timber.d("Excluindo permanentemente ponto ID: $pontoId")

            val ponto = pontoRepository.buscarPorIdIncluindoDeletados(pontoId)
                ?: return Result.failure(IllegalArgumentException("Ponto não encontrado: $pontoId"))

            // Remove a foto permanentemente da lixeira
            ponto.fotoPath?.let { fotoPath ->
                val removida = imageTrashManager.deletePermanently(fotoPath)
                if (removida) {
                    Timber.d("Foto removida permanentemente: $fotoPath")
                } else {
                    Timber.w("Foto não encontrada na lixeira: $fotoPath")
                }
            }

            // Remove o ponto do banco de dados
            pontoRepository.excluirPermanente(pontoId)

            // Registra na auditoria
            auditLogRepository.registrar(
                action = AuditAction.PERMANENT_DELETE,
                entityType = "Ponto",
                entityId = pontoId,
                description = "Ponto excluído permanentemente",
                oldValue = ponto.toString()
            )

            Timber.i("Ponto excluído permanentemente: $pontoId")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Erro ao excluir permanentemente ponto: $pontoId")
            Result.failure(e)
        }
    }

    /**
     * Exclui permanentemente múltiplos pontos.
     *
     * @param pontoIds Lista de IDs dos pontos a excluir
     * @return Result com quantidade de pontos excluídos
     */
    suspend fun excluirMultiplos(pontoIds: List<Long>): Result<Int> {
        return try {
            var excluidos = 0

            pontoIds.forEach { pontoId ->
                invoke(pontoId).onSuccess { excluidos++ }
            }

            Timber.i("$excluidos pontos excluídos permanentemente de ${pontoIds.size}")
            Result.success(excluidos)
        } catch (e: Exception) {
            Timber.e(e, "Erro ao excluir múltiplos pontos permanentemente")
            Result.failure(e)
        }
    }

    /**
     * Esvazia completamente a lixeira.
     *
     * @return Result com quantidade de pontos removidos
     */
    suspend fun esvaziarLixeira(): Result<Int> {
        return try {
            val pontosNaLixeira = pontoRepository.listarDeletados()
            var excluidos = 0

            pontosNaLixeira.forEach { ponto ->
                invoke(ponto.id).onSuccess { excluidos++ }
            }

            // Limpa também arquivos órfãos na lixeira
            imageTrashManager.cleanupExpiredFiles()

            Timber.i("Lixeira esvaziada: $excluidos pontos removidos")
            Result.success(excluidos)
        } catch (e: Exception) {
            Timber.e(e, "Erro ao esvaziar lixeira")
            Result.failure(e)
        }
    }
}
