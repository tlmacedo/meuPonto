// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/data/repository/LixeiraRepositoryImpl.kt
package br.com.tlmacedo.meuponto.data.repository

import br.com.tlmacedo.meuponto.data.local.database.dao.PontoDao
import br.com.tlmacedo.meuponto.data.local.database.entity.toDomain
import br.com.tlmacedo.meuponto.domain.model.Ponto
import br.com.tlmacedo.meuponto.domain.repository.LixeiraRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementação do LixeiraRepository.
 *
 * @author Thiago
 * @since 11.0.0
 */
@Singleton
class LixeiraRepositoryImpl @Inject constructor(
    private val pontoDao: PontoDao
) : LixeiraRepository {

    companion object {
        private const val TAG = "LixeiraRepository"

        /** Dias padrão de retenção na lixeira */
        const val DIAS_RETENCAO_PADRAO = 30
    }

    // ========================================================================
    // MOVER PARA LIXEIRA
    // ========================================================================

    override suspend fun moverParaLixeira(pontoId: Long): Result<Unit> {
        return runCatching {
            val agora = System.currentTimeMillis()
            pontoDao.softDelete(pontoId, agora)
        }
    }

    override suspend fun moverParaLixeira(pontoIds: List<Long>): Result<Int> {
        return runCatching {
            val agora = System.currentTimeMillis()
            var count = 0
            pontoIds.forEach { id ->
                pontoDao.softDelete(id, agora)
                count++
            }
            count
        }
    }

    // ========================================================================
    // RESTAURAR
    // ========================================================================

    override suspend fun restaurar(pontoId: Long): Result<Unit> {
        return runCatching {
            val agora = System.currentTimeMillis()
            pontoDao.restaurar(pontoId, agora)
        }
    }

    override suspend fun restaurar(pontoIds: List<Long>): Result<Int> {
        return runCatching {
            val agora = System.currentTimeMillis()
            var count = 0
            pontoIds.forEach { id ->
                pontoDao.restaurar(id, agora)
                count++
            }
            count
        }
    }

    // ========================================================================
    // EXCLUIR PERMANENTE
    // ========================================================================

    override suspend fun excluirPermanente(pontoId: Long): Result<Unit> {
        return runCatching {
            pontoDao.excluirPermanente(pontoId)
        }
    }

    override suspend fun excluirPermanente(pontoIds: List<Long>): Result<Int> {
        return runCatching {
            var count = 0
            pontoIds.forEach { id ->
                pontoDao.excluirPermanente(id)
                count++
            }
            count
        }
    }

    override suspend fun esvaziarLixeira(): Result<Int> {
        return runCatching {
            val deletados = pontoDao.listarDeletados()
            deletados.forEach { ponto ->
                pontoDao.excluirPermanente(ponto.id)
            }
            deletados.size
        }
    }

    // ========================================================================
    // CONSULTAS
    // ========================================================================

    override fun observarPontosNaLixeira(): Flow<List<Ponto>> {
        return pontoDao.observarDeletados().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun listarPontosNaLixeira(): List<Ponto> {
        return pontoDao.listarDeletados().map { it.toDomain() }
    }

    override suspend fun contarItensNaLixeira(): Int {
        return pontoDao.contarDeletados()
    }

    override suspend fun lixeiraVazia(): Boolean {
        return contarItensNaLixeira() == 0
    }

    // ========================================================================
    // LIMPEZA AUTOMÁTICA
    // ========================================================================

    override suspend fun limparExpirados(diasRetencao: Int): Result<Int> {
        return runCatching {
            val agora = System.currentTimeMillis()
            val limiteExpiracao = agora - TimeUnit.DAYS.toMillis(diasRetencao.toLong())

            val deletados = pontoDao.listarDeletados()
            var count = 0

            deletados.forEach { ponto ->
                val deletedAt = ponto.deletedAt
                if (deletedAt != null && deletedAt < limiteExpiracao) {
                    pontoDao.excluirPermanente(ponto.id)
                    count++
                }
            }

            count
        }
    }

    override suspend fun listarPrestesAExpirar(diasRestantes: Int): List<Ponto> {
        val agora = System.currentTimeMillis()
        val limitePrestesAExpirar = agora - TimeUnit.DAYS.toMillis(
            (DIAS_RETENCAO_PADRAO - diasRestantes).toLong()
        )

        return pontoDao.listarDeletados()
            .filter { ponto ->
                val deletedAt = ponto.deletedAt
                deletedAt != null && deletedAt < limitePrestesAExpirar
            }
            .map { it.toDomain() }
    }
}
