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
 * @property ajusteSaldoDao DAO do Room para operações de banco de dados
 * @property auditService Serviço de auditoria para logging de operações
 *
 * @author Thiago
 * @since 2.0.0
 * @updated 11.0.0 - Integração com AuditService e suporte a TipoAjusteSaldo
 */
@Singleton
class AjusteSaldoRepositoryImpl @Inject constructor(
    private val ajusteSaldoDao: AjusteSaldoDao,
    private val auditService: AuditService
) : AjusteSaldoRepository {

    // ========================================================================
    // Operações de Escrita (CRUD)
    // ========================================================================

    override suspend fun inserir(ajuste: AjusteSaldo): Long {
        val id = ajusteSaldoDao.inserir(ajuste.toEntity())

        auditService.logCreate(
            entidade = ENTIDADE,
            entidadeId = id,
            motivo = "Ajuste de saldo criado: ${ajuste.descricaoResumida} em ${ajuste.dataFormatada}",
            novoValor = ajuste,
            serializer = { auditService.toJson(it.toAuditMap()) }
        )

        return id
    }

    override suspend fun atualizar(ajuste: AjusteSaldo) {
        val anterior = ajusteSaldoDao.buscarPorId(ajuste.id)?.toDomain()
        val ajusteAtualizado = ajuste.copy(atualizadoEm = LocalDateTime.now())

        ajusteSaldoDao.atualizar(ajusteAtualizado.toEntity())

        auditService.logUpdate(
            entidade = ENTIDADE,
            entidadeId = ajuste.id,
            motivo = "Ajuste de saldo atualizado: ${ajusteAtualizado.descricaoResumida}",
            valorAntigo = anterior,
            valorNovo = ajusteAtualizado,
            serializer = { auditService.toJson(it.toAuditMap()) }
        )
    }

    override suspend fun excluir(ajuste: AjusteSaldo) {
        auditService.logPermanentDelete(
            entidade = ENTIDADE,
            entidadeId = ajuste.id,
            motivo = "Ajuste de saldo excluído: ${ajuste.descricaoResumida} em ${ajuste.dataFormatada}"
        )

        ajusteSaldoDao.excluir(ajuste.toEntity())
    }

    override suspend fun excluirPorId(id: Long) {
        val ajuste = ajusteSaldoDao.buscarPorId(id)?.toDomain()

        auditService.logPermanentDelete(
            entidade = ENTIDADE,
            entidadeId = id,
            motivo = ajuste?.let {
                "Ajuste de saldo excluído: ${it.descricaoResumida} em ${it.dataFormatada}"
            } ?: "Ajuste de saldo excluído (id: $id)"
        )

        ajusteSaldoDao.excluirPorId(id)
    }

    // ========================================================================
    // Operações de Leitura
    // ========================================================================

    override suspend fun buscarPorId(id: Long): AjusteSaldo? {
        return ajusteSaldoDao.buscarPorId(id)?.toDomain()
    }

    override suspend fun buscarPorEmprego(empregoId: Long): List<AjusteSaldo> {
        return ajusteSaldoDao.buscarPorEmprego(empregoId).map { it.toDomain() }
    }

    override suspend fun buscarPorData(empregoId: Long, data: LocalDate): List<AjusteSaldo> {
        return ajusteSaldoDao.buscarPorData(empregoId, data.toString()).map { it.toDomain() }
    }

    override suspend fun buscarPorPeriodo(
        empregoId: Long,
        dataInicio: LocalDate,
        dataFim: LocalDate
    ): List<AjusteSaldo> {
        return ajusteSaldoDao.buscarPorPeriodo(
            empregoId,
            dataInicio.toString(),
            dataFim.toString()
        ).map { it.toDomain() }
    }

    // ========================================================================
    // Cálculos
    // ========================================================================

    override suspend fun somarTotalPorEmprego(empregoId: Long): Int {
        return ajusteSaldoDao.somarTotalPorEmprego(empregoId)
    }

    override suspend fun somarPorPeriodo(
        empregoId: Long,
        dataInicio: LocalDate,
        dataFim: LocalDate
    ): Int {
        return ajusteSaldoDao.somarPorPeriodo(
            empregoId,
            dataInicio.toString(),
            dataFim.toString()
        )
    }

    override suspend fun contarPorEmprego(empregoId: Long): Int {
        return ajusteSaldoDao.contarPorEmprego(empregoId)
    }

    // ========================================================================
    // Operações Reativas (Flows)
    // ========================================================================

    override fun observarPorEmprego(empregoId: Long): Flow<List<AjusteSaldo>> {
        return ajusteSaldoDao.listarPorEmprego(empregoId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun observarPorPeriodo(
        empregoId: Long,
        dataInicio: LocalDate,
        dataFim: LocalDate
    ): Flow<List<AjusteSaldo>> {
        return ajusteSaldoDao.listarPorPeriodo(
            empregoId,
            dataInicio.toString(),
            dataFim.toString()
        ).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun observarUltimos(empregoId: Long, limite: Int): Flow<List<AjusteSaldo>> {
        return ajusteSaldoDao.listarUltimosPorEmprego(empregoId, limite).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    companion object {
        private const val ENTIDADE = "AjusteSaldo"
    }
}
