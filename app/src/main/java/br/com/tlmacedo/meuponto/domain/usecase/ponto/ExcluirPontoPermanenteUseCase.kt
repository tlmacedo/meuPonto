// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/usecase/ponto/ExcluirPontoPermanenteUseCase.kt
package br.com.tlmacedo.meuponto.domain.usecase.ponto

import br.com.tlmacedo.meuponto.domain.model.AcaoAuditoria
import br.com.tlmacedo.meuponto.domain.model.AuditLog
import br.com.tlmacedo.meuponto.domain.repository.AuditLogRepository
import br.com.tlmacedo.meuponto.domain.repository.PontoRepository
import br.com.tlmacedo.meuponto.util.foto.ImageTrashManager
import com.google.gson.Gson
import timber.log.Timber
import java.time.LocalDateTime
import javax.inject.Inject

/**
 * Use case para exclusão permanente de pontos.
 *
 * Remove definitivamente o ponto do banco de dados e a foto da lixeira.
 *
 * @author Thiago
 * @since 11.0.0
 */
class ExcluirPontoPermanenteUseCase @Inject constructor(
    private val pontoRepository: PontoRepository,
    private val auditLogRepository: AuditLogRepository,
    private val imageTrashManager: ImageTrashManager,
    private val gson: Gson
) {
    /**
     * Exclui permanentemente um ponto.
     *
     * @param pontoId ID do ponto a ser excluído permanentemente
     * @return Result indicando sucesso ou falha
     */
    suspend operator fun invoke(pontoId: Long): Result<Unit> {
        return try {
            Timber.d("Excluindo permanentemente ponto ID: $pontoId")

            val ponto = pontoRepository.buscarPorIdIncluindoDeletados(pontoId)
                ?: return Result.failure(IllegalArgumentException("Ponto não encontrado: $pontoId"))

            // Remove a foto permanentemente da lixeira
            ponto.fotoComprovantePath?.let { fotoPath ->
                val removida = imageTrashManager.deletePermanently(fotoPath)
                if (removida) {
                    Timber.d("Foto removida permanentemente: $fotoPath")
                } else {
                    Timber.w("Foto não encontrada na lixeira: $fotoPath")
                }
            }

            // Registra na auditoria ANTES de excluir (para manter os dados)
            val auditLog = AuditLog(
                entidade = "pontos",
                entidadeId = pontoId,
                acao = AcaoAuditoria.PERMANENT_DELETE,
                motivo = "Exclusão permanente solicitada pelo usuário",
                dadosAnteriores = gson.toJson(ponto.toAuditMap()),
                dadosNovos = null,
                criadoEm = LocalDateTime.now()
            )
            auditLogRepository.inserir(auditLog)

            // Remove o ponto do banco de dados
            pontoRepository.excluirPermanente(pontoId)

            Timber.i("Ponto excluído permanentemente: $pontoId")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Erro ao excluir permanentemente ponto: $pontoId")
            Result.failure(e)
        }
    }

    /**
     * Exclui permanentemente múltiplos pontos.
     */
    suspend fun excluirMultiplos(pontoIds: List<Long>): Result<Int> {
        return try {
            var excluidos = 0
            pontoIds.forEach { pontoId ->
                invoke(pontoId).onSuccess { excluidos++ }
            }
            Timber.i("$excluidos pontos excluídos permanentemente de ${pontoIds.size}")
            Result.success(excluidos)
        } catch (e: Exception) {
            Timber.e(e, "Erro ao excluir múltiplos pontos permanentemente")
            Result.failure(e)
        }
    }

    /**
     * Esvazia completamente a lixeira.
     */
    suspend fun esvaziarLixeira(): Result<Int> {
        return try {
            val pontosNaLixeira = pontoRepository.listarDeletados()
            var excluidos = 0
            pontosNaLixeira.forEach { ponto ->
                invoke(ponto.id).onSuccess { excluidos++ }
            }
            imageTrashManager.cleanupExpiredFiles()
            Timber.i("Lixeira esvaziada: $excluidos pontos removidos")
            Result.success(excluidos)
        } catch (e: Exception) {
            Timber.e(e, "Erro ao esvaziar lixeira")
            Result.failure(e)
        }
    }
}
