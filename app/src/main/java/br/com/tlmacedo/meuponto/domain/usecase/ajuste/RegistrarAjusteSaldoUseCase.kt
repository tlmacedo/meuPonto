// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/usecase/ajuste/RegistrarAjusteSaldoUseCase.kt
package br.com.tlmacedo.meuponto.domain.usecase.ajuste

import br.com.tlmacedo.meuponto.domain.model.AjusteSaldo
import br.com.tlmacedo.meuponto.domain.repository.AjusteSaldoRepository
import java.time.LocalDate
import javax.inject.Inject
import kotlin.math.abs

/**
 * Caso de uso para registrar um ajuste manual de saldo.
 *
 * Permite adicionar ou subtrair minutos do banco de horas
 * com justificativa obrigatória para auditoria.
 *
 * @property ajusteSaldoRepository Repositório de ajustes
 *
 * @author Thiago
 * @since 2.0.0
 */
class RegistrarAjusteSaldoUseCase @Inject constructor(
    private val ajusteSaldoRepository: AjusteSaldoRepository
) {
    /**
     * Resultado da operação de ajuste.
     */
    sealed class ResultadoAjuste {
        data class Sucesso(val ajuste: AjusteSaldo) : ResultadoAjuste()
        data class Erro(val mensagem: String) : ResultadoAjuste()
    }

    /**
     * Parâmetros para registrar um ajuste.
     *
     * @property empregoId ID do emprego
     * @property data Data do ajuste
     * @property minutos Minutos a ajustar (positivo = crédito, negativo = débito)
     * @property justificativa Justificativa obrigatória
     */
    data class ParametrosAjuste(
        val empregoId: Long,
        val data: LocalDate,
        val minutos: Int,
        val justificativa: String
    )

    /**
     * Registra um ajuste de saldo.
     *
     * @param parametros Parâmetros do ajuste
     * @return [ResultadoAjuste] indicando sucesso ou erro
     */
    suspend operator fun invoke(parametros: ParametrosAjuste): ResultadoAjuste {
        // Validações
        val validacao = validar(parametros)
        if (validacao != null) {
            return ResultadoAjuste.Erro(validacao)
        }

        // Cria o ajuste
        val ajuste = AjusteSaldo(
            empregoId = parametros.empregoId,
            data = parametros.data,
            minutos = parametros.minutos,
            justificativa = parametros.justificativa.trim()
        )

        // Persiste
        val id = ajusteSaldoRepository.inserir(ajuste)

        return ResultadoAjuste.Sucesso(ajuste.copy(id = id))
    }

    /**
     * Versão simplificada para registrar ajuste.
     *
     * @param empregoId ID do emprego
     * @param data Data do ajuste
     * @param minutos Minutos a ajustar
     * @param justificativa Justificativa
     * @return [ResultadoAjuste]
     */
    suspend operator fun invoke(
        empregoId: Long,
        data: LocalDate,
        minutos: Int,
        justificativa: String
    ): ResultadoAjuste {
        return invoke(ParametrosAjuste(empregoId, data, minutos, justificativa))
    }

    /**
     * Valida os parâmetros do ajuste.
     *
     * @return Mensagem de erro ou null se válido
     */
    private fun validar(parametros: ParametrosAjuste): String? {
        if (parametros.minutos == 0) {
            return "O ajuste não pode ser zero"
        }

        if (parametros.justificativa.isBlank()) {
            return "A justificativa é obrigatória"
        }

        if (parametros.justificativa.length < MIN_JUSTIFICATIVA_LENGTH) {
            return "A justificativa deve ter pelo menos $MIN_JUSTIFICATIVA_LENGTH caracteres"
        }

        if (parametros.justificativa.length > MAX_JUSTIFICATIVA_LENGTH) {
            return "A justificativa deve ter no máximo $MAX_JUSTIFICATIVA_LENGTH caracteres"
        }

        if (parametros.data.isAfter(LocalDate.now())) {
            return "Não é possível registrar ajuste para datas futuras"
        }

        // Limite máximo de ajuste por vez (4 horas)
        if (abs(parametros.minutos) > MAX_AJUSTE_MINUTOS) {
            return "O ajuste não pode exceder ${MAX_AJUSTE_MINUTOS / 60} horas por vez"
        }

        return null
    }

    companion object {
        const val MIN_JUSTIFICATIVA_LENGTH = 10
        const val MAX_JUSTIFICATIVA_LENGTH = 500
        const val MAX_AJUSTE_MINUTOS = 240 // 4 horas
    }
}
