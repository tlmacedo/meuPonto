// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/data/local/database/dao/FeriadoDao.kt
package br.com.tlmacedo.meuponto.data.local.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import br.com.tlmacedo.meuponto.data.local.database.entity.FeriadoEntity
import br.com.tlmacedo.meuponto.domain.model.feriado.TipoFeriado
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * DAO para operações de banco de dados relacionadas a Feriados.
 *
 * @author Thiago
 * @since 3.0.0
 */
@Dao
interface FeriadoDao {

    // ========================================================================
    // Operações CRUD básicas
    // ========================================================================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(feriado: FeriadoEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirTodos(feriados: List<FeriadoEntity>): List<Long>

    @Update
    suspend fun atualizar(feriado: FeriadoEntity)

    @Delete
    suspend fun excluir(feriado: FeriadoEntity)

    @Query("DELETE FROM feriados WHERE id = :id")
    suspend fun excluirPorId(id: Long)

    // ========================================================================
    // Consultas por ID
    // ========================================================================

    @Query("SELECT * FROM feriados WHERE id = :id")
    suspend fun buscarPorId(id: Long): FeriadoEntity?

    @Query("SELECT * FROM feriados WHERE id = :id")
    fun observarPorId(id: Long): Flow<FeriadoEntity?>

    // ========================================================================
    // Consultas Globais (todos os feriados)
    // ========================================================================

    /**
     * Busca todos os feriados (ativos e inativos).
     * Usado na tela de gerenciamento de feriados.
     */
    @Query("""
        SELECT * FROM feriados 
        ORDER BY 
            CASE WHEN diaMes IS NOT NULL THEN diaMes ELSE '' END,
            CASE WHEN dataEspecifica IS NOT NULL THEN dataEspecifica ELSE '' END
    """)
    suspend fun buscarTodos(): List<FeriadoEntity>

    /**
     * Observa todos os feriados (ativos e inativos).
     * Usado na tela de gerenciamento de feriados.
     */
    @Query("""
        SELECT * FROM feriados 
        ORDER BY 
            CASE WHEN diaMes IS NOT NULL THEN diaMes ELSE '' END,
            CASE WHEN dataEspecifica IS NOT NULL THEN dataEspecifica ELSE '' END
    """)
    fun observarTodos(): Flow<List<FeriadoEntity>>

    @Query("""
        SELECT * FROM feriados 
        WHERE ativo = 1 
        ORDER BY 
            CASE WHEN diaMes IS NOT NULL THEN diaMes ELSE '' END,
            CASE WHEN dataEspecifica IS NOT NULL THEN dataEspecifica ELSE '' END
    """)
    suspend fun buscarTodosAtivos(): List<FeriadoEntity>

    @Query("""
        SELECT * FROM feriados 
        WHERE ativo = 1 
        ORDER BY 
            CASE WHEN diaMes IS NOT NULL THEN diaMes ELSE '' END,
            CASE WHEN dataEspecifica IS NOT NULL THEN dataEspecifica ELSE '' END
    """)
    fun observarTodosAtivos(): Flow<List<FeriadoEntity>>

    // ========================================================================
    // Consultas por Ano
    // ========================================================================

    /**
     * Busca feriados que ocorrem em um ano específico.
     * Inclui feriados anuais (recorrentes) e únicos do ano.
     */
    @Query("""
        SELECT * FROM feriados 
        WHERE ativo = 1 
        AND (
            recorrencia = 'ANUAL' 
            OR (recorrencia = 'UNICO' AND anoReferencia = :ano)
        )
        ORDER BY 
            CASE WHEN diaMes IS NOT NULL THEN diaMes ELSE '' END,
            CASE WHEN dataEspecifica IS NOT NULL THEN dataEspecifica ELSE '' END
    """)
    suspend fun buscarPorAno(ano: Int): List<FeriadoEntity>

    @Query("""
        SELECT * FROM feriados 
        WHERE ativo = 1 
        AND (
            recorrencia = 'ANUAL' 
            OR (recorrencia = 'UNICO' AND anoReferencia = :ano)
        )
        ORDER BY 
            CASE WHEN diaMes IS NOT NULL THEN diaMes ELSE '' END,
            CASE WHEN dataEspecifica IS NOT NULL THEN dataEspecifica ELSE '' END
    """)
    fun observarPorAno(ano: Int): Flow<List<FeriadoEntity>>

    // ========================================================================
    // Consultas por Data
    // ========================================================================

    /**
     * Busca feriados que ocorrem em uma data específica.
     */
    @Query("""
        SELECT * FROM feriados 
        WHERE ativo = 1 
        AND (
            (diaMes = :diaMes AND recorrencia = 'ANUAL')
            OR (dataEspecifica = :data AND recorrencia = 'UNICO')
        )
    """)
    suspend fun buscarPorData(data: LocalDate, diaMes: String): List<FeriadoEntity>

    /**
     * Busca feriados em um período.
     */
    @Query("""
        SELECT * FROM feriados 
        WHERE ativo = 1 
        AND (
            recorrencia = 'ANUAL'
            OR (recorrencia = 'UNICO' AND dataEspecifica BETWEEN :dataInicio AND :dataFim)
        )
        ORDER BY 
            CASE WHEN diaMes IS NOT NULL THEN diaMes ELSE '' END,
            CASE WHEN dataEspecifica IS NOT NULL THEN dataEspecifica ELSE '' END
    """)
    suspend fun buscarPorPeriodo(dataInicio: LocalDate, dataFim: LocalDate): List<FeriadoEntity>

