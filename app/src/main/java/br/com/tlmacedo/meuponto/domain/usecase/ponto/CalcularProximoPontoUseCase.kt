package br.com.tlmacedo.meuponto.domain.usecase.ponto

import br.com.tlmacedo.meuponto.domain.model.Ponto
import javax.inject.Inject

class CalcularProximoPontoUseCase @Inject constructor() {
    fun proximoPontoIsEntrada(quantidadePontos: Int): Boolean {
        return quantidadePontos % 2 == 0
    }

    fun proximoPontoDescricao(quantidadePontos: Int): String {
        return when (quantidadePontos) {
            0 -> "Entrada"
            1 -> "Saída Intervalo"
            2 -> "Volta Intervalo"
            3 -> "Saída"
            else -> if (quantidadePontos % 2 == 0) "Entrada Extra" else "Saída Extra"
        }
    }

    operator fun invoke(pontosExistentes: List<Ponto>): ProximoPonto {
        val quantidade = pontosExistentes.size
        return ProximoPonto(
            isEntrada = proximoPontoIsEntrada(quantidade),
            descricao = proximoPontoDescricao(quantidade),
            indice = quantidade
        )
    }
}

data class ProximoPonto(
    val isEntrada: Boolean,
    val descricao: String,
    val indice: Int
)
