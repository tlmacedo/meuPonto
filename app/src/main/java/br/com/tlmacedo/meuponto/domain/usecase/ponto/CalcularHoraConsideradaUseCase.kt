// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/usecase/ponto/CalcularHoraConsideradaUseCase.kt
package br.com.tlmacedo.meuponto.domain.usecase.ponto

import br.com.tlmacedo.meuponto.domain.model.DiaSemana
import br.com.tlmacedo.meuponto.domain.model.HorarioDiaSemana
import br.com.tlmacedo.meuponto.domain.model.Ponto
import br.com.tlmacedo.meuponto.domain.model.VersaoJornada
import br.com.tlmacedo.meuponto.domain.repository.HorarioDiaSemanaRepository
import br.com.tlmacedo.meuponto.domain.repository.PontoRepository
import br.com.tlmacedo.meuponto.domain.repository.VersaoJornadaRepository
import timber.log.Timber
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject

/**
 * Caso de uso para calcular a hora considerada de um ponto.
 *
 * REGRAS DE TOLERÂNCIA:
 *
 * 1. ENTRADA (primeiro turno - índice 0):
 *    - Não há mais tolerância na entrada. horaConsiderada = hora real.
 *
 * 2. VOLTA DO INTERVALO DE ALMOÇO (apenas uma vez ao dia):
 *    - Identifica se o intervalo é o de almoço (baseado na proximidade com o saidaIntervaloIdeal).
 *    - Calcula hora ideal de volta = hora real da saída anterior + intervalo mínimo.
 *    - Se bateu DEPOIS da hora ideal de volta e está dentro da tolerância de intervalo.
 *    - Considera o horário ideal.
 *
 * 3. OUTROS INTERVALOS E SAÍDAS:
 *    - Não aplicamos tolerância.
 *    - horaConsiderada = hora real
 *
 * @author Thiago
 * @since 7.0.0
 * @updated 12.0.0 - Ajustado para buscar configurações da VersaoJornada vigente na data
 */
