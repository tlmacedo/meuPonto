// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/data/local/database/dao/FotoComprovanteDao.kt
package br.com.tlmacedo.meuponto.data.local.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import br.com.tlmacedo.meuponto.data.local.database.entity.FotoComprovanteEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * DAO para operações de banco de dados relacionadas às Fotos de Comprovante.
 *
 * @author Thiago
 * @since 10.0.0
 */
@Dao
interface FotoComprovanteDao {

    // ════════════════════════════════════════════════════════════════════════
    // OPERAÇÕES CRUD BÁSICAS
    // ════════════════════════════════════════════════════════════════════════

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(foto: FotoComprovanteEntity): Long

    @Update
    suspend fun atualizar(foto: FotoComprovanteEntity)

    @Delete
    suspend fun excluir(foto: FotoComprovanteEntity)

    @Query("DELETE FROM fotos_comprovante WHERE id = :id")
    suspend fun excluirPorId(id: Long)

    @Query("DELETE FROM fotos_comprovante WHERE pontoId = :pontoId")
    suspend fun excluirPorPontoId(pontoId: Long)

    // ════════════════════════════════════════════════════════════════════════
    // CONSULTAS POR ID
    // ════════════════════════════════════════════════════════════════════════

    @Query("SELECT * FROM fotos_comprovante WHERE id = :id")
    suspend fun buscarPorId(id: Long): FotoComprovanteEntity?

    @Query("SELECT * FROM fotos_comprovante WHERE id = :id")
    fun observarPorId(id: Long): Flow<FotoComprovanteEntity?>

    @Query("SELECT * FROM fotos_comprovante WHERE pontoId = :pontoId")
    suspend fun buscarPorPontoId(pontoId: Long): FotoComprovanteEntity?

    @Query("SELECT * FROM fotos_comprovante WHERE pontoId = :pontoId")
    fun observarPorPontoId(pontoId: Long): Flow<FotoComprovanteEntity?>

    // ════════════════════════════════════════════════════════════════════════
    // LISTAGENS POR EMPREGO
    // ════════════════════════════════════════════════════════════════════════

    @Query("SELECT * FROM fotos_comprovante WHERE empregoId = :empregoId ORDER BY data DESC, hora DESC")
    fun listarPorEmprego(empregoId: Long): Flow<List<FotoComprovanteEntity>>

    @Query("SELECT * FROM fotos_comprovante WHERE empregoId = :empregoId AND data = :data ORDER BY hora ASC")
    fun listarPorEmpregoEData(empregoId: Long, data: LocalDate): Flow<List<FotoComprovanteEntity>>

    @Query(
        """
        SELECT * FROM fotos_comprovante
        WHERE empregoId = :empregoId
        AND data BETWEEN :dataInicio AND :dataFim
        ORDER BY data ASC, hora ASC
    """
    )
    fun listarPorEmpregoEPeriodo(
        empregoId: Long,
        dataInicio: LocalDate,
        dataFim: LocalDate
    ): Flow<List<FotoComprovanteEntity>>

    @Query(
        """
        SELECT * FROM fotos_comprovante
        WHERE empregoId = :empregoId
        AND data BETWEEN :dataInicio AND :dataFim
        ORDER BY data ASC, hora ASC
    """
    )
    suspend fun buscarPorEmpregoEPeriodo(
        empregoId: Long,
        dataInicio: LocalDate,
        dataFim: LocalDate
    ): List<FotoComprovanteEntity>

    // ════════════════════════════════════════════════════════════════════════
    // CONSULTAS DE SINCRONIZAÇÃO
    // ════════════════════════════════════════════════════════════════════════

    @Query("SELECT * FROM fotos_comprovante WHERE sincronizadoNuvem = 0 ORDER BY criadoEm ASC")
    suspend fun buscarNaoSincronizadas(): List<FotoComprovanteEntity>

    @Query("SELECT * FROM fotos_comprovante WHERE sincronizadoNuvem = 0 AND empregoId = :empregoId ORDER BY criadoEm ASC")
    suspend fun buscarNaoSincronizadasPorEmprego(empregoId: Long): List<FotoComprovanteEntity>

    @Query("SELECT COUNT(*) FROM fotos_comprovante WHERE sincronizadoNuvem = 0")
    suspend fun contarNaoSincronizadas(): Int

    @Query(
        """
        UPDATE fotos_comprovante
        SET sincronizadoNuvem = 1,
            sincronizadoEm = :sincronizadoEm,
            cloudFileId = :cloudFileId,
            atualizadoEm = :atualizadoEm
        WHERE id = :id
    """
    )
    suspend fun marcarComoSincronizado(
        id: Long,
        sincronizadoEm: java.time.Instant,
        cloudFileId: String,
        atualizadoEm: java.time.Instant
    )

    // ════════════════════════════════════════════════════════════════════════
    // CONTAGENS E VERIFICAÇÕES
    // ════════════════════════════════════════════════════════════════════════

    @Query("SELECT COUNT(*) FROM fotos_comprovante WHERE empregoId = :empregoId")
    suspend fun contarPorEmprego(empregoId: Long): Int

    @Query("SELECT COUNT(*) FROM fotos_comprovante WHERE empregoId = :empregoId AND data = :data")
    suspend fun contarPorEmpregoEData(empregoId: Long, data: LocalDate): Int

    @Query("SELECT EXISTS(SELECT 1 FROM fotos_comprovante WHERE pontoId = :pontoId)")
    suspend fun existeParaPonto(pontoId: Long): Boolean

    // ════════════════════════════════════════════════════════════════════════
    // CONSULTAS DE INTEGRIDADE
    // ════════════════════════════════════════════════════════════════════════

    @Query("SELECT fotoPath FROM fotos_comprovante WHERE empregoId = :empregoId")
    suspend fun listarPathsPorEmprego(empregoId: Long): List<String>

    @Query("SELECT * FROM fotos_comprovante WHERE fotoHashMd5 = :hash")
    suspend fun buscarPorHash(hash: String): FotoComprovanteEntity?

    // ════════════════════════════════════════════════════════════════════════
    // ESTATÍSTICAS
    // ════════════════════════════════════════════════════════════════════════

    @Query("SELECT SUM(fotoTamanhoBytes) FROM fotos_comprovante WHERE empregoId = :empregoId")
    suspend fun calcularTamanhoTotalPorEmprego(empregoId: Long): Long?

    @Query("SELECT SUM(fotoTamanhoBytes) FROM fotos_comprovante")
    suspend fun calcularTamanhoTotal(): Long?
}
