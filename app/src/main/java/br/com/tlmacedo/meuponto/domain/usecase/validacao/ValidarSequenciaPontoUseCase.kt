// Arquivo: ValidarSequenciaPontoUseCase.kt
package br.com.tlmacedo.meuponto.domain.usecase.validacao

import br.com.tlmacedo.meuponto.domain.model.Ponto
import br.com.tlmacedo.meuponto.domain.model.PontoConstants
import javax.inject.Inject

/**
 * Caso de uso para validar sequência de pontos.
 * Com tipo calculado por posição, a sequência é sempre válida.
 *
 * @author Thiago
 * @since 2.1.0
 */
class ValidarSequenciaPontoUseCase @Inject constructor() {

    sealed class ResultadoValidacaoSequencia {
        object Valida : ResultadoValidacaoSequencia()
        object QuantidadeExcedida : ResultadoValidacaoSequencia()
        object SemRegistros : ResultadoValidacaoSequencia()
    }

    operator fun invoke(pontos: List<Ponto>): ResultadoValidacaoSequencia {
        if (pontos.isEmpty()) {
            return ResultadoValidacaoSequencia.SemRegistros
        }

        if (pontos.size > PontoConstants.MAX_PONTOS) {
            return ResultadoValidacaoSequencia.QuantidadeExcedida
        }

        return ResultadoValidacaoSequencia.Valida
    }
}
