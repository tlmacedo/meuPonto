package br.com.tlmacedo.meuponto.data.local.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import br.com.tlmacedo.meuponto.data.local.database.entity.PontoEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) para operações de banco de dados relacionadas a Ponto.
 *
 * Define todas as operações de CRUD e queries específicas para a tabela de pontos.
 *
 * @author Thiago
 * @since 1.0.0
 */
@Dao
interface PontoDao {

    // ========================================================================
    // Operações de Inserção
    // ========================================================================

    /**
     * Insere um novo registro de ponto.
     *
     * @param ponto Entidade a ser inserida
     * @return ID do registro inserido
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(ponto: PontoEntity): Long

    /**
     * Insere múltiplos registros de ponto.
     *
     * @param pontos Lista de entidades a serem inseridas
     * @return Lista de IDs dos registros inseridos
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirTodos(pontos: List<PontoEntity>): List<Long>

    // ========================================================================
    // Operações de Atualização
    // ========================================================================

    /**
     * Atualiza um registro de ponto existente.
     *
     * @param ponto Entidade com os dados atualizados
     */
    @Update
    suspend fun atualizar(ponto: PontoEntity)

    // ========================================================================
    // Operações de Exclusão
    // ========================================================================

    /**
     * Remove um registro de ponto.
     *
     * @param ponto Entidade a ser removida
     */
    @Delete
    suspend fun deletar(ponto: PontoEntity)

    /**
     * Remove todos os registros de ponto.
     */
    @Query("DELETE FROM pontos")
    suspend fun deletarTodos()

    /**
     * Remove todos os registros de uma data específica.
     *
     * @param data Data no formato ISO (yyyy-MM-dd)
     */
    @Query("DELETE FROM pontos WHERE date(data_hora) = :data")
    suspend fun deletarPorData(data: String)

    // ========================================================================
    // Operações de Consulta
    // ========================================================================

    /**
     * Busca um ponto pelo ID.
     *
     * @param id ID do ponto
     * @return Entidade encontrada ou null
     */
    @Query("SELECT * FROM pontos WHERE id = :id")
    suspend fun buscarPorId(id: Long): PontoEntity?

    /**
     * Observa todos os pontos de uma data específica, ordenados por hora.
     *
     * @param data Data no formato ISO (yyyy-MM-dd)
     * @return Flow com a lista de pontos do dia
     */
    @Query("SELECT * FROM pontos WHERE date(data_hora) = :data ORDER BY data_hora ASC")
    fun observarPontosPorData(data: String): Flow<List<PontoEntity>>

    /**
     * Busca todos os pontos de uma data específica, ordenados por hora.
     *
     * @param data Data no formato ISO (yyyy-MM-dd)
     * @return Lista de pontos do dia
     */
    @Query("SELECT * FROM pontos WHERE date(data_hora) = :data ORDER BY data_hora ASC")
    suspend fun buscarPontosPorData(data: String): List<PontoEntity>

    /**
     * Observa todos os pontos de um período, ordenados por data/hora.
     *
     * @param dataInicio Data inicial no formato ISO
     * @param dataFim Data final no formato ISO
     * @return Flow com a lista de pontos do período
     */
    @Query("SELECT * FROM pontos WHERE date(data_hora) BETWEEN :dataInicio AND :dataFim ORDER BY data_hora ASC")
    fun observarPontosPorPeriodo(dataInicio: String, dataFim: String): Flow<List<PontoEntity>>

    /**
     * Busca todos os pontos de um período, ordenados por data/hora.
     *
     * @param dataInicio Data inicial no formato ISO
     * @param dataFim Data final no formato ISO
     * @return Lista de pontos do período
     */
    @Query("SELECT * FROM pontos WHERE date(data_hora) BETWEEN :dataInicio AND :dataFim ORDER BY data_hora ASC")
    suspend fun buscarPontosPorPeriodo(dataInicio: String, dataFim: String): List<PontoEntity>

    /**
     * Busca o último ponto registrado em uma data específica.
     *
     * @param data Data no formato ISO (yyyy-MM-dd)
     * @return Último ponto do dia ou null
     */
    @Query("SELECT * FROM pontos WHERE date(data_hora) = :data ORDER BY data_hora DESC LIMIT 1")
    suspend fun buscarUltimoPontoDoDia(data: String): PontoEntity?

    /**
     * Conta o total de registros de ponto.
     *
     * @return Quantidade total de registros
     */
    @Query("SELECT COUNT(*) FROM pontos")
    suspend fun contarTodos(): Int

    /**
     * Conta os registros de uma data específica.
     *
     * @param data Data no formato ISO (yyyy-MM-dd)
     * @return Quantidade de registros do dia
     */
    @Query("SELECT COUNT(*) FROM pontos WHERE date(data_hora) = :data")
    suspend fun contarPorData(data: String): Int

    /**
     * Busca todas as datas distintas que possuem registros.
     *
     * @return Lista de datas com registros
     */
    @Query("SELECT DISTINCT date(data_hora) FROM pontos ORDER BY data_hora DESC")
    suspend fun buscarDatasComRegistros(): List<String>

    /**
     * Observa todos os pontos ordenados por data/hora decrescente.
     *
     * @return Flow com todos os pontos
     */
    @Query("SELECT * FROM pontos ORDER BY data_hora DESC")
    fun observarTodos(): Flow<List<PontoEntity>>
}
