// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/usecase/pendencias/ResolverInconsistenciaUseCase.kt
package br.com.tlmacedo.meuponto.domain.usecase.pendencias

import br.com.tlmacedo.meuponto.domain.repository.PontoRepository
import java.time.LocalDate
import javax.inject.Inject

/**
 * Salva uma justificativa de inconsistência em todos os pontos de um dia.
 *
 * Busca todos os pontos ativos do dia e atualiza o campo
 * [Ponto.justificativaInconsistencia] com o texto fornecido.
 * Quando [justificativa] é null ou em branco, limpa a justificativa existente.
 *
 * @since 14.0.0
 */
class ResolverInconsistenciaUseCase @Inject constructor(
    private val pontoRepository: PontoRepository
) {
    sealed class Resultado {
        data class Sucesso(val pontosAtualizados: Int) : Resultado()
        data class Erro(val mensagem: String) : Resultado()
    }

    suspend operator fun invoke(
        empregoId: Long,
        data: LocalDate,
        justificativa: String?
    ): Resultado {
        return try {
            val pontos = pontoRepository.buscarPorEmpregoEData(empregoId, data)

            if (pontos.isEmpty()) {
                return Resultado.Sucesso(0)
            }

            val textoJustificativa = justificativa?.trim()?.ifBlank { null }
            pontos.forEach { ponto ->
                pontoRepository.atualizar(ponto.copy(justificativaInconsistencia = textoJustificativa))
            }

            Resultado.Sucesso(pontos.size)
        } catch (e: Exception) {
            Resultado.Erro("Erro ao salvar justificativa: ${e.message}")
        }
    }
}
