// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/data/repository/AusenciaRepositoryImpl.kt
package br.com.tlmacedo.meuponto.data.repository

import br.com.tlmacedo.meuponto.data.local.database.dao.AusenciaDao
import br.com.tlmacedo.meuponto.data.local.database.entity.toDomain
import br.com.tlmacedo.meuponto.data.local.database.entity.toEntity
import br.com.tlmacedo.meuponto.domain.model.ausencia.Ausencia
import br.com.tlmacedo.meuponto.domain.model.ausencia.TipoAusencia
import br.com.tlmacedo.meuponto.domain.repository.AusenciaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementação do repositório de ausências.
 *
 * @author Thiago
 * @since 4.0.0
 */
@Singleton
class AusenciaRepositoryImpl @Inject constructor(
    private val ausenciaDao: AusenciaDao
) : AusenciaRepository {

    // ========================================================================
    // CRUD
    // ========================================================================

    override suspend fun inserir(ausencia: Ausencia): Long {
        return ausenciaDao.inserir(ausencia.toEntity())
    }

    override suspend fun inserirTodas(ausencias: List<Ausencia>): List<Long> {
        return ausenciaDao.inserirTodas(ausencias.map { it.toEntity() })
    }

    override suspend fun atualizar(ausencia: Ausencia) {
        ausenciaDao.atualizar(ausencia.toEntity())
    }

    override suspend fun excluir(ausencia: Ausencia) {
        ausenciaDao.excluir(ausencia.toEntity())
    }

    override suspend fun excluirPorId(id: Long) {
        ausenciaDao.excluirPorId(id)
    }

    // ========================================================================
    // Consultas por ID
    // ========================================================================

    override suspend fun buscarPorId(id: Long): Ausencia? {
        return ausenciaDao.buscarPorId(id)?.toDomain()
    }

    override fun observarPorId(id: Long): Flow<Ausencia?> {
        return ausenciaDao.observarPorId(id).map { it?.toDomain() }
    }

    // ========================================================================
    // Consultas Globais
    // ========================================================================

    override suspend fun buscarTodas(): List<Ausencia> {
        return ausenciaDao.buscarTodas().map { it.toDomain() }
    }

    override fun observarTodas(): Flow<List<Ausencia>> {
        return ausenciaDao.observarTodas().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun buscarTodasAtivas(): List<Ausencia> {
        return ausenciaDao.buscarTodasAtivas().map { it.toDomain() }
    }

    override fun observarTodasAtivas(): Flow<List<Ausencia>> {
        return ausenciaDao.observarTodasAtivas().map { list -> list.map { it.toDomain() } }
    }

    // ========================================================================
    // Consultas por Emprego
    // ========================================================================

    override suspend fun buscarPorEmprego(empregoId: Long): List<Ausencia> {
        return ausenciaDao.buscarPorEmprego(empregoId).map { it.toDomain() }
    }

    override fun observarPorEmprego(empregoId: Long): Flow<List<Ausencia>> {
        return ausenciaDao.observarPorEmprego(empregoId).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun buscarAtivasPorEmprego(empregoId: Long): List<Ausencia> {
        return ausenciaDao.buscarAtivasPorEmprego(empregoId).map { it.toDomain() }
    }

    override fun observarAtivasPorEmprego(empregoId: Long): Flow<List<Ausencia>> {
        return ausenciaDao.observarAtivasPorEmprego(empregoId)
            .map { list -> list.map { it.toDomain() } }
    }

    // ========================================================================
    // Consultas por Data
    // ========================================================================

    override suspend fun buscarPorData(empregoId: Long, data: LocalDate): List<Ausencia> {
        return ausenciaDao.buscarPorData(empregoId, data).map { it.toDomain() }
    }

    override fun observarPorData(empregoId: Long, data: LocalDate): Flow<List<Ausencia>> {
        return ausenciaDao.observarPorData(empregoId, data)
            .map { list -> list.map { it.toDomain() } }
    }

    override suspend fun buscarPorPeriodo(
        empregoId: Long,
        dataInicio: LocalDate,
        dataFim: LocalDate
    ): List<Ausencia> {
        return ausenciaDao.buscarPorPeriodo(empregoId, dataInicio, dataFim)
            .map { it.toDomain() }
    }

    override fun observarPorPeriodo(
        empregoId: Long,
        dataInicio: LocalDate,
        dataFim: LocalDate
    ): Flow<List<Ausencia>> {
        return ausenciaDao.observarPorPeriodo(empregoId, dataInicio, dataFim)
            .map { list -> list.map { it.toDomain() } }
    }

    // ========================================================================
    // Consultas por Tipo
    // ========================================================================

    override suspend fun buscarPorTipo(empregoId: Long, tipo: TipoAusencia): List<Ausencia> {
        return ausenciaDao.buscarPorTipo(empregoId, tipo).map { it.toDomain() }
    }

    override fun observarPorTipo(empregoId: Long, tipo: TipoAusencia): Flow<List<Ausencia>> {
        return ausenciaDao.observarPorTipo(empregoId, tipo)
            .map { list -> list.map { it.toDomain() } }
    }

    // ========================================================================
    // Consultas por Ano/Mês
    // ========================================================================

    override suspend fun buscarPorAno(empregoId: Long, ano: Int): List<Ausencia> {
        val anoInicio = LocalDate.of(ano, 1, 1)
        val anoFim = LocalDate.of(ano, 12, 31)
        return ausenciaDao.buscarPorAno(empregoId, ano.toString(), anoInicio, anoFim)
            .map { it.toDomain() }
    }

    override suspend fun buscarPorMes(empregoId: Long, mes: YearMonth): List<Ausencia> {
        val primeiroDia = mes.atDay(1)
        val ultimoDia = mes.atEndOfMonth()
        return ausenciaDao.buscarPorMes(empregoId, primeiroDia, ultimoDia)
            .map { it.toDomain() }
    }

    override fun observarPorMes(empregoId: Long, mes: YearMonth): Flow<List<Ausencia>> {
        val primeiroDia = mes.atDay(1)
        val ultimoDia = mes.atEndOfMonth()
        return ausenciaDao.observarPorMes(empregoId, primeiroDia, ultimoDia)
            .map { list -> list.map { it.toDomain() } }
    }

    // ========================================================================
    // Validações
    // ========================================================================

    override suspend fun existeSobreposicao(
        empregoId: Long,
        dataInicio: LocalDate,
        dataFim: LocalDate,
        excluirId: Long
    ): Boolean {
        return ausenciaDao.contarSobreposicoes(empregoId, dataInicio, dataFim, excluirId) > 0
    }

    override suspend fun existeAusenciaEmData(empregoId: Long, data: LocalDate): Boolean {
        return ausenciaDao.existeAusenciaEmData(empregoId, data)
    }

    // ========================================================================
    // Estatísticas
    // ========================================================================

    override suspend fun contarDiasPorTipo(
        empregoId: Long,
        tipo: TipoAusencia,
        dataInicio: LocalDate,
        dataFim: LocalDate
    ): Int {
        return ausenciaDao.contarDiasPorTipo(empregoId, tipo, dataInicio, dataFim)
    }

    override suspend fun contarPorEmprego(empregoId: Long): Int {
        return ausenciaDao.contarPorEmprego(empregoId)
    }

    // ========================================================================
    // Operações de Limpeza
    // ========================================================================

    override suspend fun desativarPorEmprego(empregoId: Long) {
        ausenciaDao.desativarPorEmprego(empregoId)
    }

    override suspend fun limparAusenciasAntigas(dataLimite: LocalDate) {
        ausenciaDao.limparAusenciasAntigas(dataLimite)
    }
}
