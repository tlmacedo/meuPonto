// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/data/repository/FechamentoPeriodoRepositoryImpl.kt
package br.com.tlmacedo.meuponto.data.repository

import br.com.tlmacedo.meuponto.data.local.database.dao.FechamentoPeriodoDao
import br.com.tlmacedo.meuponto.data.local.database.entity.toDomain
import br.com.tlmacedo.meuponto.data.local.database.entity.toEntity
import br.com.tlmacedo.meuponto.domain.model.FechamentoPeriodo
import br.com.tlmacedo.meuponto.domain.model.TipoFechamento
import br.com.tlmacedo.meuponto.domain.repository.FechamentoPeriodoRepository
import br.com.tlmacedo.meuponto.domain.service.AuditService
import br.com.tlmacedo.meuponto.util.helper.dateFormatterSimples
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
 * @updated 6.4.0 - Novo método para buscar fechamento até uma data específica
 * @updated 11.0.0 - Integração com AuditService
 * @updated 12.0.0 - Refatorado para usar AuditedRepositoryBase
 */
@Singleton
class FechamentoPeriodoRepositoryImpl @Inject constructor(
    private val fechamentoDao: FechamentoPeriodoDao,
    auditService: AuditService
) : AuditedRepositoryBase<FechamentoPeriodo>(auditService, ENTIDADE), FechamentoPeriodoRepository {

    // ========================================================================
    // PONTE COM O DAO
    // ========================================================================

    override suspend fun daoInserir(domain: FechamentoPeriodo): Long = fechamentoDao.insert(domain.toEntity())
    override suspend fun daoBuscarPorId(id: Long): FechamentoPeriodo? = fechamentoDao.getById(id)?.toDomain()
    override suspend fun daoAtualizar(domain: FechamentoPeriodo) = fechamentoDao.update(domain.toEntity())
    override suspend fun daoExcluir(domain: FechamentoPeriodo) = fechamentoDao.delete(domain.toEntity())
    override fun getEntityId(domain: FechamentoPeriodo): Long = domain.id

    override fun FechamentoPeriodo.toAuditMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "empregoId" to empregoId,
        "tipo" to tipo.name,
        "dataFechamento" to dataFechamento.format(dateFormatterSimples),
        "dataInicioPeriodo" to dataInicioPeriodo.format(dateFormatterSimples),
        "dataFimPeriodo" to dataFimPeriodo.format(dateFormatterSimples),
        "saldoAnteriorMinutos" to saldoAnteriorMinutos,
        "observacao" to observacao
    )

    // ========================================================================
    // MOTIVOS DE AUDITORIA
    // ========================================================================

    override fun motivoInserir(domain: FechamentoPeriodo): String =
        "Fechamento ${domain.tipo.name} criado: ${domain.dataInicioPeriodo.format(dateFormatterSimples)} a ${domain.dataFimPeriodo.format(dateFormatterSimples)}"

    override fun motivoAtualizar(domain: FechamentoPeriodo): String =
        "Fechamento ${domain.tipo.name} atualizado"

    override fun motivoExcluir(domain: FechamentoPeriodo): String =
        "Fechamento ${domain.tipo.name} excluído: ${domain.dataInicioPeriodo.format(dateFormatterSimples)} a ${domain.dataFimPeriodo.format(dateFormatterSimples)}"

    // ========================================================================
    // CRUD
    // ========================================================================

    override suspend fun inserir(fechamento: FechamentoPeriodo): Long = inserirComAuditoria(fechamento)

    override suspend fun atualizar(fechamento: FechamentoPeriodo) = atualizarComAuditoria(fechamento)

    override suspend fun excluir(fechamento: FechamentoPeriodo) = excluirComAuditoria(fechamento)

    // ========================================================================
    // CONSULTAS
    // ========================================================================

    override suspend fun buscarPorId(id: Long): FechamentoPeriodo? = daoBuscarPorId(id)

    override fun observarPorEmpregoId(empregoId: Long): Flow<List<FechamentoPeriodo>> =
        fechamentoDao.observeByEmpregoId(empregoId).map { list -> list.map { it.toDomain() } }

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
        fechamentoDao.observeFechamentosBancoHoras(empregoId).map { list -> list.map { it.toDomain() } }

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

    companion object {
        private const val ENTIDADE = "FechamentoPeriodo"
    }
}
