// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/data/local/database/dao/VersaoJornadaDao.kt
package br.com.tlmacedo.meuponto.data.local.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import br.com.tlmacedo.meuponto.data.local.database.entity.VersaoJornadaEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * DAO para operações de banco de dados relacionadas às Versões de Jornada.
 *
 * @author Thiago
 * @since 2.7.0
 */
@Dao
interface VersaoJornadaDao {

    // ========================================================================
    // Operações CRUD básicas
    // ========================================================================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(versao: VersaoJornadaEntity): Long

    @Update
    suspend fun atualizar(versao: VersaoJornadaEntity)

    @Delete
    suspend fun excluir(versao: VersaoJornadaEntity)

    @Query("DELETE FROM versoes_jornada WHERE id = :id")
    suspend fun excluirPorId(id: Long)

    // ========================================================================
    // Consultas por ID
    // ========================================================================

    @Query("SELECT * FROM versoes_jornada WHERE id = :id")
    suspend fun buscarPorId(id: Long): VersaoJornadaEntity?

    @Query("SELECT * FROM versoes_jornada WHERE id = :id")
    fun observarPorId(id: Long): Flow<VersaoJornadaEntity?>

    // ========================================================================
    // Consultas por Emprego
    // ========================================================================

    @Query("SELECT * FROM versoes_jornada WHERE empregoId = :empregoId ORDER BY dataInicio DESC")
    suspend fun buscarPorEmprego(empregoId: Long): List<VersaoJornadaEntity>

    @Query("SELECT * FROM versoes_jornada WHERE empregoId = :empregoId ORDER BY dataInicio DESC")
    fun observarPorEmprego(empregoId: Long): Flow<List<VersaoJornadaEntity>>

    @Query("SELECT * FROM versoes_jornada WHERE empregoId = :empregoId AND vigente = 1 LIMIT 1")
    suspend fun buscarVigente(empregoId: Long): VersaoJornadaEntity?

    @Query("SELECT * FROM versoes_jornada WHERE empregoId = :empregoId AND vigente = 1 LIMIT 1")
    fun observarVigente(empregoId: Long): Flow<VersaoJornadaEntity?>

    // ========================================================================
    // Consultas por Data
    // ========================================================================

    /**
     * Busca a versão de jornada vigente para uma data específica.
     * A data deve estar entre dataInicio e dataFim (ou dataFim nulo).
     */
    @Query(
        """
    SELECT * FROM versoes_jornada
    WHERE empregoId = :empregoId
      AND dataInicio <= :data
      AND (dataFim IS NULL OR dataFim >= :data)
    ORDER BY dataInicio DESC
    LIMIT 1
    """
    )
    suspend fun buscarPorEmpregoEData(
        empregoId: Long,
        data: LocalDate
    ): VersaoJornadaEntity?
    @Query(
        """
        SELECT * FROM versoes_jornada 
        WHERE empregoId = :empregoId 
        AND dataInicio <= :data 
        AND (dataFim IS NULL OR dataFim >= :data)
        ORDER BY dataInicio DESC
        LIMIT 1
    """
    )
    fun observarPorEmpregoEData(empregoId: Long, data: LocalDate): Flow<VersaoJornadaEntity?>

    // ========================================================================
    // Consultas para Validação
    // ========================================================================

    /**
     * Verifica se existe sobreposição de períodos para uma nova versão.
     */
    @Query(
        """
        SELECT COUNT(*) FROM versoes_jornada 
        WHERE empregoId = :empregoId 
        AND id != :excluirId
        AND (
            (dataInicio <= :dataFim OR :dataFim IS NULL)
            AND (dataFim >= :dataInicio OR dataFim IS NULL)
        )
    """
    )
    suspend fun contarSobreposicoes(
        empregoId: Long,
        dataInicio: LocalDate,
        dataFim: LocalDate?,
        excluirId: Long = 0
    ): Int

    /**
     * Busca a versão anterior a uma data específica.
     */
    @Query(
        """
        SELECT * FROM versoes_jornada 
        WHERE empregoId = :empregoId 
        AND dataInicio < :data
        ORDER BY dataInicio DESC
        LIMIT 1
    """
    )
    suspend fun buscarVersaoAnterior(empregoId: Long, data: LocalDate): VersaoJornadaEntity?

    /**
     * Busca a próxima versão após uma data específica.
     */
    @Query(
        """
        SELECT * FROM versoes_jornada 
        WHERE empregoId = :empregoId 
        AND dataInicio > :data
        ORDER BY dataInicio ASC
        LIMIT 1
    """
    )
    suspend fun buscarProximaVersao(empregoId: Long, data: LocalDate): VersaoJornadaEntity?

    // ========================================================================
    // Operações de Atualização em Lote
    // ========================================================================

    /**
     * Define dataFim em uma versão específica.
     */
    @Query("UPDATE versoes_jornada SET dataFim = :dataFim, vigente = 0, atualizadoEm = :agora WHERE id = :id")
    suspend fun definirDataFim(
        id: Long,
        dataFim: LocalDate,
        agora: java.time.LocalDateTime = java.time.LocalDateTime.now()
    )

    /**
     * Remove flag vigente de todas as versões de um emprego.
     */
    @Query("UPDATE versoes_jornada SET vigente = 0 WHERE empregoId = :empregoId")
    suspend fun removerVigenteDeTodas(empregoId: Long)

    /**
     * Define uma versão como vigente.
     */
    @Query("UPDATE versoes_jornada SET vigente = 1 WHERE id = :id")
    suspend fun definirComoVigente(id: Long)

    // ========================================================================
    // Contagens
    // ========================================================================

    @Query("SELECT COUNT(*) FROM versoes_jornada WHERE empregoId = :empregoId")
    suspend fun contarPorEmprego(empregoId: Long): Int

    @Query("SELECT MAX(numeroVersao) FROM versoes_jornada WHERE empregoId = :empregoId")
    suspend fun buscarMaiorNumeroVersao(empregoId: Long): Int?
}
