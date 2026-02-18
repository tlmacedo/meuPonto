// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/data/repository/HorarioDiaSemanaRepositoryImpl.kt
package br.com.tlmacedo.meuponto.data.repository

import br.com.tlmacedo.meuponto.data.local.database.dao.HorarioDiaSemanaDao
import br.com.tlmacedo.meuponto.data.local.database.entity.toDomain
import br.com.tlmacedo.meuponto.data.local.database.entity.toEntity
import br.com.tlmacedo.meuponto.domain.model.DiaSemana
import br.com.tlmacedo.meuponto.domain.model.HorarioDiaSemana
import br.com.tlmacedo.meuponto.domain.repository.HorarioDiaSemanaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementação concreta do repositório de horários por dia da semana.
 *
 * @property horarioDiaSemanaDao DAO do Room para operações de banco de dados
 *
 * @author Thiago
 * @since 2.0.0
 * @updated 2.7.0 - Corrigido para passar DiaSemana diretamente ao DAO
 */
@Singleton
class HorarioDiaSemanaRepositoryImpl @Inject constructor(
    private val horarioDiaSemanaDao: HorarioDiaSemanaDao
) : HorarioDiaSemanaRepository {

    // ========================================================================
    // Operações de Escrita (CRUD)
    // ========================================================================

    override suspend fun inserir(horario: HorarioDiaSemana): Long {
        return horarioDiaSemanaDao.inserir(horario.toEntity())
    }

    override suspend fun inserirTodos(horarios: List<HorarioDiaSemana>): List<Long> {
        return horarioDiaSemanaDao.inserirTodos(horarios.map { it.toEntity() })
    }

    override suspend fun atualizar(horario: HorarioDiaSemana) {
        horarioDiaSemanaDao.atualizar(horario.toEntity())
    }

    override suspend fun excluir(horario: HorarioDiaSemana) {
        horarioDiaSemanaDao.excluir(horario.toEntity())
    }

    override suspend fun excluirPorEmprego(empregoId: Long) {
        horarioDiaSemanaDao.excluirPorEmprego(empregoId)
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

}
