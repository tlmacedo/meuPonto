// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/data/local/database/dao/AuditLogDao.kt
package br.com.tlmacedo.meuponto.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import br.com.tlmacedo.meuponto.data.local.database.entity.AuditLogEntity
import br.com.tlmacedo.meuponto.domain.model.AcaoAuditoria
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operações de logs de auditoria no banco de dados.
 *
 * @author Thiago
 * @since 2.0.0
 * @updated 11.0.0 - Adicionado listarTodos()
 */
@Dao
interface AuditLogDao {

    // ========================================================================
    // Operações de Escrita
    // ========================================================================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(log: AuditLogEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirTodos(logs: List<AuditLogEntity>): List<Long>

    // ========================================================================
    // Operações de Leitura Reativas (Flow)
    // ========================================================================

    @Query("SELECT * FROM audit_logs ORDER BY criado_em DESC")
    fun listarTodos(): Flow<List<AuditLogEntity>>

    @Query("SELECT * FROM audit_logs WHERE entidade = :entidade AND entidade_id = :entidadeId ORDER BY criado_em DESC")
    fun listarPorEntidade(entidade: String, entidadeId: Long): Flow<List<AuditLogEntity>>

    @Query("SELECT * FROM audit_logs WHERE entidade = :entidade ORDER BY criado_em DESC LIMIT :limite")
    fun listarUltimosPorEntidade(entidade: String, limite: Int): Flow<List<AuditLogEntity>>

    @Query("SELECT * FROM audit_logs ORDER BY criado_em DESC LIMIT :limite")
    fun listarUltimos(limite: Int): Flow<List<AuditLogEntity>>

    @Query("SELECT * FROM audit_logs WHERE criado_em BETWEEN :dataInicio AND :dataFim ORDER BY criado_em DESC")
    fun listarPorPeriodo(dataInicio: String, dataFim: String): Flow<List<AuditLogEntity>>

    @Query("SELECT * FROM audit_logs WHERE entidade = :entidade AND criado_em BETWEEN :dataInicio AND :dataFim ORDER BY criado_em DESC")
    fun listarPorEntidadeEPeriodo(
        entidade: String,
        dataInicio: String,
        dataFim: String
    ): Flow<List<AuditLogEntity>>

    @Query("SELECT * FROM audit_logs WHERE acao = :acao ORDER BY criado_em DESC")
    fun listarPorAcao(acao: AcaoAuditoria): Flow<List<AuditLogEntity>>

    @Query("SELECT * FROM audit_logs WHERE entidade = :entidade AND acao = :acao ORDER BY criado_em DESC")
    fun listarPorEntidadeEAcao(entidade: String, acao: AcaoAuditoria): Flow<List<AuditLogEntity>>

    // ========================================================================
    // Operações de Leitura Suspensas
    // ========================================================================

    @Query("SELECT * FROM audit_logs WHERE entidade = :entidade AND entidade_id = :entidadeId ORDER BY criado_em DESC")
    suspend fun buscarPorEntidade(entidade: String, entidadeId: Long): List<AuditLogEntity>

    // ========================================================================
    // Operações de Contagem
    // ========================================================================

    @Query("SELECT COUNT(*) FROM audit_logs")
    suspend fun contarTodos(): Int

    @Query("SELECT COUNT(*) FROM audit_logs WHERE entidade = :entidade")
    suspend fun contarPorEntidade(entidade: String): Int

    @Query("SELECT COUNT(*) FROM audit_logs WHERE entidade = :entidade AND entidade_id = :entidadeId")
    suspend fun contarPorEntidadeEId(entidade: String, entidadeId: Long): Int

    // ========================================================================
    // Operações de Exclusão
    // ========================================================================

    @Query("DELETE FROM audit_logs WHERE criado_em < :dataLimite")
    suspend fun excluirAnterioresA(dataLimite: String): Int

    @Query("DELETE FROM audit_logs WHERE entidade = :entidade AND entidade_id = :entidadeId")
    suspend fun excluirPorEntidade(entidade: String, entidadeId: Long)
}
