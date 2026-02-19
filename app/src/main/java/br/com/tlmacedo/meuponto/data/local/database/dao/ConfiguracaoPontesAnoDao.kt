// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/data/local/database/dao/ConfiguracaoPontesAnoDao.kt
package br.com.tlmacedo.meuponto.data.local.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import br.com.tlmacedo.meuponto.data.local.database.entity.ConfiguracaoPontesAnoEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operações de banco de dados relacionadas à configuração de pontes por ano.
 *
 * @author Thiago
 * @since 3.0.0
 */
@Dao
interface ConfiguracaoPontesAnoDao {

    // ========================================================================
    // Operações CRUD básicas
    // ========================================================================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(config: ConfiguracaoPontesAnoEntity): Long

    @Update
    suspend fun atualizar(config: ConfiguracaoPontesAnoEntity)

    @Delete
    suspend fun excluir(config: ConfiguracaoPontesAnoEntity)

    @Query("DELETE FROM configuracao_pontes_ano WHERE id = :id")
    suspend fun excluirPorId(id: Long)

    // ========================================================================
    // Consultas
    // ========================================================================

    @Query("SELECT * FROM configuracao_pontes_ano WHERE id = :id")
    suspend fun buscarPorId(id: Long): ConfiguracaoPontesAnoEntity?

    @Query("SELECT * FROM configuracao_pontes_ano WHERE empregoId = :empregoId AND ano = :ano")
    suspend fun buscarPorEmpregoEAno(empregoId: Long, ano: Int): ConfiguracaoPontesAnoEntity?

    @Query("SELECT * FROM configuracao_pontes_ano WHERE empregoId = :empregoId AND ano = :ano")
    fun observarPorEmpregoEAno(empregoId: Long, ano: Int): Flow<ConfiguracaoPontesAnoEntity?>

    @Query("SELECT * FROM configuracao_pontes_ano WHERE empregoId = :empregoId ORDER BY ano DESC")
    suspend fun buscarPorEmprego(empregoId: Long): List<ConfiguracaoPontesAnoEntity>

    @Query("SELECT * FROM configuracao_pontes_ano WHERE empregoId = :empregoId ORDER BY ano DESC")
    fun observarPorEmprego(empregoId: Long): Flow<List<ConfiguracaoPontesAnoEntity>>

    /**
     * Busca o adicional de pontes para uma data específica.
     */
    @Query("""
        SELECT adicionalDiarioMinutos 
        FROM configuracao_pontes_ano 
        WHERE empregoId = :empregoId 
        AND ano = :ano
    """)
    suspend fun buscarAdicionalDiario(empregoId: Long, ano: Int): Int?

    // ========================================================================
    // Operações de Limpeza
    // ========================================================================

    @Query("DELETE FROM configuracao_pontes_ano WHERE empregoId = :empregoId")
    suspend fun excluirPorEmprego(empregoId: Long)

    @Query("DELETE FROM configuracao_pontes_ano WHERE ano < :anoAtual")
    suspend fun limparConfiguracoesAntigas(anoAtual: Int)
}
