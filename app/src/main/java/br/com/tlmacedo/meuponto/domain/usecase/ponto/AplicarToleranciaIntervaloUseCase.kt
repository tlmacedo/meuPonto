package br.com.tlmacedo.meuponto.domain.usecase.ponto

import br.com.tlmacedo.meuponto.domain.model.DiaSemana
import br.com.tlmacedo.meuponto.domain.model.Ponto
import br.com.tlmacedo.meuponto.domain.repository.HorarioDiaSemanaRepository
import br.com.tlmacedo.meuponto.domain.repository.VersaoJornadaRepository
import java.time.Duration
import java.time.LocalTime
import javax.inject.Inject

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

        val saidaIntervaloIdeal = configDia?.saidaIntervaloIdeal

        return aplicarTolerancia(
            pontosOrdenados,
            intervaloMinimoMinutos,
            toleranciaMinutos,
            saidaIntervaloIdeal
        )
    }

    fun invokeComConfiguracao(
        pontos: List<Ponto>,
        intervaloMinimoMinutos: Int,
        toleranciaMinutos: Int,
        saidaIntervaloIdeal: LocalTime? = null
    ): List<Ponto> {
        if (pontos.size < 4) return pontos
        val pontosOrdenados = pontos.sortedBy { it.dataHora }
        return aplicarTolerancia(
            pontosOrdenados,
            intervaloMinimoMinutos,
            toleranciaMinutos,
            saidaIntervaloIdeal
        )
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
        val saidaIntervaloIdeal = configDia?.saidaIntervaloIdeal

        val limiteMaximoMinutos = intervaloMinimoMinutos + toleranciaMinutos
        val indiceAlmoco = identificarIndiceAlmoco(
            pontosOrdenados,
            intervaloMinimoMinutos,
            toleranciaMinutos,
            saidaIntervaloIdeal
        )

        return pontosOrdenados.mapIndexed { indice, ponto ->
            calcularToleranciaParaPonto(
                ponto,
                indice,
                pontosOrdenados,
                intervaloMinimoMinutos,
                limiteMaximoMinutos,
                indiceAlmoco
            )
        }
    }

    private fun aplicarTolerancia(
        pontosOrdenados: List<Ponto>,
        intervaloMinimoMinutos: Int,
        toleranciaMinutos: Int,
        saidaIntervaloIdeal: LocalTime? = null
    ): List<Ponto> {
        val limiteMaximoMinutos = intervaloMinimoMinutos + toleranciaMinutos
        val indiceAlmoco = identificarIndiceAlmoco(
            pontosOrdenados,
            intervaloMinimoMinutos,
            toleranciaMinutos,
            saidaIntervaloIdeal
        )

        return pontosOrdenados.mapIndexed { indice, ponto ->
            calcularToleranciaParaPonto(
                ponto,
                indice,
                pontosOrdenados,
                intervaloMinimoMinutos,
                limiteMaximoMinutos,
                indiceAlmoco
            ).pontoAtualizado()
        }
    }

    private fun identificarIndiceAlmoco(
        pontos: List<Ponto>,
        minimo: Int,
        tolerancia: Int,
        ideal: LocalTime?
    ): Int? {
        val pausas = mutableListOf<Pair<Int, Long>>()
        var i = 2
        while (i < pontos.size) {
            val saida = pontos[i - 1]
            val entrada = pontos[i]
            val duracao = Duration.between(saida.dataHora, entrada.dataHora).toMinutes()
            pausas.add((i / 2 - 1) to duracao)
            i += 2
        }
        if (pausas.isEmpty()) return null

        val pausasLongas = pausas.filter { it.second >= (minimo - tolerancia) }

        return if (ideal != null && pausasLongas.isNotEmpty()) {
            pausasLongas.minByOrNull { p ->
                val horaSaida = pontos[(p.first + 1) * 2 - 1].dataHora.toLocalTime()
                Math.abs(Duration.between(horaSaida, ideal).toMinutes())
            }?.first
        } else if (pausasLongas.isNotEmpty()) {
            pausasLongas.minByOrNull { Math.abs(it.second - minimo) }?.first
        } else {
            pausas.maxByOrNull { it.second }?.first
        }
    }

    private fun calcularToleranciaParaPonto(
        ponto: Ponto,
        indice: Int,
        pontosOrdenados: List<Ponto>,
        intervaloMinimoMinutos: Int,
        limiteMaximoMinutos: Int,
        indiceAlmoco: Int?
    ): ResultadoTolerancia {
        // A volta do intervalo i (0-based) está no índice (i+1)*2 da lista de pontos
        val indicePausaAtual = if (indice >= 2 && indice % 2 == 0) (indice / 2 - 1) else -1
        val isVoltaAlmoco = indiceAlmoco != null && indicePausaAtual == indiceAlmoco

        if (!isVoltaAlmoco) {
            return ResultadoTolerancia(ponto, null, false, 0, 0, null)
        }

        val saidaIntervalo = pontosOrdenados[indice - 1]
        val intervaloRealMinutos =
            Duration.between(saidaIntervalo.dataHora, ponto.dataHora).toMinutes()
        val dentroTolerancia = intervaloRealMinutos <= limiteMaximoMinutos

        return if (dentroTolerancia && intervaloRealMinutos > intervaloMinimoMinutos) {
            val horaConsiderada =
                saidaIntervalo.dataHora.plusMinutes(intervaloMinimoMinutos.toLong()).toLocalTime()
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
        val intervaloRealMinutos =
            Duration.between(saidaIntervalo.dataHora, voltaIntervalo.dataHora).toMinutes()
        val dentroTolerancia = intervaloRealMinutos <= limiteMaximoMinutos

        return if (dentroTolerancia && intervaloRealMinutos > intervaloMinimoMinutos) {
            val horaConsiderada =
                saidaIntervalo.dataHora.plusMinutes(intervaloMinimoMinutos.toLong()).toLocalTime()
            ResultadoTolerancia(
                voltaIntervalo,
                horaConsiderada,
                true,
                intervaloRealMinutos,
                intervaloMinimoMinutos.toLong(),
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
