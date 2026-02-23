// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/repository/FechamentoPeriodoRepository.kt
package br.com.tlmacedo.meuponto.domain.repository

import br.com.tlmacedo.meuponto.domain.model.FechamentoPeriodo
import br.com.tlmacedo.meuponto.domain.model.TipoFechamento
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * Interface do repositório para operações com fechamentos de período.
 *
 * @author Thiago
 * @since 2.0.0
 * @updated 6.4.0 - Novo método para buscar fechamento até uma data específica
 */
interface FechamentoPeriodoRepository {

    suspend fun inserir(fechamento: FechamentoPeriodo): Long

    suspend fun atualizar(fechamento: FechamentoPeriodo)

    suspend fun excluir(fechamento: FechamentoPeriodo)

    suspend fun buscarPorId(id: Long): FechamentoPeriodo?

    fun observarPorEmpregoId(empregoId: Long): Flow<List<FechamentoPeriodo>>

    suspend fun buscarPorEmpregoId(empregoId: Long): List<FechamentoPeriodo>

    suspend fun buscarPorEmpregoIdETipo(empregoId: Long, tipo: TipoFechamento): List<FechamentoPeriodo>

    suspend fun buscarFechamentosBancoHoras(empregoId: Long): List<FechamentoPeriodo>

    fun observarFechamentosBancoHoras(empregoId: Long): Flow<List<FechamentoPeriodo>>

    suspend fun buscarPorData(empregoId: Long, data: LocalDate): FechamentoPeriodo?

    suspend fun buscarPorPeriodo(
        empregoId: Long,
        dataInicio: LocalDate,
        dataFim: LocalDate
    ): List<FechamentoPeriodo>

    suspend fun buscarUltimoFechamento(empregoId: Long): FechamentoPeriodo?

    suspend fun buscarUltimoFechamentoBanco(empregoId: Long): FechamentoPeriodo?

    /**
     * Busca o último fechamento de banco de horas que TERMINOU ANTES de uma data específica.
     *
     * Usado para calcular o banco de horas histórico quando navegamos para datas passadas.
     * Por exemplo: se estamos em 05/02/2026 e existe um fechamento que terminou em 10/02/2026,
     * esse fechamento NÃO deve ser considerado (o ciclo ainda não tinha terminado naquela data).
     *
     * @param empregoId ID do emprego
     * @param ateData Data limite (exclusive) - só retorna fechamentos com dataFimPeriodo < ateData
     * @return Último fechamento anterior à data, ou null se não houver
     */
    suspend fun buscarUltimoFechamentoBancoAteData(
        empregoId: Long,
        ateData: LocalDate
    ): FechamentoPeriodo?

    suspend fun excluirPorEmpregoId(empregoId: Long)

    suspend fun contarPorEmpregoId(empregoId: Long): Int
}
