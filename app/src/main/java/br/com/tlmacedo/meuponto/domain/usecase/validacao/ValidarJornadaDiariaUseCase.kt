// Arquivo: ValidarJornadaDiariaUseCase.kt
package br.com.tlmacedo.meuponto.domain.usecase.validacao

import br.com.tlmacedo.meuponto.domain.model.Ponto
import br.com.tlmacedo.meuponto.domain.model.PontoConstants
import java.time.Duration
import javax.inject.Inject

/**
 * Caso de uso para validar jornada diária.
 *
 * @author Thiago
 * @since 1.0.0
 * @updated 2.1.0 - Tipo calculado por posição
 */
class ValidarJornadaDiariaUseCase @Inject constructor() {

    sealed class ResultadoValidacaoJornada {
        object Valida : ResultadoValidacaoJornada()
        data class JornadaExcedida(val minutosExcedidos: Long) : ResultadoValidacaoJornada()
        data class IntervaloInsuficiente(val minutosIntervalo: Long, val minimoNecessario: Long) : ResultadoValidacaoJornada()
        object DadosInsuficientes : ResultadoValidacaoJornada()
    }

    operator fun invoke(
        pontos: List<Ponto>,
        jornadaMaximaMinutos: Long = 600L,
        intervaloMinimoMinutos: Long = 60L
    ): ResultadoValidacaoJornada {
        if (pontos.size < PontoConstants.MIN_PONTOS) {
            return ResultadoValidacaoJornada.DadosInsuficientes
        }

        val ordenados = pontos.sortedBy { it.hora }
        var totalTrabalhado = 0L
        var i = 0

        // Calcula tempo trabalhado
        while (i < ordenados.size - 1) {
            val entrada = ordenados[i]
            val saida = ordenados[i + 1]
            totalTrabalhado += Duration.between(entrada.hora, saida.hora).toMinutes()
            i += 2
        }

        // Verifica jornada máxima
        if (totalTrabalhado > jornadaMaximaMinutos) {
            return ResultadoValidacaoJornada.JornadaExcedida(totalTrabalhado - jornadaMaximaMinutos)
        }

        // Verifica intervalo mínimo (se tem 4+ pontos)
        if (pontos.size >= PontoConstants.PONTOS_IDEAL) {
            var totalIntervalo = 0L
            var j = 1
            while (j < ordenados.size - 1) {
                val saida = ordenados[j]
                val entrada = ordenados[j + 1]
                totalIntervalo += Duration.between(saida.hora, entrada.hora).toMinutes()
                j += 2
            }

            if (totalIntervalo < intervaloMinimoMinutos) {
                return ResultadoValidacaoJornada.IntervaloInsuficiente(
                    minutosIntervalo = totalIntervalo,
                    minimoNecessario = intervaloMinimoMinutos
                )
            }
        }

        return ResultadoValidacaoJornada.Valida
    }
}
