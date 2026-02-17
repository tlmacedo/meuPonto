// Arquivo: EditarPontoUseCase.kt
package br.com.tlmacedo.meuponto.domain.usecase.ponto

import br.com.tlmacedo.meuponto.domain.repository.PontoRepository
import java.time.LocalDateTime
import javax.inject.Inject

/**
 * Caso de uso para editar um ponto existente.
 *
 * @author Thiago
 * @since 1.0.0
 * @updated 2.1.0 - Removido tipo (calculado por posição)
 */
class EditarPontoUseCase @Inject constructor(
    private val pontoRepository: PontoRepository
) {
    data class Parametros(
        val pontoId: Long,
        val dataHora: LocalDateTime,
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

        // Busca outros pontos do mesmo dia para validar conflitos
        val pontosDoDia = pontoRepository.buscarPorEmpregoEData(
            pontoExistente.empregoId,
            parametros.dataHora.toLocalDate()
        ).filter { it.id != parametros.pontoId }

        // Valida que o novo horário não conflita com outros pontos
        val ordenados = pontosDoDia.sortedBy { it.dataHora }
        for (ponto in ordenados) {
            if (parametros.dataHora == ponto.dataHora) {
                return Resultado.Validacao(listOf("Já existe um ponto neste horário"))
            }
        }

        val pontoAtualizado = pontoExistente.copy(
            dataHora = parametros.dataHora,
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
