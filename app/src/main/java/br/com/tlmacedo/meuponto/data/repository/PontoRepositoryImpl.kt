// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/data/repository/PontoRepositoryImpl.kt
package br.com.tlmacedo.meuponto.data.repository

import br.com.tlmacedo.meuponto.data.local.database.dao.PontoDao
import br.com.tlmacedo.meuponto.data.local.database.entity.toDomain
import br.com.tlmacedo.meuponto.data.local.database.entity.toEntity
import br.com.tlmacedo.meuponto.domain.model.Ponto
import br.com.tlmacedo.meuponto.domain.repository.PontoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
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
 */
@Singleton
class PontoRepositoryImpl @Inject constructor(
    private val pontoDao: PontoDao
) : PontoRepository {

    // ========================================================================
    // Operações de Escrita (CRUD)
    // ========================================================================

    /**
     * Insere um novo registro de ponto no banco de dados.
     */
    override suspend fun inserir(ponto: Ponto): Long {
        return pontoDao.inserir(ponto.toEntity())
    }

    /**
     * Atualiza um registro de ponto existente.
     */
    override suspend fun atualizar(ponto: Ponto) {
        pontoDao.atualizar(ponto.toEntity())
    }

    /**
     * Remove um registro de ponto do banco de dados.
     */
    override suspend fun excluir(ponto: Ponto) {
        pontoDao.excluir(ponto.toEntity())
    }

    // ========================================================================
    // Operações de Leitura (Queries)
    // ========================================================================

    /**
     * Busca um ponto pelo ID e converte para modelo de domínio.
     */
    override suspend fun buscarPorId(id: Long): Ponto? {
        return pontoDao.buscarPorId(id)?.toDomain()
    }

    /**
     * Busca todos os pontos de uma data específica.
     * Utiliza o Flow interno e pega apenas o primeiro resultado.
     */
    override suspend fun buscarPontosPorData(data: LocalDate): List<Ponto> {
        return pontoDao.listarPorData(data)
            .first()
            .map { it.toDomain() }
    }

    /**
     * Busca o último ponto registrado em uma data.
     */
    override suspend fun buscarUltimoPontoDoDia(data: LocalDate): Ponto? {
        return pontoDao.listarPorData(data)
            .first()
            .lastOrNull()
            ?.toDomain()
    }

    // ========================================================================
    // Operações Reativas (Flows)
    // ========================================================================

    /**
     * Observa os pontos de uma data de forma reativa.
     */
    override fun observarPontosPorData(data: LocalDate): Flow<List<Ponto>> {
        return pontoDao.listarPorData(data).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    /**
     * Observa todos os pontos de forma reativa.
     */
    override fun observarTodos(): Flow<List<Ponto>> {
        return pontoDao.listarTodos().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    /**
     * Observa os pontos de um período de forma reativa.
     */
    override fun observarPontosPorPeriodo(
        dataInicio: LocalDate,
        dataFim: LocalDate
    ): Flow<List<Ponto>> {
        return pontoDao.listarPorPeriodo(dataInicio, dataFim).map { entities ->
            entities.map { it.toDomain() }
        }
    }
}
