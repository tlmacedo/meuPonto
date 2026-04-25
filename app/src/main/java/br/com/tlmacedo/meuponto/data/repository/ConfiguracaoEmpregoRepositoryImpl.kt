// path: app/src/main/java/br/com/tlmacedo/meuponto/data/repository/ConfiguracaoEmpregoRepositoryImpl.kt
package br.com.tlmacedo.meuponto.data.repository

import br.com.tlmacedo.meuponto.data.local.database.dao.ConfiguracaoEmpregoDao
import br.com.tlmacedo.meuponto.data.local.database.entity.toDomain
import br.com.tlmacedo.meuponto.data.local.database.entity.toEntity
import br.com.tlmacedo.meuponto.domain.model.ConfiguracaoEmprego
import br.com.tlmacedo.meuponto.domain.repository.ConfiguracaoEmpregoRepository
import br.com.tlmacedo.meuponto.domain.service.AuditService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementação do repositório de configurações de emprego.
 *
 * SIMPLIFICADO: Apenas configurações de exibição/comportamento.
 * Configurações de jornada/banco de horas foram migradas para VersaoJornadaRepository.
 *
 * @author Thiago
 * @since 2.0.0
 * @updated 8.0.0 - Simplificado após migração de campos para VersaoJornada
 * @updated 11.0.0 - Integração com AuditService
 */
@Singleton
class ConfiguracaoEmpregoRepositoryImpl @Inject constructor(
    private val configuracaoEmpregoDao: ConfiguracaoEmpregoDao,
    private val auditService: AuditService
) : ConfiguracaoEmpregoRepository {

    override suspend fun inserir(configuracao: ConfiguracaoEmprego): Long {
        val id = configuracaoEmpregoDao.inserir(configuracao.toEntity())

        auditService.logCreate(
            entidade = ENTIDADE,
            entidadeId = id,
            motivo = "Configuração de emprego criada para empregoId=${configuracao.empregoId}",
            novoValor = configuracao,
            serializer = { auditService.toJson(it.toAuditMap()) }
        )

        return id
    }

    override suspend fun atualizar(configuracao: ConfiguracaoEmprego) {
        val anterior = configuracaoEmpregoDao.buscarPorId(configuracao.id)?.toDomain()
        configuracaoEmpregoDao.atualizar(configuracao.toEntity())

        auditService.logUpdate(
            entidade = ENTIDADE,
            entidadeId = configuracao.id,
            motivo = "Configuração de emprego atualizada para empregoId=${configuracao.empregoId}",
            valorAntigo = anterior,
            valorNovo = configuracao,
            serializer = { auditService.toJson(it.toAuditMap()) }
        )
    }

    override suspend fun excluir(configuracao: ConfiguracaoEmprego) {
        auditService.logPermanentDelete(
            entidade = ENTIDADE,
            entidadeId = configuracao.id,
            motivo = "Configuração de emprego excluída para empregoId=${configuracao.empregoId}"
        )

        configuracaoEmpregoDao.excluir(configuracao.toEntity())
    }

    override suspend fun buscarPorId(id: Long): ConfiguracaoEmprego? {
        return configuracaoEmpregoDao.buscarPorId(id)?.toDomain()
    }

    override suspend fun buscarPorEmpregoId(empregoId: Long): ConfiguracaoEmprego? {
        return configuracaoEmpregoDao.buscarPorEmpregoId(empregoId)?.toDomain()
    }

    override fun observarPorEmpregoId(empregoId: Long): Flow<ConfiguracaoEmprego?> {
        return configuracaoEmpregoDao.observarPorEmpregoId(empregoId).map { it?.toDomain() }
    }

    override suspend fun listarTodas(): List<ConfiguracaoEmprego> {
        return configuracaoEmpregoDao.listarTodas().map { it.toDomain() }
    }

    override fun observarTodas(): Flow<List<ConfiguracaoEmprego>> {
        return configuracaoEmpregoDao.observarTodas().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun existeParaEmprego(empregoId: Long): Boolean {
        return configuracaoEmpregoDao.existeParaEmprego(empregoId)
    }

    // ========================================================================
    // Helpers
    // ========================================================================

    private fun ConfiguracaoEmprego.toAuditMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "empregoId" to empregoId,
        "habilitarNsr" to habilitarNsr,
        "tipoNsr" to tipoNsr.name,
        "habilitarLocalizacao" to habilitarLocalizacao,
        "localizacaoAutomatica" to localizacaoAutomatica,
        "fotoHabilitada" to fotoHabilitada,
        "fotoObrigatoria" to fotoObrigatoria,
        "exibirDuracaoTurno" to exibirDuracaoTurno,
        "exibirDuracaoIntervalo" to exibirDuracaoIntervalo
    )

    companion object {
        private const val ENTIDADE = "ConfiguracaoEmprego"
    }
}
