// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/usecase/ponto/CalcularBancoHorasUseCase.kt
package br.com.tlmacedo.meuponto.domain.usecase.ponto

import br.com.tlmacedo.meuponto.domain.model.AjusteSaldo
import br.com.tlmacedo.meuponto.domain.model.BancoHoras
import br.com.tlmacedo.meuponto.domain.model.DiaSemana
import br.com.tlmacedo.meuponto.domain.model.FechamentoPeriodo
import br.com.tlmacedo.meuponto.domain.model.Ponto
import br.com.tlmacedo.meuponto.domain.model.TipoFechamento
import br.com.tlmacedo.meuponto.domain.model.VersaoJornada
import br.com.tlmacedo.meuponto.domain.repository.AjusteSaldoRepository
import br.com.tlmacedo.meuponto.domain.repository.ConfiguracaoEmpregoRepository
import br.com.tlmacedo.meuponto.domain.repository.FechamentoPeriodoRepository
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
 * O saldo do banco é calculado considerando:
 * 1. Data de início: último fechamento de banco ou início dos registros
 * 2. Data fim: data visualizada na tela (dinâmico)
 * 3. Tolerâncias: aplica tolerância de intervalo automaticamente
 * 4. Ajustes manuais: soma ajustes do período
 * 5. Carga horária por dia: usa configuração específica de cada dia da semana
 * 6. Versão de jornada: busca a versão correta para cada data
 *
 * @author Thiago
 * @since 1.0.0
 * @updated 2.6.0 - Considera fechamentos, ajustes e tolerâncias
 * @updated 2.9.0 - Corrigido para usar versão de jornada correta para cada data
 */
