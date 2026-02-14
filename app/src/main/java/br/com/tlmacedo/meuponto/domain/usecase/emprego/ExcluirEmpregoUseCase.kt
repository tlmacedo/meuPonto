package br.com.tlmacedo.meuponto.domain.usecase.emprego

import br.com.tlmacedo.meuponto.domain.repository.EmpregoRepository
import br.com.tlmacedo.meuponto.domain.repository.PontoRepository
import javax.inject.Inject

class ExcluirEmpregoUseCase @Inject constructor(
    private val empregoRepository: EmpregoRepository,
    private val pontoRepository: PontoRepository
) {
    sealed class Resultado {
        object Sucesso : Resultado()
        data class Erro(val mensagem: String) : Resultado()
        data class NaoEncontrado(val empregoId: Long) : Resultado()
    }

    suspend operator fun invoke(empregoId: Long): Resultado {
        val emprego = empregoRepository.buscarPorId(empregoId)
            ?: return Resultado.NaoEncontrado(empregoId)

        return try {
            empregoRepository.excluir(emprego)
            Resultado.Sucesso
        } catch (e: Exception) {
            Resultado.Erro("Erro ao excluir emprego: ${e.message}")
        }
    }
}
