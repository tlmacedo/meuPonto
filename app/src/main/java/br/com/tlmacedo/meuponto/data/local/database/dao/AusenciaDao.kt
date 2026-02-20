// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/data/local/database/dao/AusenciaDao.kt
package br.com.tlmacedo.meuponto.data.local.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import br.com.tlmacedo.meuponto.data.local.database.entity.AusenciaEntity
import br.com.tlmacedo.meuponto.domain.model.ausencia.TipoAusencia
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * DAO para operações de banco de dados relacionadas a Ausências.
 *
 * @author Thiago
 * @since 4.0.0
 */
@Dao
interface AusenciaDao {

    // ========================================================================
    // Operações CRUD básicas
    // ========================================================================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(ausencia: AusenciaEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirTodas(ausencias: List<AusenciaEntity>): List<Long>

    @Update
    suspend fun atualizar(ausencia: AusenciaEntity)

    @Delete
    suspend fun excluir(ausencia: AusenciaEntity)

    @Query("DELETE FROM ausencias WHERE id = :id")
    suspend fun excluirPorId(id: Long)

    // ========================================================================
    // Consultas por ID
    // ========================================================================

    @Query("SELECT * FROM ausencias WHERE id = :id")
    suspend fun buscarPorId(id: Long): AusenciaEntity?

    @Query("SELECT * FROM ausencias WHERE id = :id")
    fun observarPorId(id: Long): Flow<AusenciaEntity?>

    // ========================================================================
    // Consultas Globais
    // ========================================================================

    @Query("""
        SELECT * FROM ausencias 
        ORDER BY dataInicio DESC
    """)
    suspend fun buscarTodas(): List<AusenciaEntity>

    @Query("""
        SELECT * FROM ausencias 
        ORDER BY dataInicio DESC
    """)
    fun observarTodas(): Flow<List<AusenciaEntity>>

    @Query("""
        SELECT * FROM ausencias 
        WHERE ativo = 1 
        ORDER BY dataInicio DESC
    """)
    suspend fun buscarTodasAtivas(): List<AusenciaEntity>

    @Query("""
        SELECT * FROM ausencias 
        WHERE ativo = 1 
        ORDER BY dataInicio DESC
    """)
    fun observarTodasAtivas(): Flow<List<AusenciaEntity>>

    // ========================================================================
    // Consultas por Emprego
    // ========================================================================

    @Query("""
        SELECT * FROM ausencias 
        WHERE empregoId = :empregoId 
        ORDER BY dataInicio DESC
    """)
    suspend fun buscarPorEmprego(empregoId: Long): List<AusenciaEntity>

    @Query("""
        SELECT * FROM ausencias 
        WHERE empregoId = :empregoId 
        ORDER BY dataInicio DESC
    """)
    fun observarPorEmprego(empregoId: Long): Flow<List<AusenciaEntity>>

    @Query("""
        SELECT * FROM ausencias 
        WHERE empregoId = :empregoId AND ativo = 1 
        ORDER BY dataInicio DESC
    """)
    suspend fun buscarAtivasPorEmprego(empregoId: Long): List<AusenciaEntity>

    @Query("""
        SELECT * FROM ausencias 
        WHERE empregoId = :empregoId AND ativo = 1 
        ORDER BY dataInicio DESC
    """)
    fun observarAtivasPorEmprego(empregoId: Long): Flow<List<AusenciaEntity>>

    // ========================================================================
    // Consultas por Data
    // ========================================================================

    /**
     * Busca ausências que ocorrem em uma data específica.
     * Uma ausência ocorre em uma data se: dataInicio <= data <= dataFim
     */
    @Query("""
        SELECT * FROM ausencias 
        WHERE empregoId = :empregoId 
        AND ativo = 1 
        AND dataInicio <= :data 
        AND dataFim >= :data
    """)
    suspend fun buscarPorData(empregoId: Long, data: LocalDate): List<AusenciaEntity>

    @Query("""
        SELECT * FROM ausencias 
        WHERE empregoId = :empregoId 
        AND ativo = 1 
        AND dataInicio <= :data 
        AND dataFim >= :data
    """)
    fun observarPorData(empregoId: Long, data: LocalDate): Flow<List<AusenciaEntity>>

    /**
     * Busca ausências que se sobrepõem com um período.
     */
    @Query("""
        SELECT * FROM ausencias 
        WHERE empregoId = :empregoId 
        AND ativo = 1 
        AND dataInicio <= :dataFim 
        AND dataFim >= :dataInicio
        ORDER BY dataInicio
    """)
    suspend fun buscarPorPeriodo(
        empregoId: Long,
        dataInicio: LocalDate,
        dataFim: LocalDate
    ): List<AusenciaEntity>

    @Query("""
        SELECT * FROM ausencias 
        WHERE empregoId = :empregoId 
        AND ativo = 1 
        AND dataInicio <= :dataFim 
        AND dataFim >= :dataInicio
        ORDER BY dataInicio
    """)
    fun observarPorPeriodo(
        empregoId: Long,
        dataInicio: LocalDate,
        dataFim: LocalDate
    ): Flow<List<AusenciaEntity>>

    // ========================================================================
    // Consultas por Tipo
    // ========================================================================

    @Query("""
        SELECT * FROM ausencias 
        WHERE empregoId = :empregoId 
        AND tipo = :tipo 
        AND ativo = 1 
        ORDER BY dataInicio DESC
    """)
    suspend fun buscarPorTipo(empregoId: Long, tipo: TipoAusencia): List<AusenciaEntity>

