// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/repository/PontoRepository.kt
package br.com.tlmacedo.meuponto.domain.repository

import br.com.tlmacedo.meuponto.domain.model.Ponto
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * Interface do repositório de pontos.
 *
 * Define o contrato para operações de persistência de registros de ponto,
 * seguindo o princípio de inversão de dependência (DIP).
 *
 * @author Thiago
 * @since 1.0.0
 * @updated 6.1.0 - Removidos 5 métodos deprecados (sem filtro por emprego)
 */
interface PontoRepository {

    // ========================================================================
    // Operações de Escrita (CRUD)
    // ========================================================================

    suspend fun inserir(ponto: Ponto): Long
    suspend fun atualizar(ponto: Ponto)
    suspend fun excluir(ponto: Ponto)
    suspend fun excluirPorId(id: Long)

    // ========================================================================
    // Operações de Leitura - Por ID
    // ========================================================================

    suspend fun buscarPorId(id: Long): Ponto?
    fun observarPorId(id: Long): Flow<Ponto?>

    // ========================================================================
    // Operações de Leitura - Por Emprego
    // ========================================================================

    fun observarPorEmprego(empregoId: Long): Flow<List<Ponto>>
    suspend fun buscarPrimeiraData(empregoId: Long): LocalDate?
    fun observarPorEmpregoEData(empregoId: Long, data: LocalDate): Flow<List<Ponto>>
    suspend fun buscarPorEmpregoEData(empregoId: Long, data: LocalDate): List<Ponto>

    fun observarPorEmpregoEPeriodo(
        empregoId: Long,
        dataInicio: LocalDate,
        dataFim: LocalDate
    ): Flow<List<Ponto>>

    suspend fun buscarPorEmpregoEPeriodo(
        empregoId: Long,
        dataInicio: LocalDate,
        dataFim: LocalDate
    ): List<Ponto>

    // ========================================================================
    // Operações de Leitura - Por Marcador
    // ========================================================================

    fun observarPorMarcador(marcadorId: Long): Flow<List<Ponto>>

    // ========================================================================
    // Operações de Contagem
    // ========================================================================

    suspend fun contarPorEmprego(empregoId: Long): Int
    suspend fun contarPorEmpregoEData(empregoId: Long, data: LocalDate): Int
    suspend fun contarPorEmpregoEPeriodo(
        empregoId: Long,
        dataInicio: LocalDate,
        dataFim: LocalDate
    ): Int

    // ========================================================================
    // Operações Auxiliares
    // ========================================================================

    fun observarDatasComRegistro(empregoId: Long): Flow<List<LocalDate>>
    suspend fun buscarUltimoPonto(empregoId: Long): Ponto?
    fun observarUltimoPonto(empregoId: Long): Flow<Ponto?>

    // ========================================================================
    // Operações de Migração
    // ========================================================================

    suspend fun migrarParaEmprego(empregoIdOrigem: Long, empregoIdDestino: Long): Int
}
