package br.com.tlmacedo.meuponto.domain.usecase.ponto

import br.com.tlmacedo.meuponto.domain.model.Ponto
import br.com.tlmacedo.meuponto.domain.repository.PontoRepository
import timber.log.Timber
import java.time.LocalDateTime
import javax.inject.Inject

/**
 * Caso de uso para atualizar um registro de ponto.
 *
 * Marca automaticamente o registro como "editado manualmente".
 *
 * @property repository Repositório de pontos
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
     * @return Result indicando sucesso ou falha
     */
    suspend operator fun invoke(ponto: Ponto): Result<Ponto> {
        return try {
            if (ponto.dataHora.isAfter(LocalDateTime.now())) {
                return Result.failure(
                    IllegalArgumentException("Não é permitido registrar ponto no futuro")
                )
            }

            val pontoAtualizado = ponto.copy(
                editadoManualmente = true,
                atualizadoEm = LocalDateTime.now()
            )

            repository.atualizar(pontoAtualizado)
            
            Timber.d("Ponto atualizado com sucesso: $pontoAtualizado")
            Result.success(pontoAtualizado)

        } catch (e: Exception) {
            Timber.e(e, "Erro ao atualizar ponto")
            Result.failure(e)
        }
    }
}
