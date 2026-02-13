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
 * Gerencia as configurações específicas de cada emprego, incluindo jornada,
 * banco de horas, NSR, localização e preferências de exibição.
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

    @Query("DELETE FROM configuracoes_emprego WHERE id = :id")
    suspend fun excluirPorId(id: Long)

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
    // Consultas auxiliares
    // ========================================================================

    @Query("SELECT EXISTS(SELECT 1 FROM configuracoes_emprego WHERE empregoId = :empregoId)")
    suspend fun existeParaEmprego(empregoId: Long): Boolean

    @Query("SELECT habilitarNsr FROM configuracoes_emprego WHERE empregoId = :empregoId")
    suspend fun isNsrHabilitado(empregoId: Long): Boolean?

    @Query("SELECT habilitarLocalizacao FROM configuracoes_emprego WHERE empregoId = :empregoId")
    suspend fun isLocalizacaoHabilitada(empregoId: Long): Boolean?

    @Query("SELECT periodoBancoHorasMeses FROM configuracoes_emprego WHERE empregoId = :empregoId")
    suspend fun buscarPeriodoBancoHoras(empregoId: Long): Int?

    // ========================================================================
    // Atualizações parciais
    // ========================================================================

    @Query("UPDATE configuracoes_emprego SET ultimoFechamentoBanco = :data, atualizadoEm = :atualizadoEm WHERE empregoId = :empregoId")
    suspend fun atualizarUltimoFechamentoBanco(empregoId: Long, data: String, atualizadoEm: String)
}
