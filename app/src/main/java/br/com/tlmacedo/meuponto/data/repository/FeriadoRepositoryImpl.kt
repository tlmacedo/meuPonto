// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/data/repository/FeriadoRepositoryImpl.kt
package br.com.tlmacedo.meuponto.data.repository

import br.com.tlmacedo.meuponto.data.local.database.dao.ConfiguracaoPontesAnoDao
import br.com.tlmacedo.meuponto.data.local.database.dao.FeriadoDao
import br.com.tlmacedo.meuponto.data.local.database.entity.toDomain
import br.com.tlmacedo.meuponto.data.local.database.entity.toEntity
import br.com.tlmacedo.meuponto.domain.model.feriado.ConfiguracaoPontesAno
import br.com.tlmacedo.meuponto.domain.model.feriado.Feriado
import br.com.tlmacedo.meuponto.domain.model.feriado.TipoFeriado
import br.com.tlmacedo.meuponto.domain.repository.FeriadoRepository
import br.com.tlmacedo.meuponto.domain.service.AuditService
import br.com.tlmacedo.meuponto.util.helper.dateFormatterSimples
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.MonthDay
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementação do repositório de feriados usando Room.
 *
 * Gerencia dois tipos de entidade: [Feriado] (via AuditedRepositoryBase)
 * e [ConfiguracaoPontesAno] (operações manuais com auditoria).
 *
 * @author Thiago
 * @since 3.0.0
 * @updated 11.0.0 - Integração com AuditService
 * @updated 12.0.0 - Refatorado para usar AuditedRepositoryBase para Feriado
 */
