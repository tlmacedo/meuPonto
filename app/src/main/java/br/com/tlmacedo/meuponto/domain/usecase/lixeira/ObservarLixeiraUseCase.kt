// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/usecase/lixeira/ObservarLixeiraUseCase.kt
package br.com.tlmacedo.meuponto.domain.usecase.lixeira

import br.com.tlmacedo.meuponto.domain.model.Ponto
import br.com.tlmacedo.meuponto.domain.repository.LixeiraRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * UseCase para observar e consultar a lixeira.
 *
 * @author Thiago
 * @since 11.0.0
 */
class ObservarLixeiraUseCase @Inject constructor(
    private val lixeiraRepository: LixeiraRepository
) {
    /**
     * Observa mudanças na lixeira (reativo).
     */
    operator fun invoke(): Flow<List<Ponto>> {
        return lixeiraRepository.observarPontosNaLixeira()
    }

    /**
     * Obtém snapshot da lixeira.
     */
    suspend fun listar(): List<Ponto> {
        return lixeiraRepository.listarPontosNaLixeira()
    }

    /**
     * Conta itens na lixeira.
     */
    suspend fun contar(): Int {
        return lixeiraRepository.contarItensNaLixeira()
    }

    /**
     * Verifica se a lixeira está vazia.
     */
    suspend fun estaVazia(): Boolean {
        return lixeiraRepository.lixeiraVazia()
    }
}
