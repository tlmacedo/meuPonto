package br.com.tlmacedo.meuponto.domain.usecase.ponto

import br.com.tlmacedo.meuponto.domain.model.Ponto
import br.com.tlmacedo.meuponto.domain.repository.PontoRepository
import timber.log.Timber
import javax.inject.Inject

/**
 * Caso de uso para deletar um registro de ponto.
 *
 * @property repository Repositório de pontos
 *
 * @author Thiago
 * @since 1.0.0
 */
class DeletarPontoUseCase @Inject constructor(
    private val repository: PontoRepository
) {
    /**
     * Remove um registro de ponto.
     *
     * @param ponto Ponto a ser removido
     * @return Result indicando sucesso ou falha
     */
    suspend operator fun invoke(ponto: Ponto): Result<Unit> {
        return try {
            repository.deletar(ponto)
            Timber.d("Ponto deletado com sucesso: ${ponto.id}")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Erro ao deletar ponto")
            Result.failure(e)
        }
    }
}
