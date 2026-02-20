// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/usecase/ausencia/AtualizarAusenciaUseCase.kt
package br.com.tlmacedo.meuponto.domain.usecase.ausencia

import br.com.tlmacedo.meuponto.domain.model.ausencia.Ausencia
import br.com.tlmacedo.meuponto.domain.repository.AusenciaRepository
import java.time.LocalDateTime
import javax.inject.Inject

/**
 * Resultado da atualização de ausência.
 */
sealed class ResultadoAtualizarAusencia {
    data class Sucesso(val ausencia: Ausencia) : ResultadoAtualizarAusencia()
    data class Erro(val mensagem: String) : ResultadoAtualizarAusencia()
}

/**
 * Caso de uso para atualizar uma ausência existente.
 *
 * @author Thiago
 * @since 4.0.0
 */
class AtualizarAusenciaUseCase @Inject constructor(
    private val ausenciaRepository: AusenciaRepository,
    private val validarSobreposicaoUseCase: ValidarSobreposicaoAusenciaUseCase
) {

    /**
     * Atualiza uma ausência existente.
     *
     * @param ausencia Ausência com dados atualizados
     * @return Resultado da operação
     */
    suspend operator fun invoke(ausencia: Ausencia): ResultadoAtualizarAusencia {
        // Validação: ID deve existir
        if (ausencia.id <= 0) {
            return ResultadoAtualizarAusencia.Erro("Ausência inválida: ID não informado")
        }

        // Validação: data fim >= data início
        if (ausencia.dataFim < ausencia.dataInicio) {
            return ResultadoAtualizarAusencia.Erro(
                "A data de fim não pode ser anterior à data de início"
            )
        }

        // Validação: verificar se existe
        val ausenciaExistente = ausenciaRepository.buscarPorId(ausencia.id)
        if (ausenciaExistente == null) {
            return ResultadoAtualizarAusencia.Erro("Ausência não encontrada")
        }

        // Validação: verificar sobreposição (excluindo a própria ausência)
        val resultadoSobreposicao = validarSobreposicaoUseCase(
            empregoId = ausencia.empregoId,
            dataInicio = ausencia.dataInicio,
            dataFim = ausencia.dataFim,
            excluirId = ausencia.id
        )

        if (resultadoSobreposicao is ResultadoValidacaoSobreposicao.Sobreposicao) {
            return ResultadoAtualizarAusencia.Erro(resultadoSobreposicao.mensagem)
        }

        // Atualizar
        val ausenciaAtualizada = ausencia.copy(
            atualizadoEm = LocalDateTime.now()
        )

        return try {
            ausenciaRepository.atualizar(ausenciaAtualizada)
            ResultadoAtualizarAusencia.Sucesso(ausenciaAtualizada)
        } catch (e: Exception) {
            ResultadoAtualizarAusencia.Erro("Erro ao atualizar ausência: ${e.message}")
        }
    }
}
