// Arquivo: DeterminarProximoTipoPontoUseCase.kt
package br.com.tlmacedo.meuponto.domain.usecase.ponto

import br.com.tlmacedo.meuponto.domain.model.Ponto
import br.com.tlmacedo.meuponto.domain.model.proximoPontoDescricao
import br.com.tlmacedo.meuponto.domain.model.proximoPontoIsEntrada
import javax.inject.Inject

/**
 * Caso de uso para determinar o próximo tipo de ponto esperado.
 *
 * @author Thiago
 * @since 1.0.0
 * @updated 2.1.0 - Simplificado (tipo calculado por quantidade)
 */
class DeterminarProximoTipoPontoUseCase @Inject constructor() {

    /**
     * Determina o próximo tipo de ponto esperado.
     *
     * @param pontosExistentes Lista de pontos já registrados no dia
     * @return ProximoPonto com informações do próximo registro esperado
     */
    operator fun invoke(pontosExistentes: List<Ponto>): ProximoPonto {
        val quantidade = pontosExistentes.size
        return ProximoPonto(
            isEntrada = proximoPontoIsEntrada(quantidade),
            descricao = proximoPontoDescricao(quantidade),
            indice = quantidade
        )
    }
}

/**
 * Informações sobre o próximo ponto esperado.
 */
data class ProximoPonto(
    val isEntrada: Boolean,
    val descricao: String,
    val indice: Int
)
