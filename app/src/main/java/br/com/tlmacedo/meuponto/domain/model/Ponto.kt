// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/model/Ponto.kt
package br.com.tlmacedo.meuponto.domain.model

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Modelo de domínio que representa um registro de ponto.
 *
 * IMPORTANTE - Diferença entre dataHora e horaConsiderada:
 * - dataHora: Hora REAL que o usuário bateu o ponto (ex: 13:24)
 * - horaConsiderada: Hora que será CONSIDERADA para cálculos (ex: 13:00, com tolerância aplicada)
 *
 * A horaConsiderada é calculada no momento do registro e persistida no banco.
 * Isso garante consistência em todos os cálculos (tela principal, histórico, relatórios).
 *
 * @property id Identificador único do registro
 * @property empregoId FK para o emprego associado
 * @property dataHora Data e hora REAL do registro (hora batida)
 * @property horaConsiderada hora CONSIDERADA (com tolerância aplicada)
 * @property nsr Número Sequencial de Registro (para compliance REP)
 * @property observacao Observação livre do usuário
 * @property isEditadoManualmente Indica se foi editado após registro inicial
 * @property latitude Latitude da localização (se habilitado)
 * @property longitude Longitude da localização (se habilitado)
 * @property endereco Endereço formatado da localização
 * @property marcadorId ID do marcador/tag associado
 * @property justificativaInconsistencia Justificativa para pontos com problema
 * @property fotoComprovantePath Caminho da foto do comprovante (opcional)
 * @property criadoEm Timestamp de criação
 * @property atualizadoEm Timestamp da última atualização
 * @property isDeleted Flag de soft delete (true = na lixeira)
 * @property deletedAt Timestamp de quando foi excluído (null = não excluído)
 * @property updatedAt Timestamp de atualização em milissegundos
 *
 * @author Thiago
 * @since 1.0.0
 * @updated 9.0.0 - Adicionado campo fotoComprovantePath para foto do comprovante
 * @updated 11.0.0 - Adicionado suporte a soft delete (isDeleted, deletedAt, updatedAt)
 */
