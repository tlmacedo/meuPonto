// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/data/local/database/dao/MarcadorDao.kt
package br.com.tlmacedo.meuponto.data.local.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import br.com.tlmacedo.meuponto.data.local.database.entity.MarcadorEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operações de banco de dados relacionadas aos Marcadores.
 *
 * @author Thiago
 * @since 2.0.0
 */
@Dao
interface MarcadorDao {

    // ========================================================================
    // Operações CRUD básicas
    // ========================================================================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(marcador: MarcadorEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirTodos(marcadores: List<MarcadorEntity>): List<Long>

    @Update
    suspend fun atualizar(marcador: MarcadorEntity)

    @Delete
    suspend fun excluir(marcador: MarcadorEntity)

    @Query("DELETE FROM marcadores WHERE id = :id")
    suspend fun excluirPorId(id: Long)

    // ========================================================================
    // Consultas por ID
    // ========================================================================

    @Query("SELECT * FROM marcadores WHERE id = :id")
    suspend fun buscarPorId(id: Long): MarcadorEntity?

    // ========================================================================
    // Listagens por emprego
    // ========================================================================

    @Query("SELECT * FROM marcadores WHERE empregoId = :empregoId ORDER BY nome ASC")
    fun listarPorEmprego(empregoId: Long): Flow<List<MarcadorEntity>>

    @Query("SELECT * FROM marcadores WHERE empregoId = :empregoId AND ativo = 1 ORDER BY nome ASC")
    fun listarAtivosPorEmprego(empregoId: Long): Flow<List<MarcadorEntity>>

    @Query("SELECT * FROM marcadores WHERE empregoId = :empregoId AND ativo = 1 ORDER BY nome ASC")
    suspend fun buscarAtivosPorEmprego(empregoId: Long): List<MarcadorEntity>

    // ========================================================================
    // Consultas específicas
    // ========================================================================

    @Query("SELECT * FROM marcadores WHERE empregoId = :empregoId AND nome = :nome LIMIT 1")
    suspend fun buscarPorNome(empregoId: Long, nome: String): MarcadorEntity?

    // ========================================================================
    // Operações de status
    // ========================================================================

    @Query("UPDATE marcadores SET ativo = :ativo, atualizadoEm = :atualizadoEm WHERE id = :id")
    suspend fun atualizarStatus(id: Long, ativo: Boolean, atualizadoEm: String)

    // ========================================================================
    // Verificações
    // ========================================================================

    @Query("SELECT EXISTS(SELECT 1 FROM marcadores WHERE empregoId = :empregoId AND nome = :nome AND id != :excludeId)")
    suspend fun existeComNome(empregoId: Long, nome: String, excludeId: Long = 0): Boolean
}
