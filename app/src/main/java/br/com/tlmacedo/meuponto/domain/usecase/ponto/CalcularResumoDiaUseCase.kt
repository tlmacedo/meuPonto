// Arquivo: CalcularResumoDiaUseCase.kt
package br.com.tlmacedo.meuponto.domain.usecase.ponto

import br.com.tlmacedo.meuponto.domain.model.Ponto
import br.com.tlmacedo.meuponto.domain.model.ResumoDia
import java.time.Duration
import java.time.LocalDate
import javax.inject.Inject

/**
 * Caso de uso para calcular o resumo de um dia de trabalho.
 *
 * Calcula as horas trabalhadas considerando pares de entrada/saída
 * baseado na posição dos pontos ordenados por dataHora.
 *
 * @author Thiago
 * @since 1.0.0
 * @updated 2.1.0 - Simplificado (tipo calculado por posição)
 */
class CalcularResumoDiaUseCase @Inject constructor() {

    operator fun invoke(
        pontos: List<Ponto>,
        data: LocalDate = LocalDate.now(),
        cargaHorariaDiariaMinutos: Int = 480
    ): ResumoDia {
        val pontosOrdenados = pontos.sortedBy { it.dataHora }
        var totalTrabalhado = Duration.ZERO

        // Calcula tempo trabalhado em pares (índice par = entrada, ímpar = saída)
        var i = 0
        while (i < pontosOrdenados.size - 1) {
            val entrada = pontosOrdenados[i]      // índice par = entrada
            val saida = pontosOrdenados[i + 1]    // índice ímpar = saída
            
            totalTrabalhado = totalTrabalhado.plus(
                Duration.between(entrada.dataHora, saida.dataHora)
            )
            i += 2
        }

        return ResumoDia(
            data = data,
            pontos = pontosOrdenados,
            horasTrabalhadas = totalTrabalhado,
            cargaHorariaDiaria = Duration.ofMinutes(cargaHorariaDiariaMinutos.toLong())
        )
    }
}
