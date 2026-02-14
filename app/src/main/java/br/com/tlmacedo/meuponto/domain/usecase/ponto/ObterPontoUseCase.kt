package br.com.tlmacedo.meuponto.domain.usecase.ponto

import br.com.tlmacedo.meuponto.domain.model.Ponto
import br.com.tlmacedo.meuponto.domain.repository.PontoRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Caso de uso para obter detalhes de um ponto específico.
 */
class ObterPontoUseCase @Inject constructor(
    private val pontoRepository: PontoRepository
) {
    sealed class Resultado {
        data class Sucesso(val ponto: Ponto) : Resultado()
        data class NaoEncontrado(val pontoId: Long) : Resultado()
    }

    suspend operator fun invoke(pontoId: Long): Resultado {
        val ponto = pontoRepository.buscarPorId(pontoId)
        return if (ponto != null) {
            Resultado.Sucesso(ponto)
        } else {
            Resultado.NaoEncontrado(pontoId)
        }
    }

    fun observar(pontoId: Long): Flow<Ponto?> {
        return pontoRepository.observarPorId(pontoId)
    }
}
