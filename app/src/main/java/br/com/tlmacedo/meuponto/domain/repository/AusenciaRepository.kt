// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/repository/AusenciaRepository.kt
package br.com.tlmacedo.meuponto.domain.repository

import br.com.tlmacedo.meuponto.domain.model.ausencia.Ausencia
import br.com.tlmacedo.meuponto.domain.model.ausencia.TipoAusencia
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.YearMonth

/**
 * Repositório para gerenciamento de ausências.
 *
 * @author Thiago
 * @since 4.0.0
 */
interface AusenciaRepository {

    // ========================================================================
    // CRUD
    // ========================================================================

    suspend fun inserir(ausencia: Ausencia): Long
    suspend fun inserirTodas(ausencias: List<Ausencia>): List<Long>
    suspend fun atualizar(ausencia: Ausencia)
    suspend fun excluir(ausencia: Ausencia)
    suspend fun excluirPorId(id: Long)

    // ========================================================================
    // Consultas por ID
    // ========================================================================

    suspend fun buscarPorId(id: Long): Ausencia?
    fun observarPorId(id: Long): Flow<Ausencia?>

    // ========================================================================
    // Consultas Globais
    // ========================================================================

    suspend fun buscarTodas(): List<Ausencia>
    fun observarTodas(): Flow<List<Ausencia>>
    suspend fun buscarTodasAtivas(): List<Ausencia>
    fun observarTodasAtivas(): Flow<List<Ausencia>>

    // ========================================================================
    // Consultas por Emprego
    // ========================================================================

    suspend fun buscarPorEmprego(empregoId: Long): List<Ausencia>
    fun observarPorEmprego(empregoId: Long): Flow<List<Ausencia>>
    suspend fun buscarAtivasPorEmprego(empregoId: Long): List<Ausencia>
    fun observarAtivasPorEmprego(empregoId: Long): Flow<List<Ausencia>>

    // ========================================================================
    // Consultas por Data
    // ========================================================================

    /**
     * Busca ausência(s) que ocorre(m) em uma data específica para um emprego.
     */
    suspend fun buscarPorData(empregoId: Long, data: LocalDate): List<Ausencia>
    fun observarPorData(empregoId: Long, data: LocalDate): Flow<List<Ausencia>>

    /**
     * Busca ausências que se sobrepõem com um período.
     */
    suspend fun buscarPorPeriodo(
        empregoId: Long,
        dataInicio: LocalDate,
        dataFim: LocalDate
    ): List<Ausencia>

    fun observarPorPeriodo(
        empregoId: Long,
        dataInicio: LocalDate,
        dataFim: LocalDate
    ): Flow<List<Ausencia>>

    // ========================================================================
    // Consultas por Tipo
    // ========================================================================

    suspend fun buscarPorTipo(empregoId: Long, tipo: TipoAusencia): List<Ausencia>
    fun observarPorTipo(empregoId: Long, tipo: TipoAusencia): Flow<List<Ausencia>>

    // ========================================================================
    // Consultas por Ano/Mês
    // ========================================================================

    suspend fun buscarPorAno(empregoId: Long, ano: Int): List<Ausencia>
    suspend fun buscarPorMes(empregoId: Long, mes: YearMonth): List<Ausencia>
    fun observarPorMes(empregoId: Long, mes: YearMonth): Flow<List<Ausencia>>

    // ========================================================================
    // Validações
    // ========================================================================

    /**
     * Verifica se existe ausência que se sobrepõe ao período informado.
     *
     * @param empregoId ID do emprego
     * @param dataInicio Data de início do período
     * @param dataFim Data de fim do período
     * @param excluirId ID da ausência a excluir da verificação (para edição)
     * @return true se existe sobreposição
     */
    suspend fun existeSobreposicao(
        empregoId: Long,
        dataInicio: LocalDate,
        dataFim: LocalDate,
        excluirId: Long = 0
    ): Boolean

    /**
     * Verifica se existe ausência em uma data específica.
     */
    suspend fun existeAusenciaEmData(empregoId: Long, data: LocalDate): Boolean

    // ========================================================================
    // Estatísticas
    // ========================================================================

    /**
     * Conta total de dias de ausência por tipo em um período.
     */
    suspend fun contarDiasPorTipo(
        empregoId: Long,
        tipo: TipoAusencia,
        dataInicio: LocalDate,
        dataFim: LocalDate
    ): Int

    /**
     * Conta quantidade de registros de ausência por emprego.
     */
    suspend fun contarPorEmprego(empregoId: Long): Int

    // ========================================================================
    // Operações de Limpeza
    // ========================================================================

    suspend fun desativarPorEmprego(empregoId: Long)
    suspend fun limparAusenciasAntigas(dataLimite: LocalDate)
}
