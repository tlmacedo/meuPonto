// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/usecase/ponto/CalcularResumoDiaUseCase.kt
package br.com.tlmacedo.meuponto.domain.usecase.ponto

import br.com.tlmacedo.meuponto.domain.model.Ponto
import br.com.tlmacedo.meuponto.domain.model.ResumoDia
import br.com.tlmacedo.meuponto.domain.model.TipoPonto
import java.time.Duration
import java.time.LocalDate
import javax.inject.Inject

/**
 * Caso de uso para calcular o resumo de um dia de trabalho.
 *
 * Calcula as horas trabalhadas considerando pares de entrada/saída
 * e retorna um resumo completo do dia.
 *
 * @author Thiago
 * @since 1.0.0
 */
class CalcularResumoDiaUseCase @Inject constructor() {

    /**
     * Calcula o resumo do dia com base nos pontos registrados.
     *
     * @param pontos Lista de pontos do dia
     * @param data Data do resumo
     * @param cargaHorariaDiariaMinutos Carga horária diária em minutos
     * @return ResumoDia com o cálculo completo
     */
    operator fun invoke(
        pontos: List<Ponto>,
        data: LocalDate = LocalDate.now(),
        cargaHorariaDiariaMinutos: Int = 480 // 8 horas
    ): ResumoDia {
        val pontosOrdenados = pontos.sortedBy { it.dataHora }
        var totalTrabalhado = Duration.ZERO

        // Calcula tempo trabalhado em pares de entrada/saída
        var i = 0
        while (i < pontosOrdenados.size - 1) {
            val atual = pontosOrdenados[i]
            val proximo = pontosOrdenados[i + 1]

            // Se entrada seguida de saída, calcula a diferença
            if (atual.tipo == TipoPonto.ENTRADA && proximo.tipo == TipoPonto.SAIDA) {
                totalTrabalhado = totalTrabalhado.plus(
                    Duration.between(atual.dataHora, proximo.dataHora)
                )
            }
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
