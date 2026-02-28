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
 * @updated 2.0.0 - Adicionado suporte a múltiplos empregos e marcadores
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

    /**
     * Remove um registro de ponto pelo ID.
     *
     * @param id Identificador único do ponto
     */
    suspend fun excluirPorId(id: Long)

    // ========================================================================
    // Operações de Leitura - Por ID
    // ========================================================================

    /**
     * Busca um ponto pelo seu ID.
     *
     * @param id Identificador único do ponto
     * @return Ponto encontrado ou null se não existir
     */
    suspend fun buscarPorId(id: Long): Ponto?

    /**
     * Observa um ponto específico de forma reativa.
     *
     * @param id Identificador único do ponto
     * @return Flow que emite o ponto atualizado sempre que houver mudanças
     */
    fun observarPorId(id: Long): Flow<Ponto?>

    // ========================================================================
    // Operações de Leitura - Legadas (sem filtro de emprego)
    // ========================================================================

    /**
     * Busca todos os pontos de uma data específica.
     * 
     * @param data Data para filtrar os pontos
     * @return Lista de pontos ordenados por hora
     * @deprecated Use [buscarPorEmpregoEData] para filtrar por emprego
     */
    @Deprecated(
        message = "Use buscarPorEmpregoEData para suporte a múltiplos empregos",
        replaceWith = ReplaceWith("buscarPorEmpregoEData(empregoId, data)")
    )
    suspend fun buscarPontosPorData(data: LocalDate): List<Ponto>

    /**
     * Busca o último ponto registrado em uma data.
     *
     * @param data Data para buscar
     * @return Último ponto do dia ou null se não houver
     * @deprecated Use [buscarUltimoPonto] com empregoId
     */
    @Deprecated(
        message = "Use buscarUltimoPonto(empregoId) para suporte a múltiplos empregos",
        replaceWith = ReplaceWith("buscarUltimoPonto(empregoId)")
    )
    suspend fun buscarUltimoPontoDoDia(data: LocalDate): Ponto?

    /**
     * Observa os pontos de uma data específica de forma reativa.
     *
     * @param data Data para observar
     * @return Flow que emite a lista atualizada sempre que houver mudanças
     * @deprecated Use [observarPorEmpregoEData] para filtrar por emprego
     */
    @Deprecated(
        message = "Use observarPorEmpregoEData para suporte a múltiplos empregos",
        replaceWith = ReplaceWith("observarPorEmpregoEData(empregoId, data)")
    )
    fun observarPontosPorData(data: LocalDate): Flow<List<Ponto>>

    /**
     * Observa todos os pontos de forma reativa.
     *
     * @return Flow que emite a lista completa sempre que houver mudanças
     * @deprecated Use [observarPorEmprego] para filtrar por emprego
     */
    @Deprecated(
        message = "Use observarPorEmprego para suporte a múltiplos empregos",
        replaceWith = ReplaceWith("observarPorEmprego(empregoId)")
    )
    fun observarTodos(): Flow<List<Ponto>>

    /**
     * Observa os pontos de um período de forma reativa.
     *
     * @param dataInicio Data inicial do período (inclusive)
     * @param dataFim Data final do período (inclusive)
     * @return Flow que emite a lista atualizada sempre que houver mudanças
     * @deprecated Use [observarPorEmpregoEPeriodo] para filtrar por emprego
     */
    @Deprecated(
        message = "Use observarPorEmpregoEPeriodo para suporte a múltiplos empregos",
        replaceWith = ReplaceWith("observarPorEmpregoEPeriodo(empregoId, dataInicio, dataFim)")
    )
    fun observarPontosPorPeriodo(dataInicio: LocalDate, dataFim: LocalDate): Flow<List<Ponto>>

    // ========================================================================
    // Operações de Leitura - Por Emprego (Novas)
    // ========================================================================

    /**
     * Observa todos os pontos de um emprego de forma reativa.
     *
     * @param empregoId ID do emprego
     * @return Flow que emite a lista ordenada por dataHora decrescente
     */
    fun observarPorEmprego(empregoId: Long): Flow<List<Ponto>>

    /**
     * Busca a primeira data com registro de ponto para um emprego.
     */
    suspend fun buscarPrimeiraData(empregoId: Long): LocalDate?

    /**
     * Observa os pontos de um emprego em uma data específica.
     *
     * @param empregoId ID do emprego
     * @param data Data para filtrar
     * @return Flow que emite a lista ordenada por hora crescente
     */
    fun observarPorEmpregoEData(empregoId: Long, data: LocalDate): Flow<List<Ponto>>

    /**
     * Busca os pontos de um emprego em uma data específica.
     *
     * @param empregoId ID do emprego
     * @param data Data para filtrar
     * @return Lista de pontos ordenados por hora crescente
     */
    suspend fun buscarPorEmpregoEData(empregoId: Long, data: LocalDate): List<Ponto>

    /**
     * Observa os pontos de um emprego em um período.
     *
     * @param empregoId ID do emprego
     * @param dataInicio Data inicial (inclusive)
     * @param dataFim Data final (inclusive)
     * @return Flow que emite a lista ordenada por data e hora crescentes
     */
    fun observarPorEmpregoEPeriodo(
        empregoId: Long,
        dataInicio: LocalDate,
        dataFim: LocalDate
    ): Flow<List<Ponto>>

    /**
     * Busca os pontos de um emprego em um período.
     *
     * @param empregoId ID do emprego
     * @param dataInicio Data inicial (inclusive)
     * @param dataFim Data final (inclusive)
     * @return Lista de pontos ordenados por data e hora crescentes
     */
    suspend fun buscarPorEmpregoEPeriodo(
        empregoId: Long,
        dataInicio: LocalDate,
        dataFim: LocalDate
    ): List<Ponto>

    // ========================================================================
    // Operações de Leitura - Por Marcador
    // ========================================================================

    /**
     * Observa os pontos associados a um marcador específico.
     *
     * @param marcadorId ID do marcador
     * @return Flow que emite a lista ordenada por dataHora decrescente
     */
    fun observarPorMarcador(marcadorId: Long): Flow<List<Ponto>>

    // ========================================================================
    // Operações de Contagem
    // ========================================================================

    /**
     * Conta o total de pontos de um emprego.
     *
     * @param empregoId ID do emprego
     * @return Quantidade de registros
     */
    suspend fun contarPorEmprego(empregoId: Long): Int

    /**
     * Conta os pontos de um emprego em uma data específica.
     *
     * @param empregoId ID do emprego
     * @param data Data para filtrar
     * @return Quantidade de registros
     */
    suspend fun contarPorEmpregoEData(empregoId: Long, data: LocalDate): Int

    /**
     * Conta os pontos de um emprego em um período.
     *
     * @param empregoId ID do emprego
     * @param dataInicio Data inicial (inclusive)
     * @param dataFim Data final (inclusive)
     * @return Quantidade de registros
     */
    suspend fun contarPorEmpregoEPeriodo(
        empregoId: Long,
        dataInicio: LocalDate,
        dataFim: LocalDate
    ): Int

    // ========================================================================
    // Operações Auxiliares
    // ========================================================================

    /**
     * Lista as datas que possuem registros de ponto para um emprego.
     *
     * @param empregoId ID do emprego
     * @return Flow que emite lista de datas ordenadas decrescentemente
     */
    fun observarDatasComRegistro(empregoId: Long): Flow<List<LocalDate>>

    /**
     * Busca o último ponto registrado de um emprego.
     *
     * @param empregoId ID do emprego
     * @return Último ponto ou null se não houver registros
     */
    suspend fun buscarUltimoPonto(empregoId: Long): Ponto?

    /**
     * Observa o último ponto registrado de um emprego de forma reativa.
     *
     * @param empregoId ID do emprego
     * @return Flow que emite o último ponto sempre que houver mudanças
     */
    fun observarUltimoPonto(empregoId: Long): Flow<Ponto?>

    // ========================================================================
    // Operações de Migração
    // ========================================================================

    /**
     * Migra todos os pontos de um emprego para outro.
     * Útil para reorganização ou fusão de empregos.
     *
     * @param empregoIdOrigem ID do emprego de origem
     * @param empregoIdDestino ID do emprego de destino
     * @return Quantidade de registros migrados
     */
    suspend fun migrarParaEmprego(empregoIdOrigem: Long, empregoIdDestino: Long): Int
}
