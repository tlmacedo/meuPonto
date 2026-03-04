// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/repository/ConfiguracaoEmpregoRepository.kt
package br.com.tlmacedo.meuponto.domain.repository

import br.com.tlmacedo.meuponto.domain.model.ConfiguracaoEmprego
import kotlinx.coroutines.flow.Flow

/**
 * Interface do repositório de configurações de emprego.
 *
 * SIMPLIFICADO: Apenas configurações de exibição/comportamento.
 * Configurações de jornada/banco de horas foram migradas para VersaoJornadaRepository.
 *
 * @author Thiago
 * @since 2.0.0
 * @updated 8.0.0 - Simplificado após migração de campos para VersaoJornada
 */
interface ConfiguracaoEmpregoRepository {

    // ========================================================================
    // Operações CRUD
    // ========================================================================

    /**
     * Insere uma nova configuração.
     */
    suspend fun inserir(configuracao: ConfiguracaoEmprego): Long

    /**
     * Atualiza uma configuração existente.
     */
    suspend fun atualizar(configuracao: ConfiguracaoEmprego)

    /**
     * Exclui uma configuração.
     */
    suspend fun excluir(configuracao: ConfiguracaoEmprego)

    // ========================================================================
    // Consultas
    // ========================================================================

    /**
     * Busca configuração pelo ID.
     */
    suspend fun buscarPorId(id: Long): ConfiguracaoEmprego?

    /**
     * Busca configuração pelo ID do emprego.
     */
    suspend fun buscarPorEmpregoId(empregoId: Long): ConfiguracaoEmprego?

    /**
     * Observa configuração de um emprego de forma reativa.
     */
    fun observarPorEmpregoId(empregoId: Long): Flow<ConfiguracaoEmprego?>

    /**
     * Lista todas as configurações.
     */
    suspend fun listarTodas(): List<ConfiguracaoEmprego>

    /**
     * Observa todas as configurações de forma reativa.
     */
    fun observarTodas(): Flow<List<ConfiguracaoEmprego>>

    // ========================================================================
    // Verificações
    // ========================================================================

    /**
     * Verifica se existe configuração para o emprego.
     */
    suspend fun existeParaEmprego(empregoId: Long): Boolean
}