@Singleton
class FeriadoRepositoryImpl @Inject constructor(
    private val feriadoDao: FeriadoDao,
    private val configuracaoPontesAnoDao: ConfiguracaoPontesAnoDao,
    auditService: AuditService
) : AuditedRepositoryBase<Feriado>(auditService, ENTIDADE_FERIADO), FeriadoRepository {

    // ========================================================================
    // PONTE COM O DAO — Feriado
    // ========================================================================

    override suspend fun daoInserir(domain: Feriado): Long = feriadoDao.inserir(domain.toEntity())
    override suspend fun daoBuscarPorId(id: Long): Feriado? = feriadoDao.buscarPorId(id)?.toDomain()
    override suspend fun daoAtualizar(domain: Feriado) = feriadoDao.atualizar(domain.toEntity())
    override suspend fun daoExcluir(domain: Feriado) = feriadoDao.excluir(domain.toEntity())
    override fun getEntityId(domain: Feriado): Long = domain.id

    override fun Feriado.toAuditMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "nome" to nome,
        "tipo" to tipo.name,
        "recorrencia" to recorrencia.name,
        "diaMes" to diaMes?.let { String.format("%02d/%02d", it.dayOfMonth, it.monthValue) },
        "dataEspecifica" to dataEspecifica?.format(dateFormatterSimples),
        "empregoId" to empregoId,
        "ativo" to ativo
    )

    // ========================================================================
    // MOTIVOS DE AUDITORIA — Feriado
    // ========================================================================

    override fun motivoInserir(domain: Feriado): String = "Feriado criado: ${domain.nome}"
    override fun motivoAtualizar(domain: Feriado): String = "Feriado atualizado: ${domain.nome}"
    override fun motivoExcluir(domain: Feriado): String = "Feriado excluído: ${domain.nome}"

    // ========================================================================
    // CRUD — Feriado
    // ========================================================================

    override suspend fun inserir(feriado: Feriado): Long = inserirComAuditoria(feriado)

    override suspend fun inserirTodos(feriados: List<Feriado>): List<Long> {
        val ids = feriadoDao.inserirTodos(feriados.map { it.toEntity() })
        auditService.logCreate(
            entidade = ENTIDADE_FERIADO,
            entidadeId = 0L,
            motivo = "Importação em lote: ${feriados.size} feriados inseridos",
            novoValor = feriados.map { it.nome },
            serializer = { auditService.toJson(it) }
        )
        return ids
    }

    override suspend fun atualizar(feriado: Feriado) = atualizarComAuditoria(feriado)

    override suspend fun excluir(feriado: Feriado) = excluirComAuditoria(feriado)

    override suspend fun excluirPorId(id: Long) =
        excluirPorIdComAuditoria(id) { feriadoDao.excluirPorId(it) }

    // ========================================================================
    // CONSULTAS — Feriado
    // ========================================================================

    override suspend fun buscarPorId(id: Long): Feriado? = daoBuscarPorId(id)

    override fun observarPorId(id: Long): Flow<Feriado?> =
        feriadoDao.observarPorId(id).map { it?.toDomain() }

    override suspend fun buscarTodos(): List<Feriado> =
        feriadoDao.buscarTodos().map { it.toDomain() }

    override fun observarTodos(): Flow<List<Feriado>> =
        feriadoDao.observarTodos().map { list -> list.map { it.toDomain() } }

    override suspend fun buscarTodosAtivos(): List<Feriado> =
        feriadoDao.buscarTodosAtivos().map { it.toDomain() }

    override fun observarTodosAtivos(): Flow<List<Feriado>> =
        feriadoDao.observarTodosAtivos().map { list -> list.map { it.toDomain() } }

    override suspend fun buscarPorAno(ano: Int): List<Feriado> =
        feriadoDao.buscarPorAno(ano).map { it.toDomain() }

    override fun observarPorAno(ano: Int): Flow<List<Feriado>> =
        feriadoDao.observarPorAno(ano).map { list -> list.map { it.toDomain() } }

    override suspend fun buscarPorData(data: LocalDate): List<Feriado> {
        val diaMes = String.format("%02d-%02d", data.monthValue, data.dayOfMonth)
        return feriadoDao.buscarPorData(data, diaMes).map { it.toDomain() }
    }

    override suspend fun buscarPorDataEEmprego(data: LocalDate, empregoId: Long): List<Feriado> {
        val diaMes = String.format("%02d-%02d", data.monthValue, data.dayOfMonth)
        return feriadoDao.buscarPorDataEEmprego(data, diaMes, empregoId).map { it.toDomain() }
    }

    override fun observarPorDataEEmprego(data: LocalDate, empregoId: Long): Flow<List<Feriado>> {
        val diaMes = String.format("%02d-%02d", data.monthValue, data.dayOfMonth)
        return feriadoDao.observarPorDataEEmprego(data, diaMes, empregoId)
            .map { list -> list.map { it.toDomain() } }
    }

    override suspend fun buscarPorPeriodo(dataInicio: LocalDate, dataFim: LocalDate): List<Feriado> =
        feriadoDao.buscarPorPeriodo(dataInicio, dataFim).map { it.toDomain() }

    override suspend fun buscarPorTipo(tipo: TipoFeriado): List<Feriado> =
        feriadoDao.buscarPorTipo(tipo).map { it.toDomain() }

    override fun observarPorTipo(tipo: TipoFeriado): Flow<List<Feriado>> =
        feriadoDao.observarPorTipo(tipo).map { list -> list.map { it.toDomain() } }

    override suspend fun buscarPorEmprego(empregoId: Long): List<Feriado> =
        feriadoDao.buscarPorEmprego(empregoId).map { it.toDomain() }

    override fun observarPorEmprego(empregoId: Long): Flow<List<Feriado>> =
        feriadoDao.observarPorEmprego(empregoId).map { list -> list.map { it.toDomain() } }

    // ========================================================================
    // PONTES
    // ========================================================================

    override suspend fun buscarPontesPorAnoEEmprego(ano: Int, empregoId: Long): List<Feriado> =
        feriadoDao.buscarPontesPorAnoEEmprego(ano, empregoId).map { it.toDomain() }

    override suspend fun contarPontesPorAnoEEmprego(ano: Int, empregoId: Long): Int =
        feriadoDao.contarPontesPorAnoEEmprego(ano, empregoId)

    // ========================================================================
    // VALIDAÇÕES
    // ========================================================================

    override suspend fun existeFeriadoDuplicado(
        nome: String,
        data: LocalDate?,
        diaMes: MonthDay?,
        excluirId: Long
    ): Boolean {
        val diaMesString = diaMes?.let { String.format("%02d-%02d", it.monthValue, it.dayOfMonth) }
        return feriadoDao.contarDuplicados(nome, diaMesString, data, excluirId) > 0
    }

    override suspend fun existemFeriadosNacionaisImportados(): Boolean =
        feriadoDao.contarFeriadosNacionais() > 0

    // ========================================================================
    // LIMPEZA — Feriado
    // ========================================================================

    override suspend fun limparFeriadosAntigos(anoAtual: Int) {
        auditService.logPermanentDelete(
            entidade = ENTIDADE_FERIADO,
            entidadeId = 0L,
            motivo = "Feriados anteriores ao ano $anoAtual foram limpos"
        )
        feriadoDao.limparFeriadosAntigos(anoAtual)
    }

    override suspend fun desativarPorEmprego(empregoId: Long) {
        auditService.logUpdate(
            entidade = ENTIDADE_FERIADO,
            entidadeId = empregoId,
            motivo = "Feriados do emprego $empregoId foram desativados",
            valorAntigo = "ativo=true",
            valorNovo = "ativo=false",
            serializer = { it }
        )
        feriadoDao.desativarPorEmprego(empregoId)
    }

    // ========================================================================
    // CRUD — ConfiguracaoPontesAno (entidade secundária, auditoria manual)
    // ========================================================================

    override suspend fun inserirConfiguracaoPontes(config: ConfiguracaoPontesAno): Long {
        val id = configuracaoPontesAnoDao.inserir(config.toEntity())
        auditService.logCreate(
            entidade = ENTIDADE_CONFIG_PONTES,
            entidadeId = id,
            motivo = "Configuração de pontes criada para ${config.ano}",
            novoValor = config,
            serializer = { auditService.toJson(it.toConfigAuditMap()) }
        )
        return id
    }

    override suspend fun atualizarConfiguracaoPontes(config: ConfiguracaoPontesAno) {
        val anterior = configuracaoPontesAnoDao.buscarPorEmpregoEAno(config.empregoId, config.ano)?.toDomain()
        configuracaoPontesAnoDao.atualizar(config.toEntity())
        auditService.logUpdate(
            entidade = ENTIDADE_CONFIG_PONTES,
            entidadeId = config.id,
            motivo = "Configuração de pontes atualizada para ${config.ano}",
            valorAntigo = anterior,
            valorNovo = config,
            serializer = { auditService.toJson(it.toConfigAuditMap()) }
        )
    }

    override suspend fun excluirConfiguracaoPontes(config: ConfiguracaoPontesAno) {
        auditService.logPermanentDelete(
            entidade = ENTIDADE_CONFIG_PONTES,
            entidadeId = config.id,
            motivo = "Configuração de pontes excluída: ano ${config.ano}"
        )
        configuracaoPontesAnoDao.excluir(config.toEntity())
    }

    // ========================================================================
    // CONSULTAS — ConfiguracaoPontesAno
    // ========================================================================

    override suspend fun buscarConfiguracaoPontes(empregoId: Long, ano: Int): ConfiguracaoPontesAno? =
        configuracaoPontesAnoDao.buscarPorEmpregoEAno(empregoId, ano)?.toDomain()

    override fun observarConfiguracaoPontes(empregoId: Long, ano: Int): Flow<ConfiguracaoPontesAno?> =
        configuracaoPontesAnoDao.observarPorEmpregoEAno(empregoId, ano).map { it?.toDomain() }

    override suspend fun buscarTodasConfiguracoesPontes(empregoId: Long): List<ConfiguracaoPontesAno> =
        configuracaoPontesAnoDao.buscarPorEmprego(empregoId).map { it.toDomain() }

    override fun observarTodasConfiguracoesPontes(empregoId: Long): Flow<List<ConfiguracaoPontesAno>> =
        configuracaoPontesAnoDao.observarPorEmprego(empregoId).map { list -> list.map { it.toDomain() } }

    override suspend fun buscarAdicionalDiarioPontes(empregoId: Long, data: LocalDate): Int =
        configuracaoPontesAnoDao.buscarAdicionalDiario(empregoId, data.year) ?: 0

    // ========================================================================
    // HELPERS
    // ========================================================================

    private fun ConfiguracaoPontesAno.toConfigAuditMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "empregoId" to empregoId,
        "ano" to ano,
        "diasPonte" to diasPonte,
        "adicionalDiarioMinutos" to adicionalDiarioMinutos,
        "diasUteisAno" to diasUteisAno,
        "cargaHorariaPonteMinutos" to cargaHorariaPonteMinutos
    )

    companion object {
        private const val ENTIDADE_FERIADO = "Feriado"
        private const val ENTIDADE_CONFIG_PONTES = "ConfiguracaoPontesAno"
    }
}
