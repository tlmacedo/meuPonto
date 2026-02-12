package br.com.tlmacedo.meuponto.domain.repository

import br.com.tlmacedo.meuponto.domain.model.Ponto
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * Interface do repositório de pontos.
 *
 * Define o contrato para operações de persistência de registros de ponto.
 * A implementação concreta está na camada Data.
 *
 * @author Thiago
 * @since 1.0.0
 */
interface PontoRepository {

    /**
     * Insere um novo registro de ponto.
     *
     * @param ponto Ponto a ser inserido
     * @return ID do registro inserido
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
    suspend fun deletar(ponto: Ponto)

    /**
     * Busca um ponto pelo ID.
     *
     * @param id ID do ponto
     * @return Ponto encontrado ou null
     */
    suspend fun buscarPorId(id: Long): Ponto?

    /**
     * Observa todos os pontos de uma data específica.
     *
     * @param data Data para buscar os pontos
     * @return Flow com a lista de pontos do dia
     */
    fun observarPontosPorData(data: LocalDate): Flow<List<Ponto>>

    /**
     * Busca todos os pontos de uma data específica.
     *
     * @param data Data para buscar os pontos
     * @return Lista de pontos do dia
     */
    suspend fun buscarPontosPorData(data: LocalDate): List<Ponto>

    /**
     * Observa todos os pontos de um período.
     *
     * @param dataInicio Data inicial do período
     * @param dataFim Data final do período
     * @return Flow com a lista de pontos do período
     */
    fun observarPontosPorPeriodo(dataInicio: LocalDate, dataFim: LocalDate): Flow<List<Ponto>>

    /**
     * Busca o último ponto registrado em uma data.
     *
     * @param data Data para buscar
     * @return Último ponto do dia ou null
     */
    suspend fun buscarUltimoPontoDoDia(data: LocalDate): Ponto?
}
