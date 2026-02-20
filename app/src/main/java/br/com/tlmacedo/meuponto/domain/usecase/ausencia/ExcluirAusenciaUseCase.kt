// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/usecase/ausencia/ExcluirAusenciaUseCase.kt
package br.com.tlmacedo.meuponto.domain.usecase.ausencia

import br.com.tlmacedo.meuponto.domain.model.ausencia.Ausencia
import br.com.tlmacedo.meuponto.domain.repository.AusenciaRepository
import javax.inject.Inject

/**
 * Resultado da exclusão de ausência.
 */
sealed class ResultadoExcluirAusencia {
    data object Sucesso : ResultadoExcluirAusencia()
    data class Erro(val mensagem: String) : ResultadoExcluirAusencia()
}

/**
 * Caso de uso para excluir uma ausência.
 *
 * @author Thiago
 * @since 4.0.0
 */
class ExcluirAusenciaUseCase @Inject constructor(
    private val ausenciaRepository: AusenciaRepository
) {

    /**
     * Exclui uma ausência por ID.
     *
     * @param id ID da ausência
     * @return Resultado da operação
     */
    suspend operator fun invoke(id: Long): ResultadoExcluirAusencia {
        if (id <= 0) {
            return ResultadoExcluirAusencia.Erro("ID inválido")
        }

        return try {
            ausenciaRepository.excluirPorId(id)
            ResultadoExcluirAusencia.Sucesso
        } catch (e: Exception) {
            ResultadoExcluirAusencia.Erro("Erro ao excluir ausência: ${e.message}")
        }
    }

    /**
     * Exclui uma ausência.
     *
     * @param ausencia Ausência a ser excluída
     * @return Resultado da operação
     */
    suspend operator fun invoke(ausencia: Ausencia): ResultadoExcluirAusencia {
        return invoke(ausencia.id)
    }
}
