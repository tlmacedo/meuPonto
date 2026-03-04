// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/usecase/ponto/AplicarToleranciaIntervaloUseCase.kt
package br.com.tlmacedo.meuponto.domain.usecase.ponto

import br.com.tlmacedo.meuponto.domain.model.DiaSemana
import br.com.tlmacedo.meuponto.domain.model.Ponto
import br.com.tlmacedo.meuponto.domain.repository.HorarioDiaSemanaRepository
import br.com.tlmacedo.meuponto.domain.repository.VersaoJornadaRepository
import java.time.Duration
import java.time.LocalTime
import javax.inject.Inject

/**
 * Caso de uso para aplicar tolerância de intervalo aos pontos de retorno.
 *
 * @author Thiago
 * @since 2.6.0
 * @updated 8.0.0 - Migrado para usar VersaoJornada
 */
class AplicarToleranciaIntervaloUseCase @Inject constructor(
    private val versaoJornadaRepository: VersaoJornadaRepository,
    private val horarioDiaSemanaRepository: HorarioDiaSemanaRepository
) {

    data class ResultadoTolerancia(
        val pontoOriginal: Ponto,
        val horaConsiderada: LocalTime?,
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

    suspend operator fun invoke(pontos: List<Ponto>, empregoId: Long): List<Ponto> {
        if (pontos.size < 4) return pontos

        val pontosOrdenados = pontos.sortedBy { it.dataHora }
        val data = pontosOrdenados.first().data
        val diaSemana = DiaSemana.fromJavaDayOfWeek(data.dayOfWeek)

        val versaoJornada = versaoJornadaRepository.buscarPorEmpregoEData(empregoId, data)

        val configDia = versaoJornada?.let {
            horarioDiaSemanaRepository.buscarPorVersaoEDia(it.id, diaSemana)
        } ?: horarioDiaSemanaRepository.buscarPorEmpregoEDia(empregoId, diaSemana)

        val intervaloMinimoMinutos = configDia?.intervaloMinimoMinutos
            ?: versaoJornada?.intervaloMinimoInterjornadaMinutos?.let { 60 }
            ?: 60

        val toleranciaMinutos = configDia?.toleranciaIntervaloMaisMinutos
            ?: versaoJornada?.toleranciaIntervaloMaisMinutos
            ?: 0

        return aplicarTolerancia(pontosOrdenados, intervaloMinimoMinutos, toleranciaMinutos)
    }

    fun invokeComConfiguracao(
        pontos: List<Ponto>,
        intervaloMinimoMinutos: Int,
        toleranciaMinutos: Int
    ): List<Ponto> {
        if (pontos.size < 4) return pontos
        val pontosOrdenados = pontos.sortedBy { it.dataHora }
        return aplicarTolerancia(pontosOrdenados, intervaloMinimoMinutos, toleranciaMinutos)
    }

    suspend fun calcularDetalhado(pontos: List<Ponto>, empregoId: Long): List<ResultadoTolerancia> {
        val pontosOrdenados = pontos.sortedBy { it.dataHora }

        if (pontosOrdenados.size < 4) {
            return pontosOrdenados.map { ponto ->
                ResultadoTolerancia(ponto, null, false, 0, 0, null)
            }
        }

        val data = pontosOrdenados.first().data
        val diaSemana = DiaSemana.fromJavaDayOfWeek(data.dayOfWeek)

        val versaoJornada = versaoJornadaRepository.buscarPorEmpregoEData(empregoId, data)
        val configDia = versaoJornada?.let {
            horarioDiaSemanaRepository.buscarPorVersaoEDia(it.id, diaSemana)
        } ?: horarioDiaSemanaRepository.buscarPorEmpregoEDia(empregoId, diaSemana)

        val intervaloMinimoMinutos = configDia?.intervaloMinimoMinutos ?: 60
        val toleranciaMinutos = configDia?.toleranciaIntervaloMaisMinutos
            ?: versaoJornada?.toleranciaIntervaloMaisMinutos ?: 0

        val limiteMaximoMinutos = intervaloMinimoMinutos + toleranciaMinutos

        return pontosOrdenados.mapIndexed { indice, ponto ->
            calcularToleranciaParaPonto(ponto, indice, pontosOrdenados, intervaloMinimoMinutos, limiteMaximoMinutos)
        }
    }

    private fun aplicarTolerancia(
        pontosOrdenados: List<Ponto>,
        intervaloMinimoMinutos: Int,
        toleranciaMinutos: Int
    ): List<Ponto> {
        val limiteMaximoMinutos = intervaloMinimoMinutos + toleranciaMinutos

        return pontosOrdenados.mapIndexed { indice, ponto ->
            calcularToleranciaParaPonto(ponto, indice, pontosOrdenados, intervaloMinimoMinutos, limiteMaximoMinutos).pontoAtualizado()
        }
    }

    private fun calcularToleranciaParaPonto(
        ponto: Ponto,
        indice: Int,
        pontosOrdenados: List<Ponto>,
        intervaloMinimoMinutos: Int,
        limiteMaximoMinutos: Int
    ): ResultadoTolerancia {
        val isVoltaIntervalo = indice >= 2 && indice % 2 == 0

        if (!isVoltaIntervalo) {
            return ResultadoTolerancia(ponto, null, false, 0, 0, null)
        }

        val saidaIntervalo = pontosOrdenados[indice - 1]
        val intervaloRealMinutos = Duration.between(saidaIntervalo.dataHora, ponto.dataHora).toMinutes()
        val dentroTolerancia = intervaloRealMinutos <= limiteMaximoMinutos

        return if (dentroTolerancia && intervaloRealMinutos > intervaloMinimoMinutos) {
            val horaConsiderada = saidaIntervalo.dataHora.plusMinutes(intervaloMinimoMinutos.toLong()).toLocalTime()
            ResultadoTolerancia(
                ponto, horaConsiderada, true, intervaloRealMinutos, intervaloMinimoMinutos.toLong(),
                "Intervalo de ${intervaloRealMinutos}min ajustado para ${intervaloMinimoMinutos}min"
            )
        } else {
            ResultadoTolerancia(
                ponto, null, false, intervaloRealMinutos, intervaloRealMinutos,
                if (intervaloRealMinutos > limiteMaximoMinutos) "Intervalo excedeu tolerância" else null
            )
        }
    }

    fun calcularParaPonto(
        saidaIntervalo: Ponto,
        voltaIntervalo: Ponto,
        intervaloMinimoMinutos: Int,
        toleranciaMinutos: Int
    ): ResultadoTolerancia {
        val limiteMaximoMinutos = intervaloMinimoMinutos + toleranciaMinutos
        val intervaloRealMinutos = Duration.between(saidaIntervalo.dataHora, voltaIntervalo.dataHora).toMinutes()
        val dentroTolerancia = intervaloRealMinutos <= limiteMaximoMinutos

        return if (dentroTolerancia && intervaloRealMinutos > intervaloMinimoMinutos) {
            val horaConsiderada = saidaIntervalo.dataHora.plusMinutes(intervaloMinimoMinutos.toLong()).toLocalTime()
            ResultadoTolerancia(
                voltaIntervalo, horaConsiderada, true, intervaloRealMinutos, intervaloMinimoMinutos.toLong(),
                "Intervalo ajustado de ${intervaloRealMinutos}min para ${intervaloMinimoMinutos}min"
            )
        } else {
            ResultadoTolerancia(
                voltaIntervalo, null, false, intervaloRealMinutos, intervaloRealMinutos,
                if (intervaloRealMinutos > limiteMaximoMinutos) "Intervalo excedeu tolerância" else null
            )
        }
    }
}
