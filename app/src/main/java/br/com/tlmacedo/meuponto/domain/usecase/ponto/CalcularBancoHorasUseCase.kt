// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/usecase/ponto/CalcularBancoHorasUseCase.kt
package br.com.tlmacedo.meuponto.domain.usecase.ponto

import br.com.tlmacedo.meuponto.domain.model.AjusteSaldo
import br.com.tlmacedo.meuponto.domain.model.BancoHoras
import br.com.tlmacedo.meuponto.domain.model.DiaSemana
import br.com.tlmacedo.meuponto.domain.model.FechamentoPeriodo
import br.com.tlmacedo.meuponto.domain.model.HorarioDiaSemana
import br.com.tlmacedo.meuponto.domain.model.Ponto
import br.com.tlmacedo.meuponto.domain.model.TipoFechamento
import br.com.tlmacedo.meuponto.domain.model.VersaoJornada
import br.com.tlmacedo.meuponto.domain.model.ausencia.Ausencia
import br.com.tlmacedo.meuponto.domain.model.feriado.Feriado
import br.com.tlmacedo.meuponto.domain.repository.AjusteSaldoRepository
import br.com.tlmacedo.meuponto.domain.repository.AusenciaRepository
import br.com.tlmacedo.meuponto.domain.repository.FechamentoPeriodoRepository
import br.com.tlmacedo.meuponto.domain.repository.FeriadoRepository
import br.com.tlmacedo.meuponto.domain.repository.HorarioDiaSemanaRepository
import br.com.tlmacedo.meuponto.domain.repository.PontoRepository
import br.com.tlmacedo.meuponto.domain.repository.VersaoJornadaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.Duration
import java.time.LocalDate
import javax.inject.Inject

/**
 * Caso de uso para calcular o banco de horas acumulado.
 *
 * @author Thiago
 * @since 1.0.0
 * @updated 8.0.0 - Migrado para usar VersaoJornada
 */
