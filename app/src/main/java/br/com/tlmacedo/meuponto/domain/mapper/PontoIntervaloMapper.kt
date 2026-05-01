// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/mapper/PontoIntervaloMapper.kt
package br.com.tlmacedo.meuponto.domain.mapper

import br.com.tlmacedo.meuponto.domain.model.IntervaloPonto
import br.com.tlmacedo.meuponto.domain.model.Ponto
import br.com.tlmacedo.meuponto.domain.model.TipoPausa
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import kotlin.math.abs

/**
 * Converte pontos do dia em turnos.
 *
 * Regra:
 * - ponto[0] + ponto[1] = turno 1
 * - ponto[2] + ponto[3] = turno 2
 * - ponto[4] + ponto[5] = turno 3
 *
 * O intervalo é sempre:
 * - saída do turno anterior -> entrada do próximo turno
 *
 * A tolerância de volta do intervalo:
 * - só pode ser aplicada uma vez por dia
 * - só aplica em intervalo real >= intervalo mínimo
 * - só aplica se intervalo real <= intervalo mínimo + tolerância
 * - se houver mais de um intervalo elegível, aplica no intervalo cuja saída anterior
 *   esteja mais próxima da saída ideal configurada pelo usuário
 */
fun List<Ponto>.toIntervalosPonto(
    intervaloMinimoMinutos: Int = 60,
    toleranciaVoltaIntervaloMinutos: Int = 0,
    saidaIntervaloIdeal: LocalTime? = null
): List<IntervaloPonto> {
    val pontosOrdenados = sortedBy { it.dataHora }

    if (pontosOrdenados.isEmpty()) return emptyList()

    val turnosBase = pontosOrdenados
        .chunked(2)
        .map { par ->
            TurnoBase(
                entrada = par[0],
                saida = par.getOrNull(1)
            )
        }

    val pausas = calcularPausasEntreTurnos(
        turnos = turnosBase,
        intervaloMinimoMinutos = intervaloMinimoMinutos,
        toleranciaVoltaIntervaloMinutos = toleranciaVoltaIntervaloMinutos,
        saidaIntervaloIdeal = saidaIntervaloIdeal
    )

    val indiceTurnoComTolerancia = selecionarTurnoQueRecebeTolerancia(pausas)

    return turnosBase.mapIndexed { index, turno ->
        val pausaAntes = pausas[index]

        val toleranciaAplicadaNesteTurno =
            index == indiceTurnoComTolerancia &&
                    pausaAntes != null &&
                    pausaAntes.elegivelParaTolerancia

        val pausaConsiderada = when {
            pausaAntes == null -> null
            toleranciaAplicadaNesteTurno -> intervaloMinimoMinutos
            else -> pausaAntes.minutosReais
        }

        val horaEntradaConsiderada = if (toleranciaAplicadaNesteTurno && pausaAntes != null) {
            pausaAntes.saidaAnteriorDataHora.plusMinutes(intervaloMinimoMinutos.toLong())
        } else {
            null
        }

        IntervaloPonto(
            entrada = turno.entrada,
            saida = turno.saida,
            pausaAntesMinutosReal = pausaAntes?.minutosReais,
            pausaAntesMinutosConsiderada = pausaConsiderada,
            tipoPausa = pausaAntes?.tipoPausa,
            horaEntradaConsiderada = horaEntradaConsiderada
        )
    }
}

private data class TurnoBase(
    val entrada: Ponto,
    val saida: Ponto?
)

private data class PausaEntreTurnos(
    val indiceTurnoAtual: Int,
    val saidaAnteriorDataHora: LocalDateTime,
    val entradaAtualDataHora: LocalDateTime,
    val minutosReais: Int,
    val tipoPausa: TipoPausa,
    val elegivelParaTolerancia: Boolean,
    val distanciaDaSaidaIdealMinutos: Int
)

private fun calcularPausasEntreTurnos(
    turnos: List<TurnoBase>,
    intervaloMinimoMinutos: Int,
    toleranciaVoltaIntervaloMinutos: Int,
    saidaIntervaloIdeal: LocalTime?
): Map<Int, PausaEntreTurnos> {
    if (turnos.size <= 1) return emptyMap()

    val pausas = mutableMapOf<Int, PausaEntreTurnos>()

    for (indiceTurnoAtual in 1 until turnos.size) {
        val turnoAnterior = turnos[indiceTurnoAtual - 1]
        val turnoAtual = turnos[indiceTurnoAtual]

        val saidaAnterior = turnoAnterior.saida ?: continue
        val entradaAtual = turnoAtual.entrada

        val saidaAnteriorDataHora = LocalDateTime.of(
            saidaAnterior.data,
            saidaAnterior.hora
        )

        val entradaAtualDataHora = LocalDateTime.of(
            entradaAtual.data,
            entradaAtual.hora
        )

        val minutosReais = Duration.between(
            saidaAnteriorDataHora,
            entradaAtualDataHora
        )
            .toMinutes()
            .toInt()
            .coerceAtLeast(0)

        val limiteComTolerancia = intervaloMinimoMinutos + toleranciaVoltaIntervaloMinutos

        val elegivel = minutosReais > intervaloMinimoMinutos &&
                minutosReais <= limiteComTolerancia &&
                toleranciaVoltaIntervaloMinutos > 0

        val distanciaIdeal = calcularDistanciaDaSaidaIdealEmMinutos(
            saidaReal = saidaAnteriorDataHora.toLocalTime(),
            saidaIdeal = saidaIntervaloIdeal
        )

        pausas[indiceTurnoAtual] = PausaEntreTurnos(
            indiceTurnoAtual = indiceTurnoAtual,
            saidaAnteriorDataHora = saidaAnteriorDataHora,
            entradaAtualDataHora = entradaAtualDataHora,
            minutosReais = minutosReais,
            tipoPausa = minutosReais.toTipoPausa(intervaloMinimoMinutos),
            elegivelParaTolerancia = elegivel,
            distanciaDaSaidaIdealMinutos = distanciaIdeal
        )
    }

    return pausas
}

private fun selecionarTurnoQueRecebeTolerancia(
    pausas: Map<Int, PausaEntreTurnos>
): Int? {
    return pausas.values
        .filter { it.elegivelParaTolerancia }
        .minWithOrNull(
            compareBy<PausaEntreTurnos> { it.distanciaDaSaidaIdealMinutos }
                .thenBy { it.indiceTurnoAtual }
        )
        ?.indiceTurnoAtual
}

/**
 * Se houver saída ideal configurada, escolhemos a pausa cuja saída anterior
 * ficou mais próxima dela.
 *
 * Se não houver saída ideal, todas recebem distância 0; nesse caso o desempate
 * fica pelo primeiro intervalo elegível do dia.
 */
private fun calcularDistanciaDaSaidaIdealEmMinutos(
    saidaReal: LocalTime,
    saidaIdeal: LocalTime?
): Int {
    if (saidaIdeal == null) return 0

    return abs(Duration.between(saidaIdeal, saidaReal).toMinutes().toInt())
}

private fun Int.toTipoPausa(
    intervaloMinimoMinutos: Int
): TipoPausa {
    return when {
        this >= intervaloMinimoMinutos -> TipoPausa.ALMOCO
        this <= 30 -> TipoPausa.CAFE
        else -> TipoPausa.SAIDA_RAPIDA
    }
}