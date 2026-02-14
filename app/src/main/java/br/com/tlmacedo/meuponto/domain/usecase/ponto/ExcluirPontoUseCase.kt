package br.com.tlmacedo.meuponto.domain.usecase.ponto

import br.com.tlmacedo.meuponto.domain.repository.PontoRepository
import javax.inject.Inject

/**
 * Caso de uso para excluir um ponto.
 */
class ExcluirPontoUseCase @Inject constructor(
    private val pontoRepository: PontoRepository
) {
    sealed class Resultado {
        object Sucesso : Resultado()
        data class Erro(val mensagem: String) : Resultado()
        data class NaoEncontrado(val pontoId: Long) : Resultado()
    }

    suspend operator fun invoke(pontoId: Long): Resultado {
        val ponto = pontoRepository.buscarPorId(pontoId)
            ?: return Resultado.NaoEncontrado(pontoId)

        return try {
            pontoRepository.excluir(ponto)
            Resultado.Sucesso
        } catch (e: Exception) {
            Resultado.Erro("Erro ao excluir ponto: ${e.message}")
        }
    }
}
