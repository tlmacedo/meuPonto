// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/data/repository/AusenciaRepositoryImpl.kt
package br.com.tlmacedo.meuponto.data.repository

import br.com.tlmacedo.meuponto.data.local.database.dao.AusenciaDao
import br.com.tlmacedo.meuponto.data.mapper.toDomain
import br.com.tlmacedo.meuponto.data.mapper.toEntity
import br.com.tlmacedo.meuponto.domain.model.ausencia.Ausencia
import br.com.tlmacedo.meuponto.domain.model.ausencia.TipoAusencia
import br.com.tlmacedo.meuponto.domain.repository.AusenciaRepository
import br.com.tlmacedo.meuponto.domain.service.AuditService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementação do repositório de ausências.
 *
 * @author Thiago
 * @since 4.0.0
 * @updated 11.0.0 - Integração com AuditService
 */
@Singleton
class AusenciaRepositoryImpl @Inject constructor(
    private val ausenciaDao: AusenciaDao,
    private val auditService: AuditService
) : AusenciaRepository {

    private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    // ========================================================================
    // CRUD
    // ========================================================================

    override suspend fun inserir(ausencia: Ausencia): Long {
        val id = ausenciaDao.inserir(ausencia.toEntity())

        auditService.logCreate(
            entidade = ENTIDADE,
            entidadeId = id,
            motivo = "Ausência criada: ${ausencia.tipo.descricao} de ${ausencia.dataInicio.format(dateFormatter)} a ${ausencia.dataFim.format(dateFormatter)}",
            novoValor = ausencia,
            serializer = { auditService.toJson(it.toAuditMap()) }
        )

        return id
    }

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

    override suspend fun atualizar(ausencia: Ausencia) {
        val anterior = ausenciaDao.buscarPorId(ausencia.id)?.toDomain()
        ausenciaDao.atualizar(ausencia.toEntity())

        auditService.logUpdate(
            entidade = ENTIDADE,
            entidadeId = ausencia.id,
            motivo = "Ausência atualizada: ${ausencia.tipo.descricao}",
            valorAntigo = anterior,
            valorNovo = ausencia,
            serializer = { auditService.toJson(it.toAuditMap()) }
        )
    }

    override suspend fun excluir(ausencia: Ausencia) {
        auditService.logPermanentDelete(
            entidade = ENTIDADE,
            entidadeId = ausencia.id,
            motivo = "Ausência excluída: ${ausencia.tipo.descricao} de ${ausencia.dataInicio.format(dateFormatter)} a ${ausencia.dataFim.format(dateFormatter)}"
        )

        ausenciaDao.excluir(ausencia.toEntity())
    }

    override suspend fun excluirPorId(id: Long) {
        val ausencia = ausenciaDao.buscarPorId(id)?.toDomain()

        auditService.logPermanentDelete(
            entidade = ENTIDADE,
            entidadeId = id,
            motivo = ausencia?.let {
                "Ausência excluída: ${it.tipo.descricao} de ${it.dataInicio.format(dateFormatter)} a ${it.dataFim.format(dateFormatter)}"
            } ?: "Ausência excluída"
        )

        ausenciaDao.excluirPorId(id)
    }

    // ========================================================================
    // Consultas por ID
    // ========================================================================

    override suspend fun buscarPorId(id: Long): Ausencia? {
        return ausenciaDao.buscarPorId(id)?.toDomain()
    }

    override fun observarPorId(id: Long): Flow<Ausencia?> {
        return ausenciaDao.observarPorId(id).map { it?.toDomain() }
    }

    // ========================================================================
    // Consultas Globais
    // ========================================================================

    override suspend fun buscarTodas(): List<Ausencia> {
        return ausenciaDao.buscarTodas().map { it.toDomain() }
    }

    override fun observarTodas(): Flow<List<Ausencia>> {
        return ausenciaDao.observarTodas().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun buscarTodasAtivas(): List<Ausencia> {
        return ausenciaDao.buscarTodasAtivas().map { it.toDomain() }
    }

    override fun observarTodasAtivas(): Flow<List<Ausencia>> {
        return ausenciaDao.observarTodasAtivas().map { list -> list.map { it.toDomain() } }
    }

    // ========================================================================
    // Consultas por Emprego
    // ========================================================================

    override suspend fun buscarPorEmprego(empregoId: Long): List<Ausencia> {
        return ausenciaDao.buscarPorEmprego(empregoId).map { it.toDomain() }
    }

    override fun observarPorEmprego(empregoId: Long): Flow<List<Ausencia>> {
        return ausenciaDao.observarPorEmprego(empregoId).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun buscarAtivasPorEmprego(empregoId: Long): List<Ausencia> {
        return ausenciaDao.buscarAtivasPorEmprego(empregoId).map { it.toDomain() }
    }

    override fun observarAtivasPorEmprego(empregoId: Long): Flow<List<Ausencia>> {
        return ausenciaDao.observarAtivasPorEmprego(empregoId)
            .map { list -> list.map { it.toDomain() } }
    }

    // ========================================================================
    // Consultas por Data
    // ========================================================================

    override suspend fun buscarPorData(empregoId: Long, data: LocalDate): List<Ausencia> {
        return ausenciaDao.buscarPorData(empregoId, data).map { it.toDomain() }
    }

    override fun observarPorData(empregoId: Long, data: LocalDate): Flow<List<Ausencia>> {
        return ausenciaDao.observarPorData(empregoId, data)
            .map { list -> list.map { it.toDomain() } }
    }

    override suspend fun buscarPorPeriodo(
        empregoId: Long,
        dataInicio: LocalDate,
        dataFim: LocalDate
    ): List<Ausencia> {
        return ausenciaDao.buscarPorPeriodo(empregoId, dataInicio, dataFim)
            .map { it.toDomain() }
    }

    override fun observarPorPeriodo(
        empregoId: Long,
        dataInicio: LocalDate,
        dataFim: LocalDate
    ): Flow<List<Ausencia>> {
        return ausenciaDao.observarPorPeriodo(empregoId, dataInicio, dataFim)
            .map { list -> list.map { it.toDomain() } }
    }

    // ========================================================================
    // Consultas por Tipo
    // ========================================================================

    override suspend fun buscarPorTipo(empregoId: Long, tipo: TipoAusencia): List<Ausencia> {
        return ausenciaDao.buscarPorTipo(empregoId, tipo).map { it.toDomain() }
    }

    override fun observarPorTipo(empregoId: Long, tipo: TipoAusencia): Flow<List<Ausencia>> {
        return ausenciaDao.observarPorTipo(empregoId, tipo)
            .map { list -> list.map { it.toDomain() } }
    }

    override suspend fun buscarFeriasPorPeriodoAquisitivo(
        empregoId: Long,
        inicio: LocalDate,
        fim: LocalDate
    ): List<Ausencia> {
        return ausenciaDao.buscarFeriasPorPeriodoAquisitivo(empregoId, inicio, fim)
            .map { it.toDomain() }
    }

    // ========================================================================
    // Consultas por Ano/Mês
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
        return ausenciaDao.buscarPorMes(empregoId, primeiroDia, ultimoDia)
            .map { it.toDomain() }
    }

    override fun observarPorMes(empregoId: Long, mes: YearMonth): Flow<List<Ausencia>> {
        val primeiroDia = mes.atDay(1)
        val ultimoDia = mes.atEndOfMonth()
        return ausenciaDao.observarPorMes(empregoId, primeiroDia, ultimoDia)
            .map { list -> list.map { it.toDomain() } }
    }

    // ========================================================================
    // Validações
    // ========================================================================

    override suspend fun existeSobreposicao(
        empregoId: Long,
        dataInicio: LocalDate,
        dataFim: LocalDate,
        excluirId: Long
    ): Boolean {
        return ausenciaDao.contarSobreposicoes(empregoId, dataInicio, dataFim, excluirId) > 0
    }

    override suspend fun existeAusenciaEmData(empregoId: Long, data: LocalDate): Boolean {
        return ausenciaDao.existeAusenciaEmData(empregoId, data)
    }

    // ========================================================================
    // Estatísticas
    // ========================================================================

    override suspend fun contarDiasPorTipo(
        empregoId: Long,
        tipo: TipoAusencia,
        dataInicio: LocalDate,
        dataFim: LocalDate
    ): Int {
        return ausenciaDao.contarDiasPorTipo(empregoId, tipo, dataInicio, dataFim)
    }

    override suspend fun contarPorEmprego(empregoId: Long): Int {
        return ausenciaDao.contarPorEmprego(empregoId)
    }

    // ========================================================================
    // Operações de Limpeza
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
            motivo = "Ausências anteriores a ${dataLimite.format(dateFormatter)} foram limpas"
        )

        ausenciaDao.limparAusenciasAntigas(dataLimite)
    }

    // ========================================================================
    // Helpers
    // ========================================================================

    private fun Ausencia.toAuditMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "empregoId" to empregoId,
        "tipo" to tipo.name,
        "tipoDescricao" to tipo.descricao,
        "dataInicio" to dataInicio.format(dateFormatter),
        "dataFim" to dataFim.format(dateFormatter),
        "observacao" to observacao,
        "ativa" to ativo
    )

    companion object {
        private const val ENTIDADE = "Ausencia"
    }
}
