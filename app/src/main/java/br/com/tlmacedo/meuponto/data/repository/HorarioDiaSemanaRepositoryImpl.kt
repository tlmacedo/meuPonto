// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/data/repository/HorarioDiaSemanaRepositoryImpl.kt
package br.com.tlmacedo.meuponto.data.repository

import br.com.tlmacedo.meuponto.data.local.database.dao.HorarioDiaSemanaDao
import br.com.tlmacedo.meuponto.data.local.database.entity.toDomain
import br.com.tlmacedo.meuponto.data.local.database.entity.toEntity
import br.com.tlmacedo.meuponto.domain.model.DiaSemana
import br.com.tlmacedo.meuponto.domain.model.HorarioDiaSemana
import br.com.tlmacedo.meuponto.domain.repository.HorarioDiaSemanaRepository
import br.com.tlmacedo.meuponto.domain.service.AuditService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementação concreta do repositório de horários por dia da semana.
 *
 * @property horarioDiaSemanaDao DAO do Room para operações de banco de dados
 * @property auditService Serviço de auditoria para logging de operações
 *
 * @author Thiago
 * @since 2.0.0
 * @updated 4.0.0 - Adicionado suporte a versões de jornada
 * @updated 11.0.0 - Integração com AuditService
 */
@Singleton
class HorarioDiaSemanaRepositoryImpl @Inject constructor(
    private val horarioDiaSemanaDao: HorarioDiaSemanaDao,
    private val auditService: AuditService
) : HorarioDiaSemanaRepository {

    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    // ========================================================================
    // Operações de Escrita (CRUD)
    // ========================================================================

    override suspend fun inserir(horario: HorarioDiaSemana): Long {
        val id = horarioDiaSemanaDao.inserir(horario.toEntity())

        auditService.logCreate(
            entidade = ENTIDADE,
            entidadeId = id,
            motivo = "Horário criado para ${horario.diaSemana.descricao}",
            novoValor = horario,
            serializer = { auditService.toJson(it.toAuditMap()) }
        )

        return id
    }

    override suspend fun inserirTodos(horarios: List<HorarioDiaSemana>): List<Long> {
        val ids = horarioDiaSemanaDao.inserirTodos(horarios.map { it.toEntity() })

        // Log resumido para inserções em lote
        auditService.logCreate(
            entidade = ENTIDADE,
            entidadeId = 0L,
            motivo = "Horários criados em lote: ${horarios.size} dias configurados",
            novoValor = horarios.map { it.diaSemana.descricao },
            serializer = { auditService.toJson(it) }
        )

        return ids
    }

    override suspend fun atualizar(horario: HorarioDiaSemana) {
        val anterior = horarioDiaSemanaDao.buscarPorId(horario.id)?.toDomain()
        horarioDiaSemanaDao.atualizar(horario.toEntity())

        auditService.logUpdate(
            entidade = ENTIDADE,
            entidadeId = horario.id,
            motivo = "Horário atualizado para ${horario.diaSemana.descricao}",
            valorAntigo = anterior,
            valorNovo = horario,
            serializer = { auditService.toJson(it.toAuditMap()) }
        )
    }

    override suspend fun excluir(horario: HorarioDiaSemana) {
        auditService.logPermanentDelete(
            entidade = ENTIDADE,
            entidadeId = horario.id,
            motivo = "Horário excluído para ${horario.diaSemana.descricao}"
        )

        horarioDiaSemanaDao.excluir(horario.toEntity())
    }

    override suspend fun excluirPorEmprego(empregoId: Long) {
        auditService.logPermanentDelete(
            entidade = ENTIDADE,
            entidadeId = empregoId,
            motivo = "Todos os horários do emprego $empregoId foram excluídos"
        )

        horarioDiaSemanaDao.excluirPorEmprego(empregoId)
    }

    override suspend fun excluirPorVersaoJornada(versaoJornadaId: Long) {
        auditService.logPermanentDelete(
            entidade = ENTIDADE,
            entidadeId = versaoJornadaId,
            motivo = "Todos os horários da versão de jornada $versaoJornadaId foram excluídos"
        )

        horarioDiaSemanaDao.excluirPorVersaoJornada(versaoJornadaId)
    }

    // ========================================================================
    // Operações de Leitura
    // ========================================================================

    override suspend fun buscarPorId(id: Long): HorarioDiaSemana? {
        return horarioDiaSemanaDao.buscarPorId(id)?.toDomain()
    }

    override suspend fun buscarPorEmprego(empregoId: Long): List<HorarioDiaSemana> {
        return horarioDiaSemanaDao.buscarPorEmprego(empregoId).map { it.toDomain() }
    }

    override suspend fun buscarPorEmpregoEDia(empregoId: Long, diaSemana: DiaSemana): HorarioDiaSemana? {
        return horarioDiaSemanaDao.buscarPorEmpregoEDia(empregoId, diaSemana)?.toDomain()
    }

    override suspend fun buscarDiasAtivos(empregoId: Long): List<HorarioDiaSemana> {
        return horarioDiaSemanaDao.buscarDiasAtivos(empregoId).map { it.toDomain() }
    }

    override suspend fun contarDiasAtivos(empregoId: Long): Int {
        return horarioDiaSemanaDao.contarDiasAtivos(empregoId)
    }

    override suspend fun somarCargaHorariaSemanal(empregoId: Long): Int {
        return horarioDiaSemanaDao.somarCargaHorariaSemanal(empregoId)
    }

    override suspend fun buscarCargaHorariaDia(empregoId: Long, diaSemana: DiaSemana): Int? {
        return horarioDiaSemanaDao.buscarCargaHorariaDia(empregoId, diaSemana)
    }

    override suspend fun isDiaAtivo(empregoId: Long, diaSemana: DiaSemana): Boolean {
        return horarioDiaSemanaDao.isDiaAtivo(empregoId, diaSemana) ?: false
    }

    // ========================================================================
    // Operações Reativas (Flows)
    // ========================================================================

    override fun observarPorEmprego(empregoId: Long): Flow<List<HorarioDiaSemana>> {
        return horarioDiaSemanaDao.listarPorEmprego(empregoId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun observarDiasAtivos(empregoId: Long): Flow<List<HorarioDiaSemana>> {
        return horarioDiaSemanaDao.listarDiasAtivos(empregoId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    // ========================================================================
    // Operações por Versão de Jornada
    // ========================================================================

    override suspend fun buscarPorVersaoJornada(versaoJornadaId: Long): List<HorarioDiaSemana> {
        return horarioDiaSemanaDao.buscarPorVersaoJornada(versaoJornadaId).map { it.toDomain() }
    }

    override suspend fun buscarPorVersaoEDia(versaoJornadaId: Long, diaSemana: DiaSemana): HorarioDiaSemana? {
        return horarioDiaSemanaDao.buscarPorVersaoEDia(versaoJornadaId, diaSemana)?.toDomain()
    }

    override fun observarPorVersaoJornada(versaoJornadaId: Long): Flow<List<HorarioDiaSemana>> {
        return horarioDiaSemanaDao.observarPorVersaoJornada(versaoJornadaId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    // ========================================================================
    // Helpers
    // ========================================================================

    private fun HorarioDiaSemana.toAuditMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "empregoId" to empregoId,
        "versaoJornadaId" to versaoJornadaId,
        "diaSemana" to diaSemana.name,
        "diaSemanaDescricao" to diaSemana.descricao,
        "ativo" to ativo,
        "cargaHorariaMinutos" to cargaHorariaMinutos,
        "entradaIdeal" to entradaIdeal?.format(timeFormatter),
        "saidaIntervaloIdeal" to saidaIntervaloIdeal?.format(timeFormatter),
        "voltaIntervaloIdeal" to voltaIntervaloIdeal?.format(timeFormatter),
        "saidaIdeal" to saidaIdeal?.format(timeFormatter)
    )

    companion object {
        private const val ENTIDADE = "HorarioDiaSemana"
    }
}
