// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/data/repository/AusenciaRepositoryImpl.kt
package br.com.tlmacedo.meuponto.data.repository

import br.com.tlmacedo.meuponto.data.local.database.dao.AusenciaDao
import br.com.tlmacedo.meuponto.data.mapper.toDomain
import br.com.tlmacedo.meuponto.data.mapper.toEntity
import br.com.tlmacedo.meuponto.domain.model.ausencia.Ausencia
import br.com.tlmacedo.meuponto.domain.model.ausencia.TipoAusencia
import br.com.tlmacedo.meuponto.domain.repository.AusenciaRepository
import br.com.tlmacedo.meuponto.domain.service.AuditService
import br.com.tlmacedo.meuponto.util.helper.dateFormatterSimples
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementação do repositório de ausências.
 *
 * @author Thiago
 * @since 4.0.0
 * @updated 11.0.0 - Integração com AuditService
 * @updated 12.0.0 - Refatorado para usar AuditedRepositoryBase
 */
@Singleton
class AusenciaRepositoryImpl @Inject constructor(
    private val ausenciaDao: AusenciaDao,
    auditService: AuditService
) : AuditedRepositoryBase<Ausencia>(auditService, ENTIDADE), AusenciaRepository {

    // ========================================================================
    // PONTE COM O DAO
    // ========================================================================

    override suspend fun daoInserir(domain: Ausencia): Long = ausenciaDao.inserir(domain.toEntity())
    override suspend fun daoBuscarPorId(id: Long): Ausencia? = ausenciaDao.buscarPorId(id)?.toDomain()
    override suspend fun daoAtualizar(domain: Ausencia) = ausenciaDao.atualizar(domain.toEntity())
    override suspend fun daoExcluir(domain: Ausencia) = ausenciaDao.excluir(domain.toEntity())
    override fun getEntityId(domain: Ausencia): Long = domain.id

    override fun Ausencia.toAuditMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "empregoId" to empregoId,
        "tipo" to tipo.descricao,
        "tipoDescricao" to tipo.descricao,
        "dataInicio" to dataInicio.format(dateFormatterSimples),
        "dataFim" to dataFim.format(dateFormatterSimples),
        "observacao" to observacao,
        "ativa" to ativo
    )

    // ========================================================================
    // MOTIVOS DE AUDITORIA
    // ========================================================================

    override fun motivoInserir(domain: Ausencia): String =
        "Ausência criada: ${domain.tipo.descricao} de ${domain.dataInicio.format(dateFormatterSimples)} a ${domain.dataFim.format(dateFormatterSimples)}"

    override fun motivoAtualizar(domain: Ausencia): String =
        "Ausência atualizada: ${domain.tipo.descricao}"

    override fun motivoExcluir(domain: Ausencia): String =
        "Ausência excluída: ${domain.tipo.descricao} de ${domain.dataInicio.format(dateFormatterSimples)} a ${domain.dataFim.format(dateFormatterSimples)}"

    // ========================================================================
    // CRUD
    // ========================================================================

    override suspend fun inserir(ausencia: Ausencia): Long = inserirComAuditoria(ausencia)

    override suspend fun inserirTodas(ausencias: List<Ausencia>): List<Long> {
        val ids = ausenciaDao.inserirTodas(ausencias.map { it.toEntity() })
        ausencias.forEachIndexed { index, ausencia ->
            auditService.logCreate(
                entidade = ENTIDADE,
                entidadeId = ids[index],
                motivo = "Ausência criada em lote: ${ausencia.tipo.descricao}",
                novoValor = ausencia,
                serializer = { auditService.toJson(it.toAuditMap()) }
            )
        }
        return ids
    }

    override suspend fun atualizar(ausencia: Ausencia) = atualizarComAuditoria(ausencia)

    override suspend fun excluir(ausencia: Ausencia) = excluirComAuditoria(ausencia)

    override suspend fun excluirPorId(id: Long) = excluirPorIdComAuditoria(id) { ausenciaDao.excluirPorId(it) }

    // ========================================================================
    // CONSULTAS POR ID
    // ========================================================================

    override suspend fun buscarPorId(id: Long): Ausencia? = daoBuscarPorId(id)

    override fun observarPorId(id: Long): Flow<Ausencia?> =
        ausenciaDao.observarPorId(id).map { it?.toDomain() }

    // ========================================================================
    // CONSULTAS GLOBAIS
    // ========================================================================

    override suspend fun buscarTodas(): List<Ausencia> =
        ausenciaDao.buscarTodas().map { it.toDomain() }

    override fun observarTodas(): Flow<List<Ausencia>> =
        ausenciaDao.observarTodas().map { list -> list.map { it.toDomain() } }

    override suspend fun buscarTodasAtivas(): List<Ausencia> =
        ausenciaDao.buscarTodasAtivas().map { it.toDomain() }

    override fun observarTodasAtivas(): Flow<List<Ausencia>> =
        ausenciaDao.observarTodasAtivas().map { list -> list.map { it.toDomain() } }

    // ========================================================================
    // CONSULTAS POR EMPREGO
    // ========================================================================

    override suspend fun buscarPorEmprego(empregoId: Long): List<Ausencia> =
        ausenciaDao.buscarPorEmprego(empregoId).map { it.toDomain() }

    override fun observarPorEmprego(empregoId: Long): Flow<List<Ausencia>> =
        ausenciaDao.observarPorEmprego(empregoId).map { list -> list.map { it.toDomain() } }

    override suspend fun buscarAtivasPorEmprego(empregoId: Long): List<Ausencia> =
        ausenciaDao.buscarAtivasPorEmprego(empregoId).map { it.toDomain() }

    override fun observarAtivasPorEmprego(empregoId: Long): Flow<List<Ausencia>> =
        ausenciaDao.observarAtivasPorEmprego(empregoId).map { list -> list.map { it.toDomain() } }

    // ========================================================================
    // CONSULTAS POR DATA
    // ========================================================================

    override suspend fun buscarPorData(empregoId: Long, data: LocalDate): List<Ausencia> =
        ausenciaDao.buscarPorData(empregoId, data).map { it.toDomain() }

    override fun observarPorData(empregoId: Long, data: LocalDate): Flow<List<Ausencia>> =
        ausenciaDao.observarPorData(empregoId, data).map { list -> list.map { it.toDomain() } }

    override suspend fun buscarPorPeriodo(
        empregoId: Long,
        dataInicio: LocalDate,
        dataFim: LocalDate
    ): List<Ausencia> =
        ausenciaDao.buscarPorPeriodo(empregoId, dataInicio, dataFim).map { it.toDomain() }

    override fun observarPorPeriodo(
        empregoId: Long,
        dataInicio: LocalDate,
        dataFim: LocalDate
    ): Flow<List<Ausencia>> =
        ausenciaDao.observarPorPeriodo(empregoId, dataInicio, dataFim)
            .map { list -> list.map { it.toDomain() } }

    // ========================================================================
    // CONSULTAS POR TIPO
    // ========================================================================

    override suspend fun buscarPorTipo(empregoId: Long, tipo: TipoAusencia): List<Ausencia> =
        ausenciaDao.buscarPorTipo(empregoId, tipo).map { it.toDomain() }

    override fun observarPorTipo(empregoId: Long, tipo: TipoAusencia): Flow<List<Ausencia>> =
        ausenciaDao.observarPorTipo(empregoId, tipo).map { list -> list.map { it.toDomain() } }

    override suspend fun buscarFeriasPorPeriodoAquisitivo(
        empregoId: Long,
        inicio: LocalDate,
        fim: LocalDate
    ): List<Ausencia> =
        ausenciaDao.buscarFeriasPorPeriodoAquisitivo(empregoId, inicio, fim).map { it.toDomain() }

    // ========================================================================
    // CONSULTAS POR ANO/MÊS
    // ========================================================================

    override suspend fun buscarPorAno(empregoId: Long, ano: Int): List<Ausencia> {
        val anoInicio = LocalDate.of(ano, 1, 1)
        val anoFim = LocalDate.of(ano, 12, 31)
        return ausenciaDao.buscarPorAno(empregoId, ano.toString(), anoInicio, anoFim)
            .map { it.toDomain() }
    }

    override suspend fun buscarPorMes(empregoId: Long, mes: YearMonth): List<Ausencia> {
        val primeiroDia = mes.atDay(1)
        val ultimoDia = mes.atEndOfMonth()
        return ausenciaDao.buscarPorMes(empregoId, primeiroDia, ultimoDia).map { it.toDomain() }
    }

    override fun observarPorMes(empregoId: Long, mes: YearMonth): Flow<List<Ausencia>> {
        val primeiroDia = mes.atDay(1)
        val ultimoDia = mes.atEndOfMonth()
        return ausenciaDao.observarPorMes(empregoId, primeiroDia, ultimoDia)
            .map { list -> list.map { it.toDomain() } }
    }

    // ========================================================================
    // VALIDAÇÕES
    // ========================================================================

    override suspend fun existeSobreposicao(
        empregoId: Long,
        dataInicio: LocalDate,
        dataFim: LocalDate,
        excluirId: Long
    ): Boolean = ausenciaDao.contarSobreposicoes(empregoId, dataInicio, dataFim, excluirId) > 0

    override suspend fun existeAusenciaEmData(empregoId: Long, data: LocalDate): Boolean =
        ausenciaDao.existeAusenciaEmData(empregoId, data)

    // ========================================================================
    // ESTATÍSTICAS
    // ========================================================================

    override suspend fun contarDiasPorTipo(
        empregoId: Long,
        tipo: TipoAusencia,
        dataInicio: LocalDate,
        dataFim: LocalDate
    ): Int = ausenciaDao.contarDiasPorTipo(empregoId, tipo, dataInicio, dataFim)

    override suspend fun contarPorEmprego(empregoId: Long): Int =
        ausenciaDao.contarPorEmprego(empregoId)

    // ========================================================================
    // OPERAÇÕES DE LIMPEZA
    // ========================================================================

    override suspend fun desativarPorEmprego(empregoId: Long) {
        auditService.logUpdate(
            entidade = ENTIDADE,
            entidadeId = empregoId,
            motivo = "Todas as ausências do emprego $empregoId foram desativadas",
            valorAntigo = "ativo=true",
            valorNovo = "ativo=false",
            serializer = { it }
        )
        ausenciaDao.desativarPorEmprego(empregoId)
    }

    override suspend fun limparAusenciasAntigas(dataLimite: LocalDate) {
        auditService.logPermanentDelete(
            entidade = ENTIDADE,
            entidadeId = 0L,
            motivo = "Ausências anteriores a ${dataLimite.format(dateFormatterSimples)} foram limpas"
        )
        ausenciaDao.limparAusenciasAntigas(dataLimite)
    }

    companion object {
        private const val ENTIDADE = "Ausencia"
    }
}