class CalcularHoraConsideradaUseCase @Inject constructor(
    private val versaoJornadaRepository: VersaoJornadaRepository,
    private val horarioDiaSemanaRepository: HorarioDiaSemanaRepository,
    private val pontoRepository: PontoRepository
) {
    companion object {
        /** Tolerância de entrada fixa em minutos */
        const val TOLERANCIA_ENTRADA_MINUTOS = 10
    }

    /**
     * Calcula a hora considerada para um novo ponto.
     */
    suspend operator fun invoke(
        empregoId: Long,
        dataHora: LocalDateTime,
        indicePonto: Int
    ): LocalTime {
        val horaReal = dataHora.toLocalTime()

        // Busca a versão da jornada vigente
        val versao = versaoJornadaRepository.buscarPorEmpregoEData(empregoId, dataHora.toLocalDate())
            ?: return horaReal

        // Busca configuração do dia
        val diaSemana = DiaSemana.fromJavaDayOfWeek(dataHora.dayOfWeek)
        val horarioDia = horarioDiaSemanaRepository.buscarPorVersaoEDia(versao.id, diaSemana)

        // Apenas entradas pares (exceto a primeira) podem ter tolerância de intervalo
        val isVoltaIntervalo = indicePonto > 0 && indicePonto % 2 == 0
        if (!isVoltaIntervalo) return horaReal

        // Para volta de intervalo, verificamos se é o almoço
        val intervaloMinimo = horarioDia?.intervaloMinimoMinutos ?: 60
        val saidaIntervaloIdeal = horarioDia?.saidaIntervaloIdeal

        return calcularToleranciaVoltaIntervalo(
            empregoId, dataHora, versao, intervaloMinimo, indicePonto, saidaIntervaloIdeal
        )
    }

    /**
     * Calcula tolerância para ENTRADA (primeiro ponto do dia).
     * Se bateu ANTES do horário ideal e dentro da tolerância fixa de 10 minutos,
     * considera o horário ideal.
     */
    private fun calcularToleranciaEntrada(
        dataHora: LocalDateTime,
        horarioDia: HorarioDiaSemana
    ): LocalTime {
        val horaReal = dataHora.toLocalTime()
        val entradaIdeal = horarioDia.entradaIdeal ?: return horaReal

        val diferencaMinutos = Duration.between(horaReal, entradaIdeal).toMinutes()

        // Se bateu ANTES do ideal e dentro da tolerância fixa
        if (diferencaMinutos > 0 && diferencaMinutos <= TOLERANCIA_ENTRADA_MINUTOS) {
            Timber.d(
                "Tolerância entrada aplicada: real=%s, ideal=%s, diferença=%dmin",
                horaReal, entradaIdeal, diferencaMinutos
            )
            return entradaIdeal
        }

        return horaReal
    }

    /**
     * Calcula tolerância para VOLTA DO INTERVALO.
     * REGRA: Apenas para o intervalo de almoço identificado por proximidade do horário ideal.
     */
    private suspend fun calcularToleranciaVoltaIntervalo(
        empregoId: Long,
        dataHora: LocalDateTime,
        versao: VersaoJornada,
        intervaloMinimoMinutos: Int,
        indicePonto: Int,
        saidaIntervaloIdeal: LocalTime?
    ): LocalTime {
        val horaReal = dataHora.toLocalTime()
        val toleranciaMinutos = versao.toleranciaIntervaloMaisMinutos

        if (toleranciaMinutos <= 0) return horaReal

        // Busca os pontos do dia
        val pontosNoDia = pontoRepository.buscarPorEmpregoEData(empregoId, dataHora.toLocalDate())
            .sortedBy { it.dataHora }

        val indiceSaidaAnterior = indicePonto - 1
        if (indiceSaidaAnterior < 0 || indiceSaidaAnterior >= pontosNoDia.size) return horaReal

        val pontoSaidaAnterior = pontosNoDia[indiceSaidaAnterior]
        val horaSaidaReal = pontoSaidaAnterior.dataHora.toLocalTime()

        // 1. Verifica se este é o intervalo de almoço (proximidade com o ideal)
        // Se não houver ideal, qualquer intervalo >= mínimo pode ser candidato, mas priorizamos o primeiro
        val isAlmoco = if (saidaIntervaloIdeal != null) {
            Math.abs(Duration.between(horaSaidaReal, saidaIntervaloIdeal).toMinutes()) <= 120 // 2h de margem
        } else {
            // Se não tem horário ideal, assume que o primeiro intervalo longo do dia é o almoço
            indicePonto == 2
        }

        if (!isAlmoco) return horaReal

        // 2. Calcula hora ideal de volta e verifica a duração real
        val intervaloRealMinutos = Duration.between(pontoSaidaAnterior.dataHora, dataHora).toMinutes()
        val horaIdealVolta = pontoSaidaAnterior.horaConsiderada.plusMinutes(intervaloMinimoMinutos.toLong())

        val dentroTolerancia = intervaloRealMinutos in (intervaloMinimoMinutos.toLong()..(intervaloMinimoMinutos + toleranciaMinutos).toLong())

        if (dentroTolerancia && horaReal.isAfter(horaIdealVolta)) {
            Timber.d("Tolerância ALMOÇO aplicada: real=%s → considerado=%s", horaReal, horaIdealVolta)
            return horaIdealVolta
        }

        return horaReal
    }

    /** Recalcula a hora considerada para um ponto existente */
    suspend fun recalcular(ponto: Ponto): Ponto {
        val pontosNoDia = pontoRepository.buscarPorEmpregoEData(ponto.empregoId, ponto.data)
            .sortedBy { it.dataHora }

        val indicePonto = pontosNoDia.indexOfFirst { it.id == ponto.id }
        if (indicePonto < 0) return ponto

        val novaHoraConsiderada = invoke(ponto.empregoId, ponto.dataHora, indicePonto)
        return ponto.comHoraConsiderada(novaHoraConsiderada)
    }

    /** Recalcula todos os pontos de um dia */
    suspend fun recalcularDia(empregoId: Long, data: java.time.LocalDate): List<Ponto> {
        val pontosNoDia = pontoRepository.buscarPorEmpregoEData(empregoId, data)
            .sortedBy { it.dataHora }

        return pontosNoDia.mapIndexed { index, ponto ->
            val novaHoraConsiderada = invoke(empregoId, ponto.dataHora, index)
            ponto.comHoraConsiderada(novaHoraConsiderada)
        }
    }
}
