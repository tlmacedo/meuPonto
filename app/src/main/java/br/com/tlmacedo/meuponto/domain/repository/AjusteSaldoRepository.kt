// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/repository/AjusteSaldoRepository.kt
package br.com.tlmacedo.meuponto.domain.repository

import br.com.tlmacedo.meuponto.domain.model.AjusteSaldo
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * Interface do repositório de ajustes de saldo.
 *
 * Define o contrato para operações de persistência de ajustes manuais
 * no banco de horas.
 *
 * @author Thiago
 * @since 2.0.0
 */
interface AjusteSaldoRepository {

    // ========================================================================
    // Operações de Escrita (CRUD)
    // ========================================================================

    /**
     * Insere um novo ajuste de saldo.
     *
     * @param ajuste Ajuste a ser inserido
     * @return ID gerado para o novo registro
     */
    suspend fun inserir(ajuste: AjusteSaldo): Long

    /**
     * Atualiza um ajuste existente.
     *
     * @param ajuste Ajuste com os dados atualizados
     */
    suspend fun atualizar(ajuste: AjusteSaldo)

    /**
     * Remove um ajuste.
     *
     * @param ajuste Ajuste a ser removido
     */
    suspend fun excluir(ajuste: AjusteSaldo)

    /**
     * Remove um ajuste pelo ID.
     *
     * @param id Identificador do ajuste
     */
    suspend fun excluirPorId(id: Long)

    // ========================================================================
    // Operações de Leitura
    // ========================================================================

    /**
     * Busca um ajuste pelo ID.
     *
     * @param id Identificador único do ajuste
     * @return Ajuste encontrado ou null
     */
    suspend fun buscarPorId(id: Long): AjusteSaldo?

    /**
     * Busca todos os ajustes de um emprego.
     *
     * @param empregoId Identificador do emprego
     * @return Lista de ajustes ordenada por data decrescente
     */
    suspend fun buscarPorEmprego(empregoId: Long): List<AjusteSaldo>

    /**
     * Busca os ajustes de uma data específica.
     *
     * @param empregoId Identificador do emprego
     * @param data Data para filtrar
     * @return Lista de ajustes da data
     */
    suspend fun buscarPorData(empregoId: Long, data: LocalDate): List<AjusteSaldo>

    /**
     * Busca os ajustes de um período.
     *
     * @param empregoId Identificador do emprego
     * @param dataInicio Data inicial (inclusive)
     * @param dataFim Data final (inclusive)
     * @return Lista de ajustes do período
     */
    suspend fun buscarPorPeriodo(
        empregoId: Long,
        dataInicio: LocalDate,
        dataFim: LocalDate
    ): List<AjusteSaldo>

    // ========================================================================
    // Cálculos
    // ========================================================================

    /**
     * Soma todos os ajustes de um emprego.
     *
     * @param empregoId Identificador do emprego
     * @return Total de minutos ajustados
     */
    suspend fun somarTotalPorEmprego(empregoId: Long): Int

    /**
     * Soma os ajustes de um período.
     *
     * @param empregoId Identificador do emprego
     * @param dataInicio Data inicial (inclusive)
     * @param dataFim Data final (inclusive)
     * @return Total de minutos ajustados no período
     */
    suspend fun somarPorPeriodo(empregoId: Long, dataInicio: LocalDate, dataFim: LocalDate): Int

    /**
     * Conta o número de ajustes de um emprego.
     *
     * @param empregoId Identificador do emprego
     * @return Quantidade de ajustes
     */
    suspend fun contarPorEmprego(empregoId: Long): Int

    // ========================================================================
    // Operações Reativas (Flows)
    // ========================================================================

    /**
     * Observa os ajustes de um emprego de forma reativa.
     *
     * @param empregoId Identificador do emprego
     * @return Flow que emite a lista sempre que houver mudanças
     */
    fun observarPorEmprego(empregoId: Long): Flow<List<AjusteSaldo>>

    /**
     * Observa os ajustes de um período de forma reativa.
     *
     * @param empregoId Identificador do emprego
     * @param dataInicio Data inicial (inclusive)
     * @param dataFim Data final (inclusive)
     * @return Flow que emite a lista do período
     */
    fun observarPorPeriodo(
        empregoId: Long,
        dataInicio: LocalDate,
        dataFim: LocalDate
    ): Flow<List<AjusteSaldo>>

    /**
     * Observa os últimos ajustes de um emprego.
     *
     * @param empregoId Identificador do emprego
     * @param limite Quantidade máxima de ajustes
     * @return Flow que emite os últimos ajustes
     */
    fun observarUltimos(empregoId: Long, limite: Int): Flow<List<AjusteSaldo>>
}
