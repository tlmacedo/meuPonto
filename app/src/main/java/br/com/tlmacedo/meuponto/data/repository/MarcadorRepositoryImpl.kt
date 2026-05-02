// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/data/repository/MarcadorRepositoryImpl.kt
package br.com.tlmacedo.meuponto.data.repository

import br.com.tlmacedo.meuponto.data.local.database.dao.MarcadorDao
import br.com.tlmacedo.meuponto.data.local.database.entity.toDomain
import br.com.tlmacedo.meuponto.data.local.database.entity.toEntity
import br.com.tlmacedo.meuponto.domain.model.Marcador
import br.com.tlmacedo.meuponto.domain.repository.MarcadorRepository
import br.com.tlmacedo.meuponto.domain.service.AuditService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementação concreta do repositório de marcadores.
 *
 * @author Thiago
 * @since 2.0.0
 * @updated 11.0.0 - Integração com AuditService
 * @updated 12.0.0 - Refatorado para usar AuditedRepositoryBase
 */
@Singleton
class MarcadorRepositoryImpl @Inject constructor(
    private val marcadorDao: MarcadorDao,
    auditService: AuditService
) : AuditedRepositoryBase<Marcador>(auditService, ENTIDADE), MarcadorRepository {

    // ========================================================================
    // PONTE COM O DAO
    // ========================================================================

    override suspend fun daoInserir(domain: Marcador): Long = marcadorDao.inserir(domain.toEntity())
    override suspend fun daoBuscarPorId(id: Long): Marcador? = marcadorDao.buscarPorId(id)?.toDomain()
    override suspend fun daoAtualizar(domain: Marcador) = marcadorDao.atualizar(domain.toEntity())
    override suspend fun daoExcluir(domain: Marcador) = marcadorDao.excluir(domain.toEntity())
    override fun getEntityId(domain: Marcador): Long = domain.id

    override fun Marcador.toAuditMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "empregoId" to empregoId,
        "nome" to nome,
        "cor" to cor,
        "icone" to icone,
        "ativo" to ativo
    )

    // ========================================================================
    // MOTIVOS DE AUDITORIA
    // ========================================================================

    override fun motivoInserir(domain: Marcador): String = "Marcador criado: ${domain.nome}"
    override fun motivoAtualizar(domain: Marcador): String = "Marcador atualizado: ${domain.nome}"
    override fun motivoExcluir(domain: Marcador): String = "Marcador excluído: ${domain.nome}"

    // ========================================================================
    // CRUD
    // ========================================================================

    override suspend fun inserir(marcador: Marcador): Long = inserirComAuditoria(marcador)

    override suspend fun inserirTodos(marcadores: List<Marcador>): List<Long> {
        val ids = marcadorDao.inserirTodos(marcadores.map { it.toEntity() })
        auditService.logCreate(
            entidade = ENTIDADE,
            entidadeId = 0L,
            motivo = "Marcadores criados em lote: ${marcadores.size} itens",
            novoValor = marcadores.map { it.nome },
            serializer = { auditService.toJson(it) }
        )
        return ids
    }

    override suspend fun atualizar(marcador: Marcador) = atualizarComAuditoria(marcador)

    override suspend fun excluir(marcador: Marcador) = excluirComAuditoria(marcador)

    override suspend fun excluirPorId(id: Long) =
        excluirPorIdComAuditoria(id) { marcadorDao.excluirPorId(it) }

    // ========================================================================
    // CONSULTAS
    // ========================================================================

    override suspend fun buscarPorId(id: Long): Marcador? = daoBuscarPorId(id)

    override suspend fun buscarPorNome(empregoId: Long, nome: String): Marcador? =
        marcadorDao.buscarPorNome(empregoId, nome)?.toDomain()

    override suspend fun buscarAtivosPorEmprego(empregoId: Long): List<Marcador> =
        marcadorDao.buscarAtivosPorEmprego(empregoId).map { it.toDomain() }

    override suspend fun existeComNome(empregoId: Long, nome: String, excludeId: Long): Boolean =
        marcadorDao.existeComNome(empregoId, nome, excludeId)

    // ========================================================================
    // FLOWS
    // ========================================================================

    override fun observarPorEmprego(empregoId: Long): Flow<List<Marcador>> =
        marcadorDao.listarPorEmprego(empregoId).map { entities -> entities.map { it.toDomain() } }

    override fun observarAtivosPorEmprego(empregoId: Long): Flow<List<Marcador>> =
        marcadorDao.listarAtivosPorEmprego(empregoId).map { entities -> entities.map { it.toDomain() } }

    // ========================================================================
    // STATUS
    // ========================================================================

    override suspend fun atualizarStatus(id: Long, ativo: Boolean) {
        val marcador = daoBuscarPorId(id)
        marcadorDao.atualizarStatus(id, ativo, LocalDateTime.now().toString())

        auditService.logUpdate(
            entidade = ENTIDADE,
            entidadeId = id,
            motivo = "${marcador?.nome ?: "Marcador"} ${if (ativo) "ativado" else "desativado"}",
            valorAntigo = !ativo,
            valorNovo = ativo,
            serializer = { "ativo=$it" }
        )
    }

    companion object {
        private const val ENTIDADE = "Marcador"
    }
}
