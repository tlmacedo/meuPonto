// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/data/local/database/dao/HorarioDiaSemanaDao.kt
package br.com.tlmacedo.meuponto.data.local.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import br.com.tlmacedo.meuponto.data.local.database.entity.HorarioDiaSemanaEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operações de banco de dados relacionadas aos Horários por Dia da Semana.
 *
 * @author Thiago
 * @since 2.0.0
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
    // Consultas por configuração
    // ========================================================================

    @Query("SELECT * FROM horarios_dia_semana WHERE configuracaoId = :configuracaoId ORDER BY diaSemana")
    fun listarPorConfiguracao(configuracaoId: Long): Flow<List<HorarioDiaSemanaEntity>>

    @Query("SELECT * FROM horarios_dia_semana WHERE configuracaoId = :configuracaoId ORDER BY diaSemana")
    suspend fun buscarPorConfiguracao(configuracaoId: Long): List<HorarioDiaSemanaEntity>

    @Query("SELECT * FROM horarios_dia_semana WHERE configuracaoId = :configuracaoId AND diaSemana = :diaSemana")
    suspend fun buscarPorConfiguracaoEDia(configuracaoId: Long, diaSemana: String): HorarioDiaSemanaEntity?

    @Query("SELECT * FROM horarios_dia_semana WHERE id = :id")
    suspend fun buscarPorId(id: Long): HorarioDiaSemanaEntity?

    // ========================================================================
    // Operações em lote
    // ========================================================================

    @Query("DELETE FROM horarios_dia_semana WHERE configuracaoId = :configuracaoId")
    suspend fun excluirPorConfiguracao(configuracaoId: Long)

    // ========================================================================
    // Consultas auxiliares
    // ========================================================================

    @Query("SELECT COUNT(*) FROM horarios_dia_semana WHERE configuracaoId = :configuracaoId AND isDiaUtil = 1")
    suspend fun contarDiasUteis(configuracaoId: Long): Int
}
