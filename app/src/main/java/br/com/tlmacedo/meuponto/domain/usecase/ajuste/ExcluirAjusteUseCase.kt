package br.com.tlmacedo.meuponto.domain.usecase.ajuste

import br.com.tlmacedo.meuponto.domain.repository.AjusteSaldoRepository
import javax.inject.Inject

class ExcluirAjusteUseCase @Inject constructor(
    private val ajusteSaldoRepository: AjusteSaldoRepository
) {
    sealed class Resultado {
        object Sucesso : Resultado()
        data class Erro(val mensagem: String) : Resultado()
        data class NaoEncontrado(val ajusteId: Long) : Resultado()
    }

    suspend operator fun invoke(ajusteId: Long): Resultado {
        val ajuste = ajusteSaldoRepository.buscarPorId(ajusteId)
            ?: return Resultado.NaoEncontrado(ajusteId)

        return try {
            ajusteSaldoRepository.excluir(ajuste)
            Resultado.Sucesso
        } catch (e: Exception) {
            Resultado.Erro("Erro ao excluir ajuste: ${e.message}")
        }
    }
}
