// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/data/repository/PontoRepositoryImpl.kt
package br.com.tlmacedo.meuponto.data.repository

import br.com.tlmacedo.meuponto.data.local.database.dao.PontoDao
import br.com.tlmacedo.meuponto.data.local.database.entity.toDomain
import br.com.tlmacedo.meuponto.data.local.database.entity.toEntity
import br.com.tlmacedo.meuponto.domain.model.Ponto
import br.com.tlmacedo.meuponto.domain.repository.PontoRepository
import br.com.tlmacedo.meuponto.domain.service.AuditService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementação do repositório de Pontos.
 *
 * @author Thiago
 * @since 1.0.0
 * @updated 11.0.0 - Adicionado suporte completo a soft delete, lixeira e auditoria
 */
@Singleton
class PontoRepositoryImpl @Inject constructor(
    private val pontoDao: PontoDao,
    private val auditService: AuditService
) : PontoRepository {

    private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    // === Operações básicas ===

    override suspend fun inserir(ponto: Ponto): Long {
        val id = pontoDao.inserir(ponto.toEntity())

        auditService.logCreate(
            entidade = ENTIDADE,
            entidadeId = id,
            motivo = "Ponto registrado em ${ponto.data.format(dateFormatter)} às ${
                ponto.hora.format(
                    timeFormatter
                )
            }",
            novoValor = ponto,
            serializer = { auditService.toJson(it.toAuditMap()) }
        )

        return id
    }

    override suspend fun atualizar(ponto: Ponto) {
        val anterior = pontoDao.buscarPorId(ponto.id)?.toDomain()
        pontoDao.atualizar(ponto.toEntity())

        auditService.logUpdate(
            entidade = ENTIDADE,
            entidadeId = ponto.id,
            motivo = "Ponto atualizado em ${ponto.data.format(dateFormatter)}",
            valorAntigo = anterior,
            valorNovo = ponto,
            serializer = { auditService.toJson(it.toAuditMap()) }
        )
    }

    override suspend fun excluir(ponto: Ponto) {
        auditService.logPermanentDelete(
            entidade = ENTIDADE,
            entidadeId = ponto.id,
            motivo = "Ponto excluído permanentemente: ${ponto.data.format(dateFormatter)} às ${
                ponto.hora.format(
                    timeFormatter
                )
            }"
        )

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
        return pontoDao.buscarPorEmpregoEPeriodo(empregoId, dataInicio, dataFim)
            .map { it.toDomain() }
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

    override suspend fun atualizarFotoComprovante(
        pontoId: Long,
        fotoPath: String?,
        fotoOrigem: br.com.tlmacedo.meuponto.domain.model.FotoOrigem
    ) {
        val anterior = pontoDao.buscarPorId(pontoId)?.toDomain()
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
        val ponto = pontoDao.buscarPorId(pontoId)?.toDomain()
        pontoDao.softDelete(pontoId, System.currentTimeMillis())

        auditService.logDelete(
            entidade = ENTIDADE,
            entidadeId = pontoId,
            motivo = ponto?.let {
                "Ponto movido para lixeira: ${it.data.format(dateFormatter)} às ${
                    it.hora.format(
                        timeFormatter
                    )
                }"
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
                "Ponto excluído permanentemente: ${it.data.format(dateFormatter)} às ${
                    it.hora.format(
                        timeFormatter
                    )
                }"
            } ?: "Ponto excluído permanentemente"
        )

        pontoDao.excluirPermanente(pontoId)
    }

    override suspend fun contarDeletados(): Int {
        return pontoDao.contarDeletados()
    }

    override suspend fun restaurar(pontoId: Long) {
        val ponto = pontoDao.buscarPorIdIncluindoDeletados(pontoId)?.toDomain()
        pontoDao.restaurar(pontoId, System.currentTimeMillis())

        auditService.logRestore(
            entidade = ENTIDADE,
            entidadeId = pontoId,
            motivo = ponto?.let {
                "Ponto restaurado: ${it.data.format(dateFormatter)} às ${
                    it.hora.format(
                        timeFormatter
                    )
                }"
            } ?: "Ponto restaurado da lixeira"
        )
    }

    // === Manutenção ===

    override suspend fun excluirPontosAnterioresA(data: LocalDate): Int {
        val count = pontoDao.excluirPontosAnterioresA(data)
        if (count > 0) {
            auditService.logPermanentDelete(
                entidade = ENTIDADE,
                entidadeId = 0L,
                motivo = "Exclusão em massa de $count pontos anteriores a ${
                    data.format(
                        dateFormatter
                    )
                }"
            )
        }
        return count
    }

    // === Helpers ===

    private fun Ponto.toAuditMapLocal(): Map<String, Any?> = mapOf(
        "id" to id,
        "empregoId" to empregoId,
        "data" to data.format(dateFormatter),
        "horaReal" to horaFormatada,
        "horaConsiderada" to horaConsideradaFormatada,
        "nsr" to nsr,
        "observacao" to observacao,
        "fotoComprovantePath" to fotoComprovantePath,
        "isEditadoManualmente" to isEditadoManualmente,
        "isDeleted" to isDeleted
    )

    companion object {
        private const val ENTIDADE = "Ponto"
    }
}
