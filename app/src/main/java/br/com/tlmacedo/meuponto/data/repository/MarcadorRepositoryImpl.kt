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
 * @property marcadorDao DAO do Room para operações de banco de dados
 * @property auditService Serviço de auditoria para logging de operações
 *
 * @author Thiago
 * @since 2.0.0
 * @updated 11.0.0 - Integração com AuditService
 */
@Singleton
class MarcadorRepositoryImpl @Inject constructor(
    private val marcadorDao: MarcadorDao,
    private val auditService: AuditService
) : MarcadorRepository {

    // ========================================================================
    // Operações de Escrita (CRUD)
    // ========================================================================

    override suspend fun inserir(marcador: Marcador): Long {
        val id = marcadorDao.inserir(marcador.toEntity())

        auditService.logCreate(
            entidade = ENTIDADE,
            entidadeId = id,
            motivo = "Marcador criado: ${marcador.nome}",
            novoValor = marcador,
            serializer = { auditService.toJson(it.toAuditMap()) }
        )

        return id
    }

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

    override suspend fun atualizar(marcador: Marcador) {
        val anterior = marcadorDao.buscarPorId(marcador.id)?.toDomain()
        marcadorDao.atualizar(marcador.toEntity())

        auditService.logUpdate(
            entidade = ENTIDADE,
            entidadeId = marcador.id,
            motivo = "Marcador atualizado: ${marcador.nome}",
            valorAntigo = anterior,
            valorNovo = marcador,
            serializer = { auditService.toJson(it.toAuditMap()) }
        )
    }

    override suspend fun excluir(marcador: Marcador) {
        auditService.logPermanentDelete(
            entidade = ENTIDADE,
            entidadeId = marcador.id,
            motivo = "Marcador excluído: ${marcador.nome}"
        )

        marcadorDao.excluir(marcador.toEntity())
    }

    override suspend fun excluirPorId(id: Long) {
        val marcador = marcadorDao.buscarPorId(id)?.toDomain()

        auditService.logPermanentDelete(
            entidade = ENTIDADE,
            entidadeId = id,
            motivo = marcador?.let { "Marcador excluído: ${it.nome}" } ?: "Marcador excluído"
        )

        marcadorDao.excluirPorId(id)
    }

    // ========================================================================
    // Operações de Leitura
    // ========================================================================

    override suspend fun buscarPorId(id: Long): Marcador? {
        return marcadorDao.buscarPorId(id)?.toDomain()
    }

    override suspend fun buscarPorNome(empregoId: Long, nome: String): Marcador? {
        return marcadorDao.buscarPorNome(empregoId, nome)?.toDomain()
    }

    override suspend fun buscarAtivosPorEmprego(empregoId: Long): List<Marcador> {
        return marcadorDao.buscarAtivosPorEmprego(empregoId).map { it.toDomain() }
    }

    override suspend fun existeComNome(empregoId: Long, nome: String, excludeId: Long): Boolean {
        return marcadorDao.existeComNome(empregoId, nome, excludeId)
    }

    // ========================================================================
    // Operações Reativas (Flows)
    // ========================================================================

    override fun observarPorEmprego(empregoId: Long): Flow<List<Marcador>> {
        return marcadorDao.listarPorEmprego(empregoId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun observarAtivosPorEmprego(empregoId: Long): Flow<List<Marcador>> {
        return marcadorDao.listarAtivosPorEmprego(empregoId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    // ========================================================================
    // Operações de Status
    // ========================================================================

    override suspend fun atualizarStatus(id: Long, ativo: Boolean) {
        val marcador = marcadorDao.buscarPorId(id)?.toDomain()
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

    // ========================================================================
    // Helpers
    // ========================================================================

    private fun Marcador.toAuditMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "empregoId" to empregoId,
        "nome" to nome,
        "cor" to cor,
        "icone" to icone,
        "ativo" to ativo
    )

    companion object {
        private const val ENTIDADE = "Marcador"
    }
}
