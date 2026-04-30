// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/repository/EmpregoRepository.kt
package br.com.tlmacedo.meuponto.domain.repository

import br.com.tlmacedo.meuponto.domain.model.Emprego
import kotlinx.coroutines.flow.Flow

/**
 * Interface do repositório de empregos.
 *
 * Define o contrato para operações de persistência de empregos,
 * seguindo o princípio de inversão de dependência (DIP).
 *
 * @author Thiago
 * @since 2.0.0
 */
interface EmpregoRepository {

    // ========================================================================
    // Operações de Escrita (CRUD)
    // ========================================================================

    /**
     * Insere um novo emprego.
     *
     * @param emprego Emprego a ser inserido
     * @return ID gerado para o novo registro
     */
    suspend fun inserir(emprego: Emprego): Long

    /**
     * Atualiza um emprego existente.
     *
     * @param emprego Emprego com os dados atualizados
     */
    suspend fun atualizar(emprego: Emprego)

    /**
     * Remove um emprego.
     *
     * @param emprego Emprego a ser removido
     */
    suspend fun excluir(emprego: Emprego)

    /**
     * Remove um emprego pelo ID.
     *
     * @param id Identificador do emprego
     */
    suspend fun excluirPorId(id: Long)

    // ========================================================================
    // Operações de Leitura
    // ========================================================================

    /**
     * Busca um emprego pelo ID.
     *
     * @param id Identificador único do emprego
     * @return Emprego encontrado ou null
     */
    suspend fun buscarPorId(empregoId: Long): Emprego?

    /**
     * Busca todos os empregos ativos e não arquivados.
     *
     * @return Lista de empregos ativos
     */
    suspend fun buscarAtivos(): List<Emprego>

    /**
     * Conta o número de empregos ativos.
     *
     * @return Quantidade de empregos ativos
     */
    suspend fun contarAtivos(): Int

    /**
     * Conta o número total de empregos.
     *
     * @return Quantidade total de empregos
     */
    suspend fun contarTodos(): Int

    /**
     * Verifica se um emprego existe.
     *
     * @param id Identificador do emprego
     * @return true se existe, false caso contrário
     */
    suspend fun existe(id: Long): Boolean

    /**
     * Busca um emprego pelo CNPJ.
     *
     * @param cnpj CNPJ a ser buscado (apenas números)
     * @return Emprego correspondente ou null se não encontrado
     */
    suspend fun buscarPorCnpj(cnpj: String): Emprego?

    // ========================================================================
    // Operações Reativas (Flows)
    // ========================================================================

    /**
     * Observa um emprego específico de forma reativa.
     *
     * @param id Identificador do emprego
     * @return Flow que emite o emprego sempre que houver mudanças
     */
    fun observarPorId(id: Long): Flow<Emprego?>

    /**
     * Observa todos os empregos de forma reativa.
     *
     * @return Flow que emite a lista completa sempre que houver mudanças
     */
    fun observarTodos(): Flow<List<Emprego>>

    /**
     * Observa os empregos ativos de forma reativa.
     *
     * @return Flow que emite a lista de ativos sempre que houver mudanças
     */
    fun observarAtivos(): Flow<List<Emprego>>

    /**
     * Observa os empregos arquivados de forma reativa.
     *
     * @return Flow que emite a lista de arquivados sempre que houver mudanças
     */
    fun observarArquivados(): Flow<List<Emprego>>

    // ========================================================================
    // Operações de Status
    // ========================================================================

    /**
     * Atualiza o status ativo/inativo de um emprego.
     *
     * @param id Identificador do emprego
     * @param ativo Novo status
     */
    suspend fun atualizarStatus(id: Long, ativo: Boolean)

    /**
     * Arquiva um emprego.
     *
     * @param id Identificador do emprego
     */
    suspend fun arquivar(id: Long)

    /**
     * Desarquiva um emprego.
     *
     * @param id Identificador do emprego
     */
    suspend fun desarquivar(id: Long)

    /**
     * Atualiza a ordem de exibição de um emprego.
     *
     * @param id Identificador do emprego
     * @param ordem Nova posição na ordenação
     */
    suspend fun atualizarOrdem(id: Long, ordem: Int)

    /**
     * Busca a próxima ordem disponível para novos empregos.
     *
     * @return Próximo valor de ordem
     */
    suspend fun buscarProximaOrdem(): Int
}
