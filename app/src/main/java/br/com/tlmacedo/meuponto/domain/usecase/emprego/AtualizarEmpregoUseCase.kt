package br.com.tlmacedo.meuponto.domain.usecase.emprego

import br.com.tlmacedo.meuponto.domain.model.Emprego
import br.com.tlmacedo.meuponto.domain.repository.EmpregoRepository
import br.com.tlmacedo.meuponto.domain.usecase.validacao.ValidarEmpregoUseCase
import java.time.LocalDateTime
import javax.inject.Inject

class AtualizarEmpregoUseCase @Inject constructor(
    private val empregoRepository: EmpregoRepository,
    private val validarEmpregoUseCase: ValidarEmpregoUseCase
) {
    data class Parametros(
        val empregoId: Long,
        val nome: String,
        val descricao: String? = null,
        val ativo: Boolean = true
    )

    sealed class Resultado {
        object Sucesso : Resultado()
        data class Erro(val mensagem: String) : Resultado()
        data class NaoEncontrado(val empregoId: Long) : Resultado()
        data class Validacao(val erros: List<String>) : Resultado()
    }

    suspend operator fun invoke(parametros: Parametros): Resultado {
        val empregoExistente = empregoRepository.buscarPorId(parametros.empregoId)
            ?: return Resultado.NaoEncontrado(parametros.empregoId)

        val empregoAtualizado = empregoExistente.copy(
            nome = parametros.nome.trim(),
            descricao = parametros.descricao?.trim(),
            ativo = parametros.ativo,
            atualizadoEm = LocalDateTime.now()
        )

        val resultadoValidacao = validarEmpregoUseCase(empregoAtualizado)
        if (resultadoValidacao is ValidarEmpregoUseCase.ResultadoValidacao.Invalido) {
            return Resultado.Validacao(resultadoValidacao.erros.map { it.mensagem })
        }

        return try {
            empregoRepository.atualizar(empregoAtualizado)
            Resultado.Sucesso
        } catch (e: Exception) {
            Resultado.Erro("Erro ao atualizar emprego: ${e.message}")
        }
    }
}
