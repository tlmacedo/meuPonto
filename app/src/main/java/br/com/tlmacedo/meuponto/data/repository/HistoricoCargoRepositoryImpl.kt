package br.com.tlmacedo.meuponto.data.repository

import br.com.tlmacedo.meuponto.data.local.database.dao.AjusteSalarialDao
import br.com.tlmacedo.meuponto.data.local.database.dao.HistoricoCargoDao
import br.com.tlmacedo.meuponto.data.local.database.entity.toDomain
import br.com.tlmacedo.meuponto.data.local.database.entity.toEntity
import br.com.tlmacedo.meuponto.domain.model.AjusteSalarial
import br.com.tlmacedo.meuponto.domain.model.HistoricoCargo
import br.com.tlmacedo.meuponto.domain.repository.HistoricoCargoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HistoricoCargoRepositoryImpl @Inject constructor(
    private val historicoCargoDao: HistoricoCargoDao,
    private val ajusteSalarialDao: AjusteSalarialDao
) : HistoricoCargoRepository {

    override fun listarPorEmprego(empregoId: Long): Flow<List<HistoricoCargo>> {
        return historicoCargoDao.listarPorEmprego(empregoId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun buscarPorId(id: Long): HistoricoCargo? {
        return historicoCargoDao.buscarPorId(id)?.toDomain()
    }

    override suspend fun salvar(historico: HistoricoCargo): Long {
        return historicoCargoDao.inserir(historico.toEntity())
    }

    override suspend fun excluir(historico: HistoricoCargo) {
        historicoCargoDao.excluir(historico.toEntity())
    }

    override fun listarAjustes(historicoCargoId: Long): Flow<List<AjusteSalarial>> {
        return ajusteSalarialDao.listarPorHistoricoCargo(historicoCargoId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun salvarAjuste(ajuste: AjusteSalarial): Long {
        return ajusteSalarialDao.inserir(ajuste.toEntity())
    }

    override suspend fun excluirAjuste(ajuste: AjusteSalarial) {
        ajusteSalarialDao.excluir(ajuste.toEntity())
    }
}
