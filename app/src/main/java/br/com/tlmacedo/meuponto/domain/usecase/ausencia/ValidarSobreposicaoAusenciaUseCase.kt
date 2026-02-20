// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/usecase/ausencia/ValidarSobreposicaoAusenciaUseCase.kt
package br.com.tlmacedo.meuponto.domain.usecase.ausencia

import br.com.tlmacedo.meuponto.domain.repository.AusenciaRepository
import java.time.LocalDate
import javax.inject.Inject

/**
 * Resultado da validação de sobreposição.
 */
sealed class ResultadoValidacaoSobreposicao {
    data object SemSobreposicao : ResultadoValidacaoSobreposicao()
    data class Sobreposicao(val mensagem: String) : ResultadoValidacaoSobreposicao()
}

/**
 * Caso de uso para validar se há sobreposição de ausências.
 *
 * Uma ausência não pode se sobrepor a outra do mesmo emprego.
 *
 * @author Thiago
 * @since 4.0.0
 */
class ValidarSobreposicaoAusenciaUseCase @Inject constructor(
    private val ausenciaRepository: AusenciaRepository
) {

    /**
     * Valida se há sobreposição com outras ausências.
     *
     * @param empregoId ID do emprego
     * @param dataInicio Data de início do período
     * @param dataFim Data de fim do período
     * @param excluirId ID da ausência a excluir da verificação (para edição)
     * @return Resultado da validação
     */
    suspend operator fun invoke(
        empregoId: Long,
        dataInicio: LocalDate,
        dataFim: LocalDate,
        excluirId: Long = 0
    ): ResultadoValidacaoSobreposicao {
        val existeSobreposicao = ausenciaRepository.existeSobreposicao(
            empregoId = empregoId,
            dataInicio = dataInicio,
            dataFim = dataFim,
            excluirId = excluirId
        )

        return if (existeSobreposicao) {
            // Buscar detalhes das ausências sobrepostas para mensagem mais informativa
            val ausenciasSobrepostas = ausenciaRepository.buscarPorPeriodo(
                empregoId = empregoId,
                dataInicio = dataInicio,
                dataFim = dataFim
            ).filter { it.id != excluirId }

            val primeira = ausenciasSobrepostas.firstOrNull()
            val mensagem = if (primeira != null) {
                "Já existe uma ausência (${primeira.tipo.descricao}) " +
                        "no período de ${primeira.formatarPeriodo()}"
            } else {
                "Já existe uma ausência registrada neste período"
            }

            ResultadoValidacaoSobreposicao.Sobreposicao(mensagem)
        } else {
            ResultadoValidacaoSobreposicao.SemSobreposicao
        }
    }
}
