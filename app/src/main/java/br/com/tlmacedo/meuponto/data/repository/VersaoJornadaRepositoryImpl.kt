// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/data/repository/VersaoJornadaRepositoryImpl.kt
package br.com.tlmacedo.meuponto.data.repository

import br.com.tlmacedo.meuponto.data.local.database.dao.HorarioDiaSemanaDao
import br.com.tlmacedo.meuponto.data.local.database.dao.VersaoJornadaDao
import br.com.tlmacedo.meuponto.data.local.database.entity.toDomain
import br.com.tlmacedo.meuponto.data.local.database.entity.toEntity
import br.com.tlmacedo.meuponto.domain.model.DiaSemana
import br.com.tlmacedo.meuponto.domain.model.HorarioDiaSemana
import br.com.tlmacedo.meuponto.domain.model.VersaoJornada
import br.com.tlmacedo.meuponto.domain.repository.VersaoJornadaRepository
import br.com.tlmacedo.meuponto.domain.service.AuditService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementação do repositório de versões de jornada.
 *
 * @author Thiago
 * @since 2.7.0
 * @updated 4.0.0 - Adicionados métodos de consulta para UseCases
 * @updated 11.0.0 - Integração com AuditService
 */
@Singleton
class VersaoJornadaRepositoryImpl @Inject constructor(
    private val versaoJornadaDao: VersaoJornadaDao,
    private val horarioDiaSemanaDao: HorarioDiaSemanaDao,
    private val auditService: AuditService
) : VersaoJornadaRepository {

    private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    // ========================================================================
    // Operações de Escrita (CRUD)
    // ========================================================================

    override suspend fun inserir(versao: VersaoJornada): Long {
        val id = versaoJornadaDao.inserir(versao.toEntity())

        auditService.logCreate(
            entidade = ENTIDADE,
            entidadeId = id,
            motivo = "Versão de jornada v${versao.numeroVersao} criada (início: ${versao.dataInicio.format(dateFormatter)})",
            novoValor = versao,
            serializer = { auditService.toJson(it.toAuditMap()) }
        )

        return id
    }

    override suspend fun atualizar(versao: VersaoJornada) {
        val anterior = versaoJornadaDao.buscarPorId(versao.id)?.toDomain()
        versaoJornadaDao.atualizar(versao.toEntity())

        auditService.logUpdate(
            entidade = ENTIDADE,
            entidadeId = versao.id,
            motivo = "Versão de jornada v${versao.numeroVersao} atualizada",
            valorAntigo = anterior,
            valorNovo = versao,
            serializer = { auditService.toJson(it.toAuditMap()) }
        )
    }

    override suspend fun excluir(versao: VersaoJornada) {
        auditService.logPermanentDelete(
            entidade = ENTIDADE,
            entidadeId = versao.id,
            motivo = "Versão de jornada v${versao.numeroVersao} excluída"
        )

        versaoJornadaDao.excluir(versao.toEntity())
    }

    override suspend fun excluirPorId(id: Long) {
        val versao = versaoJornadaDao.buscarPorId(id)?.toDomain()

        auditService.logPermanentDelete(
            entidade = ENTIDADE,
            entidadeId = id,
            motivo = versao?.let { "Versão de jornada v${it.numeroVersao} excluída" }
                ?: "Versão de jornada excluída"
        )

        versaoJornadaDao.excluirPorId(id)
    }

    // ========================================================================
    // Operações de Leitura por ID
    // ========================================================================

    override suspend fun buscarPorId(id: Long): VersaoJornada? {
        return versaoJornadaDao.buscarPorId(id)?.toDomain()
    }

    override fun observarPorId(id: Long): Flow<VersaoJornada?> {
        return versaoJornadaDao.observarPorId(id).map { it?.toDomain() }
    }

    // ========================================================================
    // Operações de Leitura por Emprego
    // ========================================================================

    override suspend fun buscarPorEmprego(empregoId: Long): List<VersaoJornada> {
        return versaoJornadaDao.buscarPorEmprego(empregoId).map { it.toDomain() }
    }

    override suspend fun listarPorEmprego(empregoId: Long): List<VersaoJornada> {
        return buscarPorEmprego(empregoId)
    }

    override fun observarPorEmprego(empregoId: Long): Flow<List<VersaoJornada>> {
        return versaoJornadaDao.observarPorEmprego(empregoId).map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun buscarVigente(empregoId: Long): VersaoJornada? {
        return versaoJornadaDao.buscarVigente(empregoId)?.toDomain()
    }

    override fun observarVigente(empregoId: Long): Flow<VersaoJornada?> {
        return versaoJornadaDao.observarVigente(empregoId).map { it?.toDomain() }
    }

    override suspend fun existeParaEmprego(empregoId: Long): Boolean {
        return versaoJornadaDao.contarPorEmprego(empregoId) > 0
    }

    // ========================================================================
    // Consultas por Data
    // ========================================================================

    override suspend fun buscarPorData(empregoId: Long, data: LocalDate): VersaoJornada? {
        return versaoJornadaDao.buscarPorEmpregoEData(empregoId, data)?.toDomain()
    }

    override suspend fun buscarPorEmpregoEData(empregoId: Long, data: LocalDate): VersaoJornada? {
        return buscarPorData(empregoId, data)
    }

    override fun observarPorEmpregoEData(empregoId: Long, data: LocalDate): Flow<VersaoJornada?> {
        return versaoJornadaDao.observarPorEmpregoEData(empregoId, data).map { it?.toDomain() }
    }

    // ========================================================================
    // Operações de Versionamento
    // ========================================================================

    override suspend fun criarNovaVersao(
        empregoId: Long,
        dataInicio: LocalDate,
        descricao: String?,
        copiarDaVersaoAnterior: Boolean
    ): Long {
        val agora = LocalDateTime.now()

        // 1. Buscar versão anterior para fechar
        val versaoAnterior = versaoJornadaDao.buscarVersaoAnterior(empregoId, dataInicio)
            ?: versaoJornadaDao.buscarVigente(empregoId)

        // 2. Fechar versão anterior
        versaoAnterior?.let {
            val dataFimAnterior = dataInicio.minusDays(1)
            versaoJornadaDao.definirDataFim(it.id, dataFimAnterior, agora)

            auditService.logUpdate(
                entidade = ENTIDADE,
                entidadeId = it.id,
                motivo = "Versão v${it.numeroVersao} encerrada em ${dataFimAnterior.format(dateFormatter)}",
                valorAntigo = "dataFim=null",
                valorNovo = "dataFim=${dataFimAnterior.format(dateFormatter)}",
                serializer = { s -> s }
            )
        }

        // 3. Remover flag vigente de todas
        versaoJornadaDao.removerVigenteDeTodas(empregoId)

        // 4. Calcular próximo número de versão
        val proximoNumero = (versaoJornadaDao.buscarMaiorNumeroVersao(empregoId) ?: 0) + 1

        // 5. Criar nova versão
        val novaVersao = VersaoJornada(
            empregoId = empregoId,
            dataInicio = dataInicio,
            dataFim = null,
            descricao = descricao,
            numeroVersao = proximoNumero,
            vigente = true,
            jornadaMaximaDiariaMinutos = versaoAnterior?.toDomain()?.jornadaMaximaDiariaMinutos ?: 600,
            intervaloMinimoInterjornadaMinutos = versaoAnterior?.toDomain()?.intervaloMinimoInterjornadaMinutos ?: 660,
            toleranciaIntervaloMaisMinutos = versaoAnterior?.toDomain()?.toleranciaIntervaloMaisMinutos ?: 0,
            criadoEm = agora,
            atualizadoEm = agora
        )

        val novaVersaoId = versaoJornadaDao.inserir(novaVersao.toEntity())

        auditService.logCreate(
            entidade = ENTIDADE,
            entidadeId = novaVersaoId,
            motivo = "Nova versão v$proximoNumero criada (início: ${dataInicio.format(dateFormatter)})${if (copiarDaVersaoAnterior && versaoAnterior != null) " - Copiada da v${versaoAnterior.numeroVersao}" else ""}",
            novoValor = novaVersao.copy(id = novaVersaoId),
            serializer = { auditService.toJson(it.toAuditMap()) }
        )

        // 6. Copiar ou criar horários
        if (copiarDaVersaoAnterior && versaoAnterior != null) {
            val horariosAnteriores = horarioDiaSemanaDao.buscarPorVersaoJornada(versaoAnterior.id)
            horariosAnteriores.forEach { horarioEntity ->
                val novoHorario = horarioEntity.copy(
                    id = 0,
                    versaoJornadaId = novaVersaoId,
                    criadoEm = agora,
                    atualizadoEm = agora
                )
                horarioDiaSemanaDao.inserir(novoHorario)
            }
        } else {
            DiaSemana.entries.forEach { diaSemana ->
                val horarioPadrao = HorarioDiaSemana.criarPadrao(empregoId, diaSemana, novaVersaoId)
                horarioDiaSemanaDao.inserir(horarioPadrao.toEntity())
            }
        }

        return novaVersaoId
    }

    override suspend fun existeSobreposicao(
        empregoId: Long,
        dataInicio: LocalDate,
        dataFim: LocalDate?,
        excluirId: Long
    ): Boolean {
        return versaoJornadaDao.contarSobreposicoes(empregoId, dataInicio, dataFim, excluirId) > 0
    }

    override suspend fun buscarVersaoAnterior(empregoId: Long, data: LocalDate): VersaoJornada? {
        return versaoJornadaDao.buscarVersaoAnterior(empregoId, data)?.toDomain()
    }

    override suspend fun buscarProximaVersao(empregoId: Long, data: LocalDate): VersaoJornada? {
        return versaoJornadaDao.buscarProximaVersao(empregoId, data)?.toDomain()
    }

    // ========================================================================
    // Contagens
    // ========================================================================

    override suspend fun contarPorEmprego(empregoId: Long): Int {
        return versaoJornadaDao.contarPorEmprego(empregoId)
    }

    // ========================================================================
    // Helpers
    // ========================================================================

    private fun VersaoJornada.toAuditMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "empregoId" to empregoId,
        "numeroVersao" to numeroVersao,
        "dataInicio" to dataInicio.format(dateFormatter),
        "dataFim" to dataFim?.format(dateFormatter),
        "vigente" to vigente,
        "descricao" to descricao,
        "jornadaMaximaDiariaMinutos" to jornadaMaximaDiariaMinutos,
        "intervaloMinimoInterjornadaMinutos" to intervaloMinimoInterjornadaMinutos
    )

    companion object {
        private const val ENTIDADE = "VersaoJornada"
    }
}
