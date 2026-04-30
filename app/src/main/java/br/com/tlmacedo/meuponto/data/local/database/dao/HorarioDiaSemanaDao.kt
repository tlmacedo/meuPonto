// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/data/local/database/dao/HorarioDiaSemanaDao.kt
package br.com.tlmacedo.meuponto.data.local.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import br.com.tlmacedo.meuponto.data.local.database.entity.HorarioDiaSemanaEntity
import br.com.tlmacedo.meuponto.domain.model.DiaSemana
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operações de banco de dados relacionadas aos Horários por Dia da Semana.
 *
 * @author Thiago
 * @since 2.0.0
 * @updated 2.7.0 - Padronizado para usar DiaSemana (enum) em todas as queries
 */
@Dao
interface HorarioDiaSemanaDao {

    // ========================================================================
    // Operações CRUD básicas
    // ========================================================================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(horario: HorarioDiaSemanaEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirTodos(horarios: List<HorarioDiaSemanaEntity>): List<Long>

    @Update
    suspend fun atualizar(horario: HorarioDiaSemanaEntity)

    @Delete
    suspend fun excluir(horario: HorarioDiaSemanaEntity)

    // ========================================================================
    // Consultas por emprego
    // ========================================================================

    @Query("SELECT * FROM horarios_dia_semana WHERE empregoId = :empregoId ORDER BY diaSemana")
    fun listarPorEmprego(empregoId: Long): Flow<List<HorarioDiaSemanaEntity>>

    @Query("SELECT * FROM horarios_dia_semana WHERE empregoId = :empregoId")
    suspend fun buscarPorEmprego(empregoId: Long): List<HorarioDiaSemanaEntity>

    @Query("SELECT * FROM horarios_dia_semana WHERE empregoId = :empregoId AND diaSemana = :diaSemana")
    suspend fun buscarPorEmpregoEDia(empregoId: Long, diaSemana: DiaSemana): HorarioDiaSemanaEntity?

    @Query("SELECT * FROM horarios_dia_semana WHERE id = :id")
    suspend fun buscarPorId(id: Long): HorarioDiaSemanaEntity?

    // ========================================================================
    // Consultas por Versão de Jornada
    // ========================================================================

    @Query("SELECT * FROM horarios_dia_semana WHERE versaoJornadaId = :versaoJornadaId ORDER BY diaSemana")
    suspend fun buscarPorVersaoJornada(versaoJornadaId: Long): List<HorarioDiaSemanaEntity>

    @Query("SELECT * FROM horarios_dia_semana WHERE versaoJornadaId = :versaoJornadaId ORDER BY diaSemana")
    fun observarPorVersaoJornada(versaoJornadaId: Long): Flow<List<HorarioDiaSemanaEntity>>

    @Query(
        """
    SELECT * FROM horarios_dia_semana
    WHERE versaoJornadaId = :versaoId
      AND diaSemana = :diaSemana
    LIMIT 1
    """
    )
    suspend fun buscarPorVersaoEDia(
        versaoId: Long,
        diaSemana: DiaSemana
    ): HorarioDiaSemanaEntity?

    // ========================================================================
    // Consultas de dias ativos
    // ========================================================================

    @Query("SELECT * FROM horarios_dia_semana WHERE empregoId = :empregoId AND ativo = 1 ORDER BY diaSemana")
    fun listarDiasAtivos(empregoId: Long): Flow<List<HorarioDiaSemanaEntity>>

    @Query("SELECT * FROM horarios_dia_semana WHERE empregoId = :empregoId AND ativo = 1 ORDER BY diaSemana")
    suspend fun buscarDiasAtivos(empregoId: Long): List<HorarioDiaSemanaEntity>

    // ========================================================================
    // Operações em lote
    // ========================================================================

    @Query("DELETE FROM horarios_dia_semana WHERE empregoId = :empregoId")
    suspend fun excluirPorEmprego(empregoId: Long)

    @Query("DELETE FROM horarios_dia_semana WHERE versaoJornadaId = :versaoJornadaId")
    suspend fun excluirPorVersaoJornada(versaoJornadaId: Long)

    // ========================================================================
    // Consultas auxiliares e cálculos
    // ========================================================================

    @Query("SELECT COUNT(*) FROM horarios_dia_semana WHERE empregoId = :empregoId AND ativo = 1")
    suspend fun contarDiasAtivos(empregoId: Long): Int

    @Query("SELECT COALESCE(SUM(cargaHorariaMinutos), 0) FROM horarios_dia_semana WHERE empregoId = :empregoId AND ativo = 1")
    suspend fun somarCargaHorariaSemanal(empregoId: Long): Int

    @Query("SELECT cargaHorariaMinutos FROM horarios_dia_semana WHERE empregoId = :empregoId AND diaSemana = :diaSemana AND ativo = 1")
    suspend fun buscarCargaHorariaDia(empregoId: Long, diaSemana: DiaSemana): Int?

    @Query("SELECT ativo FROM horarios_dia_semana WHERE empregoId = :empregoId AND diaSemana = :diaSemana")
    suspend fun isDiaAtivo(empregoId: Long, diaSemana: DiaSemana): Boolean?
}
