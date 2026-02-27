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
 *    - E está dentro da tolerância configurada
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
 * @updated 7.1.0 - Corrigido cálculo de volta do intervalo para usar hora real da saída
 */
class CalcularHoraConsideradaUseCase @Inject constructor(
    private val horarioDiaSemanaRepository: HorarioDiaSemanaRepository,
    private val pontoRepository: PontoRepository
) {
    /**
     * Calcula a hora considerada para um novo ponto.
     *
     * @param empregoId ID do emprego
     * @param dataHora Data e hora real do ponto
     * @param indicePonto Índice do ponto no dia (0 = primeira entrada, 1 = primeira saída, etc)
     * @return LocalTime com a hora considerada (pode ser igual à hora real se não houver tolerância)
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
     *
     * Se bateu ANTES do horário ideal e dentro da tolerância, considera o horário ideal.
     * Ex: Entrada ideal 08:00, tolerância 10min, bateu 07:55 → considera 08:00
     */
    private fun calcularToleranciaEntrada(
        dataHora: LocalDateTime,
        horarioDia: HorarioDiaSemana
    ): LocalTime {
        val horaReal = dataHora.toLocalTime()
        val entradaIdeal = horarioDia.entradaIdeal ?: return horaReal
        val toleranciaMinutos = horarioDia.toleranciaEntradaMinutos ?: 10 // Default 10 min

        val diferencaMinutos = Duration.between(horaReal, entradaIdeal).toMinutes()

        // Se bateu ANTES do ideal e dentro da tolerância
        if (diferencaMinutos > 0 && diferencaMinutos <= toleranciaMinutos) {
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
     *
     * A hora ideal de volta é calculada como:
     * horaIdealVolta = horaSaidaIntervaloReal + intervaloMinimoMinutos
     *
     * Se bateu DEPOIS da hora ideal de volta e dentro da tolerância, considera a hora ideal.
     *
     * Exemplo:
     * - Saiu para intervalo às 12:47 (real)
     * - Intervalo mínimo: 60 minutos
     * - Hora ideal de volta: 12:47 + 60min = 13:47
     * - Tolerância de intervalo: 20 minutos
     * - Limite máximo: 13:47 + 20min = 14:07
     * - Bateu às 14:07 → dentro da tolerância → considera 13:47
     *
     * @param empregoId ID do emprego para buscar pontos do dia
     * @param dataHora Data e hora real do ponto
     * @param horarioDia Configuração de horários do dia
     * @param indicePonto Índice do ponto (2 = volta do primeiro intervalo, 4 = volta do segundo, etc)
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

        // O índice da saída anterior é indicePonto - 1
        // Ex: indicePonto 2 (volta do intervalo) → saída no índice 1
        val indiceSaidaAnterior = indicePonto - 1
        if (indiceSaidaAnterior < 0 || indiceSaidaAnterior >= pontosNoDia.size) {
            Timber.w("Saída anterior não encontrada para índice %d", indicePonto)
            return horaReal
        }

        val pontoSaidaAnterior = pontosNoDia[indiceSaidaAnterior]
        val horaSaidaReal = pontoSaidaAnterior.horaConsiderada // Usa a hora considerada da saída

        // Calcula hora ideal de volta = hora da saída + intervalo mínimo
        val intervaloMinimoMinutos = horarioDia.intervaloMinimoMinutos.toLong()
        val horaIdealVolta = horaSaidaReal.plusMinutes(intervaloMinimoMinutos)

        // Calcula a diferença entre a hora real e a hora ideal de volta
        val diferencaMinutos = Duration.between(horaIdealVolta, horaReal).toMinutes()

        Timber.d(
            "Cálculo volta intervalo: saidaReal=%s, intervaloMin=%d, horaIdealVolta=%s, horaReal=%s, diferença=%dmin, tolerância=%dmin",
            horaSaidaReal, intervaloMinimoMinutos, horaIdealVolta, horaReal, diferencaMinutos, toleranciaMinutos
        )

        // Se bateu DEPOIS do ideal e dentro da tolerância
        if (diferencaMinutos > 0 && diferencaMinutos <= toleranciaMinutos) {
            Timber.d(
                "Tolerância volta intervalo aplicada: real=%s → considerado=%s",
                horaReal, horaIdealVolta
            )
            return horaIdealVolta
        }

        // Se bateu ANTES ou no horário ideal, usa a hora real
        // Se bateu DEPOIS e fora da tolerância, usa a hora real (chegou atrasado)
        return horaReal
    }

    /**
     * Recalcula a hora considerada para um ponto existente.
     * Útil quando a configuração de tolerância muda.
     */
    suspend fun recalcular(ponto: Ponto): Ponto {
        val pontosNoDia = pontoRepository.buscarPorEmpregoEData(ponto.empregoId, ponto.data)
            .sortedBy { it.dataHora }

        val indicePonto = pontosNoDia.indexOfFirst { it.id == ponto.id }
        if (indicePonto < 0) return ponto

        val novaHoraConsiderada = invoke(ponto.empregoId, ponto.dataHora, indicePonto)
        return ponto.comHoraConsiderada(novaHoraConsiderada)
    }

    /**
     * Recalcula todos os pontos de um dia.
     * Deve ser chamado em ordem cronológica para garantir consistência.
     *
     * @param empregoId ID do emprego
     * @param data Data dos pontos a recalcular
     * @return Lista de pontos com hora considerada recalculada
     */
    suspend fun recalcularDia(empregoId: Long, data: java.time.LocalDate): List<Ponto> {
        val pontosNoDia = pontoRepository.buscarPorEmpregoEData(empregoId, data)
            .sortedBy { it.dataHora }

        return pontosNoDia.mapIndexed { index, ponto ->
            val novaHoraConsiderada = invoke(empregoId, ponto.dataHora, index)
            ponto.comHoraConsiderada(novaHoraConsiderada)
        }
    }
}
