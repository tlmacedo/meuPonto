// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/repository/AuditLogRepository.kt
package br.com.tlmacedo.meuponto.domain.repository

import br.com.tlmacedo.meuponto.domain.model.AcaoAuditoria
import br.com.tlmacedo.meuponto.domain.model.AuditLog
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

/**
 * Interface do repositório de logs de auditoria.
 *
 * @author Thiago
 * @since 2.0.0
 * @updated 11.0.0 - Adicionados métodos de observação
 */
interface AuditLogRepository {

    // ========================================================================
    // Operações de Escrita
    // ========================================================================

    /**
     * Insere um novo log de auditoria.
     */
    suspend fun inserir(log: AuditLog): Long

    /**
     * Insere múltiplos logs de auditoria.
     */
    suspend fun inserirTodos(logs: List<AuditLog>): List<Long>

    // ========================================================================
    // Operações de Leitura
    // ========================================================================

    /**
     * Busca logs por entidade e ID.
     */
    suspend fun buscarPorEntidade(entidade: String, entidadeId: Long): List<AuditLog>

    /**
     * Conta o total de logs.
     */
    suspend fun contarTodos(): Int

    /**
     * Conta logs por tipo de entidade.
     */
    suspend fun contarPorEntidade(entidade: String): Int

    /**
     * Conta logs por entidade e ID específico.
     */
    suspend fun contarPorEntidadeEId(entidade: String, entidadeId: Long): Int

    // ========================================================================
    // Operações de Limpeza
    // ========================================================================

    /**
     * Remove logs mais antigos que a data especificada.
     */
    suspend fun excluirAnterioresA(dataLimite: LocalDateTime): Int

    /**
     * Remove logs de uma entidade específica.
     */
    suspend fun excluirPorEntidade(entidade: String, entidadeId: Long)

    // ========================================================================
    // Operações Reativas (Flows)
    // ========================================================================

    /**
     * Observa todos os logs ordenados por data (mais recentes primeiro).
     */
    fun observarTodos(): Flow<List<AuditLog>>

    /**
     * Observa logs de uma entidade específica.
     */
    fun observarPorEntidade(entidade: String, entidadeId: Long): Flow<List<AuditLog>>

    /**
     * Observa logs em um período.
     */
    fun observarPorPeriodo(dataInicio: LocalDateTime, dataFim: LocalDateTime): Flow<List<AuditLog>>

    /**
     * Observa logs por ação.
     */
    fun observarPorAcao(acao: AcaoAuditoria): Flow<List<AuditLog>>

    /**
     * Observa os últimos N logs.
     */
    fun observarUltimos(limite: Int): Flow<List<AuditLog>>

    /**
     * Observa os últimos N logs de uma entidade.
     */
    fun observarUltimosPorEntidade(entidade: String, limite: Int): Flow<List<AuditLog>>
}
