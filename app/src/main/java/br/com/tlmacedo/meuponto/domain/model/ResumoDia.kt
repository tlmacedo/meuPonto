// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/model/ResumoDia.kt
package br.com.tlmacedo.meuponto.domain.model

import java.time.Duration
import java.time.LocalDate
import kotlin.math.abs

/**
 * Modelo que representa o resumo de um dia de trabalho.
 *
 * Contém informações sobre horas trabalhadas, saldo do dia
 * e indicadores de status para o dia específico.
 *
 * @property data Data do resumo
 * @property pontos Lista de pontos registrados no dia
 * @property horasTrabalhadas Duração total trabalhada no dia
 * @property cargaHorariaDiaria Carga horária esperada para o dia
 *
 * @author Thiago
 * @since 1.0.0
 */
data class ResumoDia(
    val data: LocalDate,
    val pontos: List<Ponto> = emptyList(),
    val horasTrabalhadas: Duration = Duration.ZERO,
    val cargaHorariaDiaria: Duration = Duration.ofHours(8)
) {
    /**
     * Saldo do dia (positivo = hora extra, negativo = deve horas).
     */
    val saldoDia: Duration
        get() = horasTrabalhadas.minus(cargaHorariaDiaria)

    /**
     * Verifica se o dia tem saldo positivo.
     */
    val temSaldoPositivo: Boolean
        get() = !saldoDia.isNegative && !saldoDia.isZero

    /**
     * Verifica se o dia tem saldo negativo.
     */
    val temSaldoNegativo: Boolean
        get() = saldoDia.isNegative

    /**
     * Verifica se a jornada está completa (número par de pontos).
     */
    val jornadaCompleta: Boolean
        get() = pontos.isNotEmpty() && pontos.size % 2 == 0

    /**
     * Verifica se há inconsistência nos pontos.
     */
    val temInconsistencia: Boolean
        get() {
            if (pontos.isEmpty()) return false
            val pontosOrdenados = pontos.sortedBy { it.dataHora }
            for (i in pontosOrdenados.indices) {
                val tipoEsperado = if (i % 2 == 0) TipoPonto.ENTRADA else TipoPonto.SAIDA
                if (pontosOrdenados[i].tipo != tipoEsperado) return true
            }
            return false
        }

    /**
     * Próximo tipo de ponto esperado.
     */
    val proximoTipoEsperado: TipoPonto
        get() = if (pontos.size % 2 == 0) TipoPonto.ENTRADA else TipoPonto.SAIDA

    /**
     * Lista de intervalos entre pontos de entrada e saída.
     */
    val intervalos: List<IntervaloPonto>
        get() {
            val pontosOrdenados = pontos.sortedBy { it.dataHora }
            val lista = mutableListOf<IntervaloPonto>()
            
            var i = 0
            while (i < pontosOrdenados.size) {
                val entrada = pontosOrdenados.getOrNull(i)
                val saida = pontosOrdenados.getOrNull(i + 1)
                
                if (entrada != null) {
                    lista.add(
                        IntervaloPonto(
                            entrada = entrada,
                            saida = saida,
                            duracao = if (saida != null) {
                                Duration.between(entrada.dataHora, saida.dataHora)
                            } else null
                        )
                    )
                }
                i += 2
            }
            return lista
        }
}

/**
 * Representa um intervalo entre entrada e saída.
 */
data class IntervaloPonto(
    val entrada: Ponto,
    val saida: Ponto?,
    val duracao: Duration?
) {
    /**
     * Verifica se o intervalo está aberto (sem saída).
     */
    val aberto: Boolean
        get() = saida == null

    /**
     * Formata a duração do intervalo (compatível com API 26+).
     */
    fun formatarDuracao(): String {
        return duracao?.let {
            val totalMinutos = it.toMinutes()
            val horas = totalMinutos / 60
            val minutos = abs(totalMinutos % 60)
            "${horas}h${minutos.toString().padStart(2, '0')}min"
        } ?: "Em andamento..."
    }
}
