// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/model/Ponto.kt
package br.com.tlmacedo.meuponto.domain.model

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Modelo de domínio que representa um registro de ponto.
 *
 * Contém todas as informações relacionadas a uma batida de ponto,
 * incluindo data, hora, tipo e metadados de auditoria.
 *
 * @property id Identificador único do registro (0 para novos registros)
 * @property empregoId ID do emprego associado
 * @property dataHora Data e hora exata da batida de ponto
 * @property tipo Tipo da batida (ENTRADA ou SAIDA)
 * @property isEditadoManualmente Indica se o registro foi editado após criação
 * @property observacao Observação opcional do usuário sobre o registro
 * @property nsr Número Sequencial de Registro (opcional)
 * @property latitude Latitude da localização (opcional)
 * @property longitude Longitude da localização (opcional)
 * @property endereco Endereço geocodificado (opcional)
 * @property marcadorId ID do marcador/tag associado (opcional)
 * @property justificativaInconsistencia Justificativa para registro inconsistente
 * @property horaConsiderada Hora efetiva considerada após tolerância de intervalo
 * @property criadoEm Data e hora de criação do registro no sistema
 * @property atualizadoEm Data e hora da última atualização do registro
 *
 * @author Thiago
 * @since 1.0.0
 * @updated 2.0.0 - Adicionado suporte a múltiplos empregos, localização e marcadores
 */
data class Ponto(
    val id: Long = 0,
    val empregoId: Long = 1,
    val dataHora: LocalDateTime,
    val tipo: TipoPonto,
    val isEditadoManualmente: Boolean = false,
    val observacao: String? = null,
    val nsr: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val endereco: String? = null,
    val marcadorId: Long? = null,
    val justificativaInconsistencia: String? = null,
    val horaConsiderada: LocalDateTime? = null,
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

    /**
     * Retorna a hora considerada (após tolerância) ou a hora real.
     */
    val horaEfetiva: LocalTime
        get() = horaConsiderada?.toLocalTime() ?: hora

    /**
     * Retorna a hora efetiva formatada no padrão HH:mm.
     */
    val horaEfetivaFormatada: String
        get() = String.format("%02d:%02d", horaEfetiva.hour, horaEfetiva.minute)

    /**
     * Indica se houve ajuste por tolerância de intervalo.
     */
    val temAjusteToleranncia: Boolean
        get() = horaConsiderada != null && horaConsiderada != dataHora

    /**
     * Verifica se este ponto é do tipo entrada.
     */
    val isEntrada: Boolean
        get() = tipo.isEntrada

    /**
     * Verifica se este ponto é do tipo saída.
     */
    val isSaida: Boolean
        get() = !tipo.isEntrada

    /**
     * Verifica se possui localização registrada.
     */
    val temLocalizacao: Boolean
        get() = latitude != null && longitude != null

    /**
     * Verifica se possui inconsistência registrada.
     */
    val temInconsistencia: Boolean
        get() = !justificativaInconsistencia.isNullOrBlank()
}
