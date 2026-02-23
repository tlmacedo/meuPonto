// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/usecase/ausencia/CriarAusenciaUseCase.kt
package br.com.tlmacedo.meuponto.domain.usecase.ausencia

import br.com.tlmacedo.meuponto.domain.model.ausencia.Ausencia
import br.com.tlmacedo.meuponto.domain.model.ausencia.TipoAusencia
import br.com.tlmacedo.meuponto.domain.model.ausencia.TipoFolga
import br.com.tlmacedo.meuponto.domain.repository.AusenciaRepository
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
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
 * @author Thiago
 * @since 4.0.0
 * @updated 5.5.0 - Removido SubTipoFolga
 */
class CriarAusenciaUseCase @Inject constructor(
    private val ausenciaRepository: AusenciaRepository,
    private val validarSobreposicaoUseCase: ValidarSobreposicaoAusenciaUseCase
) {

    /**
     * Cria uma nova ausência com todos os campos específicos.
     */
    suspend operator fun invoke(
        empregoId: Long,
        tipo: TipoAusencia,
        tipoFolga: TipoFolga? = null,  // ← ADICIONAR
        dataInicio: LocalDate,
        dataFim: LocalDate = dataInicio,
        descricao: String? = null,
        observacao: String? = null,
        horaInicio: LocalTime? = null,
        duracaoDeclaracaoMinutos: Int? = null,
        duracaoAbonoMinutos: Int? = null,
        periodoAquisitivo: String? = null,
        imagemUri: String? = null
    ): ResultadoCriarAusencia {
        if (dataFim < dataInicio) {
            return ResultadoCriarAusencia.Erro(
                "A data de fim não pode ser anterior à data de início"
            )
        }

        val resultadoSobreposicao = validarSobreposicaoUseCase(
            empregoId = empregoId,
            dataInicio = dataInicio,
            dataFim = dataFim
        )

        if (resultadoSobreposicao is ResultadoValidacaoSobreposicao.Sobreposicao) {
            return ResultadoCriarAusencia.Erro(resultadoSobreposicao.mensagem)
        }

        val agora = LocalDateTime.now()
        val ausencia = Ausencia(
            empregoId = empregoId,
            tipo = tipo,
            tipoFolga = tipoFolga,
            dataInicio = dataInicio,
            dataFim = dataFim,
            descricao = descricao ?: tipo.descricao,
            observacao = observacao,
            horaInicio = horaInicio,
            duracaoDeclaracaoMinutos = duracaoDeclaracaoMinutos,
            duracaoAbonoMinutos = duracaoAbonoMinutos,
            periodoAquisitivo = periodoAquisitivo,
            imagemUri = imagemUri,
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
            tipoFolga = ausencia.tipoFolga,
            dataInicio = ausencia.dataInicio,
            dataFim = ausencia.dataFim,
            descricao = ausencia.descricao,
            observacao = ausencia.observacao,
            horaInicio = ausencia.horaInicio,
            duracaoDeclaracaoMinutos = ausencia.duracaoDeclaracaoMinutos,
            duracaoAbonoMinutos = ausencia.duracaoAbonoMinutos,
            periodoAquisitivo = ausencia.periodoAquisitivo,
            imagemUri = ausencia.imagemUri
        )
    }
}
