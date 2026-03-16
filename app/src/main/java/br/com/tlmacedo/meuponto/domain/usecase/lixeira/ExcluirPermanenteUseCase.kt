// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/usecase/lixeira/ExcluirPermanenteUseCase.kt
package br.com.tlmacedo.meuponto.domain.usecase.lixeira

import br.com.tlmacedo.meuponto.domain.repository.LixeiraRepository
import javax.inject.Inject

/**
 * UseCase para excluir permanentemente pontos (hard delete).
 *
 * @author Thiago
 * @since 11.0.0
 */
class ExcluirPermanenteUseCase @Inject constructor(
    private val lixeiraRepository: LixeiraRepository
) {
    /**
     * Exclui permanentemente um único ponto.
     */
    suspend operator fun invoke(pontoId: Long): Result<Unit> {
        return lixeiraRepository.excluirPermanente(pontoId)
    }

    /**
     * Exclui permanentemente múltiplos pontos.
     */
    suspend fun batch(pontoIds: List<Long>): Result<Int> {
        if (pontoIds.isEmpty()) return Result.success(0)
        return lixeiraRepository.excluirPermanente(pontoIds)
    }

    /**
     * Esvazia toda a lixeira.
     */
    suspend fun esvaziarLixeira(): Result<Int> {
        return lixeiraRepository.esvaziarLixeira()
    }
}
