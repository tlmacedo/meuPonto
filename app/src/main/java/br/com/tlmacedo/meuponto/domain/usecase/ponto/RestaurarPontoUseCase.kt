// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/usecase/ponto/RestaurarPontoUseCase.kt
package br.com.tlmacedo.meuponto.domain.usecase.ponto

import br.com.tlmacedo.meuponto.domain.model.AuditAction
import br.com.tlmacedo.meuponto.domain.model.Ponto
import br.com.tlmacedo.meuponto.domain.repository.AuditLogRepository
import br.com.tlmacedo.meuponto.domain.repository.PontoRepository
import br.com.tlmacedo.meuponto.util.foto.ImageTrashManager
import timber.log.Timber
import javax.inject.Inject

/**
 * Use case para restaurar pontos excluídos (soft delete).
 *
 * Restaura o ponto do estado deletado e recupera a foto da lixeira.
 *
 * @author Thiago
 * @since 11.0.0
 */
class RestaurarPontoUseCase @Inject constructor(
    private val pontoRepository: PontoRepository,
    private val auditLogRepository: AuditLogRepository,
    private val imageTrashManager: ImageTrashManager
) {
    /**
     * Restaura um ponto específico.
     *
     * @param pontoId ID do ponto a ser restaurado
     * @return Result com o ponto restaurado ou erro
     */
    suspend operator fun invoke(pontoId: Long): Result<Ponto> {
        return try {
            Timber.d("Restaurando ponto ID: $pontoId")

            // Busca o ponto (incluindo deletados)
            val ponto = pontoRepository.buscarPorIdIncluindoDeletados(pontoId)
                ?: return Result.failure(IllegalArgumentException("Ponto não encontrado: $pontoId"))

            if (!ponto.isDeleted) {
                return Result.failure(IllegalStateException("Ponto não está na lixeira"))
            }

            // Restaura a foto da lixeira, se existir
            val fotoRestaurada = ponto.fotoPath?.let { fotoPath ->
                val restaurada = imageTrashManager.restoreFromTrash(fotoPath)
                if (restaurada) {
                    Timber.d("Foto restaurada da lixeira: $fotoPath")
                    fotoPath
                } else {
                    Timber.w("Não foi possível restaurar foto da lixeira: $fotoPath")
                    null // Foto perdida, mas restaura o ponto mesmo assim
                }
            }

            // Restaura o ponto
            val pontoRestaurado = ponto.copy(
                isDeleted = false,
                deletedAt = null,
                fotoPath = fotoRestaurada ?: ponto.fotoPath,
                updatedAt = System.currentTimeMillis()
            )

            pontoRepository.atualizar(pontoRestaurado)

            // Registra na auditoria
            auditLogRepository.registrar(
                action = AuditAction.RESTORE,
                entityType = "Ponto",
                entityId = pontoId,
                description = "Ponto restaurado da lixeira",
                newValue = pontoRestaurado.toString()
            )

            Timber.i("Ponto restaurado com sucesso: $pontoId")
            Result.success(pontoRestaurado)
        } catch (e: Exception) {
            Timber.e(e, "Erro ao restaurar ponto: $pontoId")
            Result.failure(e)
        }
    }

    /**
     * Restaura múltiplos pontos.
     *
     * @param pontoIds Lista de IDs dos pontos a restaurar
     * @return Result com quantidade de pontos restaurados
     */
    suspend fun restaurarMultiplos(pontoIds: List<Long>): Result<Int> {
        return try {
            var restaurados = 0

            pontoIds.forEach { pontoId ->
                invoke(pontoId).onSuccess { restaurados++ }
            }

            Timber.i("$restaurados pontos restaurados de ${pontoIds.size}")
            Result.success(restaurados)
        } catch (e: Exception) {
            Timber.e(e, "Erro ao restaurar múltiplos pontos")
            Result.failure(e)
        }
    }
}
