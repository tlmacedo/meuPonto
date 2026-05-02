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
 * @updated 12.0.0 - Refatorado para usar AuditedRepositoryBase
 */
@Singleton
class ConfiguracaoEmpregoRepositoryImpl @Inject constructor(
    private val configuracaoEmpregoDao: ConfiguracaoEmpregoDao,
    auditService: AuditService
) : AuditedRepositoryBase<ConfiguracaoEmprego>(auditService, ENTIDADE), ConfiguracaoEmpregoRepository {

    // ========================================================================
    // PONTE COM O DAO
    // ========================================================================

    override suspend fun daoInserir(domain: ConfiguracaoEmprego): Long =
        configuracaoEmpregoDao.inserir(domain.toEntity())

    override suspend fun daoBuscarPorId(id: Long): ConfiguracaoEmprego? =
        configuracaoEmpregoDao.buscarPorId(id)?.toDomain()

    override suspend fun daoAtualizar(domain: ConfiguracaoEmprego) =
        configuracaoEmpregoDao.atualizar(domain.toEntity())

    override suspend fun daoExcluir(domain: ConfiguracaoEmprego) =
        configuracaoEmpregoDao.excluir(domain.toEntity())

    override fun getEntityId(domain: ConfiguracaoEmprego): Long = domain.id

    override fun ConfiguracaoEmprego.toAuditMap(): Map<String, Any?> = mapOf(
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

    // ========================================================================
    // MOTIVOS DE AUDITORIA
    // ========================================================================

    override fun motivoInserir(domain: ConfiguracaoEmprego): String =
        "Configuração de emprego criada para empregoId=${domain.empregoId}"

    override fun motivoAtualizar(domain: ConfiguracaoEmprego): String =
        "Configuração de emprego atualizada para empregoId=${domain.empregoId}"

    override fun motivoExcluir(domain: ConfiguracaoEmprego): String =
        "Configuração de emprego excluída para empregoId=${domain.empregoId}"

    // ========================================================================
    // CRUD
    // ========================================================================

    override suspend fun inserir(configuracao: ConfiguracaoEmprego): Long =
        inserirComAuditoria(configuracao)

    override suspend fun atualizar(configuracao: ConfiguracaoEmprego) =
        atualizarComAuditoria(configuracao)

    override suspend fun excluir(configuracao: ConfiguracaoEmprego) =
        excluirComAuditoria(configuracao)

    // ========================================================================
    // CONSULTAS
    // ========================================================================

    override suspend fun buscarPorId(id: Long): ConfiguracaoEmprego? = daoBuscarPorId(id)

    override suspend fun buscarPorEmpregoId(empregoId: Long): ConfiguracaoEmprego? =
        configuracaoEmpregoDao.buscarPorEmpregoId(empregoId)?.toDomain()

    override fun observarPorEmpregoId(empregoId: Long): Flow<ConfiguracaoEmprego?> =
        configuracaoEmpregoDao.observarPorEmpregoId(empregoId).map { it?.toDomain() }

    override suspend fun listarTodas(): List<ConfiguracaoEmprego> =
        configuracaoEmpregoDao.listarTodas().map { it.toDomain() }

    override fun observarTodas(): Flow<List<ConfiguracaoEmprego>> =
        configuracaoEmpregoDao.observarTodas().map { list -> list.map { it.toDomain() } }

    override suspend fun existeParaEmprego(empregoId: Long): Boolean =
        configuracaoEmpregoDao.existeParaEmprego(empregoId)

    companion object {
        private const val ENTIDADE = "ConfiguracaoEmprego"
    }
}
