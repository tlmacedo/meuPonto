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

    @Query("SELECT * FROM fechamentos_periodo WHERE empregoId = :empregoId ORDER BY dataFim DESC")
    fun listarPorEmprego(empregoId: Long): Flow<List<FechamentoPeriodoEntity>>

    @Query("SELECT * FROM fechamentos_periodo WHERE empregoId = :empregoId ORDER BY dataFim DESC")
    suspend fun buscarPorEmprego(empregoId: Long): List<FechamentoPeriodoEntity>

    // ========================================================================
    // Consultas específicas
    // ========================================================================

    @Query("""
        SELECT * FROM fechamentos_periodo 
        WHERE empregoId = :empregoId 
        AND dataInicio = :dataInicio 
        AND dataFim = :dataFim
    """)
    suspend fun buscarPorPeriodo(empregoId: Long, dataInicio: String, dataFim: String): FechamentoPeriodoEntity?

    @Query("""
        SELECT * FROM fechamentos_periodo 
        WHERE empregoId = :empregoId 
        ORDER BY dataFim DESC 
        LIMIT 1
    """)
    suspend fun buscarUltimoFechamento(empregoId: Long): FechamentoPeriodoEntity?

    @Query("""
        SELECT * FROM fechamentos_periodo 
        WHERE empregoId = :empregoId 
        ORDER BY dataFim DESC 
        LIMIT 1
    """)
    fun observarUltimoFechamento(empregoId: Long): Flow<FechamentoPeriodoEntity?>

    // ========================================================================
    // Verificações
    // ========================================================================

    @Query("""
        SELECT EXISTS(
            SELECT 1 FROM fechamentos_periodo 
            WHERE empregoId = :empregoId 
            AND (
                (dataInicio <= :dataFim AND dataFim >= :dataInicio)
            )
        )
    """)
    suspend fun existeFechamentoNoPeriodo(empregoId: Long, dataInicio: String, dataFim: String): Boolean

    // ========================================================================
    // Cálculos
    // ========================================================================

    @Query("SELECT saldoAcumulado FROM fechamentos_periodo WHERE empregoId = :empregoId ORDER BY dataFim DESC LIMIT 1")
    suspend fun buscarSaldoAcumuladoAtual(empregoId: Long): Int?
}
