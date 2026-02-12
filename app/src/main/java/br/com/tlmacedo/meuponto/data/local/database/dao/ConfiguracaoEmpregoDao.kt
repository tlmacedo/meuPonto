// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/data/local/database/dao/ConfiguracaoEmpregoDao.kt
package br.com.tlmacedo.meuponto.data.local.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import br.com.tlmacedo.meuponto.data.local.database.entity.ConfiguracaoEmpregoEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operações de banco de dados relacionadas às Configurações de Emprego.
 *
 * @author Thiago
 * @since 2.0.0
 */
@Dao
interface ConfiguracaoEmpregoDao {

    // ========================================================================
    // Operações CRUD básicas
    // ========================================================================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(configuracao: ConfiguracaoEmpregoEntity): Long

    @Update
    suspend fun atualizar(configuracao: ConfiguracaoEmpregoEntity)

    @Delete
    suspend fun excluir(configuracao: ConfiguracaoEmpregoEntity)

    // ========================================================================
    // Consultas por emprego
    // ========================================================================

    @Query("SELECT * FROM configuracoes_emprego WHERE empregoId = :empregoId")
    suspend fun buscarPorEmpregoId(empregoId: Long): ConfiguracaoEmpregoEntity?

    @Query("SELECT * FROM configuracoes_emprego WHERE empregoId = :empregoId")
    fun observarPorEmpregoId(empregoId: Long): Flow<ConfiguracaoEmpregoEntity?>

    @Query("SELECT * FROM configuracoes_emprego WHERE id = :id")
    suspend fun buscarPorId(id: Long): ConfiguracaoEmpregoEntity?

    // ========================================================================
    // Operações de NSR
    // ========================================================================

    @Query("UPDATE configuracoes_emprego SET proximoNsr = proximoNsr + 1, atualizadoEm = :atualizadoEm WHERE empregoId = :empregoId")
    suspend fun incrementarNsr(empregoId: Long, atualizadoEm: String)

    @Query("SELECT proximoNsr FROM configuracoes_emprego WHERE empregoId = :empregoId")
    suspend fun buscarProximoNsr(empregoId: Long): Int?

    // ========================================================================
    // Consultas auxiliares
    // ========================================================================

    @Query("SELECT EXISTS(SELECT 1 FROM configuracoes_emprego WHERE empregoId = :empregoId)")
    suspend fun existeParaEmprego(empregoId: Long): Boolean
}
