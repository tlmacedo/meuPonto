// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/usecase/ponto/CalcularHoraConsideradaUseCase.kt
package br.com.tlmacedo.meuponto.domain.usecase.ponto

import br.com.tlmacedo.meuponto.domain.model.DiaSemana
import br.com.tlmacedo.meuponto.domain.model.HorarioDiaSemana
import br.com.tlmacedo.meuponto.domain.model.Ponto
import br.com.tlmacedo.meuponto.domain.repository.HorarioDiaSemanaRepository
import br.com.tlmacedo.meuponto.domain.repository.PontoRepository
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
 *    - Se bateu ANTES do horário ideal (ex: 07:55 para entrada às 08:00)
 *    - E está dentro da tolerância fixa de 10 minutos
 *    - Considera o horário ideal (08:00)
 *
 * 2. VOLTA DO INTERVALO (índice 2, 4, etc - entradas após saída):
 *    - Calcula hora ideal de volta = hora real da saída anterior + intervalo mínimo
 *    - Se bateu DEPOIS da hora ideal de volta (ex: 14:07 para volta às 13:47)
 *    - E está dentro da tolerância de intervalo configurada
 *    - Considera o horário ideal (13:47)
 *
 * 3. SAÍDAS (índices ímpares):
 *    - Não aplicamos tolerância em saídas
 *    - horaConsiderada = hora real
 *
 * @author Thiago
 * @since 7.0.0
 * @updated 7.2.0 - Tolerância de entrada agora é fixa (10 minutos)
 */
class CalcularHoraConsideradaUseCase @Inject constructor(
    private val horarioDiaSemanaRepository: HorarioDiaSemanaRepository,
    private val pontoRepository: PontoRepository
) {
    companion object {
        /** Tolerância de entrada fixa em minutos */
        const val TOLERANCIA_ENTRADA_MINUTOS = 10
    }

    /**
     * Calcula a hora considerada para um novo ponto.
     *
     * @param empregoId ID do emprego
     * @param dataHora Data e hora real do ponto
     * @param indicePonto Índice do ponto no dia (0 = primeira entrada, 1 = primeira saída, etc)
     * @return LocalTime com a hora considerada
     */
    suspend operator fun invoke(
        empregoId: Long,
        dataHora: LocalDateTime,
        indicePonto: Int
    ): LocalTime {
        val horaReal = dataHora.toLocalTime()

        // Busca configuração do dia
        val diaSemana = DiaSemana.fromJavaDayOfWeek(dataHora.dayOfWeek)
        val horarioDia = horarioDiaSemanaRepository.buscarPorEmpregoEDia(empregoId, diaSemana)
            ?: return horaReal // Sem configuração = sem tolerância

        // Saídas não têm tolerância
        val isSaida = indicePonto % 2 != 0
        if (isSaida) return horaReal

        // Determina qual tolerância aplicar baseado no índice
        return when (indicePonto) {
            0 -> calcularToleranciaEntrada(dataHora, horarioDia)
            else -> calcularToleranciaVoltaIntervalo(empregoId, dataHora, horarioDia, indicePonto)
        }
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
     * horaIdealVolta = horaSaidaIntervaloReal + intervaloMinimoMinutos
     */
    private suspend fun calcularToleranciaVoltaIntervalo(
        empregoId: Long,
        dataHora: LocalDateTime,
        horarioDia: HorarioDiaSemana,
        indicePonto: Int
    ): LocalTime {
        val horaReal = dataHora.toLocalTime()

        // Verifica se há tolerância configurada
        val toleranciaMinutos = horarioDia.toleranciaIntervaloMaisMinutos
        if (toleranciaMinutos <= 0) {
            Timber.d("Sem tolerância de intervalo configurada")
            return horaReal
        }

        // Busca os pontos do dia para encontrar a saída anterior
        val pontosNoDia = pontoRepository.buscarPorEmpregoEData(empregoId, dataHora.toLocalDate())
            .sortedBy { it.dataHora }

        val indiceSaidaAnterior = indicePonto - 1
        if (indiceSaidaAnterior < 0 || indiceSaidaAnterior >= pontosNoDia.size) {
            Timber.w("Saída anterior não encontrada para índice %d", indicePonto)
            return horaReal
        }

        val pontoSaidaAnterior = pontosNoDia[indiceSaidaAnterior]
        val horaSaidaReal = pontoSaidaAnterior.horaConsiderada

        // Calcula hora ideal de volta = hora da saída + intervalo mínimo
        val intervaloMinimoMinutos = horarioDia.intervaloMinimoMinutos.toLong()
        val horaIdealVolta = horaSaidaReal.plusMinutes(intervaloMinimoMinutos)

        val diferencaMinutos = Duration.between(horaIdealVolta, horaReal).toMinutes()

        Timber.d(
            "Cálculo volta intervalo: saidaReal=%s, intervaloMin=%d, horaIdealVolta=%s, horaReal=%s, diferença=%dmin, tolerância=%dmin",
            horaSaidaReal, intervaloMinimoMinutos, horaIdealVolta, horaReal, diferencaMinutos, toleranciaMinutos
        )

        // Se bateu DEPOIS do ideal e dentro da tolerância
        if (diferencaMinutos > 0 && diferencaMinutos <= toleranciaMinutos) {
            Timber.d("Tolerância volta intervalo aplicada: real=%s → considerado=%s", horaReal, horaIdealVolta)
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
