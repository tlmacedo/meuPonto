// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/data/local/database/dao/FechamentoPeriodoDao.kt
package br.com.tlmacedo.meuponto.data.local.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import br.com.tlmacedo.meuponto.data.local.database.entity.FechamentoPeriodoEntity
import br.com.tlmacedo.meuponto.domain.model.TipoFechamento
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * DAO para operações com fechamentos de período.
 *
 * @author Thiago
 * @since 2.0.0
 * @updated 6.4.0 - Novo método para buscar fechamento até uma data específica
 */
@Dao
interface FechamentoPeriodoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(fechamento: FechamentoPeriodoEntity): Long

    @Update
    suspend fun update(fechamento: FechamentoPeriodoEntity)

    @Delete
    suspend fun delete(fechamento: FechamentoPeriodoEntity)

    @Query("SELECT * FROM fechamentos_periodo WHERE id = :id")
    suspend fun getById(id: Long): FechamentoPeriodoEntity?

    @Query("SELECT * FROM fechamentos_periodo WHERE emprego_id = :empregoId ORDER BY data_fechamento DESC")
    fun observeByEmpregoId(empregoId: Long): Flow<List<FechamentoPeriodoEntity>>

    @Query("SELECT * FROM fechamentos_periodo WHERE emprego_id = :empregoId ORDER BY data_fechamento DESC")
    suspend fun getByEmpregoId(empregoId: Long): List<FechamentoPeriodoEntity>

    @Query("""
        SELECT * FROM fechamentos_periodo 
        WHERE emprego_id = :empregoId 
        AND tipo = :tipo
        ORDER BY data_fechamento DESC
    """)
    suspend fun getByEmpregoIdAndTipo(empregoId: Long, tipo: TipoFechamento): List<FechamentoPeriodoEntity>

    @Query("""
        SELECT * FROM fechamentos_periodo 
        WHERE emprego_id = :empregoId 
        AND tipo IN ('BANCO_HORAS', 'CICLO_BANCO_AUTOMATICO')
        ORDER BY data_fechamento DESC
    """)
    suspend fun getFechamentosBancoHoras(empregoId: Long): List<FechamentoPeriodoEntity>

    @Query("""
        SELECT * FROM fechamentos_periodo 
        WHERE emprego_id = :empregoId 
        AND tipo IN ('BANCO_HORAS', 'CICLO_BANCO_AUTOMATICO')
        ORDER BY data_fechamento DESC
    """)
    fun observeFechamentosBancoHoras(empregoId: Long): Flow<List<FechamentoPeriodoEntity>>

    @Query("""
        SELECT * FROM fechamentos_periodo 
        WHERE emprego_id = :empregoId 
        AND :data >= data_inicio_periodo 
        AND :data <= data_fim_periodo
        LIMIT 1
    """)
    suspend fun buscarPorData(empregoId: Long, data: LocalDate): FechamentoPeriodoEntity?

    @Query("""
        SELECT * FROM fechamentos_periodo 
        WHERE emprego_id = :empregoId 
        AND data_fechamento >= :dataInicio 
        AND data_fechamento <= :dataFim
        ORDER BY data_fechamento DESC
    """)
    suspend fun getByPeriodo(
        empregoId: Long,
        dataInicio: LocalDate,
        dataFim: LocalDate
    ): List<FechamentoPeriodoEntity>

    @Query("""
        SELECT * FROM fechamentos_periodo 
        WHERE emprego_id = :empregoId 
        ORDER BY data_fechamento DESC
        LIMIT 1
    """)
    suspend fun getUltimoFechamento(empregoId: Long): FechamentoPeriodoEntity?

    @Query("""
        SELECT * FROM fechamentos_periodo 
        WHERE emprego_id = :empregoId 
        AND tipo IN ('BANCO_HORAS', 'CICLO_BANCO_AUTOMATICO')
        ORDER BY data_fechamento DESC
        LIMIT 1
    """)
    suspend fun getUltimoFechamentoBanco(empregoId: Long): FechamentoPeriodoEntity?

    /**
     * Busca o último fechamento de banco de horas que TERMINOU ANTES de uma data específica.
     *
     * Usado para calcular o banco de horas histórico. Por exemplo:
     * - Se consultamos 05/02/2026 e existe fechamento com data_fim_periodo = 10/02/2026
     * - Esse fechamento NÃO será retornado (porque 10/02 >= 05/02)
     * - Apenas fechamentos cujo ciclo terminou ANTES da data consultada são considerados
     */
    @Query("""
        SELECT * FROM fechamentos_periodo 
        WHERE emprego_id = :empregoId 
        AND tipo IN ('BANCO_HORAS', 'CICLO_BANCO_AUTOMATICO')
        AND data_fim_periodo < :ateData
        ORDER BY data_fim_periodo DESC
        LIMIT 1
    """)
    suspend fun getUltimoFechamentoBancoAteData(
        empregoId: Long,
        ateData: LocalDate
    ): FechamentoPeriodoEntity?

    @Query("DELETE FROM fechamentos_periodo WHERE emprego_id = :empregoId")
    suspend fun deleteByEmpregoId(empregoId: Long)

    @Query("SELECT COUNT(*) FROM fechamentos_periodo WHERE emprego_id = :empregoId")
    suspend fun countByEmpregoId(empregoId: Long): Int
}
