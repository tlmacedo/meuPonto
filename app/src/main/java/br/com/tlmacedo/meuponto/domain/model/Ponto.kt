package br.com.tlmacedo.meuponto.domain.model

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Modelo de domínio que representa um registro de ponto.
 *
 * Contém todas as informações relacionadas a uma batida de ponto,
 * incluindo data, hora, tipo e metadados opcionais.
 *
 * @property id Identificador único do registro
 * @property dataHora Data e hora exata da batida
 * @property tipo Tipo da batida (ENTRADA, SAIDA_ALMOCO, RETORNO_ALMOCO, SAIDA)
 * @property editadoManualmente Indica se o registro foi editado após criação
 * @property observacao Observação opcional do usuário
 * @property criadoEm Data/hora de criação do registro
 * @property atualizadoEm Data/hora da última atualização
 *
 * @author Thiago
 * @since 1.0.0
 */
data class Ponto(
    val id: Long = 0,
    val dataHora: LocalDateTime,
    val tipo: TipoPonto,
    val editadoManualmente: Boolean = false,
    val observacao: String? = null,
    val criadoEm: LocalDateTime = LocalDateTime.now(),
    val atualizadoEm: LocalDateTime = LocalDateTime.now()
) {
    /**
     * Retorna apenas a data do registro.
     */
    val data: LocalDate
        get() = dataHora.toLocalDate()

    /**
     * Retorna apenas a hora do registro.
     */
    val hora: LocalTime
        get() = dataHora.toLocalTime()

    /**
     * Retorna a hora formatada no padrão HH:mm.
     */
    val horaFormatada: String
        get() = String.format("%02d:%02d", hora.hour, hora.minute)
}
