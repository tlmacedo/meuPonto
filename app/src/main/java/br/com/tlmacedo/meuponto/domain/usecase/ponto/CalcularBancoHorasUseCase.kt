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
 * @updated 6.4.0 - Corrigido cálculo histórico para navegação entre datas
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
     *
     * IMPORTANTE: Para navegação entre datas, considera o fechamento mais recente
     * que TERMINOU ANTES da data solicitada, não fechamentos futuros.
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
            // ═══════════════════════════════════════════════════════════════════
            // IMPORTANTE: Buscar o fechamento relevante para a data solicitada
            // Um fechamento é relevante se dataFimPeriodo < ateData
            // Isso garante que ao navegar para 05/02, não consideramos
            // o fechamento de 10/02 que ainda não existia naquela data
            // ═══════════════════════════════════════════════════════════════════
            val fechamentoRelevante = fechamentos
                .filter { it.tipo in listOf(TipoFechamento.BANCO_HORAS, TipoFechamento.CICLO_BANCO_AUTOMATICO) }
                .filter { it.dataFimPeriodo < ateData } // Só fechamentos ANTERIORES à data
                .maxByOrNull { it.dataFimPeriodo }

            calcularBancoHoras(
                empregoId = empregoId,
                pontos = pontos,
                ultimoFechamento = fechamentoRelevante,
                ajustes = ajustes,
                ausencias = ausencias,
                feriados = feriados,
                ateData = ateData
            )
        }
    }

    /**
     * Calcula o banco de horas de forma síncrona (suspend).
     * Considera fechamentos anteriores para determinar a data de início.
     */
    suspend fun calcular(
        empregoId: Long,
        ateData: LocalDate = LocalDate.now()
    ): ResultadoBancoHoras {
        // Buscar fechamento relevante (que terminou ANTES da data solicitada)
        val ultimoFechamento = fechamentoPeriodoRepository.buscarUltimoFechamentoBancoAteData(
            empregoId = empregoId,
            ateData = ateData
        )

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

    /**
     * Calcula o banco de horas até uma data específica (suspend).
     * Usado para exibição na tela.
     */
    suspend fun calcularAteData(
        empregoId: Long,
        ateData: LocalDate
    ): ResultadoBancoHoras {
        return calcular(empregoId, ateData)
    }

    /**
     * Calcula o saldo de um período específico, IGNORANDO fechamentos.
     *
     * Usado para fechamento de ciclos, onde precisamos calcular o saldo
     * exatamente do período do ciclo (dataInicio ~ dataFim), sem considerar
     * fechamentos anteriores que poderiam alterar a data de início do cálculo.
     *
     * @param empregoId ID do emprego
     * @param dataInicio Data de início do período (inclusive)
     * @param dataFim Data fim do período (inclusive)
     * @return Resultado com o saldo do período
     */
    suspend fun calcularParaPeriodo(
        empregoId: Long,
        dataInicio: LocalDate,
        dataFim: LocalDate
    ): ResultadoBancoHoras {

        val pontos = pontoRepository.buscarPorEmpregoEPeriodo(empregoId, dataInicio, dataFim)
        val ajustes = ajusteSaldoRepository.buscarPorPeriodo(empregoId, dataInicio, dataFim)
        val ausencias = ausenciaRepository.buscarPorPeriodo(empregoId, dataInicio, dataFim)
        val feriados = feriadoRepository.buscarPorPeriodo(dataInicio, dataFim)


        return calcularBancoHorasParaPeriodo(
            empregoId = empregoId,
            pontos = pontos,
            ajustes = ajustes,
            ausencias = ausencias,
            feriados = feriados,
            dataInicio = dataInicio,
            dataFim = dataFim
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

        val resultado = calcularBancoHorasInterno(
            empregoId = empregoId,
            pontos = pontos,
            ajustes = ajustes,
            ausencias = ausencias,
            feriados = feriados,
            dataInicio = dataInicio,
            dataFim = ateData
        )

        return resultado.copy(ultimoFechamento = ultimoFechamento)
    }

    /**
     * Calcula o banco de horas para um período específico (sem considerar fechamentos).
     */
    private suspend fun calcularBancoHorasParaPeriodo(
        empregoId: Long,
        pontos: List<Ponto>,
        ajustes: List<AjusteSaldo>,
        ausencias: List<Ausencia>,
        feriados: List<Feriado>,
        dataInicio: LocalDate,
        dataFim: LocalDate
    ): ResultadoBancoHoras {
        return calcularBancoHorasInterno(
            empregoId = empregoId,
            pontos = pontos,
            ajustes = ajustes,
            ausencias = ausencias,
            feriados = feriados,
            dataInicio = dataInicio,
            dataFim = dataFim
        ).copy(ultimoFechamento = null)
    }

    /**
     * Lógica interna de cálculo do banco de horas.
     * Compartilhada entre os métodos públicos.
     */
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
        val ausenciasNoPeriodo = ausencias.filter {
            it.ativo && it.dataInicio <= dataFim && it.dataFim >= dataInicio
        }

        // Pré-processar dados para evitar N+1 queries
        val pontosPorDia = pontosNoPeriodo.groupBy { it.data }

        // Mapear feriados por data
        val feriadosPorData = mutableMapOf<LocalDate, Feriado>()
        val anosNoPeriodo = (dataInicio.year..dataFim.year).toList()
        for (feriado in feriados.filter { it.ativo }) {
            for (ano in anosNoPeriodo) {
                feriado.getDataParaAno(ano)?.let { data ->
                    if (data in dataInicio..dataFim) {
                        feriadosPorData[data] = feriado
                    }
                }
            }
        }

        // Mapear ausências por data
        val ausenciasPorData = mutableMapOf<LocalDate, MutableList<Ausencia>>()
        for (ausencia in ausenciasNoPeriodo) {
            var data = maxOf(ausencia.dataInicio, dataInicio)
            while (data <= minOf(ausencia.dataFim, dataFim)) {
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
        while (dataAtual <= dataFim) {
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
            dataFim = dataFim,
            diasTrabalhados = diasTrabalhados,
            diasComAusencia = diasComAusencia,
            diasUteisSemRegistro = diasUteisSemRegistro,
            totalAjustesMinutos = totalAjustesMinutos,
            totalAbonosMinutos = totalAbonosMinutos,
            ultimoFechamento = null
        )
    }
}
