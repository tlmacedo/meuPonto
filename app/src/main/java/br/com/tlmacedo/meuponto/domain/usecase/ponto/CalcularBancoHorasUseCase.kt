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
import br.com.tlmacedo.meuponto.domain.repository.ConfiguracaoEmpregoRepository
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
 * IMPORTANTE: Itera por TODOS os dias do período (não apenas dias com pontos),
 * pois dias úteis sem registro geram saldo negativo.
 *
 * FÓRMULA DO BANCO:
 * bancoHoras = Σ(saldoDia) + Σ(ajustesManuais)
 *
 * Onde saldoDia é calculado pelo ObterResumoDiaCompletoUseCase:
 * saldoDia = trabalhado - jornadaEfetiva
 * jornadaEfetiva = se (zeraJornada) 0 else jornada - abono
 *
 * @author Thiago
 * @since 1.0.0
 * @updated 6.1.0 - Corrigido para iterar todos os dias do período
 */
class CalcularBancoHorasUseCase @Inject constructor(
    private val pontoRepository: PontoRepository,
    private val fechamentoPeriodoRepository: FechamentoPeriodoRepository,
    private val ajusteSaldoRepository: AjusteSaldoRepository,
    private val ausenciaRepository: AusenciaRepository,
    private val feriadoRepository: FeriadoRepository,
    private val configuracaoEmpregoRepository: ConfiguracaoEmpregoRepository,
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
        val bancoHoras: BancoHoras
            get() = BancoHoras(saldoTotal = saldoTotal)

        val saldoFormatado: String
            get() = bancoHoras.formatarSaldo()

        val positivo: Boolean
            get() = !saldoTotal.isNegative && !saldoTotal.isZero

        val negativo: Boolean
            get() = saldoTotal.isNegative
    }

    private data class VersaoCache(
        val versao: VersaoJornada,
        val horariosPorDia: Map<DiaSemana, HorarioDiaSemana>
    )

    /**
     * Calcula o banco de horas até uma data específica de forma reativa.
     */
    operator fun invoke(
        empregoId: Long,
        ateData: LocalDate = LocalDate.now()
    ): Flow<ResultadoBancoHoras> {
        return combine(
            pontoRepository.observarPorEmprego(empregoId),
            fechamentoPeriodoRepository.observarPorEmpregoId(empregoId),
            ajusteSaldoRepository.observarPorEmprego(empregoId),
            ausenciaRepository.observarAtivasPorEmprego(empregoId),
            feriadoRepository.observarTodosAtivos()
        ) { pontos, fechamentos, ajustes, ausencias, feriados ->
            val ultimoFechamento = fechamentos
                .filter { it.tipo in listOf(TipoFechamento.BANCO_HORAS, TipoFechamento.CICLO_BANCO_AUTOMATICO) }
                .maxByOrNull { it.dataFechamento }

            calcularBancoHoras(
                empregoId = empregoId,
                pontos = pontos,
                ultimoFechamento = ultimoFechamento,
                ajustes = ajustes,
                ausencias = ausencias,
                feriados = feriados,
                ateData = ateData
            )
        }
    }

    /**
     * Calcula o banco de horas de forma síncrona (suspend).
     */
    suspend fun calcular(
        empregoId: Long,
        ateData: LocalDate = LocalDate.now()
    ): ResultadoBancoHoras {
        val ultimoFechamento = fechamentoPeriodoRepository.buscarUltimoFechamentoBanco(empregoId)

        val dataInicio = ultimoFechamento?.dataFimPeriodo?.plusDays(1)
            ?: pontoRepository.buscarPrimeiraData(empregoId)
            ?: ateData

        val pontos = pontoRepository.buscarPorEmpregoEPeriodo(empregoId, dataInicio, ateData)
        val ajustes = ajusteSaldoRepository.buscarPorPeriodo(empregoId, dataInicio, ateData)
        val ausencias = ausenciaRepository.buscarPorPeriodo(empregoId, dataInicio, ateData)
        val feriados = feriadoRepository.buscarPorPeriodo(dataInicio, ateData)

        return calcularBancoHoras(
            empregoId = empregoId,
            pontos = pontos,
            ultimoFechamento = ultimoFechamento,
            ajustes = ajustes,
            ausencias = ausencias,
            feriados = feriados,
            ateData = ateData
        )
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
        // Determinar data de início do cálculo
        val dataInicio = ultimoFechamento?.dataFimPeriodo?.plusDays(1)
            ?: pontos.minOfOrNull { it.data }
            ?: ateData

        // Se não há pontos e não há fechamento, retorna zerado
        if (dataInicio > ateData) {
            return ResultadoBancoHoras(
                saldoTotal = Duration.ZERO,
                dataInicio = ateData,
                dataFim = ateData,
                diasTrabalhados = 0,
                diasComAusencia = 0,
                diasUteisSemRegistro = 0,
                totalAjustesMinutos = 0,
                totalAbonosMinutos = 0,
                ultimoFechamento = ultimoFechamento
            )
        }

        val pontosNoPeriodo = pontos.filter { it.data in dataInicio..ateData }
        val ajustesNoPeriodo = ajustes.filter { it.data in dataInicio..ateData }
        val ausenciasNoPeriodo = ausencias.filter {
            it.ativo && it.dataInicio <= ateData && it.dataFim >= dataInicio
        }

        // Pré-processar dados para evitar N+1 queries
        val pontosPorDia = pontosNoPeriodo.groupBy { it.data }

        // Mapear feriados por data
        val feriadosPorData = mutableMapOf<LocalDate, Feriado>()
        val anosNoPeriodo = (dataInicio.year..ateData.year).toList()
        for (feriado in feriados.filter { it.ativo }) {
            for (ano in anosNoPeriodo) {
                feriado.getDataParaAno(ano)?.let { data ->
                    if (data in dataInicio..ateData) {
                        feriadosPorData[data] = feriado
                    }
                }
            }
        }

        // Mapear ausências por data
        val ausenciasPorData = mutableMapOf<LocalDate, MutableList<Ausencia>>()
        for (ausencia in ausenciasNoPeriodo) {
            var data = maxOf(ausencia.dataInicio, dataInicio)
            while (data <= minOf(ausencia.dataFim, ateData)) {
                ausenciasPorData.getOrPut(data) { mutableListOf() }.add(ausencia)
                data = data.plusDays(1)
            }
        }

        // Cache de versões de jornada e horários
        val versaoCache = mutableMapOf<Long, VersaoCache>()
        val horarioSemVersaoCache = mutableMapOf<DiaSemana, HorarioDiaSemana?>()
        val configGlobal = configuracaoEmpregoRepository.buscarPorEmpregoId(empregoId)
        val cargaPadrao = configGlobal?.cargaHorariaDiariaMinutos ?: 480

        var saldoTotal = Duration.ZERO
        var diasTrabalhados = 0
        var diasComAusencia = 0
        var diasUteisSemRegistro = 0
        var totalAbonosMinutos = 0

        // ═══════════════════════════════════════════════════════════════════
        // IMPORTANTE: Iterar por TODOS os dias do período, não apenas
        // os dias com pontos/ausências. Dias úteis sem registro geram
        // saldo negativo!
        // ═══════════════════════════════════════════════════════════════════
        var dataAtual = dataInicio
        while (dataAtual <= ateData) {
            val pontosNoDia = pontosPorDia[dataAtual] ?: emptyList()
            val ausenciasDoDia = ausenciasPorData[dataAtual] ?: emptyList()
            val feriadoDoDia = feriadosPorData[dataAtual]

            // Buscar configuração de jornada para o dia
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
                // Cache para horários sem versão específica
                horarioSemVersaoCache.getOrPut(diaSemana) {
                    horarioDiaSemanaRepository.buscarPorEmpregoEDia(empregoId, diaSemana)
                }
            }

            // Usar a FONTE ÚNICA para calcular o resumo do dia
            val resumoCompleto = obterResumoDiaCompletoUseCase.invokeComDados(
                data = dataAtual,
                pontos = pontosNoDia,
                ausencias = ausenciasDoDia,
                feriado = feriadoDoDia,
                horarioDia = horarioDia,
                cargaHorariaPadrao = cargaPadrao
            )

            // Contabilizar estatísticas
            if (resumoCompleto.jornadaCompleta) {
                diasTrabalhados++
            }
            if (ausenciasDoDia.isNotEmpty()) {
                diasComAusencia++
            }

            // Dia útil sem registro = tinha jornada esperada mas não bateu ponto
            val jornadaEsperada = resumoCompleto.cargaHorariaEfetivaMinutos
            if (jornadaEsperada > 0 && pontosNoDia.isEmpty() && ausenciasDoDia.isEmpty() && feriadoDoDia == null) {
                diasUteisSemRegistro++
            }

            totalAbonosMinutos += resumoCompleto.tempoAbonadoMinutos

            // Somar saldo do dia ao total
            saldoTotal = saldoTotal.plus(resumoCompleto.saldoDia)

            // Próximo dia
            dataAtual = dataAtual.plusDays(1)
        }

        // Adicionar ajustes manuais
        val totalAjustesMinutos = ajustesNoPeriodo.sumOf { it.minutos }
        saldoTotal = saldoTotal.plusMinutes(totalAjustesMinutos.toLong())

        return ResultadoBancoHoras(
            saldoTotal = saldoTotal,
            dataInicio = dataInicio,
            dataFim = ateData,
            diasTrabalhados = diasTrabalhados,
            diasComAusencia = diasComAusencia,
            diasUteisSemRegistro = diasUteisSemRegistro,
            totalAjustesMinutos = totalAjustesMinutos,
            totalAbonosMinutos = totalAbonosMinutos,
            ultimoFechamento = ultimoFechamento
        )
    }
}
