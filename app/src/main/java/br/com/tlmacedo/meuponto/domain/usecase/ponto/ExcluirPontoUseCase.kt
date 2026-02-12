// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/usecase/ponto/ExcluirPontoUseCase.kt
package br.com.tlmacedo.meuponto.domain.usecase.ponto

import br.com.tlmacedo.meuponto.domain.model.Ponto
import br.com.tlmacedo.meuponto.domain.repository.PontoRepository
import javax.inject.Inject

/**
 * Caso de uso para excluir um ponto.
 *
 * @property repository Repositório de pontos
 *
 * @author Thiago
 * @since 1.0.0
 */
class ExcluirPontoUseCase @Inject constructor(
    private val repository: PontoRepository
) {
    /**
     * Exclui um ponto existente.
     *
     * @param ponto Ponto a ser excluído
     * @return Result indicando sucesso ou falha
     */
    suspend operator fun invoke(ponto: Ponto): Result<Unit> {
        return try {
            repository.excluir(ponto)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
