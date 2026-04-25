// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/data/repository/FechamentoPeriodoRepositoryImpl.kt
package br.com.tlmacedo.meuponto.data.repository

import br.com.tlmacedo.meuponto.data.local.database.dao.FechamentoPeriodoDao
import br.com.tlmacedo.meuponto.data.local.database.entity.toDomain
import br.com.tlmacedo.meuponto.data.local.database.entity.toEntity
import br.com.tlmacedo.meuponto.domain.model.FechamentoPeriodo
import br.com.tlmacedo.meuponto.domain.model.TipoFechamento
import br.com.tlmacedo.meuponto.domain.repository.FechamentoPeriodoRepository
import br.com.tlmacedo.meuponto.domain.service.AuditService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementação do repositório de fechamentos de período.
 *
 * @author Thiago
 * @since 2.0.0
 * @updated 6.4.0 - Novo método para buscar fechamento até uma data específica
 * @updated 11.0.0 - Integração com AuditService
 */
@Singleton
class FechamentoPeriodoRepositoryImpl @Inject constructor(
    private val fechamentoDao: FechamentoPeriodoDao,
    private val auditService: AuditService
) : FechamentoPeriodoRepository {

    private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    override suspend fun inserir(fechamento: FechamentoPeriodo): Long {
        val id = fechamentoDao.insert(fechamento.toEntity())

        auditService.logCreate(
            entidade = ENTIDADE,
            entidadeId = id,
            motivo = "Fechamento ${fechamento.tipo.name} criado: ${
                fechamento.dataInicioPeriodo.format(
                    dateFormatter
                )
            } a ${fechamento.dataFimPeriodo.format(dateFormatter)}",
            novoValor = fechamento,
            serializer = { auditService.toJson(it.toAuditMap()) }
        )

        return id
    }

    override suspend fun atualizar(fechamento: FechamentoPeriodo) {
        val anterior = fechamentoDao.getById(fechamento.id)?.toDomain()
        fechamentoDao.update(fechamento.toEntity())

        auditService.logUpdate(
            entidade = ENTIDADE,
            entidadeId = fechamento.id,
            motivo = "Fechamento ${fechamento.tipo.name} atualizado",
            valorAntigo = anterior,
            valorNovo = fechamento,
            serializer = { auditService.toJson(it.toAuditMap()) }
        )
    }

    override suspend fun excluir(fechamento: FechamentoPeriodo) {
        auditService.logPermanentDelete(
            entidade = ENTIDADE,
            entidadeId = fechamento.id,
            motivo = "Fechamento ${fechamento.tipo.name} excluído: ${
                fechamento.dataInicioPeriodo.format(
                    dateFormatter
                )
            } a ${fechamento.dataFimPeriodo.format(dateFormatter)}"

        )

        fechamentoDao.delete(fechamento.toEntity())
    }

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

    override suspend fun buscarUltimoFechamentoBancoAteData(
        empregoId: Long,
        ateData: LocalDate
    ): FechamentoPeriodo? =
        fechamentoDao.getUltimoFechamentoBancoAteData(empregoId, ateData)?.toDomain()

    override suspend fun excluirPorEmpregoId(empregoId: Long) {
        auditService.logPermanentDelete(
            entidade = ENTIDADE,
            entidadeId = empregoId,
            motivo = "Todos os fechamentos do emprego $empregoId foram excluídos"
        )

        fechamentoDao.deleteByEmpregoId(empregoId)
    }

    override suspend fun contarPorEmpregoId(empregoId: Long): Int =
        fechamentoDao.countByEmpregoId(empregoId)

    // ========================================================================
    // Helpers
    // ========================================================================

    private fun FechamentoPeriodo.toAuditMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "empregoId" to empregoId,
        "tipo" to tipo.name,
        "dataFechamento" to dataFechamento.format(dateFormatter),
        "dataInicioPeriodo" to dataInicioPeriodo.format(dateFormatter),
        "dataFimPeriodo" to dataFimPeriodo.format(dateFormatter),
        "saldoAnteriorMinutos" to saldoAnteriorMinutos,
        "observacao" to observacao
    )

    companion object {
        private const val ENTIDADE = "FechamentoPeriodo"
    }
}
