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
 * @updated 3.0.0 - Novos métodos para ciclos de banco de horas
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

    suspend fun excluirPorEmpregoId(empregoId: Long)

    suspend fun contarPorEmpregoId(empregoId: Long): Int
}
