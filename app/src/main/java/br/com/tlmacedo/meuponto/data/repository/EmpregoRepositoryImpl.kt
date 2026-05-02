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
 * @author Thiago
 * @since 2.0.0
 * @updated 11.0.0 - Integração com AuditService
 * @updated 12.0.0 - Refatorado para usar AuditedRepositoryBase
 */
@Singleton
class EmpregoRepositoryImpl @Inject constructor(
    private val empregoDao: EmpregoDao,
    auditService: AuditService
) : AuditedRepositoryBase<Emprego>(auditService, ENTIDADE), EmpregoRepository {

    // ========================================================================
    // PONTE COM O DAO
    // ========================================================================

    override suspend fun daoInserir(domain: Emprego): Long = empregoDao.inserir(domain.toEntity())
    override suspend fun daoBuscarPorId(id: Long): Emprego? = empregoDao.buscarPorId(id)?.toDomain()
    override suspend fun daoAtualizar(domain: Emprego) = empregoDao.atualizar(domain.toEntity())
    override suspend fun daoExcluir(domain: Emprego) = empregoDao.excluir(domain.toEntity())
    override fun getEntityId(domain: Emprego): Long = domain.id

    override fun Emprego.toAuditMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "nome" to nome,
        "dataInicioTrabalho" to dataInicioTrabalho?.toString(),
        "descricao" to descricao,
        "ativo" to ativo,
        "arquivado" to arquivado,
        "ordem" to ordem
    )

    // ========================================================================
    // MOTIVOS DE AUDITORIA
    // ========================================================================

    override fun motivoInserir(domain: Emprego): String = "Emprego criado: ${domain.nome}"
    override fun motivoAtualizar(domain: Emprego): String = "Emprego atualizado: ${domain.nome}"
    override fun motivoExcluir(domain: Emprego): String = "Emprego excluído permanentemente: ${domain.nome}"

    // ========================================================================
    // CRUD
    // ========================================================================

    override suspend fun inserir(emprego: Emprego): Long = inserirComAuditoria(emprego)

    override suspend fun atualizar(emprego: Emprego) = atualizarComAuditoria(emprego)

    override suspend fun excluir(emprego: Emprego) = excluirComAuditoria(emprego)

    override suspend fun excluirPorId(id: Long) =
        excluirPorIdComAuditoria(id) { empregoDao.excluirPorId(it) }

    // ========================================================================
    // CONSULTAS
    // ========================================================================

    override suspend fun buscarPorId(id: Long): Emprego? = daoBuscarPorId(id)

    override suspend fun buscarAtivos(): List<Emprego> =
        empregoDao.buscarAtivos().map { it.toDomain() }

    override suspend fun contarAtivos(): Int = empregoDao.contarAtivos()

    override suspend fun contarTodos(): Int = empregoDao.contarTodos()

    override suspend fun existe(id: Long): Boolean = empregoDao.existe(id)

    override suspend fun buscarPorCnpj(cnpj: String): Emprego? =
        empregoDao.buscarPorCnpj(cnpj)?.toDomain()

    // ========================================================================
    // FLOWS
    // ========================================================================

    override fun observarPorId(id: Long): Flow<Emprego?> =
        empregoDao.observarPorId(id).map { it?.toDomain() }

    override fun observarTodos(): Flow<List<Emprego>> =
        empregoDao.listarTodos().map { entities -> entities.map { it.toDomain() } }

    override fun observarAtivos(): Flow<List<Emprego>> =
        empregoDao.listarAtivos().map { entities -> entities.map { it.toDomain() } }

    override fun observarArquivados(): Flow<List<Emprego>> =
        empregoDao.listarArquivados().map { entities -> entities.map { it.toDomain() } }

    // ========================================================================
    // STATUS E ORDEM
    // ========================================================================

    override suspend fun atualizarStatus(id: Long, ativo: Boolean) {
        val emprego = daoBuscarPorId(id)
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
        val emprego = daoBuscarPorId(id)
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
        val emprego = daoBuscarPorId(id)
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
        val emprego = daoBuscarPorId(id)
        empregoDao.atualizarOrdem(id, ordem, LocalDateTime.now().toString())

        auditService.logUpdate(
            entidade = ENTIDADE,
            entidadeId = id,
            motivo = "Ordem alterada: ${emprego?.nome ?: "ID $id"}",
            valorAntigo = emprego?.ordem,
            valorNovo = ordem,
            serializer = { "ordem=$it" }
        )
    }

    override suspend fun buscarProximaOrdem(): Int = (empregoDao.buscarMaiorOrdem() ?: 0) + 1

    companion object {
        private const val ENTIDADE = "Emprego"
    }
}