data class Ponto(
    val id: Long = 0,
    val empregoId: Long,
    val dataHora: LocalDateTime,
    val horaConsiderada: LocalTime,
    val nsr: String? = null,
    val observacao: String? = null,
    val isEditadoManualmente: Boolean = false,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val endereco: String? = null,
    val marcadorId: Long? = null,
    val justificativaInconsistencia: String? = null,
    val fotoComprovantePath: String? = null,
    val criadoEm: LocalDateTime = LocalDateTime.now(),
    val atualizadoEm: LocalDateTime = LocalDateTime.now(),
    // === Soft Delete ===
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null,
    val updatedAt: Long = System.currentTimeMillis()
) {
    // ========================================================================
    // PROPRIEDADES DERIVADAS
    // ========================================================================

    /** Data do registro (extraída de dataHora) */
    val data: LocalDate
        get() = dataHora.toLocalDate()

    /** Hora do registro REAL (extraída de dataHora) */
    val hora: LocalTime
        get() = dataHora.toLocalTime()

    /** Hora CONSIDERADA para cálculos (extraída de horaConsiderada) */
    val horaEfetiva: LocalTime
        get() = horaConsiderada

    /**
     * Data e hora efetiva para cálculos.
     * Retorna horaConsiderada (que já inclui tolerância aplicada).
     */
    val dataHoraEfetiva: LocalDateTime
        get() = LocalDateTime.of(data, horaEfetiva)

    /**
     * Verifica se houve ajuste de tolerância (hora considerada diferente da hora real).
     */
    val temAjusteTolerancia: Boolean
        get() = hora != horaConsiderada

    /**
     * Diferença em minutos entre hora real e hora considerada.
     * Positivo = entrada adiantada (ex: chegou 13:24, considerado 13:00 = -24min)
     * Negativo = entrada atrasada (ex: chegou 07:55, considerado 08:00 = +5min)
     */
    val diferencaToleranciaMinutos: Int
        get() = java.time.Duration.between(horaConsiderada, hora).toMinutes().toInt()

    /** Verifica se tem localização registrada */
    val temLocalizacao: Boolean
        get() = latitude != null && longitude != null

    /** Verifica se tem NSR */
    val temNsr: Boolean
        get() = !nsr.isNullOrBlank()

    /** Verifica se tem observação */
    val temObservacao: Boolean
        get() = !observacao.isNullOrBlank()

    /** Verifica se tem marcador */
    val temMarcador: Boolean
        get() = marcadorId != null

    /** Verifica se tem justificativa de inconsistência */
    val temJustificativa: Boolean
        get() = !justificativaInconsistencia.isNullOrBlank()

    /** Verifica se tem foto do comprovante */
    val temFotoComprovante: Boolean
        get() = !fotoComprovantePath.isNullOrBlank()

    /** Verifica se está na lixeira */
    val estaNaLixeira: Boolean
        get() = isDeleted

    // ========================================================================
    // FORMATADORES
    // ========================================================================

    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")

    /** Hora REAL formatada (ex: "13:24") */
    val horaFormatada: String
        get() = hora.format(timeFormatter)

    /** Hora CONSIDERADA formatada (ex: "13:00") */
    val horaConsideradaFormatada: String
        get() = horaEfetiva.format(timeFormatter)

    /** Data formatada (ex: "25/02/2026") */
    val dataFormatada: String
        get() = data.format(dateFormatter)

    /** Data e hora REAL formatadas (ex: "25/02/2026 13:24") */
    val dataHoraFormatada: String
        get() = dataHora.format(dateTimeFormatter)

    /**
     * Descrição da tolerância aplicada.
     * Ex: "Batido: 13:24 → Considerado: 13:00"
     */
    val descricaoTolerancia: String?
        get() = if (temAjusteTolerancia) {
            "Batido: $horaFormatada → Considerado: $horaConsideradaFormatada"
        } else null

    // ========================================================================
    // MÉTODOS AUXILIARES
    // ========================================================================

    /**
     * Cria uma cópia com horaConsiderada atualizada.
     * Útil para recalcular tolerância quando configuração muda.
     */
    fun comHoraConsiderada(novaHoraConsiderada: LocalTime): Ponto {
        return copy(
            horaConsiderada = novaHoraConsiderada,
            atualizadoEm = LocalDateTime.now(),
            updatedAt = System.currentTimeMillis()
        )
    }

    /**
     * Cria uma cópia marcada como editada manualmente.
     */
    fun marcarComoEditado(): Ponto {
        return copy(
            isEditadoManualmente = true,
            atualizadoEm = LocalDateTime.now(),
            updatedAt = System.currentTimeMillis()
        )
    }

    /**
     * Cria uma cópia com foto do comprovante.
     */
    fun comFotoComprovante(path: String?): Ponto {
        return copy(
            fotoComprovantePath = path,
            atualizadoEm = LocalDateTime.now(),
            updatedAt = System.currentTimeMillis()
        )
    }

    /**
     * Marca o ponto como excluído (soft delete).
     */
    fun marcarComoExcluido(): Ponto {
        val agora = System.currentTimeMillis()
        return copy(
            isDeleted = true,
            deletedAt = agora,
            updatedAt = agora
        )
    }

    /**
     * Restaura o ponto da lixeira.
     */
    fun restaurar(): Ponto {
        return copy(
            isDeleted = false,
            deletedAt = null,
            updatedAt = System.currentTimeMillis()
        )
    }

    /**
     * Converte o ponto para um mapa de auditoria.
     * Usado para registrar valores anteriores/novos em logs de auditoria.
     */
    fun toAuditMap(): Map<String, Any?> = buildMap {
        put("id", id)
        put("empregoId", empregoId)
        put("data", dataFormatada)
        put("horaReal", horaFormatada)
        put("horaConsiderada", horaConsideradaFormatada)
        nsr?.let { put("nsr", it) }
        observacao?.let { put("observacao", it) }
        if (isEditadoManualmente) put("editadoManualmente", true)
        latitude?.let { put("latitude", it) }
        longitude?.let { put("longitude", it) }
        endereco?.let { put("endereco", it) }
        marcadorId?.let { put("marcadorId", it) }
        justificativaInconsistencia?.let { put("justificativa", it) }
        fotoComprovantePath?.let { put("fotoComprovante", it) }
        if (isDeleted) put("naLixeira", true)
    }

    /**
     * Converte para string formatada para auditoria.
     */
    fun toAuditString(): String {
        return toAuditMap().entries.joinToString("\n") { (key, value) ->
            "$key: $value"
        }
    }

    companion object {
        /**
         * Cria um novo ponto com horaConsiderada igual a dataHora (sem tolerância).
         * Use quando não há configuração de tolerância ou para pontos de saída.
         */
        fun criar(
            empregoId: Long,
            dataHora: LocalDateTime,
            nsr: String? = null,
            observacao: String? = null,
            latitude: Double? = null,
            longitude: Double? = null,
            endereco: String? = null,
            marcadorId: Long? = null,
            fotoComprovantePath: String? = null
        ): Ponto {
            val agora = LocalDateTime.now()
            return Ponto(
                empregoId = empregoId,
                dataHora = dataHora,
                horaConsiderada = dataHora.toLocalTime(),
                nsr = nsr,
                observacao = observacao,
                latitude = latitude,
                longitude = longitude,
                endereco = endereco,
                marcadorId = marcadorId,
                fotoComprovantePath = fotoComprovantePath,
                criadoEm = agora,
                atualizadoEm = agora
            )
        }

        /**
         * Cria um novo ponto com tolerância já aplicada.
         */
        fun criarComTolerancia(
            empregoId: Long,
            dataHora: LocalDateTime,
            horaConsiderada: LocalTime,
            nsr: String? = null,
            observacao: String? = null,
            latitude: Double? = null,
            longitude: Double? = null,
            endereco: String? = null,
            marcadorId: Long? = null,
            fotoComprovantePath: String? = null
        ): Ponto {
            val agora = LocalDateTime.now()
            return Ponto(
                empregoId = empregoId,
                dataHora = dataHora,
                horaConsiderada = horaConsiderada,
                nsr = nsr,
                observacao = observacao,
                latitude = latitude,
                longitude = longitude,
                endereco = endereco,
                marcadorId = marcadorId,
                fotoComprovantePath = fotoComprovantePath,
                criadoEm = agora,
                atualizadoEm = agora
            )
        }
    }
}
