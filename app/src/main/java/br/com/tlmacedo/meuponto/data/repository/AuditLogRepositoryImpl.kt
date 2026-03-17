// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/data/repository/AuditLogRepositoryImpl.kt
package br.com.tlmacedo.meuponto.data.repository

import br.com.tlmacedo.meuponto.data.local.database.dao.AuditLogDao
import br.com.tlmacedo.meuponto.data.local.database.entity.toDomain
import br.com.tlmacedo.meuponto.data.local.database.entity.toEntity
import br.com.tlmacedo.meuponto.domain.model.AcaoAuditoria
import br.com.tlmacedo.meuponto.domain.model.AuditLog
import br.com.tlmacedo.meuponto.domain.repository.AuditLogRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementação concreta do repositório de logs de auditoria.
 *
 * @property auditLogDao DAO do Room para operações de banco de dados
 *
 * @author Thiago
 * @since 2.0.0
 * @updated 11.0.0 - Adicionado observarTodos()
 */
@Singleton
class AuditLogRepositoryImpl @Inject constructor(
    private val auditLogDao: AuditLogDao
) : AuditLogRepository {

    // ========================================================================
    // Operações de Escrita
    // ========================================================================

    override suspend fun inserir(log: AuditLog): Long {
        return auditLogDao.inserir(log.toEntity())
    }

    override suspend fun inserirTodos(logs: List<AuditLog>): List<Long> {
        return auditLogDao.inserirTodos(logs.map { it.toEntity() })
    }

    // ========================================================================
    // Operações de Leitura
    // ========================================================================

    override suspend fun buscarPorEntidade(entidade: String, entidadeId: Long): List<AuditLog> {
        return auditLogDao.buscarPorEntidade(entidade, entidadeId).map { it.toDomain() }
    }

    override suspend fun contarTodos(): Int {
        return auditLogDao.contarTodos()
    }

    override suspend fun contarPorEntidade(entidade: String): Int {
        return auditLogDao.contarPorEntidade(entidade)
    }

    override suspend fun contarPorEntidadeEId(entidade: String, entidadeId: Long): Int {
        return auditLogDao.contarPorEntidadeEId(entidade, entidadeId)
    }

    // ========================================================================
    // Operações de Limpeza
    // ========================================================================

    override suspend fun excluirAnterioresA(dataLimite: LocalDateTime): Int {
        return auditLogDao.excluirAnterioresA(dataLimite.toString())
    }

    override suspend fun excluirPorEntidade(entidade: String, entidadeId: Long) {
        auditLogDao.excluirPorEntidade(entidade, entidadeId)
    }

    // ========================================================================
    // Operações Reativas (Flows)
    // ========================================================================

    override fun observarTodos(): Flow<List<AuditLog>> {
        return auditLogDao.listarTodos().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun observarPorEntidade(entidade: String, entidadeId: Long): Flow<List<AuditLog>> {
        return auditLogDao.listarPorEntidade(entidade, entidadeId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun observarPorPeriodo(
        dataInicio: LocalDateTime,
        dataFim: LocalDateTime
    ): Flow<List<AuditLog>> {
        return auditLogDao.listarPorPeriodo(
            dataInicio.toString(),
            dataFim.toString()
        ).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun observarPorAcao(acao: AcaoAuditoria): Flow<List<AuditLog>> {
        return auditLogDao.listarPorAcao(acao).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun observarUltimos(limite: Int): Flow<List<AuditLog>> {
        return auditLogDao.listarUltimos(limite).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun observarUltimosPorEntidade(entidade: String, limite: Int): Flow<List<AuditLog>> {
        return auditLogDao.listarUltimosPorEntidade(entidade, limite).map { entities ->
            entities.map { it.toDomain() }
        }
    }
}
