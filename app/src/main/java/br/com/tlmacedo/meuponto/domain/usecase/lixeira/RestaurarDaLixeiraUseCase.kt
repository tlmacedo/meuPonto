// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/usecase/lixeira/RestaurarDaLixeiraUseCase.kt
package br.com.tlmacedo.meuponto.domain.usecase.lixeira

import br.com.tlmacedo.meuponto.domain.repository.LixeiraRepository
import javax.inject.Inject

/**
 * UseCase para restaurar pontos da lixeira.
 *
 * @author Thiago
 * @since 11.0.0
 */
class RestaurarDaLixeiraUseCase @Inject constructor(
    private val lixeiraRepository: LixeiraRepository
) {
    /**
     * Restaura um único ponto da lixeira.
     */
    suspend operator fun invoke(pontoId: Long): Result<Unit> {
        return lixeiraRepository.restaurar(pontoId)
    }

    /**
     * Restaura múltiplos pontos da lixeira.
     */
    suspend fun batch(pontoIds: List<Long>): Result<Int> {
        if (pontoIds.isEmpty()) return Result.success(0)
        return lixeiraRepository.restaurar(pontoIds)
    }

    /**
     * Restaura todos os pontos da lixeira.
     */
    suspend fun restaurarTodos(): Result<Int> {
        val pontos = lixeiraRepository.listarPontosNaLixeira()
        if (pontos.isEmpty()) return Result.success(0)
        return lixeiraRepository.restaurar(pontos.map { it.id })
    }
}
