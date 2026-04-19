// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/data/repository/LixeiraRepositoryImpl.kt
package br.com.tlmacedo.meuponto.data.repository

import br.com.tlmacedo.meuponto.data.local.database.dao.PontoDao
import br.com.tlmacedo.meuponto.data.local.database.entity.toDomain
import br.com.tlmacedo.meuponto.domain.model.Ponto
import br.com.tlmacedo.meuponto.domain.repository.LixeiraRepository
import br.com.tlmacedo.meuponto.domain.service.AuditService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementação do LixeiraRepository.
 *
 * @author Thiago
 * @since 11.0.0
 */
@Singleton
class LixeiraRepositoryImpl @Inject constructor(
    private val pontoDao: PontoDao,
    private val auditService: AuditService
) : LixeiraRepository {

    private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    companion object {
        private const val TAG = "LixeiraRepository"
        private const val ENTIDADE = "Ponto"

        /** Dias padrão de retenção na lixeira */
        const val DIAS_RETENCAO_PADRAO = 30
    }

    // ========================================================================
    // MOVER PARA LIXEIRA
    // ========================================================================

    override suspend fun moverParaLixeira(pontoId: Long): Result<Unit> {
        return runCatching {
            val ponto = pontoDao.buscarPorId(pontoId)?.toDomain()
            val agora = System.currentTimeMillis()
            pontoDao.softDelete(pontoId, agora)

            auditService.logDelete(
                entidade = ENTIDADE,
                entidadeId = pontoId,
                motivo = ponto?.let {
                    "Ponto movido para lixeira: ${it.data.format(dateFormatter)} às ${it.hora.format(timeFormatter)}"
                } ?: "Ponto movido para lixeira",
                dadosAnteriores = ponto?.let { auditService.toJson(it.toAuditMap()) }
            )
        }
    }

    override suspend fun moverParaLixeira(pontoIds: List<Long>): Result<Int> {
        return runCatching {
            val agora = System.currentTimeMillis()
            var count = 0
            pontoIds.forEach { id ->
                pontoDao.softDelete(id, agora)
                count++
            }

            auditService.logDelete(
                entidade = ENTIDADE,
                entidadeId = 0L,
                motivo = "Lote de $count pontos movidos para lixeira"
            )

            count
        }
    }

    // ========================================================================
    // RESTAURAR
    // ========================================================================

    override suspend fun restaurar(pontoId: Long): Result<Unit> {
        return runCatching {
            val ponto = pontoDao.buscarPorIdIncluindoDeletados(pontoId)?.toDomain()
            val agora = System.currentTimeMillis()
            pontoDao.restaurar(pontoId, agora)

            auditService.logRestore(
                entidade = ENTIDADE,
                entidadeId = pontoId,
                motivo = ponto?.let {
                    "Ponto restaurado: ${it.data.format(dateFormatter)} às ${it.hora.format(timeFormatter)}"
                } ?: "Ponto restaurado da lixeira"
            )
        }
    }

    override suspend fun restaurar(pontoIds: List<Long>): Result<Int> {
        return runCatching {
            val agora = System.currentTimeMillis()
            var count = 0
            pontoIds.forEach { id ->
                pontoDao.restaurar(id, agora)
                count++
            }

            auditService.logRestore(
                entidade = ENTIDADE,
                entidadeId = 0L,
                motivo = "Lote de $count pontos restaurados da lixeira"
            )

            count
        }
    }

    // ========================================================================
    // EXCLUIR PERMANENTE
    // ========================================================================

    override suspend fun excluirPermanente(pontoId: Long): Result<Unit> {
        return runCatching {
            val ponto = pontoDao.buscarPorIdIncluindoDeletados(pontoId)?.toDomain()

            auditService.logPermanentDelete(
                entidade = ENTIDADE,
                entidadeId = pontoId,
                motivo = ponto?.let {
                    "Ponto excluído permanentemente: ${it.data.format(dateFormatter)} às ${it.hora.format(timeFormatter)}"
                } ?: "Ponto excluído permanentemente"
            )

            pontoDao.excluirPermanente(pontoId)
        }
    }

    override suspend fun excluirPermanente(pontoIds: List<Long>): Result<Int> {
        return runCatching {
            var count = 0
            pontoIds.forEach { id ->
                pontoDao.excluirPermanente(id)
                count++
            }

            auditService.logPermanentDelete(
                entidade = ENTIDADE,
                entidadeId = 0L,
                motivo = "Lote de $count pontos excluídos permanentemente"
            )

            count
        }
    }

    override suspend fun esvaziarLixeira(): Result<Int> {
        return runCatching {
            val deletados = pontoDao.listarDeletados()
            deletados.forEach { ponto ->
                pontoDao.excluirPermanente(ponto.id)
            }

            auditService.logPermanentDelete(
                entidade = ENTIDADE,
                entidadeId = 0L,
                motivo = "Lixeira esvaziada: ${deletados.size} pontos excluídos permanentemente"
            )

            deletados.size
        }
    }

    // ========================================================================
    // CONSULTAS
    // ========================================================================

    override fun observarPontosNaLixeira(): Flow<List<Ponto>> {
        return pontoDao.observarDeletados().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun listarPontosNaLixeira(): List<Ponto> {
        return pontoDao.listarDeletados().map { it.toDomain() }
    }

    override suspend fun contarItensNaLixeira(): Int {
        return pontoDao.contarDeletados()
    }

    override suspend fun lixeiraVazia(): Boolean {
        return contarItensNaLixeira() == 0
    }

    // ========================================================================
    // LIMPEZA AUTOMÁTICA
    // ========================================================================

    override suspend fun limparExpirados(diasRetencao: Int): Result<Int> {
        return runCatching {
            val agora = System.currentTimeMillis()
            val limiteExpiracao = agora - TimeUnit.DAYS.toMillis(diasRetencao.toLong())

            val deletados = pontoDao.listarDeletados()
            var count = 0

            deletados.forEach { ponto ->
                val deletedAt = ponto.deletedAt
                if (deletedAt != null && deletedAt < limiteExpiracao) {
                    pontoDao.excluirPermanente(ponto.id)
                    count++
                }
            }

            if (count > 0) {
                auditService.logPermanentDelete(
                    entidade = ENTIDADE,
                    entidadeId = 0L,
                    motivo = "Limpeza automática: $count pontos expirados excluídos (retenção: $diasRetencao dias)"
                )
            }

            count
        }
    }

    override suspend fun listarPrestesAExpirar(diasRestantes: Int): List<Ponto> {
        val agora = System.currentTimeMillis()
        val limitePrestesAExpirar = agora - TimeUnit.DAYS.toMillis(
            (DIAS_RETENCAO_PADRAO - diasRestantes).toLong()
        )

        return pontoDao.listarDeletados()
            .filter { ponto ->
                val deletedAt = ponto.deletedAt
                deletedAt != null && deletedAt < limitePrestesAExpirar
            }
            .map { it.toDomain() }
    }

    // ========================================================================
    // Helpers
    // ========================================================================

    private fun Ponto.toAuditMapLocal(): Map<String, Any?> = mapOf(
        "id" to id,
        "empregoId" to empregoId,
        "data" to data.format(dateFormatter),
        "hora" to hora.format(timeFormatter),
        "horaConsiderada" to horaConsideradaFormatada,
        "observacao" to observacao,
        "fotoComprovantePath" to fotoComprovantePath
    )
}
