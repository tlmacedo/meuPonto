// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/repository/PontoRepository.kt
package br.com.tlmacedo.meuponto.domain.repository

import br.com.tlmacedo.meuponto.domain.model.Ponto
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * Interface do repositório de Pontos.
 *
 * @author Thiago
 * @since 1.0.0
 * @updated 11.0.0 - Adicionado suporte completo a soft delete e lixeira
 */
interface PontoRepository {

    // === Operações básicas ===

    suspend fun inserir(ponto: Ponto): Long

    suspend fun atualizar(ponto: Ponto)

    suspend fun excluir(ponto: Ponto)

    // === Consultas por ID ===

    suspend fun buscarPorId(id: Long): Ponto?

    fun observarPorId(id: Long): Flow<Ponto?>

    // === Consultas gerais ===

    fun listarTodos(): Flow<List<Ponto>>

    fun observarTodos(): Flow<List<Ponto>>

    // === Consultas por Emprego ===

    fun listarPorEmprego(empregoId: Long): Flow<List<Ponto>>

    fun observarPorEmprego(empregoId: Long): Flow<List<Ponto>>

    suspend fun contarPorEmprego(empregoId: Long): Int

    suspend fun buscarPrimeiraData(empregoId: Long): LocalDate?

    // === Consultas por Data ===

    fun listarPorData(data: LocalDate): Flow<List<Ponto>>

    suspend fun buscarPorEmpregoEData(empregoId: Long, data: LocalDate): List<Ponto>

    fun observarPorEmpregoEData(empregoId: Long, data: LocalDate): Flow<List<Ponto>>

    // === Consultas por Período ===

    fun listarPorPeriodo(dataInicio: LocalDate, dataFim: LocalDate): Flow<List<Ponto>>

    fun listarPorEmpregoEPeriodo(
        empregoId: Long,
        dataInicio: LocalDate,
        dataFim: LocalDate
    ): Flow<List<Ponto>>

    suspend fun buscarPorEmpregoEPeriodo(
        empregoId: Long,
        dataInicio: LocalDate,
        dataFim: LocalDate
    ): List<Ponto>

    fun observarPorEmpregoEPeriodo(
        empregoId: Long,
        dataInicio: LocalDate,
        dataFim: LocalDate
    ): Flow<List<Ponto>>

    // === Atualização de foto ===

    suspend fun atualizarFotoComprovante(pontoId: Long, fotoPath: String?)

    // === Soft Delete e Lixeira ===

    suspend fun buscarPorIdIncluindoDeletados(id: Long): Ponto?

    suspend fun listarDeletados(): List<Ponto>

    fun observarDeletados(): Flow<List<Ponto>>

    suspend fun softDelete(pontoId: Long)

    suspend fun excluirPermanente(pontoId: Long)

    suspend fun contarDeletados(): Int

    suspend fun restaurar(pontoId: Long)

    // === Manutenção ===

    suspend fun excluirPontosAnterioresA(data: LocalDate): Int
}
