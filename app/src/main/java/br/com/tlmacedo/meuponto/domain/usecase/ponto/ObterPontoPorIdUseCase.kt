// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/usecase/ponto/ObterPontoPorIdUseCase.kt
package br.com.tlmacedo.meuponto.domain.usecase.ponto

import br.com.tlmacedo.meuponto.domain.model.Ponto
import br.com.tlmacedo.meuponto.domain.repository.PontoRepository
import javax.inject.Inject

/**
 * Caso de uso para obter um ponto específico pelo ID.
 *
 * Busca um registro de ponto no banco de dados pelo seu identificador único.
 *
 * @property repository Repositório de pontos
 *
 * @author Thiago
 * @since 1.0.0
 */
class ObterPontoPorIdUseCase @Inject constructor(
    private val repository: PontoRepository
) {
    /**
     * Busca um ponto pelo ID.
     *
     * @param id Identificador único do ponto
     * @return Ponto encontrado ou null se não existir
     */
    suspend operator fun invoke(id: Long): Ponto? {
        return repository.buscarPorId(id)
    }
}
