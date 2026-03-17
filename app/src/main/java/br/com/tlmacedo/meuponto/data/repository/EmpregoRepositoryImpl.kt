// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/data/repository/EmpregoRepositoryImpl.kt
package br.com.tlmacedo.meuponto.data.repository

import br.com.tlmacedo.meuponto.data.local.database.dao.EmpregoDao
import br.com.tlmacedo.meuponto.data.local.database.entity.toDomain
import br.com.tlmacedo.meuponto.data.local.database.entity.toEntity
import br.com.tlmacedo.meuponto.domain.model.Emprego
import br.com.tlmacedo.meuponto.domain.repository.EmpregoRepository
import br.com.tlmacedo.meuponto.domain.service.AuditService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementação concreta do repositório de empregos.
 *
 * @property empregoDao DAO do Room para operações de banco de dados
 * @property auditService Serviço de auditoria para logging de operações
 *
 * @author Thiago
 * @since 2.0.0
 * @updated 11.0.0 - Integração com AuditService
 */
@Singleton
class EmpregoRepositoryImpl @Inject constructor(
    private val empregoDao: EmpregoDao,
    private val auditService: AuditService
) : EmpregoRepository {

    // ========================================================================
    // Operações de Escrita (CRUD)
    // ========================================================================

    override suspend fun inserir(emprego: Emprego): Long {
        val id = empregoDao.inserir(emprego.toEntity())

        auditService.logCreate(
            entidade = ENTIDADE,
            entidadeId = id,
            motivo = "Emprego criado: ${emprego.nome}",
            novoValor = emprego,
            serializer = { auditService.toJson(it.toAuditMap()) }
        )

        return id
    }

    override suspend fun atualizar(emprego: Emprego) {
        val anterior = empregoDao.buscarPorId(emprego.id)?.toDomain()
        empregoDao.atualizar(emprego.toEntity())

        auditService.logUpdate(
            entidade = ENTIDADE,
            entidadeId = emprego.id,
            motivo = "Emprego atualizado: ${emprego.nome}",
            valorAntigo = anterior,
            valorNovo = emprego,
            serializer = { auditService.toJson(it.toAuditMap()) }
        )
    }

    override suspend fun excluir(emprego: Emprego) {
        auditService.logPermanentDelete(
            entidade = ENTIDADE,
            entidadeId = emprego.id,
            motivo = "Emprego excluído permanentemente: ${emprego.nome}"
        )

        empregoDao.excluir(emprego.toEntity())
    }

    override suspend fun excluirPorId(id: Long) {
        val emprego = empregoDao.buscarPorId(id)?.toDomain()

        auditService.logPermanentDelete(
            entidade = ENTIDADE,
            entidadeId = id,
            motivo = emprego?.let { "Emprego excluído permanentemente: ${it.nome}" }
                ?: "Emprego excluído permanentemente"
        )

        empregoDao.excluirPorId(id)
    }

    // ========================================================================
    // Operações de Leitura
    // ========================================================================

    override suspend fun buscarPorId(id: Long): Emprego? {
        return empregoDao.buscarPorId(id)?.toDomain()
    }

    override suspend fun buscarAtivos(): List<Emprego> {
        return empregoDao.buscarAtivos().map { it.toDomain() }
    }

    override suspend fun contarAtivos(): Int {
        return empregoDao.contarAtivos()
    }

    override suspend fun contarTodos(): Int {
        return empregoDao.contarTodos()
    }

    override suspend fun existe(id: Long): Boolean {
        return empregoDao.existe(id)
    }

    // ========================================================================
    // Operações Reativas (Flows)
    // ========================================================================

    override fun observarPorId(id: Long): Flow<Emprego?> {
        return empregoDao.observarPorId(id).map { it?.toDomain() }
    }

    override fun observarTodos(): Flow<List<Emprego>> {
        return empregoDao.listarTodos().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun observarAtivos(): Flow<List<Emprego>> {
        return empregoDao.listarAtivos().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun observarArquivados(): Flow<List<Emprego>> {
        return empregoDao.listarArquivados().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    // ========================================================================
    // Operações de Status
    // ========================================================================

    override suspend fun atualizarStatus(id: Long, ativo: Boolean) {
        val emprego = empregoDao.buscarPorId(id)?.toDomain()
        empregoDao.atualizarStatus(id, ativo, LocalDateTime.now().toString())

        auditService.logUpdate(
            entidade = ENTIDADE,
            entidadeId = id,
            motivo = "${emprego?.nome ?: "Emprego"} ${if (ativo) "ativado" else "desativado"}",
            valorAntigo = !ativo,
            valorNovo = ativo,
            serializer = { "ativo=$it" }
        )
    }

    override suspend fun arquivar(id: Long) {
        val emprego = empregoDao.buscarPorId(id)?.toDomain()
        empregoDao.atualizarArquivado(id, true, LocalDateTime.now().toString())

        auditService.logUpdate(
            entidade = ENTIDADE,
            entidadeId = id,
            motivo = "Emprego arquivado: ${emprego?.nome ?: "ID $id"}",
            valorAntigo = false,
            valorNovo = true,
            serializer = { "arquivado=$it" }
        )
    }

    override suspend fun desarquivar(id: Long) {
        val emprego = empregoDao.buscarPorId(id)?.toDomain()
        empregoDao.atualizarArquivado(id, false, LocalDateTime.now().toString())

        auditService.logUpdate(
            entidade = ENTIDADE,
            entidadeId = id,
            motivo = "Emprego desarquivado: ${emprego?.nome ?: "ID $id"}",
            valorAntigo = true,
            valorNovo = false,
            serializer = { "arquivado=$it" }
        )
    }

    override suspend fun atualizarOrdem(id: Long, ordem: Int) {
        val emprego = empregoDao.buscarPorId(id)?.toDomain()
        val ordemAnterior = emprego?.ordem
        empregoDao.atualizarOrdem(id, ordem, LocalDateTime.now().toString())

        auditService.logUpdate(
            entidade = ENTIDADE,
            entidadeId = id,
            motivo = "Ordem alterada: ${emprego?.nome ?: "ID $id"}",
            valorAntigo = ordemAnterior,
            valorNovo = ordem,
            serializer = { "ordem=$it" }
        )
    }

    override suspend fun buscarProximaOrdem(): Int {
        return (empregoDao.buscarMaiorOrdem() ?: 0) + 1
    }

    // ========================================================================
    // Helpers
    // ========================================================================

    private fun Emprego.toAuditMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "nome" to nome,
        "dataInicioTrabalho" to dataInicioTrabalho?.toString(),
        "descricao" to descricao,
        "ativo" to ativo,
        "arquivado" to arquivado,
        "ordem" to ordem
    )

    companion object {
        private const val ENTIDADE = "Emprego"
    }
}
