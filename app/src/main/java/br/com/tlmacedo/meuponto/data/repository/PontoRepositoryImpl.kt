// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/data/repository/PontoRepositoryImpl.kt
package br.com.tlmacedo.meuponto.data.repository

import br.com.tlmacedo.meuponto.data.local.database.dao.PontoDao
import br.com.tlmacedo.meuponto.data.local.database.entity.PontoEntity
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
 * @updated 11.0.0 - Adicionado suporte a soft delete e lixeira
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

    // === Consultas padrão ===

    override suspend fun buscarPorId(id: Long): Ponto? {
        return pontoDao.buscarPorId(id)?.toDomain()
    }

    override fun listarTodos(): Flow<List<Ponto>> {
        return pontoDao.listarTodos().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun listarPorEmprego(empregoId: Long): Flow<List<Ponto>> {
        return pontoDao.listarPorEmprego(empregoId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun listarPorData(data: LocalDate): Flow<List<Ponto>> {
        return pontoDao.listarPorData(data).map { entities ->
            entities.map { it.toDomain() }
        }
    }

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
}
