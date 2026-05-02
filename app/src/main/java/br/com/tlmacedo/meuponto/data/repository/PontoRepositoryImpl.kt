// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/data/repository/PontoRepositoryImpl.kt
package br.com.tlmacedo.meuponto.data.repository

import br.com.tlmacedo.meuponto.data.local.database.dao.PontoDao
import br.com.tlmacedo.meuponto.data.local.database.entity.toDomain
import br.com.tlmacedo.meuponto.data.local.database.entity.toEntity
import br.com.tlmacedo.meuponto.domain.model.FotoOrigem
import br.com.tlmacedo.meuponto.domain.model.Ponto
import br.com.tlmacedo.meuponto.domain.repository.PontoRepository
import br.com.tlmacedo.meuponto.domain.service.AuditService
import br.com.tlmacedo.meuponto.util.helper.dateFormatterSimples
import br.com.tlmacedo.meuponto.util.helper.horaFormatter
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
 * @updated 11.0.0 - Adicionado suporte completo a soft delete, lixeira e auditoria
 * @updated 12.0.0 - Refatorado para usar AuditedRepositoryBase
 */
@Singleton
class PontoRepositoryImpl @Inject constructor(
    private val pontoDao: PontoDao,
    auditService: AuditService
) : AuditedRepositoryBase<Ponto>(auditService, ENTIDADE), PontoRepository {

    // ========================================================================
    // PONTE COM O DAO
    // ========================================================================

    override suspend fun daoInserir(domain: Ponto): Long = pontoDao.inserir(domain.toEntity())
    override suspend fun daoBuscarPorId(id: Long): Ponto? = pontoDao.buscarPorId(id)?.toDomain()
    override suspend fun daoAtualizar(domain: Ponto) = pontoDao.atualizar(domain.toEntity())
    override suspend fun daoExcluir(domain: Ponto) = pontoDao.excluir(domain.toEntity())
    override fun getEntityId(domain: Ponto): Long = domain.id

    override fun Ponto.toAuditMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "empregoId" to empregoId,
        "data" to data.format(dateFormatterSimples),
        "horaReal" to horaFormatada,
        "horaConsiderada" to horaConsideradaFormatada,
        "nsr" to nsr,
        "observacao" to observacao,
        "fotoComprovantePath" to fotoComprovantePath,
        "isEditadoManualmente" to isEditadoManualmente,
        "isDeleted" to isDeleted
    )

    // ========================================================================
    // MOTIVOS DE AUDITORIA
    // ========================================================================

    override fun motivoInserir(domain: Ponto): String =
        "Ponto registrado em ${domain.data.format(dateFormatterSimples)} às ${domain.hora.format(horaFormatter)}"

    override fun motivoAtualizar(domain: Ponto): String =
        "Ponto atualizado em ${domain.data.format(dateFormatterSimples)}"

    override fun motivoExcluir(domain: Ponto): String =
        "Ponto excluído permanentemente: ${domain.data.format(dateFormatterSimples)} às ${domain.hora.format(horaFormatter)}"

    // ========================================================================
    // CRUD
    // ========================================================================

    override suspend fun inserir(ponto: Ponto): Long = inserirComAuditoria(ponto)

    override suspend fun atualizar(ponto: Ponto) = atualizarComAuditoria(ponto)

    override suspend fun excluir(ponto: Ponto) = excluirComAuditoria(ponto)

    // ========================================================================
    // CONSULTAS POR ID
    // ========================================================================

    override suspend fun buscarPorId(id: Long): Ponto? = daoBuscarPorId(id)

    override fun observarPorId(id: Long): Flow<Ponto?> =
        pontoDao.observarPorId(id).map { it?.toDomain() }

    // ========================================================================
    // CONSULTAS GERAIS
    // ========================================================================

    override fun listarTodos(): Flow<List<Ponto>> =
        pontoDao.listarTodos().map { entities -> entities.map { it.toDomain() } }

    override fun observarTodos(): Flow<List<Ponto>> =
        pontoDao.observarTodos().map { entities -> entities.map { it.toDomain() } }

    // ========================================================================
    // CONSULTAS POR EMPREGO
    // ========================================================================

    override fun listarPorEmprego(empregoId: Long): Flow<List<Ponto>> =
        pontoDao.listarPorEmprego(empregoId).map { entities -> entities.map { it.toDomain() } }

    override fun observarPorEmprego(empregoId: Long): Flow<List<Ponto>> =
        pontoDao.observarPorEmprego(empregoId).map { entities -> entities.map { it.toDomain() } }

    override suspend fun contarPorEmprego(empregoId: Long): Int =
        pontoDao.contarPorEmprego(empregoId)

    override suspend fun buscarPrimeiraData(empregoId: Long): LocalDate? =
        pontoDao.buscarPrimeiraData(empregoId)

    // ========================================================================
    // CONSULTAS POR DATA
    // ========================================================================

    override fun listarPorData(data: LocalDate): Flow<List<Ponto>> =
        pontoDao.listarPorData(data).map { entities -> entities.map { it.toDomain() } }

    override suspend fun buscarPorEmpregoEData(empregoId: Long, data: LocalDate): List<Ponto> =
        pontoDao.buscarPorEmpregoEData(empregoId, data).map { it.toDomain() }

    override fun observarPorEmpregoEData(empregoId: Long, data: LocalDate): Flow<List<Ponto>> =
        pontoDao.observarPorEmpregoEData(empregoId, data).map { entities -> entities.map { it.toDomain() } }

    // ========================================================================
    // CONSULTAS POR PERÍODO
    // ========================================================================

    override fun listarPorPeriodo(dataInicio: LocalDate, dataFim: LocalDate): Flow<List<Ponto>> =
        pontoDao.listarPorPeriodo(dataInicio, dataFim).map { entities -> entities.map { it.toDomain() } }

    override fun listarPorEmpregoEPeriodo(
        empregoId: Long,
        dataInicio: LocalDate,
        dataFim: LocalDate
    ): Flow<List<Ponto>> =
        pontoDao.listarPorEmpregoEPeriodo(empregoId, dataInicio, dataFim)
            .map { entities -> entities.map { it.toDomain() } }

    override suspend fun buscarPorEmpregoEPeriodo(
        empregoId: Long,
        dataInicio: LocalDate,
        dataFim: LocalDate
    ): List<Ponto> =
        pontoDao.buscarPorEmpregoEPeriodo(empregoId, dataInicio, dataFim).map { it.toDomain() }

    override fun observarPorEmpregoEPeriodo(
        empregoId: Long,
        dataInicio: LocalDate,
        dataFim: LocalDate
    ): Flow<List<Ponto>> =
        pontoDao.observarPorEmpregoEPeriodo(empregoId, dataInicio, dataFim)
            .map { entities -> entities.map { it.toDomain() } }

    // ========================================================================
    // ATUALIZAÇÃO DE FOTO
    // ========================================================================

    override suspend fun atualizarFotoComprovante(
        pontoId: Long,
        fotoPath: String?,
        fotoOrigem: FotoOrigem
    ) {
        val anterior = daoBuscarPorId(pontoId)
        pontoDao.atualizarFotoComprovante(pontoId, fotoPath, fotoOrigem.id)

        auditService.logUpdate(
            entidade = ENTIDADE,
            entidadeId = pontoId,
            motivo = if (fotoPath != null) "Foto comprovante adicionada (${fotoOrigem.descricao})" else "Foto comprovante removida",
            valorAntigo = anterior?.fotoComprovantePath,
            valorNovo = fotoPath ?: "",
            serializer = { it }
        )
    }

    // ========================================================================
    // SOFT DELETE E LIXEIRA
    // ========================================================================

    override suspend fun buscarPorIdIncluindoDeletados(id: Long): Ponto? =
        pontoDao.buscarPorIdIncluindoDeletados(id)?.toDomain()

    override suspend fun listarDeletados(): List<Ponto> =
        pontoDao.listarDeletados().map { it.toDomain() }

    override fun observarDeletados(): Flow<List<Ponto>> =
        pontoDao.observarDeletados().map { entities -> entities.map { it.toDomain() } }

    override suspend fun softDelete(pontoId: Long) {
        val ponto = daoBuscarPorId(pontoId)
        pontoDao.softDelete(pontoId, System.currentTimeMillis())

        auditService.logDelete(
            entidade = ENTIDADE,
            entidadeId = pontoId,
            motivo = ponto?.let {
                "Ponto movido para lixeira: ${it.data.format(dateFormatterSimples)} às ${it.hora.format(horaFormatter)}"
            } ?: "Ponto movido para lixeira",
            dadosAnteriores = ponto?.let { auditService.toJson(it.toAuditMap()) }
        )
    }

    override suspend fun excluirPermanente(pontoId: Long) {
        val ponto = pontoDao.buscarPorIdIncluindoDeletados(pontoId)?.toDomain()

        auditService.logPermanentDelete(
            entidade = ENTIDADE,
            entidadeId = pontoId,
            motivo = ponto?.let {
                "Ponto excluído permanentemente: ${it.data.format(dateFormatterSimples)} às ${it.hora.format(horaFormatter)}"
            } ?: "Ponto excluído permanentemente"
        )

        pontoDao.excluirPermanente(pontoId)
    }

    override suspend fun contarDeletados(): Int = pontoDao.contarDeletados()

    override suspend fun restaurar(pontoId: Long) {
        val ponto = pontoDao.buscarPorIdIncluindoDeletados(pontoId)?.toDomain()
        pontoDao.restaurar(pontoId, System.currentTimeMillis())

        auditService.logRestore(
            entidade = ENTIDADE,
            entidadeId = pontoId,
            motivo = ponto?.let {
                "Ponto restaurado: ${it.data.format(dateFormatterSimples)} às ${it.hora.format(horaFormatter)}"
            } ?: "Ponto restaurado da lixeira"
        )
    }

    // ========================================================================
    // MANUTENÇÃO
    // ========================================================================

    override suspend fun excluirPontosAnterioresA(data: LocalDate): Int {
        val count = pontoDao.excluirPontosAnterioresA(data)
        if (count > 0) {
            auditService.logPermanentDelete(
                entidade = ENTIDADE,
                entidadeId = 0L,
                motivo = "Exclusão em massa de $count pontos anteriores a ${data.format(dateFormatterSimples)}"
            )
        }
        return count
    }

    companion object {
        private const val ENTIDADE = "Ponto"
    }
}
