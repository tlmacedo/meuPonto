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
    // Listagens por emprego
    // ========================================================================

    @Query("SELECT * FROM audit_log WHERE empregoId = :empregoId ORDER BY criadoEm DESC")
    fun listarPorEmprego(empregoId: Long): Flow<List<AuditLogEntity>>

    @Query("SELECT * FROM audit_log WHERE empregoId = :empregoId ORDER BY criadoEm DESC LIMIT :limite")
    fun listarUltimosPorEmprego(empregoId: Long, limite: Int): Flow<List<AuditLogEntity>>

    // ========================================================================
    // Listagens por entidade
    // ========================================================================

    @Query("SELECT * FROM audit_log WHERE entidade = :entidade AND entidadeId = :entidadeId ORDER BY criadoEm DESC")
    fun listarPorEntidade(entidade: String, entidadeId: Long): Flow<List<AuditLogEntity>>

    @Query("SELECT * FROM audit_log WHERE entidade = :entidade AND entidadeId = :entidadeId ORDER BY criadoEm DESC")
    suspend fun buscarPorEntidade(entidade: String, entidadeId: Long): List<AuditLogEntity>

    // ========================================================================
    // Listagens por período
    // ========================================================================

    @Query("""
        SELECT * FROM audit_log 
        WHERE empregoId = :empregoId 
        AND criadoEm BETWEEN :dataInicio AND :dataFim 
        ORDER BY criadoEm DESC
    """)
    fun listarPorPeriodo(empregoId: Long, dataInicio: String, dataFim: String): Flow<List<AuditLogEntity>>

    // ========================================================================
    // Limpeza de dados antigos
    // ========================================================================

    @Query("DELETE FROM audit_log WHERE criadoEm < :dataLimite")
    suspend fun excluirAnterioresA(dataLimite: String): Int

    @Query("DELETE FROM audit_log WHERE empregoId = :empregoId")
    suspend fun excluirPorEmprego(empregoId: Long)

    // ========================================================================
    // Contagens
    // ========================================================================

    @Query("SELECT COUNT(*) FROM audit_log WHERE empregoId = :empregoId")
    suspend fun contarPorEmprego(empregoId: Long): Int
}
