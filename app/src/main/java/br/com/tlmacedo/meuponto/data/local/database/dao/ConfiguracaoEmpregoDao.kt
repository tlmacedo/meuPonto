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
 * @updated 3.0.0 - Atualização para nova estrutura de ciclos de banco de horas
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

    // ========================================================================
    // Consultas de Banco de Horas (ATUALIZADAS para novos campos)
    // ========================================================================

    /**
     * Verifica se o banco de horas está habilitado.
     */
    @Query("SELECT bancoHorasHabilitado FROM configuracoes_emprego WHERE empregoId = :empregoId")
    suspend fun isBancoHorasHabilitado(empregoId: Long): Boolean?

    /**
     * Retorna o período do banco em semanas (1-3).
     */
    @Query("SELECT periodoBancoSemanas FROM configuracoes_emprego WHERE empregoId = :empregoId")
    suspend fun buscarPeriodoBancoSemanas(empregoId: Long): Int?

    /**
     * Retorna o período do banco em meses.
     */
    @Query("SELECT periodoBancoMeses FROM configuracoes_emprego WHERE empregoId = :empregoId")
    suspend fun buscarPeriodoBancoMeses(empregoId: Long): Int?

    /**
     * Retorna a data de início do ciclo atual do banco.
     */
    @Query("SELECT dataInicioCicloBancoAtual FROM configuracoes_emprego WHERE empregoId = :empregoId")
    suspend fun buscarDataInicioCicloBanco(empregoId: Long): String?

    /**
     * Retorna o dia de início do fechamento RH.
     */
    @Query("SELECT diaInicioFechamentoRH FROM configuracoes_emprego WHERE empregoId = :empregoId")
    suspend fun buscarDiaInicioFechamentoRH(empregoId: Long): Int?

    /**
     * Verifica se deve zerar registros antes do período.
     */
    @Query("SELECT zerarBancoAntesPeriodo FROM configuracoes_emprego WHERE empregoId = :empregoId")
    suspend fun isZerarBancoAntesPeriodo(empregoId: Long): Boolean?

    /**
     * Retorna empregos com banco de horas habilitado.
     */
    @Query("""
        SELECT * FROM configuracoes_emprego 
        WHERE bancoHorasHabilitado = 1 
        AND (periodoBancoSemanas > 0 OR periodoBancoMeses > 0)
    """)
    suspend fun buscarComBancoHorasHabilitado(): List<ConfiguracaoEmpregoEntity>

    // ========================================================================
    // Atualizações parciais
    // ========================================================================

    /**
     * Atualiza a data de início do ciclo do banco de horas.
     */
    @Query("""
        UPDATE configuracoes_emprego 
        SET dataInicioCicloBancoAtual = :data, atualizadoEm = :atualizadoEm 
        WHERE empregoId = :empregoId
    """)
    suspend fun atualizarDataInicioCicloBanco(empregoId: Long, data: String?, atualizadoEm: String)
}
