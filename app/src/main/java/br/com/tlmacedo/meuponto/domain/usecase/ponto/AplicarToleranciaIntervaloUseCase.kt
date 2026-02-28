// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/usecase/ponto/AplicarToleranciaIntervaloUseCase.kt
package br.com.tlmacedo.meuponto.domain.usecase.ponto

import br.com.tlmacedo.meuponto.domain.model.DiaSemana
import br.com.tlmacedo.meuponto.domain.model.Ponto
import br.com.tlmacedo.meuponto.domain.repository.ConfiguracaoEmpregoRepository
import br.com.tlmacedo.meuponto.domain.repository.HorarioDiaSemanaRepository
import java.time.Duration
import java.time.LocalDateTime
import javax.inject.Inject

/**
 * Caso de uso para aplicar tolerância de intervalo aos pontos de retorno.
 *
 * A tolerância é aplicada APENAS na volta do intervalo:
 * - Se o tempo de intervalo real estiver dentro do limite (intervaloMinimo + tolerancia),
 *   a horaConsiderada será ajustada para saidaIntervalo + intervaloMinimo.
 * - Se exceder o limite, mantém a hora real registrada.
 *
 * Exemplo:
 * - Saída intervalo: 12:43
 * - Intervalo mínimo: 60 min
 * - Tolerância: 20 min
 * - Retorno real: 14:01 (78 min de intervalo)
 * - Como 78 ≤ 80 → horaConsiderada = 13:43 (12:43 + 60min)
 *
 * @author Thiago
 * @since 2.6.0
 */
