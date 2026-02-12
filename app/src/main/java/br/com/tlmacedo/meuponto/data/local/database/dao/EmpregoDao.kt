// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/data/local/database/dao/EmpregoDao.kt
package br.com.tlmacedo.meuponto.data.local.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import br.com.tlmacedo.meuponto.data.local.database.entity.EmpregoEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operações de banco de dados relacionadas a Empregos.
 *
 * @author Thiago
 * @since 2.0.0
 */
@Dao
interface EmpregoDao {

    // ========================================================================
    // Operações CRUD básicas
    // ========================================================================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(emprego: EmpregoEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirTodos(empregos: List<EmpregoEntity>): List<Long>

    @Update
    suspend fun atualizar(emprego: EmpregoEntity)

    @Delete
    suspend fun excluir(emprego: EmpregoEntity)

    @Query("DELETE FROM empregos WHERE id = :id")
    suspend fun excluirPorId(id: Long)

    // ========================================================================
    // Consultas por ID
    // ========================================================================

    @Query("SELECT * FROM empregos WHERE id = :id")
    suspend fun buscarPorId(id: Long): EmpregoEntity?

    @Query("SELECT * FROM empregos WHERE id = :id")
    fun observarPorId(id: Long): Flow<EmpregoEntity?>

    // ========================================================================
    // Listagens
    // ========================================================================

    @Query("SELECT * FROM empregos ORDER BY ordem ASC, nome ASC")
    fun listarTodos(): Flow<List<EmpregoEntity>>

    @Query("SELECT * FROM empregos WHERE ativo = 1 AND arquivado = 0 ORDER BY ordem ASC, nome ASC")
    fun listarAtivos(): Flow<List<EmpregoEntity>>

    @Query("SELECT * FROM empregos WHERE arquivado = 1 ORDER BY nome ASC")
    fun listarArquivados(): Flow<List<EmpregoEntity>>

    @Query("SELECT * FROM empregos WHERE ativo = 1 AND arquivado = 0 ORDER BY ordem ASC, nome ASC")
    suspend fun buscarAtivos(): List<EmpregoEntity>

    // ========================================================================
    // Contagens
    // ========================================================================

    @Query("SELECT COUNT(*) FROM empregos")
    suspend fun contarTodos(): Int

    @Query("SELECT COUNT(*) FROM empregos WHERE ativo = 1 AND arquivado = 0")
    suspend fun contarAtivos(): Int

    // ========================================================================
    // Operações de status
    // ========================================================================

    @Query("UPDATE empregos SET ativo = :ativo, atualizadoEm = :atualizadoEm WHERE id = :id")
    suspend fun atualizarStatus(id: Long, ativo: Boolean, atualizadoEm: String)

    @Query("UPDATE empregos SET arquivado = :arquivado, atualizadoEm = :atualizadoEm WHERE id = :id")
    suspend fun atualizarArquivado(id: Long, arquivado: Boolean, atualizadoEm: String)

    @Query("UPDATE empregos SET ordem = :ordem, atualizadoEm = :atualizadoEm WHERE id = :id")
    suspend fun atualizarOrdem(id: Long, ordem: Int, atualizadoEm: String)

    // ========================================================================
    // Consultas auxiliares
    // ========================================================================

    @Query("SELECT MAX(ordem) FROM empregos")
    suspend fun buscarMaiorOrdem(): Int?

    @Query("SELECT EXISTS(SELECT 1 FROM empregos WHERE id = :id)")
    suspend fun existe(id: Long): Boolean
}
