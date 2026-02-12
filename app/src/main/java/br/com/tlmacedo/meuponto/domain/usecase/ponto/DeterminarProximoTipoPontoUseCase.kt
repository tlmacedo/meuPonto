// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/usecase/ponto/DeterminarProximoTipoPontoUseCase.kt
package br.com.tlmacedo.meuponto.domain.usecase.ponto

import br.com.tlmacedo.meuponto.domain.model.Ponto
import br.com.tlmacedo.meuponto.domain.model.TipoPonto
import javax.inject.Inject

/**
 * Caso de uso para determinar o próximo tipo de ponto esperado.
 *
 * Analisa os pontos do dia e determina se o próximo registro
 * deve ser uma ENTRADA ou SAÍDA, garantindo alternância correta.
 *
 * @author Thiago
 * @since 1.0.0
 */
class DeterminarProximoTipoPontoUseCase @Inject constructor() {

    /**
     * Determina o próximo tipo de ponto com base nos registros existentes.
     *
     * @param pontosHoje Lista de pontos do dia atual
     * @return TipoPonto esperado (ENTRADA ou SAIDA)
     */
    operator fun invoke(pontosHoje: List<Ponto>): TipoPonto {
        return if (pontosHoje.size % 2 == 0) {
            TipoPonto.ENTRADA
        } else {
            TipoPonto.SAIDA
        }
    }
}