class CalcularBancoHorasUseCase @Inject constructor(
    private val pontoRepository: PontoRepository,
    private val fechamentoPeriodoRepository: FechamentoPeriodoRepository,
    private val ajusteSaldoRepository: AjusteSaldoRepository,
    private val ausenciaRepository: AusenciaRepository,
    private val feriadoRepository: FeriadoRepository,
    private val horarioDiaSemanaRepository: HorarioDiaSemanaRepository,
    private val versaoJornadaRepository: VersaoJornadaRepository,
    private val obterResumoDiaCompletoUseCase: ObterResumoDiaCompletoUseCase
) {

    data class ResultadoBancoHoras(
        val saldoTotal: Duration,
        val dataInicio: LocalDate,
        val dataFim: LocalDate,
        val diasTrabalhados: Int,
        val diasComAusencia: Int = 0,
        val diasUteisSemRegistro: Int = 0,
        val totalAjustesMinutos: Int,
        val totalAbonosMinutos: Int = 0,
        val ultimoFechamento: FechamentoPeriodo?
    ) {
        val bancoHoras: BancoHoras get() = BancoHoras(saldoTotal = saldoTotal)
        val saldoFormatado: String get() = bancoHoras.formatarSaldo()
        val positivo: Boolean get() = !saldoTotal.isNegative && !saldoTotal.isZero
        val negativo: Boolean get() = saldoTotal.isNegative
    }

    private data class VersaoCache(
        val versao: VersaoJornada,
        val horariosPorDia: Map<DiaSemana, HorarioDiaSemana>
    )

    operator fun invoke(empregoId: Long, ateData: LocalDate = LocalDate.now()): Flow<ResultadoBancoHoras> {
        return combine(
            pontoRepository.observarPorEmprego(empregoId),
            fechamentoPeriodoRepository.observarPorEmpregoId(empregoId),
            ajusteSaldoRepository.observarPorEmprego(empregoId),
            ausenciaRepository.observarAtivasPorEmprego(empregoId),
            feriadoRepository.observarTodosAtivos()
        ) { pontos, fechamentos, ajustes, ausencias, feriados ->
            val fechamentoRelevante = fechamentos
                .filter { it.tipo in listOf(TipoFechamento.BANCO_HORAS, TipoFechamento.CICLO_BANCO_AUTOMATICO) }
                .filter { it.dataFimPeriodo < ateData }
                .maxByOrNull { it.dataFimPeriodo }

            calcularBancoHoras(empregoId, pontos, fechamentoRelevante, ajustes, ausencias, feriados, ateData)
        }
    }

    suspend fun calcular(empregoId: Long, ateData: LocalDate = LocalDate.now()): ResultadoBancoHoras {
        val ultimoFechamento = fechamentoPeriodoRepository.buscarUltimoFechamentoBancoAteData(empregoId, ateData)
        val dataInicio = ultimoFechamento?.dataFimPeriodo?.plusDays(1)
            ?: pontoRepository.buscarPrimeiraData(empregoId) ?: ateData

        val pontos = pontoRepository.buscarPorEmpregoEPeriodo(empregoId, dataInicio, ateData)
        val ajustes = ajusteSaldoRepository.buscarPorPeriodo(empregoId, dataInicio, ateData)
        val ausencias = ausenciaRepository.buscarPorPeriodo(empregoId, dataInicio, ateData)
        val feriados = feriadoRepository.buscarPorPeriodo(dataInicio, ateData)

        return calcularBancoHoras(empregoId, pontos, ultimoFechamento, ajustes, ausencias, feriados, ateData)
    }

    suspend fun calcularAteData(empregoId: Long, ateData: LocalDate): ResultadoBancoHoras = calcular(empregoId, ateData)

    suspend fun calcularParaPeriodo(empregoId: Long, dataInicio: LocalDate, dataFim: LocalDate): ResultadoBancoHoras {
        val pontos = pontoRepository.buscarPorEmpregoEPeriodo(empregoId, dataInicio, dataFim)
        val ajustes = ajusteSaldoRepository.buscarPorPeriodo(empregoId, dataInicio, dataFim)
        val ausencias = ausenciaRepository.buscarPorPeriodo(empregoId, dataInicio, dataFim)
        val feriados = feriadoRepository.buscarPorPeriodo(dataInicio, dataFim)

        return calcularBancoHorasParaPeriodo(empregoId, pontos, ajustes, ausencias, feriados, dataInicio, dataFim)
    }

    private suspend fun calcularBancoHoras(
        empregoId: Long,
        pontos: List<Ponto>,
        ultimoFechamento: FechamentoPeriodo?,
        ajustes: List<AjusteSaldo>,
        ausencias: List<Ausencia>,
        feriados: List<Feriado>,
        ateData: LocalDate
    ): ResultadoBancoHoras {
        val dataInicio = ultimoFechamento?.dataFimPeriodo?.plusDays(1) ?: pontos.minOfOrNull { it.data } ?: ateData

        if (dataInicio > ateData) {
            return ResultadoBancoHoras(Duration.ZERO, ateData, ateData, 0, 0, 0, 0, 0, ultimoFechamento)
        }

        return calcularBancoHorasInterno(empregoId, pontos, ajustes, ausencias, feriados, dataInicio, ateData)
            .copy(ultimoFechamento = ultimoFechamento)
    }

    private suspend fun calcularBancoHorasParaPeriodo(
        empregoId: Long,
        pontos: List<Ponto>,
        ajustes: List<AjusteSaldo>,
        ausencias: List<Ausencia>,
        feriados: List<Feriado>,
        dataInicio: LocalDate,
        dataFim: LocalDate
    ): ResultadoBancoHoras {
        return calcularBancoHorasInterno(empregoId, pontos, ajustes, ausencias, feriados, dataInicio, dataFim)
            .copy(ultimoFechamento = null)
    }

    private suspend fun calcularBancoHorasInterno(
        empregoId: Long,
        pontos: List<Ponto>,
        ajustes: List<AjusteSaldo>,
        ausencias: List<Ausencia>,
        feriados: List<Feriado>,
        dataInicio: LocalDate,
        dataFim: LocalDate
    ): ResultadoBancoHoras {
        val pontosNoPeriodo = pontos.filter { it.data in dataInicio..dataFim }
        val ajustesNoPeriodo = ajustes.filter { it.data in dataInicio..dataFim }
        val ausenciasNoPeriodo = ausencias.filter { it.ativo && it.dataInicio <= dataFim && it.dataFim >= dataInicio }

        val pontosPorDia = pontosNoPeriodo.groupBy { it.data }

        val feriadosPorData = mutableMapOf<LocalDate, Feriado>()
        val anosNoPeriodo = (dataInicio.year..dataFim.year).toList()
        for (feriado in feriados.filter { it.ativo }) {
            for (ano in anosNoPeriodo) {
                feriado.getDataParaAno(ano)?.let { data ->
                    if (data in dataInicio..dataFim) feriadosPorData[data] = feriado
                }
            }
        }

        val ausenciasPorData = mutableMapOf<LocalDate, MutableList<Ausencia>>()
        for (ausencia in ausenciasNoPeriodo) {
            var data = maxOf(ausencia.dataInicio, dataInicio)
            while (data <= minOf(ausencia.dataFim, dataFim)) {
                ausenciasPorData.getOrPut(data) { mutableListOf() }.add(ausencia)
                data = data.plusDays(1)
            }
        }

        val versaoCache = mutableMapOf<Long, VersaoCache>()
        val horarioSemVersaoCache = mutableMapOf<DiaSemana, HorarioDiaSemana?>()

        // Buscar carga padrão da versão vigente
        val versaoVigente = versaoJornadaRepository.buscarVigente(empregoId)
        val cargaPadrao = versaoVigente?.cargaHorariaDiariaMinutos ?: 480

        var saldoTotal = Duration.ZERO
        var diasTrabalhados = 0
        var diasComAusencia = 0
        var diasUteisSemRegistro = 0
        var totalAbonosMinutos = 0

        var dataAtual = dataInicio
        while (dataAtual <= dataFim) {
            val pontosNoDia = pontosPorDia[dataAtual] ?: emptyList()
            val ausenciasDoDia = ausenciasPorData[dataAtual] ?: emptyList()
            val feriadoDoDia = feriadosPorData[dataAtual]

            val diaSemana = DiaSemana.fromJavaDayOfWeek(dataAtual.dayOfWeek)
            val versaoJornada = versaoJornadaRepository.buscarPorEmpregoEData(empregoId, dataAtual)

            val horarioDia = if (versaoJornada != null) {
                val cached = versaoCache[versaoJornada.id] ?: run {
                    val horarios = horarioDiaSemanaRepository.buscarPorVersaoJornada(versaoJornada.id)
                        .associateBy { it.diaSemana }
                    VersaoCache(versaoJornada, horarios).also { versaoCache[versaoJornada.id] = it }
                }
                cached.horariosPorDia[diaSemana]
            } else {
                horarioSemVersaoCache.getOrPut(diaSemana) {
                    horarioDiaSemanaRepository.buscarPorEmpregoEDia(empregoId, diaSemana)
                }
            }

            val resumoCompleto = obterResumoDiaCompletoUseCase.invokeComDados(
                data = dataAtual,
                pontos = pontosNoDia,
                ausencias = ausenciasDoDia,
                feriado = feriadoDoDia,
                horarioDia = horarioDia,
                cargaHorariaPadrao = cargaPadrao
            )

            if (resumoCompleto.jornadaCompleta) diasTrabalhados++
            if (ausenciasDoDia.isNotEmpty()) diasComAusencia++

            val jornadaEsperada = resumoCompleto.cargaHorariaEfetivaMinutos
            if (jornadaEsperada > 0 && pontosNoDia.isEmpty() && ausenciasDoDia.isEmpty() && feriadoDoDia == null) {
                diasUteisSemRegistro++
            }

            totalAbonosMinutos += resumoCompleto.tempoAbonadoMinutos
            saldoTotal = saldoTotal.plus(resumoCompleto.saldoDia)
            dataAtual = dataAtual.plusDays(1)
        }

        val totalAjustesMinutos = ajustesNoPeriodo.sumOf { it.minutos }
        saldoTotal = saldoTotal.plusMinutes(totalAjustesMinutos.toLong())

        return ResultadoBancoHoras(
            saldoTotal, dataInicio, dataFim, diasTrabalhados, diasComAusencia,
            diasUteisSemRegistro, totalAjustesMinutos, totalAbonosMinutos, null
        )
    }
}
