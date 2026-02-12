// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/repository/PontoRepository.kt
package br.com.tlmacedo.meuponto.domain.repository

import br.com.tlmacedo.meuponto.domain.model.Ponto
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * Interface do repositório de pontos.
 *
 * Define o contrato para operações de persistência de registros de ponto,
 * seguindo o princípio de inversão de dependência (DIP). A implementação
 * concreta fica na camada de dados.
 *
 * @author Thiago
 * @since 1.0.0
 */
interface PontoRepository {

    // ========================================================================
    // Operações de Escrita (CRUD)
    // ========================================================================

    /**
     * Insere um novo registro de ponto.
     *
     * @param ponto Ponto a ser inserido
     * @return ID gerado para o novo registro
     */
    suspend fun inserir(ponto: Ponto): Long

    /**
     * Atualiza um registro de ponto existente.
     *
     * @param ponto Ponto com os dados atualizados
     */
    suspend fun atualizar(ponto: Ponto)

    /**
     * Remove um registro de ponto.
     *
     * @param ponto Ponto a ser removido
     */
    suspend fun excluir(ponto: Ponto)

    // ========================================================================
    // Operações de Leitura (Queries)
    // ========================================================================

    /**
     * Busca um ponto pelo seu ID.
     *
     * @param id Identificador único do ponto
     * @return Ponto encontrado ou null se não existir
     */
    suspend fun buscarPorId(id: Long): Ponto?

    /**
     * Busca todos os pontos de uma data específica (suspensa).
     *
     * @param data Data para filtrar os pontos
     * @return Lista de pontos ordenados por hora
     */
    suspend fun buscarPontosPorData(data: LocalDate): List<Ponto>

    /**
     * Busca o último ponto registrado em uma data.
     *
     * @param data Data para buscar
     * @return Último ponto do dia ou null se não houver
     */
    suspend fun buscarUltimoPontoDoDia(data: LocalDate): Ponto?

    // ========================================================================
    // Operações Reativas (Flows)
    // ========================================================================

    /**
     * Observa os pontos de uma data específica de forma reativa.
     *
     * @param data Data para observar
     * @return Flow que emite a lista atualizada sempre que houver mudanças
     */
    fun observarPontosPorData(data: LocalDate): Flow<List<Ponto>>

    /**
     * Observa todos os pontos de forma reativa.
     *
     * @return Flow que emite a lista completa sempre que houver mudanças
     */
    fun observarTodos(): Flow<List<Ponto>>

    /**
     * Observa os pontos de um período de forma reativa.
     *
     * @param dataInicio Data inicial do período (inclusive)
     * @param dataFim Data final do período (inclusive)
     * @return Flow que emite a lista atualizada sempre que houver mudanças
     */
    fun observarPontosPorPeriodo(dataInicio: LocalDate, dataFim: LocalDate): Flow<List<Ponto>>
}
