// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/data/repository/ConfiguracaoEmpregoRepositoryImpl.kt
package br.com.tlmacedo.meuponto.data.repository

import br.com.tlmacedo.meuponto.data.local.database.dao.ConfiguracaoEmpregoDao
import br.com.tlmacedo.meuponto.data.local.database.entity.toDomain
import br.com.tlmacedo.meuponto.data.local.database.entity.toEntity
import br.com.tlmacedo.meuponto.domain.model.ConfiguracaoEmprego
import br.com.tlmacedo.meuponto.domain.repository.ConfiguracaoEmpregoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementação do repositório de configurações de emprego.
 *
 * SIMPLIFICADO: Apenas configurações de exibição/comportamento.
 * Configurações de jornada/banco de horas foram migradas para VersaoJornadaRepository.
 *
 * @author Thiago
 * @since 2.0.0
 * @updated 8.0.0 - Simplificado após migração de campos para VersaoJornada
 */
@Singleton
class ConfiguracaoEmpregoRepositoryImpl @Inject constructor(
    private val configuracaoEmpregoDao: ConfiguracaoEmpregoDao
) : ConfiguracaoEmpregoRepository {

    override suspend fun inserir(configuracao: ConfiguracaoEmprego): Long {
        return configuracaoEmpregoDao.inserir(configuracao.toEntity())
    }

    override suspend fun atualizar(configuracao: ConfiguracaoEmprego) {
        configuracaoEmpregoDao.atualizar(configuracao.toEntity())
    }

    override suspend fun excluir(configuracao: ConfiguracaoEmprego) {
        configuracaoEmpregoDao.excluir(configuracao.toEntity())
    }

    override suspend fun buscarPorId(id: Long): ConfiguracaoEmprego? {
        return configuracaoEmpregoDao.buscarPorId(id)?.toDomain()
    }

    override suspend fun buscarPorEmpregoId(empregoId: Long): ConfiguracaoEmprego? {
        return configuracaoEmpregoDao.buscarPorEmpregoId(empregoId)?.toDomain()
    }

    override fun observarPorEmpregoId(empregoId: Long): Flow<ConfiguracaoEmprego?> {
        return configuracaoEmpregoDao.observarPorEmpregoId(empregoId).map { it?.toDomain() }
    }

    override suspend fun listarTodas(): List<ConfiguracaoEmprego> {
        return configuracaoEmpregoDao.listarTodas().map { it.toDomain() }
    }

    override fun observarTodas(): Flow<List<ConfiguracaoEmprego>> {
        return configuracaoEmpregoDao.observarTodas().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun existeParaEmprego(empregoId: Long): Boolean {
        return configuracaoEmpregoDao.existeParaEmprego(empregoId)
    }
}
