// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/usecase/versaojornada/CriarVersaoJornadaUseCase.kt
package br.com.tlmacedo.meuponto.domain.usecase.versaojornada

import br.com.tlmacedo.meuponto.domain.model.DiaSemana
import br.com.tlmacedo.meuponto.domain.model.HorarioDiaSemana
import br.com.tlmacedo.meuponto.domain.model.VersaoJornada
import br.com.tlmacedo.meuponto.domain.repository.EmpregoRepository
import br.com.tlmacedo.meuponto.domain.repository.HorarioDiaSemanaRepository
import br.com.tlmacedo.meuponto.domain.repository.VersaoJornadaRepository
import timber.log.Timber
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject

/**
 * UseCase para criar uma nova versão de jornada com validações.
 *
 * @author Thiago
 * @since 4.0.0
 * @updated 7.2.0 - Removidos toleranciaEntradaMinutos/toleranciaSaidaMinutos, adicionado turnoMaximoMinutos
 */
class CriarVersaoJornadaUseCase @Inject constructor(
    private val versaoJornadaRepository: VersaoJornadaRepository,
    private val horarioDiaSemanaRepository: HorarioDiaSemanaRepository,
    private val empregoRepository: EmpregoRepository
) {
    suspend operator fun invoke(params: Params): Result<VersaoJornada> {
        return try {
            val emprego = empregoRepository.buscarAtivos().firstOrNull()
                ?: return Result.failure(VersaoJornadaException.EmpregoNaoEncontrado)

            val versoesExistentes = versaoJornadaRepository.buscarPorEmprego(emprego.id)

            val versaoConflitante = verificarSobreposicao(
                dataInicio = params.dataInicio,
                dataFim = params.dataFim,
                versoesExistentes = versoesExistentes,
                versaoIdIgnorar = null
            )
            if (versaoConflitante != null) {
                return Result.failure(VersaoJornadaException.SobreposicaoDatas(versaoConflitante))
            }

            val proximoNumero = (versoesExistentes.maxOfOrNull { it.numeroVersao } ?: 0) + 1

            if (params.vigente) {
                versoesExistentes
                    .filter { it.vigente }
                    .forEach { versao ->
                        versaoJornadaRepository.atualizar(
                            versao.copy(
                                vigente = false,
                                dataFim = params.dataInicio.minusDays(1),
                                atualizadoEm = LocalDateTime.now()
                            )
                        )
                    }
            }

            val novaVersao = VersaoJornada(
                id = 0,
                empregoId = emprego.id,
                dataInicio = params.dataInicio,
                dataFim = params.dataFim,
                descricao = params.descricao?.ifBlank { null },
                numeroVersao = proximoNumero,
                vigente = params.vigente,
                jornadaMaximaDiariaMinutos = params.jornadaMaximaDiariaMinutos,
                intervaloMinimoInterjornadaMinutos = params.intervaloMinimoInterjornadaMinutos,
                intervaloMinimoAlmocoMinutos = params.intervaloMinimoAlmocoMinutos,
                intervaloMinimoDescansoMinutos = params.intervaloMinimoDescansoMinutos,
                toleranciaIntervaloMaisMinutos = params.toleranciaIntervaloMaisMinutos,
                toleranciaRetornoIntervaloMinutos = params.toleranciaRetornoIntervaloMinutos,
                turnoMaximoMinutos = params.turnoMaximoMinutos,

                // Carga horária
                cargaHorariaDiariaMinutos = params.cargaHorariaDiariaMinutos,
                acrescimoMinutosDiasPontes = params.acrescimoMinutosDiasPontes,
                cargaHorariaSemanalMinutos = params.cargaHorariaSemanalMinutos,

                // Período/Saldo
                primeiroDiaSemana = params.primeiroDiaSemana,
                diaInicioFechamentoRH = params.diaInicioFechamentoRH,
                zerarSaldoSemanal = params.zerarSaldoSemanal,
                zerarSaldoPeriodoRH = params.zerarSaldoPeriodoRH,
                ocultarSaldoTotal = params.ocultarSaldoTotal,

                // Banco de Horas
                bancoHorasHabilitado = params.bancoHorasHabilitado,
                periodoBancoDias = params.periodoBancoDias,
                periodoBancoSemanas = params.periodoBancoSemanas,
                periodoBancoMeses = params.periodoBancoMeses,
                periodoBancoAnos = params.periodoBancoAnos,
                dataInicioCicloBancoAtual = params.dataInicioCicloBancoAtual,
                diasUteisLembreteFechamento = params.diasUteisLembreteFechamento,
                habilitarSugestaoAjuste = params.habilitarSugestaoAjuste,
                zerarBancoAntesPeriodo = params.zerarBancoAntesPeriodo,

                // Validação
                exigeJustificativaInconsistencia = params.exigeJustificativaInconsistencia,

                criadoEm = LocalDateTime.now(),
                atualizadoEm = LocalDateTime.now()
            )

            val versaoId = versaoJornadaRepository.inserir(novaVersao)
            val versaoCriada = novaVersao.copy(id = versaoId)

            criarHorariosPadrao(emprego.id, versaoId, params.criarHorariosPadrao)

            Timber.i("Versão de jornada criada: $versaoId (v$proximoNumero)")
            Result.success(versaoCriada)

        } catch (e: Exception) {
            Timber.e(e, "Erro ao criar versão de jornada")
            Result.failure(VersaoJornadaException.ErroInterno(e.message ?: "Erro desconhecido"))
        }
    }

    private suspend fun criarHorariosPadrao(empregoId: Long, versaoId: Long, criarPadrao: Boolean) {
        DiaSemana.entries.forEach { dia ->
            val horario = if (criarPadrao && dia.isDiaUtil) {
                HorarioDiaSemana(
                    id = 0,
                    empregoId = empregoId,
                    versaoJornadaId = versaoId,
                    diaSemana = dia,
                    ativo = true,
                    cargaHorariaMinutos = 492,
                    entradaIdeal = LocalTime.of(8, 0),
                    saidaIntervaloIdeal = LocalTime.of(12, 0),
                    voltaIntervaloIdeal = LocalTime.of(13, 0),
                    saidaIdeal = LocalTime.of(17, 12),
                    intervaloMinimoMinutos = 60
                )
            } else {
                HorarioDiaSemana(
                    id = 0,
                    empregoId = empregoId,
                    versaoJornadaId = versaoId,
                    diaSemana = dia,
                    ativo = false,
                    cargaHorariaMinutos = 0,
                    intervaloMinimoMinutos = 60
                )
            }
            horarioDiaSemanaRepository.inserir(horario)
        }
    }

    data class Params(
        val dataInicio: LocalDate,
        val dataFim: LocalDate? = null,
        val descricao: String? = null,
        val vigente: Boolean = true,
        val jornadaMaximaDiariaMinutos: Int = 600,
        val intervaloMinimoInterjornadaMinutos: Int = 660,
        val intervaloMinimoAlmocoMinutos: Int = 60,
        val intervaloMinimoDescansoMinutos: Int = 15,
        val toleranciaIntervaloMaisMinutos: Int = 0,
        val toleranciaRetornoIntervaloMinutos: Int = 5,
        val turnoMaximoMinutos: Int = 360,

        // Carga horária
        val cargaHorariaDiariaMinutos: Int = 480,
        val acrescimoMinutosDiasPontes: Int = 12,
        val cargaHorariaSemanalMinutos: Int = 2460,

        // Período/Saldo
        val primeiroDiaSemana: br.com.tlmacedo.meuponto.domain.model.DiaSemana = br.com.tlmacedo.meuponto.domain.model.DiaSemana.SEGUNDA,
        val diaInicioFechamentoRH: Int = 1,
        val zerarSaldoSemanal: Boolean = false,
        val zerarSaldoPeriodoRH: Boolean = false,
        val ocultarSaldoTotal: Boolean = false,

        // Banco de Horas
        val bancoHorasHabilitado: Boolean = false,
        val periodoBancoDias: Int = 0,
        val periodoBancoSemanas: Int = 0,
        val periodoBancoMeses: Int = 0,
        val periodoBancoAnos: Int = 0,
        val dataInicioCicloBancoAtual: LocalDate? = null,
        val diasUteisLembreteFechamento: Int = 3,
        val habilitarSugestaoAjuste: Boolean = false,
        val zerarBancoAntesPeriodo: Boolean = false,

        // Validação
        val exigeJustificativaInconsistencia: Boolean = false,

        val criarHorariosPadrao: Boolean = true
    )
}
