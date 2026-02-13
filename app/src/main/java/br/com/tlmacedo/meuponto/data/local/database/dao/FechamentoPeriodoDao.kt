// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/data/local/database/dao/FechamentoPeriodoDao.kt
package br.com.tlmacedo.meuponto.data.local.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import br.com.tlmacedo.meuponto.data.local.database.entity.FechamentoPeriodoEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operações de banco de dados relacionadas aos Fechamentos de Período.
 * 
 * Gerencia o registro de fechamentos de período (semanal, mensal ou banco de horas),
 * onde o saldo é zerado e registrado para histórico.
 *
 * @author Thiago
 * @since 2.0.0
 */
@Dao
interface FechamentoPeriodoDao {

    // ========================================================================
    // Operações CRUD básicas
    // ========================================================================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(fechamento: FechamentoPeriodoEntity): Long

    @Update
    suspend fun atualizar(fechamento: FechamentoPeriodoEntity)

    @Delete
    suspend fun excluir(fechamento: FechamentoPeriodoEntity)

    @Query("DELETE FROM fechamentos_periodo WHERE id = :id")
    suspend fun excluirPorId(id: Long)

    // ========================================================================
    // Consultas por ID
    // ========================================================================

    @Query("SELECT * FROM fechamentos_periodo WHERE id = :id")
    suspend fun buscarPorId(id: Long): FechamentoPeriodoEntity?

    // ========================================================================
    // Listagens por emprego
    // ========================================================================

    @Query("SELECT * FROM fechamentos_periodo WHERE empregoId = :empregoId ORDER BY dataFimPeriodo DESC")
    fun listarPorEmprego(empregoId: Long): Flow<List<FechamentoPeriodoEntity>>

    @Query("SELECT * FROM fechamentos_periodo WHERE empregoId = :empregoId ORDER BY dataFimPeriodo DESC")
    suspend fun buscarPorEmprego(empregoId: Long): List<FechamentoPeriodoEntity>

    @Query("SELECT * FROM fechamentos_periodo WHERE empregoId = :empregoId ORDER BY dataFimPeriodo DESC LIMIT :limite")
    fun listarUltimosPorEmprego(empregoId: Long, limite: Int): Flow<List<FechamentoPeriodoEntity>>

    // ========================================================================
    // Listagens por tipo
    // ========================================================================

    @Query("SELECT * FROM fechamentos_periodo WHERE empregoId = :empregoId AND tipo = :tipo ORDER BY dataFimPeriodo DESC")
    fun listarPorTipo(empregoId: Long, tipo: String): Flow<List<FechamentoPeriodoEntity>>

    @Query("SELECT * FROM fechamentos_periodo WHERE empregoId = :empregoId AND tipo = :tipo ORDER BY dataFimPeriodo DESC")
    suspend fun buscarPorTipo(empregoId: Long, tipo: String): List<FechamentoPeriodoEntity>

    // ========================================================================
    // Consultas específicas
    // ========================================================================

    @Query("""
        SELECT * FROM fechamentos_periodo 
        WHERE empregoId = :empregoId 
        AND dataInicioPeriodo = :dataInicio 
        AND dataFimPeriodo = :dataFim
    """)
    suspend fun buscarPorPeriodo(empregoId: Long, dataInicio: String, dataFim: String): FechamentoPeriodoEntity?

    @Query("""
        SELECT * FROM fechamentos_periodo 
        WHERE empregoId = :empregoId 
        ORDER BY dataFimPeriodo DESC 
        LIMIT 1
    """)
    suspend fun buscarUltimoFechamento(empregoId: Long): FechamentoPeriodoEntity?

    @Query("""
        SELECT * FROM fechamentos_periodo 
        WHERE empregoId = :empregoId 
        ORDER BY dataFimPeriodo DESC 
        LIMIT 1
    """)
    fun observarUltimoFechamento(empregoId: Long): Flow<FechamentoPeriodoEntity?>

    @Query("""
        SELECT * FROM fechamentos_periodo 
        WHERE empregoId = :empregoId 
        AND tipo = :tipo
        ORDER BY dataFimPeriodo DESC 
        LIMIT 1
    """)
    suspend fun buscarUltimoFechamentoPorTipo(empregoId: Long, tipo: String): FechamentoPeriodoEntity?

    // ========================================================================
    // Verificações
    // ========================================================================

    @Query("""
        SELECT EXISTS(
            SELECT 1 FROM fechamentos_periodo 
            WHERE empregoId = :empregoId 
            AND (dataInicioPeriodo <= :dataFim AND dataFimPeriodo >= :dataInicio)
        )
    """)
    suspend fun existeFechamentoNoPeriodo(empregoId: Long, dataInicio: String, dataFim: String): Boolean

    @Query("SELECT EXISTS(SELECT 1 FROM fechamentos_periodo WHERE empregoId = :empregoId)")
    suspend fun existeFechamentoPorEmprego(empregoId: Long): Boolean

    // ========================================================================
    // Cálculos
    // ========================================================================

    @Query("SELECT saldoAnteriorMinutos FROM fechamentos_periodo WHERE empregoId = :empregoId ORDER BY dataFimPeriodo DESC LIMIT 1")
    suspend fun buscarUltimoSaldoAnterior(empregoId: Long): Int?

    @Query("SELECT COALESCE(SUM(saldoAnteriorMinutos), 0) FROM fechamentos_periodo WHERE empregoId = :empregoId")
    suspend fun somarSaldosAnteriores(empregoId: Long): Int

    @Query("SELECT COUNT(*) FROM fechamentos_periodo WHERE empregoId = :empregoId")
    suspend fun contarPorEmprego(empregoId: Long): Int
}
