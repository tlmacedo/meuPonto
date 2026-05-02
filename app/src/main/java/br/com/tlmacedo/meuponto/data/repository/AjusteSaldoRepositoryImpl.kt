// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/data/repository/AjusteSaldoRepositoryImpl.kt
package br.com.tlmacedo.meuponto.data.repository

import br.com.tlmacedo.meuponto.data.local.database.dao.AjusteSaldoDao
import br.com.tlmacedo.meuponto.data.local.database.entity.toDomain
import br.com.tlmacedo.meuponto.data.local.database.entity.toEntity
import br.com.tlmacedo.meuponto.domain.model.AjusteSaldo
import br.com.tlmacedo.meuponto.domain.repository.AjusteSaldoRepository
import br.com.tlmacedo.meuponto.domain.service.AuditService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementação concreta do repositório de ajustes de saldo.
 *
 * @author Thiago
 * @since 2.0.0
 * @updated 11.0.0 - Integração com AuditService e suporte a TipoAjusteSaldo
 * @updated 12.0.0 - Refatorado para usar AuditedRepositoryBase
 */
@Singleton
class AjusteSaldoRepositoryImpl @Inject constructor(
    private val ajusteSaldoDao: AjusteSaldoDao,
    auditService: AuditService
) : AuditedRepositoryBase<AjusteSaldo>(auditService, ENTIDADE), AjusteSaldoRepository {

    // ========================================================================
    // PONTE COM O DAO
    // ========================================================================

    override suspend fun daoInserir(domain: AjusteSaldo): Long = ajusteSaldoDao.inserir(domain.toEntity())
    override suspend fun daoBuscarPorId(id: Long): AjusteSaldo? = ajusteSaldoDao.buscarPorId(id)?.toDomain()
    override suspend fun daoAtualizar(domain: AjusteSaldo) = ajusteSaldoDao.atualizar(domain.toEntity())
    override suspend fun daoExcluir(domain: AjusteSaldo) = ajusteSaldoDao.excluir(domain.toEntity())
    override fun getEntityId(domain: AjusteSaldo): Long = domain.id

    override fun AjusteSaldo.toAuditMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "empregoId" to empregoId,
        "data" to dataFormatada,
        "minutos" to minutos,
        "minutosFormatado" to minutosFormatadosExtenso,
        "tipo" to tipo.name,
        "tipoDescricao" to tipo.descricao,
        "justificativa" to justificativa
    )

    // ========================================================================
    // MOTIVOS DE AUDITORIA
    // ========================================================================

    override fun motivoInserir(domain: AjusteSaldo): String =
        "Ajuste de saldo criado: ${domain.descricaoResumida} em ${domain.dataFormatada}"

    override fun motivoAtualizar(domain: AjusteSaldo): String =
        "Ajuste de saldo atualizado: ${domain.descricaoResumida}"

    override fun motivoExcluir(domain: AjusteSaldo): String =
        "Ajuste de saldo excluído: ${domain.descricaoResumida} em ${domain.dataFormatada}"

    // ========================================================================
    // CRUD
    // ========================================================================

    override suspend fun inserir(ajuste: AjusteSaldo): Long = inserirComAuditoria(ajuste)

    override suspend fun atualizar(ajuste: AjusteSaldo) {
        // Atualiza timestamp antes de persistir
        val ajusteAtualizado = ajuste.copy(atualizadoEm = LocalDateTime.now())
        atualizarComAuditoria(ajusteAtualizado)
    }

    override suspend fun excluir(ajuste: AjusteSaldo) = excluirComAuditoria(ajuste)

    override suspend fun excluirPorId(id: Long) =
        excluirPorIdComAuditoria(id) { ajusteSaldoDao.excluirPorId(it) }

    // ========================================================================
    // CONSULTAS
    // ========================================================================

    override suspend fun buscarPorId(id: Long): AjusteSaldo? = daoBuscarPorId(id)

    override suspend fun buscarPorEmprego(empregoId: Long): List<AjusteSaldo> =
        ajusteSaldoDao.buscarPorEmprego(empregoId).map { it.toDomain() }

    override suspend fun buscarPorData(empregoId: Long, data: LocalDate): List<AjusteSaldo> =
        ajusteSaldoDao.buscarPorData(empregoId, data.toString()).map { it.toDomain() }

    override suspend fun buscarPorPeriodo(
        empregoId: Long,
        dataInicio: LocalDate,
        dataFim: LocalDate
    ): List<AjusteSaldo> =
        ajusteSaldoDao.buscarPorPeriodo(empregoId, dataInicio.toString(), dataFim.toString())
            .map { it.toDomain() }

    // ========================================================================
    // CÁLCULOS
    // ========================================================================

    override suspend fun somarTotalPorEmprego(empregoId: Long): Int =
        ajusteSaldoDao.somarTotalPorEmprego(empregoId)

    override suspend fun somarPorPeriodo(
        empregoId: Long,
        dataInicio: LocalDate,
        dataFim: LocalDate
    ): Int = ajusteSaldoDao.somarPorPeriodo(empregoId, dataInicio.toString(), dataFim.toString())

    override suspend fun contarPorEmprego(empregoId: Long): Int =
        ajusteSaldoDao.contarPorEmprego(empregoId)

    // ========================================================================
    // FLOWS
    // ========================================================================

    override fun observarPorEmprego(empregoId: Long): Flow<List<AjusteSaldo>> =
        ajusteSaldoDao.listarPorEmprego(empregoId).map { entities -> entities.map { it.toDomain() } }

    override fun observarPorPeriodo(
        empregoId: Long,
        dataInicio: LocalDate,
        dataFim: LocalDate
    ): Flow<List<AjusteSaldo>> =
        ajusteSaldoDao.listarPorPeriodo(empregoId, dataInicio.toString(), dataFim.toString())
            .map { entities -> entities.map { it.toDomain() } }

    override fun observarUltimos(empregoId: Long, limite: Int): Flow<List<AjusteSaldo>> =
        ajusteSaldoDao.listarUltimosPorEmprego(empregoId, limite)
            .map { entities -> entities.map { it.toDomain() } }

    companion object {
        private const val ENTIDADE = "AjusteSaldo"
    }
}
