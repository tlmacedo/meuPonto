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
import br.com.tlmacedo.meuponto.util.helper.dateFormatterSimples
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementação do repositório de versões de jornada.
 *
 * @author Thiago
 * @since 2.7.0
 * @updated 4.0.0 - Adicionados métodos de consulta para UseCases
 * @updated 11.0.0 - Integração com AuditService
 * @updated 12.0.0 - Refatorado para usar AuditedRepositoryBase
 */
@Singleton
class VersaoJornadaRepositoryImpl @Inject constructor(
    private val versaoJornadaDao: VersaoJornadaDao,
    private val horarioDiaSemanaDao: HorarioDiaSemanaDao,
    auditService: AuditService
) : AuditedRepositoryBase<VersaoJornada>(auditService, ENTIDADE), VersaoJornadaRepository {

    // ========================================================================
    // PONTE COM O DAO
    // ========================================================================

    override suspend fun daoInserir(domain: VersaoJornada): Long = versaoJornadaDao.inserir(domain.toEntity())
    override suspend fun daoBuscarPorId(id: Long): VersaoJornada? = versaoJornadaDao.buscarPorId(id)?.toDomain()
    override suspend fun daoAtualizar(domain: VersaoJornada) = versaoJornadaDao.atualizar(domain.toEntity())
    override suspend fun daoExcluir(domain: VersaoJornada) = versaoJornadaDao.excluir(domain.toEntity())
    override fun getEntityId(domain: VersaoJornada): Long = domain.id

    override fun VersaoJornada.toAuditMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "empregoId" to empregoId,
        "numeroVersao" to numeroVersao,
        "dataInicio" to dataInicio.format(dateFormatterSimples),
        "dataFim" to dataFim?.format(dateFormatterSimples),
        "vigente" to vigente,
        "descricao" to descricao,
        "jornadaMaximaDiariaMinutos" to jornadaMaximaDiariaMinutos,
        "intervaloMinimoInterjornadaMinutos" to intervaloMinimoInterjornadaMinutos,
        "toleranciaIntervaloMaisMinutos" to toleranciaIntervaloMaisMinutos,
        "turnoMaximoMinutos" to turnoMaximoMinutos,
        "cargaHorariaDiariaMinutos" to cargaHorariaDiariaMinutos,
        "acrescimoMinutosDiasPontes" to acrescimoMinutosDiasPontes,
        "cargaHorariaSemanalMinutos" to cargaHorariaSemanalMinutos,
        "primeiroDiaSemana" to primeiroDiaSemana.name,
        "diaInicioFechamentoRH" to diaInicioFechamentoRH,
        "zerarSaldoSemanal" to zerarSaldoSemanal,
        "zerarSaldoPeriodoRH" to zerarSaldoPeriodoRH,
        "ocultarSaldoTotal" to ocultarSaldoTotal,
        "bancoHorasHabilitado" to bancoHorasHabilitado,
        "periodoBancoSemanas" to periodoBancoSemanas,
        "periodoBancoMeses" to periodoBancoMeses,
        "dataInicioCicloBancoAtual" to dataInicioCicloBancoAtual?.format(dateFormatterSimples),
        "diasUteisLembreteFechamento" to diasUteisLembreteFechamento,
        "habilitarSugestaoAjuste" to habilitarSugestaoAjuste,
        "zerarBancoAntesPeriodo" to zerarBancoAntesPeriodo,
        "exigeJustificativaInconsistencia" to exigeJustificativaInconsistencia
    )

    // ========================================================================
    // MOTIVOS DE AUDITORIA
    // ========================================================================

    override fun motivoInserir(domain: VersaoJornada): String =
        "Versão de jornada v${domain.numeroVersao} criada (início: ${domain.dataInicio.format(dateFormatterSimples)})"

    override fun motivoAtualizar(domain: VersaoJornada): String =
        "Versão de jornada v${domain.numeroVersao} atualizada"

    override fun motivoExcluir(domain: VersaoJornada): String =
        "Versão de jornada v${domain.numeroVersao} excluída"

    // ========================================================================
    // CRUD
    // ========================================================================

    override suspend fun inserir(versao: VersaoJornada): Long = inserirComAuditoria(versao)

    override suspend fun atualizar(versao: VersaoJornada) = atualizarComAuditoria(versao)

    override suspend fun excluir(versao: VersaoJornada) = excluirComAuditoria(versao)

    override suspend fun excluirPorId(id: Long) =
        excluirPorIdComAuditoria(id) { versaoJornadaDao.excluirPorId(it) }

    // ========================================================================
    // CONSULTAS POR ID
    // ========================================================================

    override suspend fun buscarPorId(id: Long): VersaoJornada? = daoBuscarPorId(id)

    override fun observarPorId(id: Long): Flow<VersaoJornada?> =
        versaoJornadaDao.observarPorId(id).map { it?.toDomain() }

    // ========================================================================
    // CONSULTAS POR EMPREGO
    // ========================================================================

    override suspend fun buscarPorEmprego(empregoId: Long): List<VersaoJornada> =
        versaoJornadaDao.buscarPorEmprego(empregoId).map { it.toDomain() }

    override suspend fun listarPorEmprego(empregoId: Long): List<VersaoJornada> =
        buscarPorEmprego(empregoId)

    override fun observarPorEmprego(empregoId: Long): Flow<List<VersaoJornada>> =
        versaoJornadaDao.observarPorEmprego(empregoId).map { list -> list.map { it.toDomain() } }

    override suspend fun buscarVigente(empregoId: Long): VersaoJornada? =
        versaoJornadaDao.buscarVigente(empregoId)?.toDomain()

    override fun observarVigente(empregoId: Long): Flow<VersaoJornada?> =
        versaoJornadaDao.observarVigente(empregoId).map { it?.toDomain() }

    override suspend fun existeParaEmprego(empregoId: Long): Boolean =
        versaoJornadaDao.contarPorEmprego(empregoId) > 0

    // ========================================================================
    // CONSULTAS POR DATA
    // ========================================================================

    override suspend fun buscarPorData(empregoId: Long, data: LocalDate): VersaoJornada? =
        versaoJornadaDao.buscarPorEmpregoEData(empregoId, data)?.toDomain()

    override suspend fun buscarPorEmpregoEData(empregoId: Long, data: LocalDate): VersaoJornada? =
        buscarPorData(empregoId, data)

    override fun observarPorEmpregoEData(empregoId: Long, data: LocalDate): Flow<VersaoJornada?> =
        versaoJornadaDao.observarPorEmpregoEData(empregoId, data).map { it?.toDomain() }

    // ========================================================================
    // VERSIONAMENTO — lógica complexa, não usa base class
    // ========================================================================

    override suspend fun criarNovaVersao(
        empregoId: Long,
        dataInicio: LocalDate,
        descricao: String?,
        copiarDaVersaoAnterior: Boolean
    ): Long {
        val agora = LocalDateTime.now()

        val versaoAnterior = versaoJornadaDao.buscarVersaoAnterior(empregoId, dataInicio)
        val proximaVersao = versaoJornadaDao.buscarProximaVersao(empregoId, dataInicio)

        versaoAnterior?.let {
            val dataFimAnterior = dataInicio.minusDays(1)
            if (it.dataFim == null || it.dataFim != dataFimAnterior) {
                versaoJornadaDao.definirDataFim(it.id, dataFimAnterior, agora)

                auditService.logUpdate(
                    entidade = ENTIDADE,
                    entidadeId = it.id,
                    motivo = "Versão v${it.numeroVersao} ajustada para encerrar em ${dataFimAnterior.format(dateFormatterSimples)}",
                    valorAntigo = "dataFim=${it.dataFim?.format(dateFormatterSimples) ?: "null"}",
                    valorNovo = "dataFim=${dataFimAnterior.format(dateFormatterSimples)}",
                    serializer = { s -> s }
                )
            }
        }

        val dataFimNovaVersao = proximaVersao?.dataInicio?.minusDays(1)
        val isVigente = dataFimNovaVersao == null

        if (isVigente) {
            versaoJornadaDao.removerVigenteDeTodas(empregoId)
        }

        val proximoNumero = (versaoJornadaDao.buscarMaiorNumeroVersao(empregoId) ?: 0) + 1
        val anterior = versaoAnterior?.toDomain()

        val novaVersao = VersaoJornada(
            empregoId = empregoId,
            dataInicio = dataInicio,
            dataFim = dataFimNovaVersao,
            descricao = descricao,
            numeroVersao = proximoNumero,
            vigente = isVigente,

            jornadaMaximaDiariaMinutos = anterior?.jornadaMaximaDiariaMinutos ?: 600,
            intervaloMinimoInterjornadaMinutos = anterior?.intervaloMinimoInterjornadaMinutos ?: 660,
            toleranciaIntervaloMaisMinutos = anterior?.toleranciaIntervaloMaisMinutos ?: 0,
            turnoMaximoMinutos = anterior?.turnoMaximoMinutos ?: 360,

            cargaHorariaDiariaMinutos = anterior?.cargaHorariaDiariaMinutos ?: 480,
            acrescimoMinutosDiasPontes = anterior?.acrescimoMinutosDiasPontes ?: 12,
            cargaHorariaSemanalMinutos = anterior?.cargaHorariaSemanalMinutos ?: 2460,

            primeiroDiaSemana = anterior?.primeiroDiaSemana ?: DiaSemana.SEGUNDA,
            diaInicioFechamentoRH = anterior?.diaInicioFechamentoRH ?: 1,
            zerarSaldoSemanal = anterior?.zerarSaldoSemanal ?: false,
            zerarSaldoPeriodoRH = anterior?.zerarSaldoPeriodoRH ?: false,
            ocultarSaldoTotal = anterior?.ocultarSaldoTotal ?: false,

            bancoHorasHabilitado = anterior?.bancoHorasHabilitado ?: false,
            periodoBancoSemanas = anterior?.periodoBancoSemanas ?: 0,
            periodoBancoMeses = anterior?.periodoBancoMeses ?: 0,
            dataInicioCicloBancoAtual = anterior?.dataInicioCicloBancoAtual,
            diasUteisLembreteFechamento = anterior?.diasUteisLembreteFechamento ?: 3,
            habilitarSugestaoAjuste = anterior?.habilitarSugestaoAjuste ?: false,
            zerarBancoAntesPeriodo = anterior?.zerarBancoAntesPeriodo ?: false,

            exigeJustificativaInconsistencia = anterior?.exigeJustificativaInconsistencia ?: false,

            criadoEm = agora,
            atualizadoEm = agora
        )

        val novaVersaoId = versaoJornadaDao.inserir(novaVersao.toEntity())

        val copiadaDe = if (copiarDaVersaoAnterior && versaoAnterior != null)
            " - Copiada da v${versaoAnterior.numeroVersao}" else ""

        auditService.logCreate(
            entidade = ENTIDADE,
            entidadeId = novaVersaoId,
            motivo = "Nova versão v$proximoNumero criada (início: ${dataInicio.format(dateFormatterSimples)})$copiadaDe",
            novoValor = novaVersao.copy(id = novaVersaoId),
            serializer = { auditService.toJson(it.toAuditMap()) }
        )

        if (copiarDaVersaoAnterior && versaoAnterior != null) {
            horarioDiaSemanaDao.buscarPorVersaoJornada(versaoAnterior.id).forEach { horarioEntity ->
                horarioDiaSemanaDao.inserir(
                    horarioEntity.copy(id = 0, versaoJornadaId = novaVersaoId, criadoEm = agora, atualizadoEm = agora)
                )
            }
        } else {
            DiaSemana.entries.forEach { diaSemana ->
                horarioDiaSemanaDao.inserir(HorarioDiaSemana.criarPadrao(empregoId, diaSemana, novaVersaoId).toEntity())
            }
        }

        return novaVersaoId
    }

    override suspend fun existeSobreposicao(
        empregoId: Long,
        dataInicio: LocalDate,
        dataFim: LocalDate?,
        excluirId: Long
    ): Boolean = versaoJornadaDao.contarSobreposicoes(empregoId, dataInicio, dataFim, excluirId) > 0

    override suspend fun buscarVersaoAnterior(empregoId: Long, data: LocalDate): VersaoJornada? =
        versaoJornadaDao.buscarVersaoAnterior(empregoId, data)?.toDomain()

    override suspend fun buscarProximaVersao(empregoId: Long, data: LocalDate): VersaoJornada? =
        versaoJornadaDao.buscarProximaVersao(empregoId, data)?.toDomain()

    override suspend fun contarPorEmprego(empregoId: Long): Int =
        versaoJornadaDao.contarPorEmprego(empregoId)

    companion object {
        private const val ENTIDADE = "VersaoJornada"
    }
}
