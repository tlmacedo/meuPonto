// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/data/local/database/dao/AuditLogDao.kt
package br.com.tlmacedo.meuponto.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import br.com.tlmacedo.meuponto.data.local.database.entity.AuditLogEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operações de banco de dados relacionadas ao Log de Auditoria.
 * 
 * Gerencia registros de auditoria que rastreiam alterações em todas as entidades
 * do sistema, permitindo histórico completo e possível reversão de ações.
 *
 * @author Thiago
 * @since 2.0.0
 */
@Dao
interface AuditLogDao {

    // ========================================================================
    // Operações de inserção
    // ========================================================================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(log: AuditLogEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirTodos(logs: List<AuditLogEntity>): List<Long>

    // ========================================================================
    // Listagens por entidade
    // ========================================================================

    @Query("SELECT * FROM audit_logs WHERE entidade = :entidade AND entidadeId = :entidadeId ORDER BY criadoEm DESC")
    fun listarPorEntidade(entidade: String, entidadeId: Long): Flow<List<AuditLogEntity>>

    @Query("SELECT * FROM audit_logs WHERE entidade = :entidade AND entidadeId = :entidadeId ORDER BY criadoEm DESC")
    suspend fun buscarPorEntidade(entidade: String, entidadeId: Long): List<AuditLogEntity>

    @Query("SELECT * FROM audit_logs WHERE entidade = :entidade ORDER BY criadoEm DESC LIMIT :limite")
    fun listarUltimosPorEntidade(entidade: String, limite: Int): Flow<List<AuditLogEntity>>

    @Query("SELECT * FROM audit_logs ORDER BY criadoEm DESC LIMIT :limite")
    fun listarUltimos(limite: Int): Flow<List<AuditLogEntity>>

    // ========================================================================
    // Listagens por período
    // ========================================================================

    @Query("""
        SELECT * FROM audit_logs 
        WHERE criadoEm BETWEEN :dataInicio AND :dataFim 
        ORDER BY criadoEm DESC
    """)
    fun listarPorPeriodo(dataInicio: String, dataFim: String): Flow<List<AuditLogEntity>>

    @Query("""
        SELECT * FROM audit_logs 
        WHERE entidade = :entidade
        AND criadoEm BETWEEN :dataInicio AND :dataFim 
        ORDER BY criadoEm DESC
    """)
    fun listarPorEntidadeEPeriodo(entidade: String, dataInicio: String, dataFim: String): Flow<List<AuditLogEntity>>

    // ========================================================================
    // Listagens por ação
    // ========================================================================

    @Query("SELECT * FROM audit_logs WHERE acao = :acao ORDER BY criadoEm DESC")
    fun listarPorAcao(acao: String): Flow<List<AuditLogEntity>>

    @Query("SELECT * FROM audit_logs WHERE entidade = :entidade AND acao = :acao ORDER BY criadoEm DESC")
    fun listarPorEntidadeEAcao(entidade: String, acao: String): Flow<List<AuditLogEntity>>

    // ========================================================================
    // Limpeza de dados antigos
    // ========================================================================

    @Query("DELETE FROM audit_logs WHERE criadoEm < :dataLimite")
    suspend fun excluirAnterioresA(dataLimite: String): Int

    @Query("DELETE FROM audit_logs WHERE entidade = :entidade AND entidadeId = :entidadeId")
    suspend fun excluirPorEntidade(entidade: String, entidadeId: Long)

    // ========================================================================
    // Contagens
    // ========================================================================

    @Query("SELECT COUNT(*) FROM audit_logs")
    suspend fun contarTodos(): Int

    @Query("SELECT COUNT(*) FROM audit_logs WHERE entidade = :entidade")
    suspend fun contarPorEntidade(entidade: String): Int

    @Query("SELECT COUNT(*) FROM audit_logs WHERE entidade = :entidade AND entidadeId = :entidadeId")
    suspend fun contarPorEntidadeEId(entidade: String, entidadeId: Long): Int
}