    // ========================================================================
    // Consultas por Tipo
    // ========================================================================

    @Query("""
        SELECT * FROM feriados 
        WHERE ativo = 1 AND tipo = :tipo
        ORDER BY 
            CASE WHEN diaMes IS NOT NULL THEN diaMes ELSE '' END,
            CASE WHEN dataEspecifica IS NOT NULL THEN dataEspecifica ELSE '' END
    """)
    suspend fun buscarPorTipo(tipo: TipoFeriado): List<FeriadoEntity>

    @Query("""
        SELECT * FROM feriados 
        WHERE ativo = 1 AND tipo = :tipo
        ORDER BY 
            CASE WHEN diaMes IS NOT NULL THEN diaMes ELSE '' END,
            CASE WHEN dataEspecifica IS NOT NULL THEN dataEspecifica ELSE '' END
    """)
    fun observarPorTipo(tipo: TipoFeriado): Flow<List<FeriadoEntity>>

    // ========================================================================
    // Consultas por Emprego
    // ========================================================================

    /**
     * Busca feriados aplicáveis a um emprego (globais + específicos do emprego).
     */
    @Query("""
        SELECT * FROM feriados 
        WHERE ativo = 1 
        AND (
            abrangencia = 'GLOBAL' 
            OR (abrangencia = 'EMPREGO_ESPECIFICO' AND empregoId = :empregoId)
        )
        ORDER BY 
            CASE WHEN diaMes IS NOT NULL THEN diaMes ELSE '' END,
            CASE WHEN dataEspecifica IS NOT NULL THEN dataEspecifica ELSE '' END
    """)
    suspend fun buscarPorEmprego(empregoId: Long): List<FeriadoEntity>

    @Query("""
        SELECT * FROM feriados 
        WHERE ativo = 1 
        AND (
            abrangencia = 'GLOBAL' 
            OR (abrangencia = 'EMPREGO_ESPECIFICO' AND empregoId = :empregoId)
        )
        ORDER BY 
            CASE WHEN diaMes IS NOT NULL THEN diaMes ELSE '' END,
            CASE WHEN dataEspecifica IS NOT NULL THEN dataEspecifica ELSE '' END
    """)
    fun observarPorEmprego(empregoId: Long): Flow<List<FeriadoEntity>>

    // ========================================================================
    // Consultas de Pontes
    // ========================================================================

    /**
     * Busca feriados ponte de um ano para um emprego.
     */
    @Query("""
        SELECT * FROM feriados 
        WHERE ativo = 1 
        AND tipo = 'PONTE'
        AND (anoReferencia = :ano OR recorrencia = 'ANUAL')
        AND (
            abrangencia = 'GLOBAL' 
            OR (abrangencia = 'EMPREGO_ESPECIFICO' AND empregoId = :empregoId)
        )
        ORDER BY dataEspecifica
    """)
    suspend fun buscarPontesPorAnoEEmprego(ano: Int, empregoId: Long): List<FeriadoEntity>

    /**
     * Conta quantidade de dias de ponte em um ano para um emprego.
     */
    @Query("""
        SELECT COUNT(*) FROM feriados 
        WHERE ativo = 1 
        AND tipo = 'PONTE'
        AND anoReferencia = :ano
        AND (
            abrangencia = 'GLOBAL' 
            OR (abrangencia = 'EMPREGO_ESPECIFICO' AND empregoId = :empregoId)
        )
    """)
    suspend fun contarPontesPorAnoEEmprego(ano: Int, empregoId: Long): Int

    // ========================================================================
    // Consultas de Validação
    // ========================================================================

    /**
     * Verifica se existe feriado com o mesmo nome e data.
     */
    @Query("""
        SELECT COUNT(*) FROM feriados 
        WHERE nome = :nome 
        AND (
            (diaMes = :diaMes AND recorrencia = 'ANUAL')
            OR (dataEspecifica = :dataEspecifica AND recorrencia = 'UNICO')
        )
        AND id != :excluirId
    """)
    suspend fun contarDuplicados(
        nome: String,
        diaMes: String?,
        dataEspecifica: LocalDate?,
        excluirId: Long = 0
    ): Int

    /**
     * Verifica se existe feriado nacional importado.
     */
    @Query("""
        SELECT COUNT(*) FROM feriados 
        WHERE tipo = 'NACIONAL' 
        AND abrangencia = 'GLOBAL'
    """)
    suspend fun contarFeriadosNacionais(): Int

    // ========================================================================
    // Operações de Limpeza
    // ========================================================================

    /**
     * Remove feriados únicos de anos anteriores.
     */
    @Query("""
        DELETE FROM feriados 
        WHERE recorrencia = 'UNICO' 
        AND anoReferencia < :anoAtual
    """)
    suspend fun limparFeriadosAntigos(anoAtual: Int)

    /**
     * Desativa feriados de um emprego específico.
     */
    @Query("UPDATE feriados SET ativo = 0, atualizadoEm = :agora WHERE empregoId = :empregoId")
    suspend fun desativarPorEmprego(empregoId: Long, agora: java.time.LocalDateTime = java.time.LocalDateTime.now())
}
