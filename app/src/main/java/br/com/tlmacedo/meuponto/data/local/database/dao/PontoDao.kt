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
 * DAO para operações de banco de dados relacionadas aos Pontos.
 *
 * @author Thiago
 * @since 1.0.0
 * @updated 2.0.0 - Adicionado suporte a múltiplos empregos
 */
@Dao
interface PontoDao {

    // ========================================================================
    // Operações CRUD básicas
    // ========================================================================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(ponto: PontoEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirTodos(pontos: List<PontoEntity>): List<Long>

    @Update
    suspend fun atualizar(ponto: PontoEntity)

    @Delete
    suspend fun excluir(ponto: PontoEntity)

    @Query("DELETE FROM pontos WHERE id = :id")
    suspend fun excluirPorId(id: Long)

    // ========================================================================
    // Consultas por ID
    // ========================================================================

    @Query("SELECT * FROM pontos WHERE id = :id")
    suspend fun buscarPorId(id: Long): PontoEntity?

    @Query("SELECT * FROM pontos WHERE id = :id")
    fun observarPorId(id: Long): Flow<PontoEntity?>

    // ========================================================================
    // Listagens legadas (sem filtro de emprego - retrocompatibilidade)
    // ========================================================================

    @Query("SELECT * FROM pontos WHERE data = :data ORDER BY dataHora ASC")
    fun listarPorData(data: LocalDate): Flow<List<PontoEntity>>

    @Query("SELECT * FROM pontos ORDER BY dataHora DESC")
    fun listarTodos(): Flow<List<PontoEntity>>

    @Query("SELECT * FROM pontos WHERE data BETWEEN :dataInicio AND :dataFim ORDER BY dataHora ASC")
    fun listarPorPeriodo(dataInicio: LocalDate, dataFim: LocalDate): Flow<List<PontoEntity>>

    // ========================================================================
    // Listagens por emprego (novas)
    // ========================================================================

    @Query("SELECT * FROM pontos WHERE empregoId = :empregoId ORDER BY dataHora DESC")
    fun listarPorEmprego(empregoId: Long): Flow<List<PontoEntity>>

    @Query("SELECT * FROM pontos WHERE empregoId = :empregoId AND data = :data ORDER BY dataHora ASC")
    fun listarPorEmpregoEData(empregoId: Long, data: LocalDate): Flow<List<PontoEntity>>

    @Query("SELECT * FROM pontos WHERE empregoId = :empregoId AND data = :data ORDER BY dataHora ASC")
    suspend fun buscarPorEmpregoEData(empregoId: Long, data: LocalDate): List<PontoEntity>

    @Query("""
        SELECT * FROM pontos 
        WHERE empregoId = :empregoId 
        AND data BETWEEN :dataInicio AND :dataFim 
        ORDER BY data ASC, dataHora ASC
    """)
    fun listarPorEmpregoEPeriodo(empregoId: Long, dataInicio: LocalDate, dataFim: LocalDate): Flow<List<PontoEntity>>

    @Query("""
        SELECT * FROM pontos 
        WHERE empregoId = :empregoId 
        AND data BETWEEN :dataInicio AND :dataFim 
        ORDER BY data ASC, dataHora ASC
    """)
    suspend fun buscarPorEmpregoEPeriodo(empregoId: Long, dataInicio: LocalDate, dataFim: LocalDate): List<PontoEntity>

    // ========================================================================
    // Listagens por marcador
    // ========================================================================

    @Query("SELECT * FROM pontos WHERE marcadorId = :marcadorId ORDER BY dataHora DESC")
    fun listarPorMarcador(marcadorId: Long): Flow<List<PontoEntity>>

    // ========================================================================
    // Contagens
    // ========================================================================

    @Query("SELECT COUNT(*) FROM pontos WHERE empregoId = :empregoId")
    suspend fun contarPorEmprego(empregoId: Long): Int

    @Query("SELECT COUNT(*) FROM pontos WHERE empregoId = :empregoId AND data = :data")
    suspend fun contarPorEmpregoEData(empregoId: Long, data: LocalDate): Int

    @Query("""
        SELECT COUNT(*) FROM pontos 
        WHERE empregoId = :empregoId 
        AND data BETWEEN :dataInicio AND :dataFim
    """)
    suspend fun contarPorEmpregoEPeriodo(empregoId: Long, dataInicio: LocalDate, dataFim: LocalDate): Int

    // ========================================================================
    // Consultas auxiliares
    // ========================================================================

    @Query("SELECT DISTINCT data FROM pontos WHERE empregoId = :empregoId ORDER BY data DESC")
    fun listarDatasComRegistro(empregoId: Long): Flow<List<LocalDate>>

    @Query("SELECT * FROM pontos WHERE empregoId = :empregoId ORDER BY dataHora DESC LIMIT 1")
    suspend fun buscarUltimoPonto(empregoId: Long): PontoEntity?

    @Query("SELECT * FROM pontos WHERE empregoId = :empregoId ORDER BY dataHora DESC LIMIT 1")
    fun observarUltimoPonto(empregoId: Long): Flow<PontoEntity?>

    // ========================================================================
    // Operações de migração/atualização em lote
    // ========================================================================

    @Query("UPDATE pontos SET empregoId = :empregoId WHERE empregoId = :empregoIdAntigo")
    suspend fun migrarParaEmprego(empregoIdAntigo: Long, empregoId: Long): Int
}
