package br.com.tlmacedo.meuponto.data.repository

import br.com.tlmacedo.meuponto.data.local.database.dao.PontoDao
import br.com.tlmacedo.meuponto.data.local.database.entity.PontoEntity
import br.com.tlmacedo.meuponto.domain.model.Ponto
import br.com.tlmacedo.meuponto.domain.repository.PontoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementação concreta do repositório de pontos.
 *
 * Responsável por intermediar as operações entre a camada de domínio
 * e a camada de dados (Room Database).
 *
 * @property pontoDao DAO para operações de banco de dados
 *
 * @author Thiago
 * @since 1.0.0
 */
@Singleton
class PontoRepositoryImpl @Inject constructor(
    private val pontoDao: PontoDao
) : PontoRepository {

    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    /**
     * Insere um novo registro de ponto.
     *
     * @param ponto Ponto a ser inserido
     * @return ID do registro inserido
     */
    override suspend fun inserir(ponto: Ponto): Long {
        val entity = PontoEntity.fromDomain(ponto)
        return pontoDao.inserir(entity)
    }

    /**
     * Atualiza um registro de ponto existente.
     *
     * @param ponto Ponto com os dados atualizados
     */
    override suspend fun atualizar(ponto: Ponto) {
        val entity = PontoEntity.fromDomain(ponto)
        pontoDao.atualizar(entity)
    }

    /**
     * Remove um registro de ponto.
     *
     * @param ponto Ponto a ser removido
     */
    override suspend fun deletar(ponto: Ponto) {
        val entity = PontoEntity.fromDomain(ponto)
        pontoDao.deletar(entity)
    }

    /**
     * Busca um ponto pelo ID.
     *
     * @param id ID do ponto
     * @return Ponto encontrado ou null
     */
    override suspend fun buscarPorId(id: Long): Ponto? {
        return pontoDao.buscarPorId(id)?.toDomain()
    }

    /**
     * Observa todos os pontos de uma data específica.
     *
     * @param data Data para buscar os pontos
     * @return Flow com a lista de pontos do dia
     */
    override fun observarPontosPorData(data: LocalDate): Flow<List<Ponto>> {
        val dataString = data.format(dateFormatter)
        return pontoDao.observarPontosPorData(dataString).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    /**
     * Busca todos os pontos de uma data específica.
     *
     * @param data Data para buscar os pontos
     * @return Lista de pontos do dia
     */
    override suspend fun buscarPontosPorData(data: LocalDate): List<Ponto> {
        val dataString = data.format(dateFormatter)
        return pontoDao.buscarPontosPorData(dataString).map { it.toDomain() }
    }

    /**
     * Observa todos os pontos de um período.
     *
     * @param dataInicio Data inicial do período
     * @param dataFim Data final do período
     * @return Flow com a lista de pontos do período
     */
    override fun observarPontosPorPeriodo(
        dataInicio: LocalDate,
        dataFim: LocalDate
    ): Flow<List<Ponto>> {
        val dataInicioString = dataInicio.format(dateFormatter)
        val dataFimString = dataFim.format(dateFormatter)
        return pontoDao.observarPontosPorPeriodo(dataInicioString, dataFimString).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    /**
     * Busca o último ponto registrado em uma data.
     *
     * @param data Data para buscar
     * @return Último ponto do dia ou null
     */
    override suspend fun buscarUltimoPontoDoDia(data: LocalDate): Ponto? {
        val dataString = data.format(dateFormatter)
        return pontoDao.buscarUltimoPontoDoDia(dataString)?.toDomain()
    }
}
