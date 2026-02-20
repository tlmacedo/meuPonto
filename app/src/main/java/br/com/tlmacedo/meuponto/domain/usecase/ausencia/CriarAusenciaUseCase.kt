// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/usecase/ausencia/CriarAusenciaUseCase.kt
package br.com.tlmacedo.meuponto.domain.usecase.ausencia

import br.com.tlmacedo.meuponto.domain.model.ausencia.Ausencia
import br.com.tlmacedo.meuponto.domain.model.ausencia.TipoAusencia
import br.com.tlmacedo.meuponto.domain.repository.AusenciaRepository
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

/**
 * Resultado da criação de ausência.
 */
sealed class ResultadoCriarAusencia {
    data class Sucesso(val ausencia: Ausencia) : ResultadoCriarAusencia()
    data class Erro(val mensagem: String) : ResultadoCriarAusencia()
}

/**
 * Caso de uso para criar uma nova ausência.
 *
 * Validações:
 * - Data fim >= data início
 * - Não há sobreposição com outras ausências do mesmo emprego
 * - Emprego é válido
 *
 * @author Thiago
 * @since 4.0.0
 */
class CriarAusenciaUseCase @Inject constructor(
    private val ausenciaRepository: AusenciaRepository,
    private val validarSobreposicaoUseCase: ValidarSobreposicaoAusenciaUseCase
) {

    /**
     * Cria uma nova ausência.
     *
     * @param empregoId ID do emprego
     * @param tipo Tipo da ausência
     * @param dataInicio Data de início
     * @param dataFim Data de fim (padrão = dataInicio)
     * @param descricao Descrição opcional
     * @param observacao Observação opcional
     * @return Resultado da operação
     */
    suspend operator fun invoke(
        empregoId: Long,
        tipo: TipoAusencia,
        dataInicio: LocalDate,
        dataFim: LocalDate = dataInicio,
        descricao: String? = null,
        observacao: String? = null
    ): ResultadoCriarAusencia {
        // Validação: data fim >= data início
        if (dataFim < dataInicio) {
            return ResultadoCriarAusencia.Erro(
                "A data de fim não pode ser anterior à data de início"
            )
        }

        // Validação: verificar sobreposição
        val resultadoSobreposicao = validarSobreposicaoUseCase(
            empregoId = empregoId,
            dataInicio = dataInicio,
            dataFim = dataFim
        )

        if (resultadoSobreposicao is ResultadoValidacaoSobreposicao.Sobreposicao) {
            return ResultadoCriarAusencia.Erro(resultadoSobreposicao.mensagem)
        }

        // Criar ausência
        val agora = LocalDateTime.now()
        val ausencia = Ausencia(
            empregoId = empregoId,
            tipo = tipo,
            dataInicio = dataInicio,
            dataFim = dataFim,
            descricao = descricao ?: tipo.descricao,
            observacao = observacao,
            ativo = true,
            criadoEm = agora,
            atualizadoEm = agora
        )

        return try {
            val id = ausenciaRepository.inserir(ausencia)
            val ausenciaSalva = ausencia.copy(id = id)
            ResultadoCriarAusencia.Sucesso(ausenciaSalva)
        } catch (e: Exception) {
            ResultadoCriarAusencia.Erro("Erro ao salvar ausência: ${e.message}")
        }
    }

    /**
     * Cria ausência a partir de um objeto Ausencia já montado.
     */
    suspend operator fun invoke(ausencia: Ausencia): ResultadoCriarAusencia {
        return invoke(
            empregoId = ausencia.empregoId,
            tipo = ausencia.tipo,
            dataInicio = ausencia.dataInicio,
            dataFim = ausencia.dataFim,
            descricao = ausencia.descricao,
            observacao = ausencia.observacao
        )
    }
}
