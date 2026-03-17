// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/data/repository/PontoRepositoryImpl.kt
package br.com.tlmacedo.meuponto.data.repository

import br.com.tlmacedo.meuponto.data.local.database.dao.PontoDao
import br.com.tlmacedo.meuponto.data.local.database.entity.toDomain
import br.com.tlmacedo.meuponto.data.local.database.entity.toEntity
import br.com.tlmacedo.meuponto.domain.model.Ponto
import br.com.tlmacedo.meuponto.domain.repository.PontoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementação do repositório de Pontos.
 *
 * @author Thiago
 * @since 1.0.0
 * @updated 11.0.0 - Adicionado suporte completo a soft delete e lixeira
 */
@Singleton
class PontoRepositoryImpl @Inject constructor(
    private val pontoDao: PontoDao
) : PontoRepository {

    // === Operações básicas ===

    override suspend fun inserir(ponto: Ponto): Long {
        return pontoDao.inserir(ponto.toEntity())
    }

    override suspend fun atualizar(ponto: Ponto) {
        pontoDao.atualizar(ponto.toEntity())
    }

    override suspend fun excluir(ponto: Ponto) {
        pontoDao.excluir(ponto.toEntity())
    }

    // === Consultas por ID ===

    override suspend fun buscarPorId(id: Long): Ponto? {
        return pontoDao.buscarPorId(id)?.toDomain()
    }

    override fun observarPorId(id: Long): Flow<Ponto?> {
        return pontoDao.observarPorId(id).map { it?.toDomain() }
    }

    // === Consultas gerais ===

    override fun listarTodos(): Flow<List<Ponto>> {
        return pontoDao.listarTodos().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun observarTodos(): Flow<List<Ponto>> {
        return pontoDao.observarTodos().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    // === Consultas por Emprego ===

    override fun listarPorEmprego(empregoId: Long): Flow<List<Ponto>> {
        return pontoDao.listarPorEmprego(empregoId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun observarPorEmprego(empregoId: Long): Flow<List<Ponto>> {
        return pontoDao.observarPorEmprego(empregoId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun contarPorEmprego(empregoId: Long): Int {
        return pontoDao.contarPorEmprego(empregoId)
    }

    override suspend fun buscarPrimeiraData(empregoId: Long): LocalDate? {
        return pontoDao.buscarPrimeiraData(empregoId)
    }

    // === Consultas por Data ===

    override fun listarPorData(data: LocalDate): Flow<List<Ponto>> {
        return pontoDao.listarPorData(data).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun buscarPorEmpregoEData(empregoId: Long, data: LocalDate): List<Ponto> {
        return pontoDao.buscarPorEmpregoEData(empregoId, data).map { it.toDomain() }
    }

    override fun observarPorEmpregoEData(empregoId: Long, data: LocalDate): Flow<List<Ponto>> {
        return pontoDao.observarPorEmpregoEData(empregoId, data).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    // === Consultas por Período ===

    override fun listarPorPeriodo(dataInicio: LocalDate, dataFim: LocalDate): Flow<List<Ponto>> {
        return pontoDao.listarPorPeriodo(dataInicio, dataFim).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun listarPorEmpregoEPeriodo(
        empregoId: Long,
        dataInicio: LocalDate,
        dataFim: LocalDate
    ): Flow<List<Ponto>> {
        return pontoDao.listarPorEmpregoEPeriodo(empregoId, dataInicio, dataFim).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun buscarPorEmpregoEPeriodo(
        empregoId: Long,
        dataInicio: LocalDate,
        dataFim: LocalDate
    ): List<Ponto> {
        return pontoDao.buscarPorEmpregoEPeriodo(empregoId, dataInicio, dataFim).map { it.toDomain() }
    }

    override fun observarPorEmpregoEPeriodo(
        empregoId: Long,
        dataInicio: LocalDate,
        dataFim: LocalDate
    ): Flow<List<Ponto>> {
        return pontoDao.observarPorEmpregoEPeriodo(empregoId, dataInicio, dataFim).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    // === Atualização de foto ===

    override suspend fun atualizarFotoComprovante(pontoId: Long, fotoPath: String?) {
        pontoDao.atualizarFotoComprovante(pontoId, fotoPath)
    }

    // === Soft Delete e Lixeira ===

    override suspend fun buscarPorIdIncluindoDeletados(id: Long): Ponto? {
        return pontoDao.buscarPorIdIncluindoDeletados(id)?.toDomain()
    }

    override suspend fun listarDeletados(): List<Ponto> {
        return pontoDao.listarDeletados().map { it.toDomain() }
    }

    override fun observarDeletados(): Flow<List<Ponto>> {
        return pontoDao.observarDeletados().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun softDelete(pontoId: Long) {
        pontoDao.softDelete(pontoId, System.currentTimeMillis())
    }

    override suspend fun excluirPermanente(pontoId: Long) {
        pontoDao.excluirPermanente(pontoId)
    }

    override suspend fun contarDeletados(): Int {
        return pontoDao.contarDeletados()
    }

    override suspend fun restaurar(pontoId: Long) {
        pontoDao.restaurar(pontoId, System.currentTimeMillis())
    }
}
