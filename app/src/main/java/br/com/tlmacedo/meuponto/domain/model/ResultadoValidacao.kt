// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/model/ResultadoValidacao.kt
package br.com.tlmacedo.meuponto.domain.model

import java.time.LocalDateTime

/**
 * Resultado de uma validação de registro de ponto.
 *
 * Encapsula o resultado completo de uma validação, incluindo se foi
 * aprovada, as inconsistências encontradas, e mensagens para o usuário.
 *
 * @property isValido Indica se o registro passou na validação
 * @property inconsistencias Lista de inconsistências encontradas
 * @property mensagens Mensagens informativas para o usuário
 * @property pontoValidado Ponto com possíveis ajustes aplicados (tolerância, etc.)
 * @property validadoEm Data/hora em que a validação foi realizada
 *
 * @author Thiago
 * @since 2.0.0
 */
data class ResultadoValidacao(
    val isValido: Boolean,
    val inconsistencias: List<InconsistenciaDetectada> = emptyList(),
    val mensagens: List<String> = emptyList(),
    val pontoValidado: Ponto? = null,
    val validadoEm: LocalDateTime = LocalDateTime.now()
) {
    /**
     * Verifica se existem inconsistências.
     */
    val temInconsistencias: Boolean
        get() = inconsistencias.isNotEmpty()

    /**
     * Verifica se existem inconsistências bloqueantes.
     */
    val temInconsistenciasBloqueantes: Boolean
        get() = inconsistencias.any { it.inconsistencia.isBloqueante }

    /**
     * Retorna apenas as inconsistências bloqueantes.
     */
    val inconsistenciasBloqueantes: List<InconsistenciaDetectada>
        get() = inconsistencias.filter { it.inconsistencia.isBloqueante }

    /**
     * Retorna apenas os alertas (inconsistências não bloqueantes).
     */
    val alertas: List<InconsistenciaDetectada>
        get() = inconsistencias.filter { !it.inconsistencia.isBloqueante }

    /**
     * Verifica se existem apenas alertas (sem bloqueantes).
     */
    val temApenasAlertas: Boolean
        get() = temInconsistencias && !temInconsistenciasBloqueantes

    /**
     * Conta o total de inconsistências por severidade.
     */
    val contadorPorSeveridade: Map<Inconsistencia.Severidade, Int>
        get() = inconsistencias.groupingBy { it.inconsistencia.severidade }.eachCount()

    /**
     * Retorna a mensagem principal de erro ou sucesso.
     */
    val mensagemPrincipal: String
        get() = when {
            isValido && !temInconsistencias -> "Registro válido"
            isValido && temApenasAlertas -> "Registro válido com alertas"
            !isValido && temInconsistenciasBloqueantes ->
                inconsistenciasBloqueantes.first().inconsistencia.descricao

            else -> mensagens.firstOrNull() ?: "Validação concluída"
        }

    companion object {
        /**
         * Cria um resultado de validação bem-sucedida.
         *
         * @param ponto Ponto validado (com possíveis ajustes)
         * @param alertas Lista opcional de alertas não bloqueantes
         * @return ResultadoValidacao indicando sucesso
         */
        fun sucesso(
            ponto: Ponto,
            alertas: List<InconsistenciaDetectada> = emptyList()
        ): ResultadoValidacao {
            return ResultadoValidacao(
                isValido = true,
                inconsistencias = alertas,
                pontoValidado = ponto
            )
        }

        /**
         * Cria um resultado de validação com falha.
         *
         * @param inconsistencias Lista de inconsistências encontradas
         * @param mensagem Mensagem opcional adicional
         * @return ResultadoValidacao indicando falha
         */
        fun falha(
            inconsistencias: List<InconsistenciaDetectada>,
            mensagem: String? = null
        ): ResultadoValidacao {
            return ResultadoValidacao(
                isValido = false,
                inconsistencias = inconsistencias,
                mensagens = listOfNotNull(mensagem)
            )
        }

        /**
         * Cria um resultado de validação com falha para uma única inconsistência.
         *
         * @param inconsistencia Inconsistência detectada
         * @param detalhes Detalhes adicionais sobre a inconsistência
         * @return ResultadoValidacao indicando falha
         */
        fun falha(
            inconsistencia: Inconsistencia,
            detalhes: String? = null
        ): ResultadoValidacao {
            return falha(
                inconsistencias = listOf(
                    InconsistenciaDetectada(
                        inconsistencia = inconsistencia,
                        detalhes = detalhes
                    )
                )
            )
        }
    }
}

/**
 * Representa uma inconsistência detectada durante a validação.
 *
 * Combina o tipo de inconsistência com informações contextuais
 * específicas da ocorrência.
 *
 * @property inconsistencia Tipo de inconsistência detectada
 * @property detalhes Informações adicionais sobre a ocorrência
 * @property pontoRelacionadoId ID do ponto relacionado à inconsistência (se houver)
 * @property detectadoEm Data/hora da detecção
 *
 * @author Thiago
 * @since 2.0.0
 */
data class InconsistenciaDetectada(
    val inconsistencia: Inconsistencia,
    val detalhes: String? = null,
    val pontoRelacionadoId: Long? = null,
    val detectadoEm: LocalDateTime = LocalDateTime.now()
) {
    /**
     * Descrição completa da inconsistência para exibição.
     */
    val descricaoCompleta: String
        get() = buildString {
            append(inconsistencia.descricao)
            detalhes?.let { append(": $it") }
        }

    /**
     * Verifica se esta inconsistência é bloqueante.
     */
    val isBloqueante: Boolean
        get() = inconsistencia.isBloqueante

    /**
     * Retorna a severidade da inconsistência.
     */
    val severidade: Inconsistencia.Severidade
        get() = inconsistencia.severidade
}
