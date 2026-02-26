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
 * Implementação concreta do repositório de pontos.
 *
 * Atua como intermediário entre a camada de domínio e a camada de dados,
 * realizando a conversão entre modelos de domínio (Ponto) e entidades
 * de banco de dados (PontoEntity).
 *
 * @property pontoDao DAO do Room para operações de banco de dados
 *
 * @author Thiago
 * @since 1.0.0
 * @updated 6.1.0 - Removidos métodos deprecados (sem filtro por emprego)
 */
@Singleton
class PontoRepositoryImpl @Inject constructor(
    private val pontoDao: PontoDao
) : PontoRepository {

    // ========================================================================
    // Operações de Escrita (CRUD)
    // ========================================================================

    override suspend fun inserir(ponto: Ponto): Long {
        return pontoDao.inserir(ponto.toEntity())
    }

    override suspend fun atualizar(ponto: Ponto) {
        pontoDao.atualizar(ponto.toEntity())
    }

    override suspend fun excluir(ponto: Ponto) {
        pontoDao.excluir(ponto.toEntity())
    }

    override suspend fun excluirPorId(id: Long) {
        pontoDao.excluirPorId(id)
    }

    // ========================================================================
    // Operações de Leitura - Por ID
    // ========================================================================

    override suspend fun buscarPorId(id: Long): Ponto? {
        return pontoDao.buscarPorId(id)?.toDomain()
    }

    override fun observarPorId(id: Long): Flow<Ponto?> {
        return pontoDao.observarPorId(id).map { entity ->
            entity?.toDomain()
        }
    }

    // ========================================================================
    // Operações de Leitura - Por Emprego
    // ========================================================================

    override fun observarPorEmprego(empregoId: Long): Flow<List<Ponto>> {
        return pontoDao.listarPorEmprego(empregoId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun buscarPrimeiraData(empregoId: Long): LocalDate? {
        return pontoDao.buscarPrimeiraData(empregoId)
    }

    override fun observarPorEmpregoEData(empregoId: Long, data: LocalDate): Flow<List<Ponto>> {
        return pontoDao.listarPorEmpregoEData(empregoId, data).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun buscarPorEmpregoEData(empregoId: Long, data: LocalDate): List<Ponto> {
        return pontoDao.buscarPorEmpregoEData(empregoId, data).map { it.toDomain() }
    }

    override fun observarPorEmpregoEPeriodo(
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
        return pontoDao.buscarPorEmpregoEPeriodo(empregoId, dataInicio, dataFim)
            .map { it.toDomain() }
    }

    // ========================================================================
    // Operações de Leitura - Por Marcador
    // ========================================================================

    override fun observarPorMarcador(marcadorId: Long): Flow<List<Ponto>> {
        return pontoDao.listarPorMarcador(marcadorId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    // ========================================================================
    // Operações de Contagem
    // ========================================================================

    override suspend fun contarPorEmprego(empregoId: Long): Int {
        return pontoDao.contarPorEmprego(empregoId)
    }

    override suspend fun contarPorEmpregoEData(empregoId: Long, data: LocalDate): Int {
        return pontoDao.contarPorEmpregoEData(empregoId, data)
    }

    override suspend fun contarPorEmpregoEPeriodo(
        empregoId: Long,
        dataInicio: LocalDate,
        dataFim: LocalDate
    ): Int {
        return pontoDao.contarPorEmpregoEPeriodo(empregoId, dataInicio, dataFim)
    }

    // ========================================================================
    // Operações Auxiliares
    // ========================================================================

    override fun observarDatasComRegistro(empregoId: Long): Flow<List<LocalDate>> {
        return pontoDao.listarDatasComRegistro(empregoId)
    }

    override suspend fun buscarUltimoPonto(empregoId: Long): Ponto? {
        return pontoDao.buscarUltimoPonto(empregoId)?.toDomain()
    }

    override fun observarUltimoPonto(empregoId: Long): Flow<Ponto?> {
        return pontoDao.observarUltimoPonto(empregoId).map { entity ->
            entity?.toDomain()
        }
    }

    // ========================================================================
    // Operações de Migração
    // ========================================================================

    override suspend fun migrarParaEmprego(empregoIdOrigem: Long, empregoIdDestino: Long): Int {
        return pontoDao.migrarParaEmprego(empregoIdOrigem, empregoIdDestino)
    }
}
