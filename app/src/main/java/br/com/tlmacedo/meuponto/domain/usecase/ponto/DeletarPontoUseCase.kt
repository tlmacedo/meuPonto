// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/usecase/ponto/DeletarPontoUseCase.kt
package br.com.tlmacedo.meuponto.domain.usecase.ponto

import br.com.tlmacedo.meuponto.domain.model.Ponto
import br.com.tlmacedo.meuponto.domain.repository.PontoRepository
import timber.log.Timber
import javax.inject.Inject

/**
 * Caso de uso para deletar um registro de ponto.
 *
 * Responsável por remover um ponto existente do sistema. A exclusão é
 * permanente e não pode ser desfeita.
 *
 * @property repository Repositório de pontos para operações de persistência
 *
 * @author Thiago
 * @since 1.0.0
 */
class DeletarPontoUseCase @Inject constructor(
    private val repository: PontoRepository
) {
    /**
     * Remove um registro de ponto do sistema.
     *
     * @param ponto Ponto a ser removido
     * @return [Result] indicando sucesso ([Unit]) ou falha com a exceção
     */
    suspend operator fun invoke(ponto: Ponto): Result<Unit> {
        return try {
            repository.excluir(ponto)
            Timber.d("Ponto deletado com sucesso: id=${ponto.id}")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Erro ao deletar ponto: id=${ponto.id}")
            Result.failure(e)
        }
    }
}
