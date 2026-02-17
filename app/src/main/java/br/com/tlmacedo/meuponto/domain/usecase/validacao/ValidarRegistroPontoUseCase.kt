// Arquivo: ValidarRegistroPontoUseCase.kt
package br.com.tlmacedo.meuponto.domain.usecase.validacao

import br.com.tlmacedo.meuponto.domain.model.Ponto
import br.com.tlmacedo.meuponto.domain.model.PontoConstants
import br.com.tlmacedo.meuponto.domain.model.proximoPontoIsEntrada
import java.time.Duration
import java.time.LocalDateTime
import javax.inject.Inject

/**
 * Caso de uso para validar o registro de um novo ponto.
 *
 * @author Thiago
 * @since 1.0.0
 * @updated 2.1.0 - Simplificado (tipo calculado por posição)
 */
class ValidarRegistroPontoUseCase @Inject constructor() {

    operator fun invoke(
        novaDataHora: LocalDateTime,
        registrosExistentes: List<Ponto>,
        intervaloMinimoMinutos: Int = 1
    ): ResultadoValidacao {
        val ordenados = registrosExistentes.sortedBy { it.dataHora }

        // 1. Verifica limite máximo de registros
        if (ordenados.size >= PontoConstants.MAX_PONTOS) {
            return ResultadoValidacao.Invalido(
                "Limite máximo de ${PontoConstants.MAX_PONTOS} registros por dia atingido"
            )
        }

        // 2. Verifica se não está no futuro
        if (novaDataHora.isAfter(LocalDateTime.now().plusMinutes(5))) {
            return ResultadoValidacao.Invalido(
                "Não é permitido registrar ponto no futuro"
            )
        }

        // 3. Verifica intervalo mínimo com registros existentes
        for (ponto in ordenados) {
            val diferenca = Duration.between(ponto.dataHora, novaDataHora).abs()
            if (diferenca.toMinutes() < intervaloMinimoMinutos) {
                return ResultadoValidacao.Invalido(
                    "Intervalo mínimo de $intervaloMinimoMinutos minuto(s) entre registros"
                )
            }
        }

        // 4. Verifica ordem cronológica se inserindo no meio
        val ultimoRegistro = ordenados.lastOrNull()
        if (ultimoRegistro != null && novaDataHora.isBefore(ultimoRegistro.dataHora)) {
            // Permitir inserção retroativa, mas validar que não quebra a ordem
            val posicaoInserir = ordenados.indexOfLast { it.dataHora.isBefore(novaDataHora) } + 1
            if (posicaoInserir < ordenados.size) {
                val proximo = ordenados[posicaoInserir]
                if (novaDataHora.isAfter(proximo.dataHora)) {
                    return ResultadoValidacao.Invalido(
                        "Horário conflita com registros existentes"
                    )
                }
            }
        }

        // Determina qual será o tipo do novo ponto
        val novoIndice = ordenados.count { it.dataHora.isBefore(novaDataHora) }
        val isEntrada = proximoPontoIsEntrada(novoIndice)
        val tipoDescricao = if (isEntrada) "Entrada" else "Saída"

        return ResultadoValidacao.Valido(tipoDescricao)
    }
}

sealed class ResultadoValidacao {
    data class Valido(val tipoDescricao: String) : ResultadoValidacao()
    data class Invalido(val mensagem: String) : ResultadoValidacao()
}
