// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/data/repository/HorarioDiaSemanaRepositoryImpl.kt
package br.com.tlmacedo.meuponto.data.repository

import br.com.tlmacedo.meuponto.data.local.database.dao.HorarioDiaSemanaDao
import br.com.tlmacedo.meuponto.data.local.database.entity.toDomain
import br.com.tlmacedo.meuponto.data.local.database.entity.toEntity
import br.com.tlmacedo.meuponto.domain.model.DiaSemana
import br.com.tlmacedo.meuponto.domain.model.HorarioDiaSemana
import br.com.tlmacedo.meuponto.domain.repository.HorarioDiaSemanaRepository
import br.com.tlmacedo.meuponto.domain.service.AuditService
import br.com.tlmacedo.meuponto.util.helper.horaFormatter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementação concreta do repositório de horários por dia da semana.
 *
 * @author Thiago
 * @since 2.0.0
 * @updated 4.0.0 - Adicionado suporte a versões de jornada
 * @updated 11.0.0 - Integração com AuditService
 * @updated 12.0.0 - Refatorado para usar AuditedRepositoryBase
 */
@Singleton
class HorarioDiaSemanaRepositoryImpl @Inject constructor(
    private val horarioDiaSemanaDao: HorarioDiaSemanaDao,
    auditService: AuditService
) : AuditedRepositoryBase<HorarioDiaSemana>(auditService, ENTIDADE), HorarioDiaSemanaRepository {

    // ========================================================================
    // PONTE COM O DAO
    // ========================================================================

    override suspend fun daoInserir(domain: HorarioDiaSemana): Long =
        horarioDiaSemanaDao.inserir(domain.toEntity())

    override suspend fun daoBuscarPorId(id: Long): HorarioDiaSemana? =
        horarioDiaSemanaDao.buscarPorId(id)?.toDomain()

    override suspend fun daoAtualizar(domain: HorarioDiaSemana) =
        horarioDiaSemanaDao.atualizar(domain.toEntity())

    override suspend fun daoExcluir(domain: HorarioDiaSemana) =
        horarioDiaSemanaDao.excluir(domain.toEntity())

    override fun getEntityId(domain: HorarioDiaSemana): Long = domain.id

    override fun HorarioDiaSemana.toAuditMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "empregoId" to empregoId,
        "versaoJornadaId" to versaoJornadaId,
        "diaSemana" to diaSemana.name,
        "diaSemanaDescricao" to diaSemana.descricao,
        "ativo" to ativo,
        "cargaHorariaMinutos" to cargaHorariaMinutos,
        "entradaIdeal" to entradaIdeal?.format(horaFormatter),
        "saidaIntervaloIdeal" to saidaIntervaloIdeal?.format(horaFormatter),
        "voltaIntervaloIdeal" to voltaIntervaloIdeal?.format(horaFormatter),
        "saidaIdeal" to saidaIdeal?.format(horaFormatter)
    )

    // ========================================================================
    // MOTIVOS DE AUDITORIA
    // ========================================================================

    override fun motivoInserir(domain: HorarioDiaSemana): String =
        "Horário criado para ${domain.diaSemana.descricao}"

    override fun motivoAtualizar(domain: HorarioDiaSemana): String =
        "Horário atualizado para ${domain.diaSemana.descricao}"

    override fun motivoExcluir(domain: HorarioDiaSemana): String =
        "Horário excluído para ${domain.diaSemana.descricao}"

    // ========================================================================
    // CRUD
    // ========================================================================

    override suspend fun inserir(horario: HorarioDiaSemana): Long = inserirComAuditoria(horario)

    override suspend fun inserirTodos(horarios: List<HorarioDiaSemana>): List<Long> {
        val ids = horarioDiaSemanaDao.inserirTodos(horarios.map { it.toEntity() })
        auditService.logCreate(
            entidade = ENTIDADE,
            entidadeId = 0L,
            motivo = "Horários criados em lote: ${horarios.size} dias configurados",
            novoValor = horarios.map { it.diaSemana.descricao },
            serializer = { auditService.toJson(it) }
        )
        return ids
    }

    override suspend fun atualizar(horario: HorarioDiaSemana) = atualizarComAuditoria(horario)

    override suspend fun excluir(horario: HorarioDiaSemana) = excluirComAuditoria(horario)

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
    // CONSULTAS
    // ========================================================================

    override suspend fun buscarPorId(id: Long): HorarioDiaSemana? = daoBuscarPorId(id)

    override suspend fun buscarPorEmprego(empregoId: Long): List<HorarioDiaSemana> =
        horarioDiaSemanaDao.buscarPorEmprego(empregoId).map { it.toDomain() }

    override suspend fun buscarPorEmpregoEDia(
        empregoId: Long,
        diaSemana: DiaSemana
    ): HorarioDiaSemana? =
        horarioDiaSemanaDao.buscarPorEmpregoEDia(empregoId, diaSemana)?.toDomain()

    override suspend fun buscarDiasAtivos(empregoId: Long): List<HorarioDiaSemana> =
        horarioDiaSemanaDao.buscarDiasAtivos(empregoId).map { it.toDomain() }

    override suspend fun contarDiasAtivos(empregoId: Long): Int =
        horarioDiaSemanaDao.contarDiasAtivos(empregoId)

    override suspend fun somarCargaHorariaSemanal(empregoId: Long): Int =
        horarioDiaSemanaDao.somarCargaHorariaSemanal(empregoId)

    override suspend fun buscarCargaHorariaDia(empregoId: Long, diaSemana: DiaSemana): Int? =
        horarioDiaSemanaDao.buscarCargaHorariaDia(empregoId, diaSemana)

    override suspend fun isDiaAtivo(empregoId: Long, diaSemana: DiaSemana): Boolean =
        horarioDiaSemanaDao.isDiaAtivo(empregoId, diaSemana) ?: false

    // ========================================================================
    // FLOWS
    // ========================================================================

    override fun observarPorEmprego(empregoId: Long): Flow<List<HorarioDiaSemana>> =
        horarioDiaSemanaDao.listarPorEmprego(empregoId).map { entities -> entities.map { it.toDomain() } }

    override fun observarDiasAtivos(empregoId: Long): Flow<List<HorarioDiaSemana>> =
        horarioDiaSemanaDao.listarDiasAtivos(empregoId).map { entities -> entities.map { it.toDomain() } }

    // ========================================================================
    // CONSULTAS POR VERSÃO DE JORNADA
    // ========================================================================

    override suspend fun buscarPorVersaoJornada(versaoJornadaId: Long): List<HorarioDiaSemana> =
        horarioDiaSemanaDao.buscarPorVersaoJornada(versaoJornadaId).map { it.toDomain() }

    override suspend fun buscarPorVersaoEDia(
        versaoJornadaId: Long,
        diaSemana: DiaSemana
    ): HorarioDiaSemana? =
        horarioDiaSemanaDao.buscarPorVersaoEDia(versaoJornadaId, diaSemana)?.toDomain()

    override fun observarPorVersaoJornada(versaoJornadaId: Long): Flow<List<HorarioDiaSemana>> =
        horarioDiaSemanaDao.observarPorVersaoJornada(versaoJornadaId)
            .map { entities -> entities.map { it.toDomain() } }

    companion object {
        private const val ENTIDADE = "HorarioDiaSemana"
    }
}
