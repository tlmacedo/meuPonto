// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/usecase/lixeira/MoverParaLixeiraUseCase.kt
package br.com.tlmacedo.meuponto.domain.usecase.lixeira

import br.com.tlmacedo.meuponto.domain.repository.LixeiraRepository
import javax.inject.Inject

/**
 * UseCase para mover pontos para a lixeira (soft delete).
 *
 * @author Thiago
 * @since 11.0.0
 */
class MoverParaLixeiraUseCase @Inject constructor(
    private val lixeiraRepository: LixeiraRepository
) {
    /**
     * Move um único ponto para a lixeira.
     */
    suspend operator fun invoke(pontoId: Long): Result<Unit> {
        return lixeiraRepository.moverParaLixeira(pontoId)
    }

    /**
     * Move múltiplos pontos para a lixeira.
     */
    suspend fun batch(pontoIds: List<Long>): Result<Int> {
        if (pontoIds.isEmpty()) return Result.success(0)
        return lixeiraRepository.moverParaLixeira(pontoIds)
    }
}
