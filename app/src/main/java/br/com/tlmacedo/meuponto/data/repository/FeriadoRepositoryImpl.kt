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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.MonthDay
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementação do repositório de feriados usando Room.
 *
 * @author Thiago
 * @since 3.0.0
 * @updated 11.0.0 - Integração com AuditService
 */
@Singleton
class FeriadoRepositoryImpl @Inject constructor(
    private val feriadoDao: FeriadoDao,
    private val configuracaoPontesAnoDao: ConfiguracaoPontesAnoDao,
    private val auditService: AuditService
) : FeriadoRepository {

    private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    // ========================================================================
    // CRUD de Feriados
    // ========================================================================

    override suspend fun inserir(feriado: Feriado): Long {
        val id = feriadoDao.inserir(feriado.toEntity())

        auditService.logCreate(
            entidade = ENTIDADE_FERIADO,
            entidadeId = id,
            motivo = "Feriado criado: ${feriado.nome}",
            novoValor = feriado,
            serializer = { auditService.toJson(it.toAuditMap()) }
        )

        return id
    }

    override suspend fun inserirTodos(feriados: List<Feriado>): List<Long> {
        val ids = feriadoDao.inserirTodos(feriados.map { it.toEntity() })

        // Log apenas o resumo para inserções em lote
        auditService.logCreate(
            entidade = ENTIDADE_FERIADO,
            entidadeId = 0L,
            motivo = "Importação em lote: ${feriados.size} feriados inseridos",
            novoValor = feriados.map { it.nome },
            serializer = { auditService.toJson(it) }
        )

        return ids
    }

    override suspend fun atualizar(feriado: Feriado) {
        val anterior = feriadoDao.buscarPorId(feriado.id)?.toDomain()
        feriadoDao.atualizar(feriado.toEntity())

        auditService.logUpdate(
            entidade = ENTIDADE_FERIADO,
            entidadeId = feriado.id,
            motivo = "Feriado atualizado: ${feriado.nome}",
            valorAntigo = anterior,
            valorNovo = feriado,
            serializer = { auditService.toJson(it.toAuditMap()) }
        )
    }

    override suspend fun excluir(feriado: Feriado) {
        auditService.logPermanentDelete(
            entidade = ENTIDADE_FERIADO,
            entidadeId = feriado.id,
            motivo = "Feriado excluído: ${feriado.nome}"
        )

        feriadoDao.excluir(feriado.toEntity())
    }

    override suspend fun excluirPorId(id: Long) {
        val feriado = feriadoDao.buscarPorId(id)?.toDomain()

        auditService.logPermanentDelete(
            entidade = ENTIDADE_FERIADO,
            entidadeId = id,
            motivo = feriado?.let { "Feriado excluído: ${it.nome}" } ?: "Feriado excluído"
        )

        feriadoDao.excluirPorId(id)
    }

    // ========================================================================
    // Consultas de Feriados
    // ========================================================================

    override suspend fun buscarPorId(id: Long): Feriado? {
        return feriadoDao.buscarPorId(id)?.toDomain()
    }

    override fun observarPorId(id: Long): Flow<Feriado?> {
        return feriadoDao.observarPorId(id).map { it?.toDomain() }
    }

    override suspend fun buscarTodos(): List<Feriado> {
        return feriadoDao.buscarTodos().map { it.toDomain() }
    }

    override fun observarTodos(): Flow<List<Feriado>> {
        return feriadoDao.observarTodos().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun buscarTodosAtivos(): List<Feriado> {
        return feriadoDao.buscarTodosAtivos().map { it.toDomain() }
    }

    override fun observarTodosAtivos(): Flow<List<Feriado>> {
        return feriadoDao.observarTodosAtivos().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun buscarPorAno(ano: Int): List<Feriado> {
        return feriadoDao.buscarPorAno(ano).map { it.toDomain() }
    }

    override fun observarPorAno(ano: Int): Flow<List<Feriado>> {
        return feriadoDao.observarPorAno(ano).map { list -> list.map { it.toDomain() } }
    }

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

    override suspend fun buscarPorPeriodo(dataInicio: LocalDate, dataFim: LocalDate): List<Feriado> {
        return feriadoDao.buscarPorPeriodo(dataInicio, dataFim).map { it.toDomain() }
    }

    override suspend fun buscarPorTipo(tipo: TipoFeriado): List<Feriado> {
        return feriadoDao.buscarPorTipo(tipo).map { it.toDomain() }
    }

    override fun observarPorTipo(tipo: TipoFeriado): Flow<List<Feriado>> {
        return feriadoDao.observarPorTipo(tipo).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun buscarPorEmprego(empregoId: Long): List<Feriado> {
        return feriadoDao.buscarPorEmprego(empregoId).map { it.toDomain() }
    }

    override fun observarPorEmprego(empregoId: Long): Flow<List<Feriado>> {
        return feriadoDao.observarPorEmprego(empregoId).map { list -> list.map { it.toDomain() } }
    }

    // ========================================================================
    // Consultas Específicas de Pontes
    // ========================================================================

    override suspend fun buscarPontesPorAnoEEmprego(ano: Int, empregoId: Long): List<Feriado> {
        return feriadoDao.buscarPontesPorAnoEEmprego(ano, empregoId).map { it.toDomain() }
    }

    override suspend fun contarPontesPorAnoEEmprego(ano: Int, empregoId: Long): Int {
        return feriadoDao.contarPontesPorAnoEEmprego(ano, empregoId)
    }

    // ========================================================================
    // Validações
    // ========================================================================

    override suspend fun existeFeriadoDuplicado(
        nome: String,
        data: LocalDate?,
        diaMes: MonthDay?,
        excluirId: Long
    ): Boolean {
        val diaMesString = diaMes?.let {
            String.format("%02d-%02d", it.monthValue, it.dayOfMonth)
        }
        return feriadoDao.contarDuplicados(nome, diaMesString, data, excluirId) > 0
    }

    override suspend fun existemFeriadosNacionaisImportados(): Boolean {
        return feriadoDao.contarFeriadosNacionais() > 0
    }

    // ========================================================================
    // Operações de Limpeza
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
    // CRUD de Configuração de Pontes
    // ========================================================================

    override suspend fun inserirConfiguracaoPontes(config: ConfiguracaoPontesAno): Long {
        val id = configuracaoPontesAnoDao.inserir(config.toEntity())

        auditService.logCreate(
            entidade = ENTIDADE_CONFIG_PONTES,
            entidadeId = id,
            motivo = "Configuração de pontes criada para ${config.ano}",
            novoValor = config,
            serializer = { auditService.toJson(it.toAuditMap()) }
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
            serializer = { auditService.toJson(it.toAuditMap()) }
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
    // Consultas de Configuração de Pontes
    // ========================================================================

    override suspend fun buscarConfiguracaoPontes(empregoId: Long, ano: Int): ConfiguracaoPontesAno? {
        return configuracaoPontesAnoDao.buscarPorEmpregoEAno(empregoId, ano)?.toDomain()
    }

    override fun observarConfiguracaoPontes(empregoId: Long, ano: Int): Flow<ConfiguracaoPontesAno?> {
        return configuracaoPontesAnoDao.observarPorEmpregoEAno(empregoId, ano)
            .map { it?.toDomain() }
    }

    override suspend fun buscarTodasConfiguracoesPontes(empregoId: Long): List<ConfiguracaoPontesAno> {
        return configuracaoPontesAnoDao.buscarPorEmprego(empregoId).map { it.toDomain() }
    }

    override fun observarTodasConfiguracoesPontes(empregoId: Long): Flow<List<ConfiguracaoPontesAno>> {
        return configuracaoPontesAnoDao.observarPorEmprego(empregoId)
            .map { list -> list.map { it.toDomain() } }
    }

    override suspend fun buscarAdicionalDiarioPontes(empregoId: Long, data: LocalDate): Int {
        return configuracaoPontesAnoDao.buscarAdicionalDiario(empregoId, data.year) ?: 0
    }

    // ========================================================================
    // Helpers
    // ========================================================================

    private fun Feriado.toAuditMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "nome" to nome,
        "tipo" to tipo.name,
        "recorrencia" to recorrencia.name,
        "diaMes" to diaMes?.let { String.format("%02d/%02d", it.dayOfMonth, it.monthValue) },
        "dataEspecifica" to dataEspecifica?.format(dateFormatter),
        "empregoId" to empregoId,
        "ativo" to ativo
    )

    private fun ConfiguracaoPontesAno.toAuditMap(): Map<String, Any?> = mapOf(
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