class AplicarToleranciaIntervaloUseCase @Inject constructor(
    private val configuracaoEmpregoRepository: ConfiguracaoEmpregoRepository,
    private val horarioDiaSemanaRepository: HorarioDiaSemanaRepository
) {

    /**
     * Resultado da aplicação de tolerância para um ponto.
     */
    data class ResultadoTolerancia(
        val pontoOriginal: Ponto,
        val horaConsiderada: LocalDateTime?,
        val foiAjustado: Boolean,
        val intervaloRealMinutos: Long,
        val intervaloConsideradoMinutos: Long,
        val motivoAjuste: String?
    ) {
        fun pontoAtualizado(): Ponto {
            return if (foiAjustado && horaConsiderada != null) {
                pontoOriginal.copy(horaConsiderada = horaConsiderada)
            } else {
                pontoOriginal
            }
        }
    }

    /**
     * Aplica tolerância de intervalo a uma lista de pontos do dia.
     */
    suspend operator fun invoke(
        pontos: List<Ponto>,
        empregoId: Long
    ): List<Ponto> {
        if (pontos.size < 4) return pontos

        val pontosOrdenados = pontos.sortedBy { it.dataHora }
        val data = pontosOrdenados.first().data
        val diaSemana = DiaSemana.fromJavaDayOfWeek(data.dayOfWeek)

        val configGlobal = configuracaoEmpregoRepository.buscarPorEmpregoId(empregoId)
        val configDia = horarioDiaSemanaRepository.buscarPorEmpregoEDia(empregoId, diaSemana)

        val intervaloMinimoMinutos = configDia?.intervaloMinimoMinutos
            ?: configGlobal?.intervaloMinimoMinutos
            ?: 60

        val toleranciaMinutos = configDia?.toleranciaIntervaloMaisMinutos
            ?: configGlobal?.toleranciaIntervaloMaisMinutos
            ?: 0

        return aplicarTolerancia(pontosOrdenados, intervaloMinimoMinutos, toleranciaMinutos)
    }

    /**
     * Aplica tolerância usando configurações já conhecidas.
     */
    fun invokeComConfiguracao(
        pontos: List<Ponto>,
        intervaloMinimoMinutos: Int,
        toleranciaMinutos: Int
    ): List<Ponto> {
        if (pontos.size < 4) return pontos
        val pontosOrdenados = pontos.sortedBy { it.dataHora }
        return aplicarTolerancia(pontosOrdenados, intervaloMinimoMinutos, toleranciaMinutos)
    }

    /**
     * Calcula tolerância e retorna resultados detalhados.
     */
    suspend fun calcularDetalhado(
        pontos: List<Ponto>,
        empregoId: Long
    ): List<ResultadoTolerancia> {
        val pontosOrdenados = pontos.sortedBy { it.dataHora }

        if (pontosOrdenados.size < 4) {
            return pontosOrdenados.map { ponto ->
                ResultadoTolerancia(
                    pontoOriginal = ponto,
                    horaConsiderada = null,
                    foiAjustado = false,
                    intervaloRealMinutos = 0,
                    intervaloConsideradoMinutos = 0,
                    motivoAjuste = null
                )
            }
        }

        val data = pontosOrdenados.first().data
        val diaSemana = DiaSemana.fromJavaDayOfWeek(data.dayOfWeek)

        val configGlobal = configuracaoEmpregoRepository.buscarPorEmpregoId(empregoId)
        val configDia = horarioDiaSemanaRepository.buscarPorEmpregoEDia(empregoId, diaSemana)

        val intervaloMinimoMinutos = configDia?.intervaloMinimoMinutos
            ?: configGlobal?.intervaloMinimoMinutos
            ?: 60

        val toleranciaMinutos = configDia?.toleranciaIntervaloMaisMinutos
            ?: configGlobal?.toleranciaIntervaloMaisMinutos
            ?: 0

        val limiteMaximoMinutos = intervaloMinimoMinutos + toleranciaMinutos

        return pontosOrdenados.mapIndexed { indice, ponto ->
            calcularToleranciaParaPonto(
                ponto = ponto,
                indice = indice,
                pontosOrdenados = pontosOrdenados,
                intervaloMinimoMinutos = intervaloMinimoMinutos,
                limiteMaximoMinutos = limiteMaximoMinutos
            )
        }
    }

    private fun aplicarTolerancia(
        pontosOrdenados: List<Ponto>,
        intervaloMinimoMinutos: Int,
        toleranciaMinutos: Int
    ): List<Ponto> {
        val limiteMaximoMinutos = intervaloMinimoMinutos + toleranciaMinutos

        return pontosOrdenados.mapIndexed { indice, ponto ->
            val resultado = calcularToleranciaParaPonto(
                ponto = ponto,
                indice = indice,
                pontosOrdenados = pontosOrdenados,
                intervaloMinimoMinutos = intervaloMinimoMinutos,
                limiteMaximoMinutos = limiteMaximoMinutos
            )
            resultado.pontoAtualizado()
        }
    }

    private fun calcularToleranciaParaPonto(
        ponto: Ponto,
        indice: Int,
        pontosOrdenados: List<Ponto>,
        intervaloMinimoMinutos: Int,
        limiteMaximoMinutos: Int
    ): ResultadoTolerancia {
        // Tolerância só se aplica a pontos de "entrada após intervalo"
        // Índices pares >= 2: são voltas de intervalo (2 = volta do 1º intervalo, 4 = volta do 2º, etc.)
        val isVoltaIntervalo = indice >= 2 && indice % 2 == 0

        if (!isVoltaIntervalo) {
            return ResultadoTolerancia(
                pontoOriginal = ponto,
                horaConsiderada = null,
                foiAjustado = false,
                intervaloRealMinutos = 0,
                intervaloConsideradoMinutos = 0,
                motivoAjuste = null
            )
        }

        val saidaIntervalo = pontosOrdenados[indice - 1]
        val retornoReal = ponto.dataHora

        val intervaloRealMinutos = Duration.between(
            saidaIntervalo.dataHora,
            retornoReal
        ).toMinutes()

        val dentroTolerancia = intervaloRealMinutos <= limiteMaximoMinutos

        return if (dentroTolerancia && intervaloRealMinutos > intervaloMinimoMinutos) {
            val horaConsiderada = saidaIntervalo.dataHora.plusMinutes(intervaloMinimoMinutos.toLong())

            ResultadoTolerancia(
                pontoOriginal = ponto,
                horaConsiderada = horaConsiderada,
                foiAjustado = true,
                intervaloRealMinutos = intervaloRealMinutos,
                intervaloConsideradoMinutos = intervaloMinimoMinutos.toLong(),
                motivoAjuste = "Intervalo de ${intervaloRealMinutos}min ajustado para ${intervaloMinimoMinutos}min (tolerância: ${limiteMaximoMinutos - intervaloMinimoMinutos}min)"
            )
        } else {
            ResultadoTolerancia(
                pontoOriginal = ponto,
                horaConsiderada = null,
                foiAjustado = false,
                intervaloRealMinutos = intervaloRealMinutos,
                intervaloConsideradoMinutos = intervaloRealMinutos,
                motivoAjuste = if (intervaloRealMinutos > limiteMaximoMinutos) {
                    "Intervalo de ${intervaloRealMinutos}min excedeu tolerância (limite: ${limiteMaximoMinutos}min)"
                } else {
                    null
                }
            )
        }
    }

    /**
     * Calcula a tolerância para um único par saída/volta de intervalo.
     */
    fun calcularParaPonto(
        saidaIntervalo: Ponto,
        voltaIntervalo: Ponto,
        intervaloMinimoMinutos: Int,
        toleranciaMinutos: Int
    ): ResultadoTolerancia {
        val limiteMaximoMinutos = intervaloMinimoMinutos + toleranciaMinutos

        val intervaloRealMinutos = Duration.between(
            saidaIntervalo.dataHora,
            voltaIntervalo.dataHora
        ).toMinutes()

        val dentroTolerancia = intervaloRealMinutos <= limiteMaximoMinutos

        return if (dentroTolerancia && intervaloRealMinutos > intervaloMinimoMinutos) {
            val horaConsiderada = saidaIntervalo.dataHora.plusMinutes(intervaloMinimoMinutos.toLong())

            ResultadoTolerancia(
                pontoOriginal = voltaIntervalo,
                horaConsiderada = horaConsiderada,
                foiAjustado = true,
                intervaloRealMinutos = intervaloRealMinutos,
                intervaloConsideradoMinutos = intervaloMinimoMinutos.toLong(),
                motivoAjuste = "Intervalo ajustado de ${intervaloRealMinutos}min para ${intervaloMinimoMinutos}min"
            )
        } else {
            ResultadoTolerancia(
                pontoOriginal = voltaIntervalo,
                horaConsiderada = null,
                foiAjustado = false,
                intervaloRealMinutos = intervaloRealMinutos,
                intervaloConsideradoMinutos = intervaloRealMinutos,
                motivoAjuste = if (intervaloRealMinutos > limiteMaximoMinutos) {
                    "Intervalo excedeu tolerância"
                } else {
                    null
                }
            )
        }
    }
}
