package br.com.tlmacedo.meuponto.domain.repository

import br.com.tlmacedo.meuponto.domain.model.AjusteSalarial
import br.com.tlmacedo.meuponto.domain.model.HistoricoCargo
import kotlinx.coroutines.flow.Flow

interface HistoricoCargoRepository {
    fun listarPorEmprego(empregoId: Long): Flow<List<HistoricoCargo>>
    suspend fun buscarPorId(id: Long): HistoricoCargo?
    suspend fun salvar(historico: HistoricoCargo): Long
    suspend fun excluir(historico: HistoricoCargo)

    fun listarAjustes(historicoCargoId: Long): Flow<List<AjusteSalarial>>
    suspend fun salvarAjuste(ajuste: AjusteSalarial): Long
    suspend fun excluirAjuste(ajuste: AjusteSalarial)
}
