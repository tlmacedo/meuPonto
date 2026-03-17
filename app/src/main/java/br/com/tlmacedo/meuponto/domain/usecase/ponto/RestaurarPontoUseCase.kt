// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/usecase/ponto/RestaurarPontoUseCase.kt
package br.com.tlmacedo.meuponto.domain.usecase.ponto

import br.com.tlmacedo.meuponto.domain.model.AcaoAuditoria
import br.com.tlmacedo.meuponto.domain.model.AuditLog
import br.com.tlmacedo.meuponto.domain.model.Ponto
import br.com.tlmacedo.meuponto.domain.repository.AuditLogRepository
import br.com.tlmacedo.meuponto.domain.repository.PontoRepository
import br.com.tlmacedo.meuponto.util.foto.ImageTrashManager
import br.com.tlmacedo.meuponto.util.foto.RestoreResult
import com.google.gson.Gson
import timber.log.Timber
import java.time.LocalDateTime
import javax.inject.Inject

/**
 * Use case para restaurar pontos excluídos (soft delete).
 *
 * Restaura o ponto do estado deletado e recupera a foto da lixeira.
 *
 * @author Thiago
 * @since 11.0.0
 */
class RestaurarPontoUseCase @Inject constructor(
    private val pontoRepository: PontoRepository,
    private val auditLogRepository: AuditLogRepository,
    private val imageTrashManager: ImageTrashManager,
    private val gson: Gson
) {
    /**
     * Restaura um ponto específico.
     *
     * @param pontoId ID do ponto a ser restaurado
     * @return Result com o ponto restaurado ou erro
     */
    suspend operator fun invoke(pontoId: Long): Result<Ponto> {
        return try {
            Timber.d("Restaurando ponto ID: $pontoId")

            // Busca o ponto (incluindo deletados)
            val ponto = pontoRepository.buscarPorIdIncluindoDeletados(pontoId)
                ?: return Result.failure(IllegalArgumentException("Ponto não encontrado: $pontoId"))

            if (!ponto.isDeleted) {
                return Result.failure(IllegalStateException("Ponto não está na lixeira"))
            }

            // Restaura a foto da lixeira, se existir
            var fotoRestauradaPath: String? = ponto.fotoComprovantePath
            ponto.fotoComprovantePath?.let { fotoPath ->
                when (val resultado = imageTrashManager.restoreFromTrash(fotoPath)) {
                    is RestoreResult.Success -> {
                        Timber.d("Foto restaurada da lixeira: ${resultado.originalPath}")
                        fotoRestauradaPath = resultado.originalPath
                    }
                    is RestoreResult.FileNotFound -> {
                        Timber.w("Foto não encontrada na lixeira: $fotoPath")
                        fotoRestauradaPath = null // Foto perdida
                    }
                    is RestoreResult.InvalidMetadata -> {
                        Timber.w("Metadados inválidos para foto: $fotoPath")
                        fotoRestauradaPath = null
                    }
                    is RestoreResult.Error -> {
                        Timber.w("Erro ao restaurar foto: ${resultado.message}")
                        // Mantém o path original, pode ser que a foto ainda exista
                    }
                }
            }

            // Restaura o ponto
            val pontoRestaurado = ponto.copy(
                isDeleted = false,
                deletedAt = null,
                fotoComprovantePath = fotoRestauradaPath,
                atualizadoEm = LocalDateTime.now()
            )

            pontoRepository.atualizar(pontoRestaurado)

            // Registra na auditoria
            val auditLog = AuditLog(
                entidade = "pontos",
                entidadeId = pontoId,
                acao = AcaoAuditoria.RESTORE,
                motivo = "Ponto restaurado da lixeira",
                dadosAnteriores = gson.toJson(ponto.toAuditMap()),
                dadosNovos = gson.toJson(pontoRestaurado.toAuditMap()),
                criadoEm = LocalDateTime.now()
            )
            auditLogRepository.inserir(auditLog)

            Timber.i("Ponto restaurado com sucesso: $pontoId")
            Result.success(pontoRestaurado)
        } catch (e: Exception) {
            Timber.e(e, "Erro ao restaurar ponto: $pontoId")
            Result.failure(e)
        }
    }

    /**
     * Restaura múltiplos pontos.
     *
     * @param pontoIds Lista de IDs dos pontos a restaurar
     * @return Result com quantidade de pontos restaurados
     */
    suspend fun restaurarMultiplos(pontoIds: List<Long>): Result<Int> {
        return try {
            var restaurados = 0

            pontoIds.forEach { pontoId ->
                invoke(pontoId).onSuccess { restaurados++ }
            }

            Timber.i("$restaurados pontos restaurados de ${pontoIds.size}")
            Result.success(restaurados)
        } catch (e: Exception) {
            Timber.e(e, "Erro ao restaurar múltiplos pontos")
            Result.failure(e)
        }
    }
}