class CalcularBancoHorasUseCase @Inject constructor(
    private val pontoRepository: PontoRepository,
    private val fechamentoPeriodoRepository: FechamentoPeriodoRepository,
    private val ajusteSaldoRepository: AjusteSaldoRepository,
    private val configuracaoEmpregoRepository: ConfiguracaoEmpregoRepository,
    private val horarioDiaSemanaRepository: HorarioDiaSemanaRepository,
    private val versaoJornadaRepository: VersaoJornadaRepository,
    private val aplicarToleranciaIntervaloUseCase: AplicarToleranciaIntervaloUseCase
) {

    /**
     * Resultado detalhado do cálculo do banco de horas.
     */
    data class ResultadoBancoHoras(
        val saldoTotal: Duration,
        val dataInicio: LocalDate,
        val dataFim: LocalDate,
        val diasTrabalhados: Int,
        val totalAjustesMinutos: Int,
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

    /**
     * Cache de versões de jornada para evitar múltiplas consultas ao banco.
     * Chave: "empregoId_dataInicio_dataFim" da versão
     */
    private data class VersaoCache(
        val versao: VersaoJornada,
        val horariosPorDia: Map<DiaSemana, br.com.tlmacedo.meuponto.domain.model.HorarioDiaSemana>
    )

    /**
     * Calcula o banco de horas até uma data específica de forma reativa.
     *
     * @param empregoId ID do emprego
     * @param ateData Data limite para cálculo (data visualizada na tela)
     * @return Flow que emite o ResultadoBancoHoras atualizado
     */
    operator fun invoke(
        empregoId: Long,
        ateData: LocalDate = LocalDate.now()
    ): Flow<ResultadoBancoHoras> {
        return combine(
            pontoRepository.observarPorEmprego(empregoId),
            fechamentoPeriodoRepository.observarUltimoFechamento(empregoId),
            ajusteSaldoRepository.observarPorEmprego(empregoId)
        ) { pontos, ultimoFechamento, ajustes ->
            calcularBancoHoras(
                empregoId = empregoId,
                pontos = pontos,
                ultimoFechamento = ultimoFechamento,
                ajustes = ajustes,
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
        val ultimoFechamento = fechamentoPeriodoRepository.buscarUltimoFechamentoPorTipo(
            empregoId,
            TipoFechamento.BANCO_HORAS
        )

        val dataInicio = ultimoFechamento?.dataFimPeriodo?.plusDays(1)
            ?: pontoRepository.buscarPrimeiraData(empregoId)
            ?: ateData

        val pontos = pontoRepository.buscarPorEmpregoEPeriodo(empregoId, dataInicio, ateData)
        val ajustes = ajusteSaldoRepository.buscarPorPeriodo(empregoId, dataInicio, ateData)

        return calcularBancoHoras(
            empregoId = empregoId,
            pontos = pontos,
            ultimoFechamento = ultimoFechamento,
            ajustes = ajustes,
            ateData = ateData
        )
    }

    private suspend fun calcularBancoHoras(
        empregoId: Long,
        pontos: List<Ponto>,
        ultimoFechamento: FechamentoPeriodo?,
        ajustes: List<AjusteSaldo>,
        ateData: LocalDate
    ): ResultadoBancoHoras {
        // Determinar data de início do período
        val dataInicio = ultimoFechamento?.dataFimPeriodo?.plusDays(1)
            ?: pontos.minOfOrNull { it.data }
            ?: ateData

        // Filtrar pontos e ajustes do período
        val pontosNoPeriodo = pontos.filter { it.data in dataInicio..ateData }
        val ajustesNoPeriodo = ajustes.filter { it.data in dataInicio..ateData }

        // Agrupar pontos por dia
        val pontosPorDia = pontosNoPeriodo.groupBy { it.data }

        // Buscar configuração global como fallback
        val configGlobal = configuracaoEmpregoRepository.buscarPorEmpregoId(empregoId)

        // Cache de versões e horários para evitar múltiplas consultas
        val versaoCache = mutableMapOf<Long, VersaoCache>()

        var saldoTotal = Duration.ZERO
        var diasTrabalhados = 0

        // Calcular saldo de cada dia
        for ((data, pontosNoDia) in pontosPorDia) {
            if (pontosNoDia.size >= 2 && pontosNoDia.size % 2 == 0) {
                // Jornada completa
                diasTrabalhados++

                val diaSemana = DiaSemana.fromJavaDayOfWeek(data.dayOfWeek)

                // Buscar versão de jornada para esta data específica
                val versaoJornada = versaoJornadaRepository.buscarPorEmpregoEData(empregoId, data)

                // Buscar configuração do dia (da versão correta ou fallback)
                val configDia = versaoJornada?.let { versao ->
                    // Usar cache se disponível
                    val cached = versaoCache[versao.id] ?: run {
                        val horarios = horarioDiaSemanaRepository.buscarPorVersaoJornada(versao.id)
                            .associateBy { it.diaSemana }
                        VersaoCache(versao, horarios).also { versaoCache[versao.id] = it }
                    }
                    cached.horariosPorDia[diaSemana]
                } ?: run {
                    // Fallback: buscar pelo emprego (compatibilidade)
                    horarioDiaSemanaRepository.buscarPorEmpregoEDia(empregoId, diaSemana)
                }

                // Obter configurações do dia
                val intervaloMinimo = configDia?.intervaloMinimoMinutos
                    ?: configGlobal?.intervaloMinimoMinutos
                    ?: 60
                val toleranciaIntervalo = configDia?.toleranciaIntervaloMaisMinutos
                    ?: configGlobal?.toleranciaIntervaloMaisMinutos
                    ?: 0
                val cargaHorariaDia = configDia?.cargaHorariaMinutos
                    ?: configGlobal?.cargaHorariaDiariaMinutos
                    ?: 480

                // Aplicar tolerância de intervalo
                val pontosComTolerancia = aplicarToleranciaIntervaloUseCase.invokeComConfiguracao(
                    pontos = pontosNoDia,
                    intervaloMinimoMinutos = intervaloMinimo,
                    toleranciaMinutos = toleranciaIntervalo
                )

                // Calcular horas trabalhadas usando horaEfetiva
                val horasTrabalhadas = calcularHorasTrabalhadas(pontosComTolerancia)

                // Calcular saldo do dia
                val cargaHoraria = Duration.ofMinutes(cargaHorariaDia.toLong())
                val saldoDia = horasTrabalhadas.minus(cargaHoraria)

                saldoTotal = saldoTotal.plus(saldoDia)
            }
        }

        // Somar ajustes manuais
        val totalAjustesMinutos = ajustesNoPeriodo.sumOf { it.minutos }
        saldoTotal = saldoTotal.plusMinutes(totalAjustesMinutos.toLong())

        return ResultadoBancoHoras(
            saldoTotal = saldoTotal,
            dataInicio = dataInicio,
            dataFim = ateData,
            diasTrabalhados = diasTrabalhados,
            totalAjustesMinutos = totalAjustesMinutos,
            ultimoFechamento = ultimoFechamento
        )
    }

    /**
     * Calcula horas trabalhadas usando horaEfetiva dos pontos.
     */
    private fun calcularHorasTrabalhadas(pontos: List<Ponto>): Duration {
        if (pontos.size < 2) return Duration.ZERO

        val pontosOrdenados = pontos.sortedBy { it.dataHora }
        var total = Duration.ZERO
        var i = 0

        while (i < pontosOrdenados.size - 1) {
            val entrada = pontosOrdenados[i]
            val saida = pontosOrdenados[i + 1]

            // Usar horaConsiderada se existir (tolerância aplicada), senão dataHora
            val horaEntrada = entrada.horaConsiderada ?: entrada.dataHora
            val horaSaida = saida.horaConsiderada ?: saida.dataHora

            total = total.plus(Duration.between(horaEntrada, horaSaida))
            i += 2
        }

        return total
    }
}