    @Query("""
        SELECT * FROM ausencias 
        WHERE empregoId = :empregoId 
        AND tipo = :tipo 
        AND ativo = 1 
        ORDER BY dataInicio DESC
    """)
    fun observarPorTipo(empregoId: Long, tipo: TipoAusencia): Flow<List<AusenciaEntity>>

    // ========================================================================
    // Consultas por Ano/Mês
    // ========================================================================

    /**
     * Busca ausências que ocorrem em um ano específico.
     */
    @Query("""
        SELECT * FROM ausencias 
        WHERE empregoId = :empregoId 
        AND ativo = 1 
        AND (
            (strftime('%Y', dataInicio) = :ano) 
            OR (strftime('%Y', dataFim) = :ano)
            OR (dataInicio < :anoInicio AND dataFim > :anoFim)
        )
        ORDER BY dataInicio
    """)
    suspend fun buscarPorAno(
        empregoId: Long,
        ano: String,
        anoInicio: LocalDate,
        anoFim: LocalDate
    ): List<AusenciaEntity>

    /**
     * Busca ausências que ocorrem em um mês específico.
     */
    @Query("""
        SELECT * FROM ausencias 
        WHERE empregoId = :empregoId 
        AND ativo = 1 
        AND dataInicio <= :mesUltimoDia 
        AND dataFim >= :mesPrimeiroDia
        ORDER BY dataInicio
    """)
    suspend fun buscarPorMes(
        empregoId: Long,
        mesPrimeiroDia: LocalDate,
        mesUltimoDia: LocalDate
    ): List<AusenciaEntity>

    @Query("""
        SELECT * FROM ausencias 
        WHERE empregoId = :empregoId 
        AND ativo = 1 
        AND dataInicio <= :mesUltimoDia 
        AND dataFim >= :mesPrimeiroDia
        ORDER BY dataInicio
    """)
    fun observarPorMes(
        empregoId: Long,
        mesPrimeiroDia: LocalDate,
        mesUltimoDia: LocalDate
    ): Flow<List<AusenciaEntity>>

    // ========================================================================
    // Consultas de Validação
    // ========================================================================

    /**
     * Verifica se existe ausência que se sobrepõe ao período informado.
     * Usado para validar antes de criar/editar uma ausência.
     */
    @Query("""
        SELECT COUNT(*) FROM ausencias 
        WHERE empregoId = :empregoId 
        AND ativo = 1 
        AND dataInicio <= :dataFim 
        AND dataFim >= :dataInicio
        AND id != :excluirId
    """)
    suspend fun contarSobreposicoes(
        empregoId: Long,
        dataInicio: LocalDate,
        dataFim: LocalDate,
        excluirId: Long = 0
    ): Int

    /**
     * Verifica se existe ausência em uma data específica.
     */
    @Query("""
        SELECT EXISTS(
            SELECT 1 FROM ausencias 
            WHERE empregoId = :empregoId 
            AND ativo = 1 
            AND dataInicio <= :data 
            AND dataFim >= :data
        )
    """)
    suspend fun existeAusenciaEmData(empregoId: Long, data: LocalDate): Boolean

    // ========================================================================
    // Contadores e Estatísticas
    // ========================================================================

    /**
     * Conta total de dias de ausência por tipo em um período.
     */
    @Query("""
        SELECT COUNT(DISTINCT date(d.date)) as dias
        FROM ausencias a
        JOIN (
            SELECT date(:dataInicio, '+' || seq || ' days') as date
            FROM (
                SELECT 0 as seq UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4
                UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9
                UNION SELECT 10 UNION SELECT 11 UNION SELECT 12 UNION SELECT 13 UNION SELECT 14
                UNION SELECT 15 UNION SELECT 16 UNION SELECT 17 UNION SELECT 18 UNION SELECT 19
                UNION SELECT 20 UNION SELECT 21 UNION SELECT 22 UNION SELECT 23 UNION SELECT 24
                UNION SELECT 25 UNION SELECT 26 UNION SELECT 27 UNION SELECT 28 UNION SELECT 29
                UNION SELECT 30
            )
        ) d
        WHERE a.empregoId = :empregoId
        AND a.tipo = :tipo
        AND a.ativo = 1
        AND d.date >= a.dataInicio
        AND d.date <= a.dataFim
        AND d.date >= :dataInicio
        AND d.date <= :dataFim
    """)
    suspend fun contarDiasPorTipo(
        empregoId: Long,
        tipo: TipoAusencia,
        dataInicio: LocalDate,
        dataFim: LocalDate
    ): Int

    /**
     * Conta quantidade de registros de ausência por emprego.
     */
    @Query("SELECT COUNT(*) FROM ausencias WHERE empregoId = :empregoId AND ativo = 1")
    suspend fun contarPorEmprego(empregoId: Long): Int

    // ========================================================================
    // Operações de Limpeza
    // ========================================================================

    /**
     * Desativa todas as ausências de um emprego.
     */
    @Query("UPDATE ausencias SET ativo = 0, atualizadoEm = :agora WHERE empregoId = :empregoId")
    suspend fun desativarPorEmprego(
        empregoId: Long,
        agora: LocalDateTime = LocalDateTime.now()
    )

    /**
     * Remove ausências antigas (mais de X anos).
     */
    @Query("DELETE FROM ausencias WHERE dataFim < :dataLimite AND ativo = 0")
    suspend fun limparAusenciasAntigas(dataLimite: LocalDate)
}
