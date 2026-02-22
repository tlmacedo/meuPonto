// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/data/repository/FechamentoPeriodoRepositoryImpl.kt
package br.com.tlmacedo.meuponto.data.repository

import br.com.tlmacedo.meuponto.data.local.database.dao.FechamentoPeriodoDao
import br.com.tlmacedo.meuponto.data.local.database.entity.toDomain
import br.com.tlmacedo.meuponto.data.local.database.entity.toEntity
import br.com.tlmacedo.meuponto.domain.model.FechamentoPeriodo
import br.com.tlmacedo.meuponto.domain.model.TipoFechamento
import br.com.tlmacedo.meuponto.domain.repository.FechamentoPeriodoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementação do repositório de fechamentos de período.
 *
 * @author Thiago
 * @since 2.0.0
 * @updated 3.0.0 - Suporte a ciclos de banco de horas
 */
@Singleton
class FechamentoPeriodoRepositoryImpl @Inject constructor(
    private val fechamentoDao: FechamentoPeriodoDao
) : FechamentoPeriodoRepository {

    override suspend fun inserir(fechamento: FechamentoPeriodo): Long =
        fechamentoDao.insert(fechamento.toEntity())

    override suspend fun atualizar(fechamento: FechamentoPeriodo) =
        fechamentoDao.update(fechamento.toEntity())

    override suspend fun excluir(fechamento: FechamentoPeriodo) =
        fechamentoDao.delete(fechamento.toEntity())

    override suspend fun buscarPorId(id: Long): FechamentoPeriodo? =
        fechamentoDao.getById(id)?.toDomain()

    override fun observarPorEmpregoId(empregoId: Long): Flow<List<FechamentoPeriodo>> =
        fechamentoDao.observeByEmpregoId(empregoId).map { list ->
            list.map { it.toDomain() }
        }

    override suspend fun buscarPorEmpregoId(empregoId: Long): List<FechamentoPeriodo> =
        fechamentoDao.getByEmpregoId(empregoId).map { it.toDomain() }

    override suspend fun buscarPorEmpregoIdETipo(
        empregoId: Long,
        tipo: TipoFechamento
    ): List<FechamentoPeriodo> =
        fechamentoDao.getByEmpregoIdAndTipo(empregoId, tipo).map { it.toDomain() }

    override suspend fun buscarFechamentosBancoHoras(empregoId: Long): List<FechamentoPeriodo> =
        fechamentoDao.getFechamentosBancoHoras(empregoId).map { it.toDomain() }

    override fun observarFechamentosBancoHoras(empregoId: Long): Flow<List<FechamentoPeriodo>> =
        fechamentoDao.observeFechamentosBancoHoras(empregoId).map { list ->
            list.map { it.toDomain() }
        }

    override suspend fun buscarPorData(empregoId: Long, data: LocalDate): FechamentoPeriodo? =
        fechamentoDao.buscarPorData(empregoId, data)?.toDomain()

    override suspend fun buscarPorPeriodo(
        empregoId: Long,
        dataInicio: LocalDate,
        dataFim: LocalDate
    ): List<FechamentoPeriodo> =
        fechamentoDao.getByPeriodo(empregoId, dataInicio, dataFim).map { it.toDomain() }

    override suspend fun buscarUltimoFechamento(empregoId: Long): FechamentoPeriodo? =
        fechamentoDao.getUltimoFechamento(empregoId)?.toDomain()

    override suspend fun buscarUltimoFechamentoBanco(empregoId: Long): FechamentoPeriodo? =
        fechamentoDao.getUltimoFechamentoBanco(empregoId)?.toDomain()

    override suspend fun excluirPorEmpregoId(empregoId: Long) =
        fechamentoDao.deleteByEmpregoId(empregoId)

    override suspend fun contarPorEmpregoId(empregoId: Long): Int =
        fechamentoDao.countByEmpregoId(empregoId)
}
