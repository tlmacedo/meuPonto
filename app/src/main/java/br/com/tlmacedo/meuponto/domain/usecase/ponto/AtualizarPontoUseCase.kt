// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/usecase/ponto/AtualizarPontoUseCase.kt
package br.com.tlmacedo.meuponto.domain.usecase.ponto

import br.com.tlmacedo.meuponto.domain.model.Ponto
import br.com.tlmacedo.meuponto.domain.repository.PontoRepository
import timber.log.Timber
import java.time.LocalDateTime
import javax.inject.Inject

/**
 * Caso de uso para atualizar um registro de ponto existente.
 *
 * Responsável por validar e persistir alterações em um ponto já registrado.
 * Marca automaticamente o registro como "editado manualmente" para fins
 * de auditoria e identificação de alterações posteriores.
 *
 * Validações realizadas:
 * - Não permite alterar para data/hora no futuro
 *
 * @property repository Repositório de pontos para operações de persistência
 *
 * @author Thiago
 * @since 1.0.0
 */
class AtualizarPontoUseCase @Inject constructor(
    private val repository: PontoRepository
) {
    /**
     * Atualiza um registro de ponto existente.
     *
     * @param ponto Ponto com os dados atualizados
     * @return [Result] contendo o [Ponto] atualizado em caso de sucesso, ou erro
     */
    suspend operator fun invoke(ponto: Ponto): Result<Ponto> {
        return try {
            // Validação: não permite ponto no futuro
            if (ponto.dataHora.isAfter(LocalDateTime.now())) {
                return Result.failure(
                    IllegalArgumentException("Não é permitido registrar ponto no futuro")
                )
            }

            // Marca como editado manualmente e atualiza timestamp
            val pontoAtualizado = ponto.copy(
                isEditadoManualmente = true,
                atualizadoEm = LocalDateTime.now()
            )

            // Persiste a atualização
            repository.atualizar(pontoAtualizado)

            Timber.d("Ponto atualizado com sucesso: $pontoAtualizado")
            Result.success(pontoAtualizado)

        } catch (e: Exception) {
            Timber.e(e, "Erro ao atualizar ponto")
            Result.failure(e)
        }
    }
}
