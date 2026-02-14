package br.com.tlmacedo.meuponto.domain.usecase.ponto

import br.com.tlmacedo.meuponto.domain.model.TipoPonto
import br.com.tlmacedo.meuponto.domain.repository.PontoRepository
import br.com.tlmacedo.meuponto.domain.usecase.validacao.ValidarRegistroPontoUseCase
import java.time.LocalDateTime
import javax.inject.Inject

class EditarPontoUseCase @Inject constructor(
    private val pontoRepository: PontoRepository,
    private val validarRegistroPontoUseCase: ValidarRegistroPontoUseCase
) {
    data class Parametros(
        val pontoId: Long,
        val dataHora: LocalDateTime,
        val tipo: TipoPonto,
        val observacao: String? = null
    )

    sealed class Resultado {
        object Sucesso : Resultado()
        data class Erro(val mensagem: String) : Resultado()
        data class NaoEncontrado(val pontoId: Long) : Resultado()
        data class Validacao(val erros: List<String>) : Resultado()
    }

    suspend operator fun invoke(parametros: Parametros): Resultado {
        val pontoExistente = pontoRepository.buscarPorId(parametros.pontoId)
            ?: return Resultado.NaoEncontrado(parametros.pontoId)

        // Valida o registro (ignorando o próprio ponto na validação)
        val resultadoValidacao = validarRegistroPontoUseCase(
            empregoId = pontoExistente.empregoId,
            data = parametros.dataHora.toLocalDate(),
            hora = parametros.dataHora.toLocalTime(),
            tipo = parametros.tipo,
            ignorarHorarioFuturo = true
        )

        if (resultadoValidacao is ValidarRegistroPontoUseCase.ResultadoValidacao.Invalido) {
            return Resultado.Validacao(resultadoValidacao.erros.map { it.mensagem })
        }

        val pontoAtualizado = pontoExistente.copy(
            dataHora = parametros.dataHora,
            tipo = parametros.tipo,
            observacao = parametros.observacao,
            isEditadoManualmente = true,
            atualizadoEm = LocalDateTime.now()
        )

        return try {
            pontoRepository.atualizar(pontoAtualizado)
            Resultado.Sucesso
        } catch (e: Exception) {
            Resultado.Erro("Erro ao atualizar ponto: ${e.message}")
        }
    }
}
