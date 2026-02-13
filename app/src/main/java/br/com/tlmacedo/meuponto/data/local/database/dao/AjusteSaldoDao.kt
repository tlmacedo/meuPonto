// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/data/local/database/dao/AjusteSaldoDao.kt
package br.com.tlmacedo.meuponto.data.local.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import br.com.tlmacedo.meuponto.data.local.database.entity.AjusteSaldoEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operações de banco de dados relacionadas aos Ajustes de Saldo.
 * 
 * Gerencia ajustes manuais no banco de horas, permitindo adicionar ou
 * subtrair minutos do saldo com justificativa obrigatória para auditoria.
 *
 * @author Thiago
 * @since 2.0.0
 */
@Dao
interface AjusteSaldoDao {

    // ========================================================================
    // Operações CRUD básicas
    // ========================================================================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(ajuste: AjusteSaldoEntity): Long

    @Update
    suspend fun atualizar(ajuste: AjusteSaldoEntity)

    @Delete
    suspend fun excluir(ajuste: AjusteSaldoEntity)

    @Query("DELETE FROM ajustes_saldo WHERE id = :id")
    suspend fun excluirPorId(id: Long)

    // ========================================================================
    // Consultas por ID
    // ========================================================================

    @Query("SELECT * FROM ajustes_saldo WHERE id = :id")
    suspend fun buscarPorId(id: Long): AjusteSaldoEntity?

    // ========================================================================
    // Listagens por emprego
    // ========================================================================

    @Query("SELECT * FROM ajustes_saldo WHERE empregoId = :empregoId ORDER BY data DESC")
    fun listarPorEmprego(empregoId: Long): Flow<List<AjusteSaldoEntity>>

    @Query("SELECT * FROM ajustes_saldo WHERE empregoId = :empregoId ORDER BY data DESC")
    suspend fun buscarPorEmprego(empregoId: Long): List<AjusteSaldoEntity>

    @Query("SELECT * FROM ajustes_saldo WHERE empregoId = :empregoId ORDER BY data DESC LIMIT :limite")
    fun listarUltimosPorEmprego(empregoId: Long, limite: Int): Flow<List<AjusteSaldoEntity>>

    // ========================================================================
    // Listagens por período
    // ========================================================================

    @Query("""
        SELECT * FROM ajustes_saldo 
        WHERE empregoId = :empregoId 
        AND data BETWEEN :dataInicio AND :dataFim 
        ORDER BY data ASC
    """)
    fun listarPorPeriodo(empregoId: Long, dataInicio: String, dataFim: String): Flow<List<AjusteSaldoEntity>>

    @Query("""
        SELECT * FROM ajustes_saldo 
        WHERE empregoId = :empregoId 
        AND data BETWEEN :dataInicio AND :dataFim 
        ORDER BY data ASC
    """)
    suspend fun buscarPorPeriodo(empregoId: Long, dataInicio: String, dataFim: String): List<AjusteSaldoEntity>

    // ========================================================================
    // Consultas por data específica
    // ========================================================================

    @Query("SELECT * FROM ajustes_saldo WHERE empregoId = :empregoId AND data = :data")
    suspend fun buscarPorData(empregoId: Long, data: String): List<AjusteSaldoEntity>

    // ========================================================================
    // Cálculos
    // ========================================================================

    @Query("SELECT COALESCE(SUM(minutos), 0) FROM ajustes_saldo WHERE empregoId = :empregoId")
    suspend fun somarTotalPorEmprego(empregoId: Long): Int

    @Query("""
        SELECT COALESCE(SUM(minutos), 0) FROM ajustes_saldo 
        WHERE empregoId = :empregoId 
        AND data BETWEEN :dataInicio AND :dataFim
    """)
    suspend fun somarPorPeriodo(empregoId: Long, dataInicio: String, dataFim: String): Int

    @Query("SELECT COUNT(*) FROM ajustes_saldo WHERE empregoId = :empregoId")
    suspend fun contarPorEmprego(empregoId: Long): Int
}
