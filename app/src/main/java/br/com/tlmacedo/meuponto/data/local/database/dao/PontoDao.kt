// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/data/local/database/dao/PontoDao.kt
package br.com.tlmacedo.meuponto.data.local.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import br.com.tlmacedo.meuponto.data.local.database.entity.PontoEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * DAO para operações com a tabela de Pontos.
 *
 * @author Thiago
 * @since 1.0.0
 * @updated 11.0.0 - Adicionado suporte a soft delete e métodos completos
 */
@Dao
interface PontoDao {

    // === Operações básicas ===

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(ponto: PontoEntity): Long

    @Update
    suspend fun atualizar(ponto: PontoEntity)

    @Delete
    suspend fun excluir(ponto: PontoEntity)

    // === Consultas por ID ===

    @Query("SELECT * FROM pontos WHERE id = :id AND is_deleted = 0")
    suspend fun buscarPorId(id: Long): PontoEntity?

    @Query("SELECT * FROM pontos WHERE id = :id AND is_deleted = 0")
    fun observarPorId(id: Long): Flow<PontoEntity?>

    // === Consultas gerais ===

    @Query("SELECT * FROM pontos WHERE is_deleted = 0 ORDER BY data DESC, hora DESC")
    fun listarTodos(): Flow<List<PontoEntity>>

    @Query("SELECT * FROM pontos WHERE is_deleted = 0 ORDER BY data DESC, hora DESC")
    fun observarTodos(): Flow<List<PontoEntity>>

    // === Consultas por Emprego ===

    @Query("SELECT * FROM pontos WHERE empregoId = :empregoId AND is_deleted = 0 ORDER BY data DESC, hora DESC")
    fun listarPorEmprego(empregoId: Long): Flow<List<PontoEntity>>

    @Query("SELECT * FROM pontos WHERE empregoId = :empregoId AND is_deleted = 0 ORDER BY data DESC, hora DESC")
    fun observarPorEmprego(empregoId: Long): Flow<List<PontoEntity>>

    @Query("SELECT COUNT(*) FROM pontos WHERE empregoId = :empregoId AND is_deleted = 0")
    suspend fun contarPorEmprego(empregoId: Long): Int

    @Query("SELECT MIN(data) FROM pontos WHERE empregoId = :empregoId AND is_deleted = 0")
    suspend fun buscarPrimeiraData(empregoId: Long): LocalDate?

    // === Consultas por Data ===

    @Query("SELECT * FROM pontos WHERE data = :data AND is_deleted = 0 ORDER BY hora ASC")
    fun listarPorData(data: LocalDate): Flow<List<PontoEntity>>

    @Query("SELECT * FROM pontos WHERE empregoId = :empregoId AND data = :data AND is_deleted = 0 ORDER BY hora ASC")
    suspend fun buscarPorEmpregoEData(empregoId: Long, data: LocalDate): List<PontoEntity>

    @Query("SELECT * FROM pontos WHERE empregoId = :empregoId AND data = :data AND is_deleted = 0 ORDER BY hora ASC")
    fun observarPorEmpregoEData(empregoId: Long, data: LocalDate): Flow<List<PontoEntity>>

    // === Consultas por Período ===

    @Query("""
        SELECT * FROM pontos 
        WHERE data BETWEEN :dataInicio AND :dataFim 
        AND is_deleted = 0 
        ORDER BY data DESC, hora DESC
    """)
    fun listarPorPeriodo(dataInicio: LocalDate, dataFim: LocalDate): Flow<List<PontoEntity>>

    @Query("""
        SELECT * FROM pontos 
        WHERE empregoId = :empregoId 
        AND data BETWEEN :dataInicio AND :dataFim 
        AND is_deleted = 0 
        ORDER BY data DESC, hora DESC
    """)
    fun listarPorEmpregoEPeriodo(
        empregoId: Long,
        dataInicio: LocalDate,
        dataFim: LocalDate
    ): Flow<List<PontoEntity>>

    @Query("""
        SELECT * FROM pontos 
        WHERE empregoId = :empregoId 
        AND data BETWEEN :dataInicio AND :dataFim 
        AND is_deleted = 0 
        ORDER BY data ASC, hora ASC
    """)
    suspend fun buscarPorEmpregoEPeriodo(
        empregoId: Long,
        dataInicio: LocalDate,
        dataFim: LocalDate
    ): List<PontoEntity>

    @Query("""
        SELECT * FROM pontos 
        WHERE empregoId = :empregoId 
        AND data BETWEEN :dataInicio AND :dataFim 
        AND is_deleted = 0 
        ORDER BY data ASC, hora ASC
    """)
    fun observarPorEmpregoEPeriodo(
        empregoId: Long,
        dataInicio: LocalDate,
        dataFim: LocalDate
    ): Flow<List<PontoEntity>>

    // === Atualização de foto ===

    @Query("UPDATE pontos SET fotoComprovantePath = :fotoPath, updated_at = :updatedAt WHERE id = :pontoId")
    suspend fun atualizarFotoComprovante(pontoId: Long, fotoPath: String?, updatedAt: Long = System.currentTimeMillis())

    // === Soft Delete e Lixeira ===

    @Query("SELECT * FROM pontos WHERE id = :id")
    suspend fun buscarPorIdIncluindoDeletados(id: Long): PontoEntity?

    @Query("SELECT * FROM pontos WHERE is_deleted = 1 ORDER BY deleted_at DESC")
    suspend fun listarDeletados(): List<PontoEntity>

    @Query("SELECT * FROM pontos WHERE is_deleted = 1 ORDER BY deleted_at DESC")
    fun observarDeletados(): Flow<List<PontoEntity>>

    @Query("UPDATE pontos SET is_deleted = 1, deleted_at = :deletedAt, updated_at = :deletedAt WHERE id = :pontoId")
    suspend fun softDelete(pontoId: Long, deletedAt: Long)

    @Query("DELETE FROM pontos WHERE id = :pontoId")
    suspend fun excluirPermanente(pontoId: Long)

    @Query("SELECT COUNT(*) FROM pontos WHERE is_deleted = 1")
    suspend fun contarDeletados(): Int

    @Query("UPDATE pontos SET is_deleted = 0, deleted_at = NULL, updated_at = :updatedAt WHERE id = :pontoId")
    suspend fun restaurar(pontoId: Long, updatedAt: Long)

    @Query("DELETE FROM pontos WHERE data < :data")
    suspend fun excluirPontosAnterioresA(data: LocalDate): Int
}
