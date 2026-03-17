// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/model/AjusteSaldo.kt
package br.com.tlmacedo.meuponto.domain.model

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.abs

/**
 * Modelo de domínio que representa um ajuste manual no banco de horas.
 *
 * Permite adicionar ou subtrair minutos do saldo de forma controlada,
 * com justificativa obrigatória para auditoria.
 *
 * @property id Identificador único do ajuste
 * @property empregoId FK para o emprego associado
 * @property data Data à qual o ajuste está vinculado
 * @property minutos Quantidade de minutos a ajustar (positivo = adicionar, negativo = subtrair)
 * @property tipo Tipo do ajuste (manual, correção, migração, etc.)
 * @property justificativa Justificativa obrigatória para o ajuste
 * @property criadoEm Timestamp de criação
 * @property atualizadoEm Timestamp da última atualização
 *
 * @author Thiago
 * @since 2.0.0
 * @updated 11.0.0 - Adicionado tipo de ajuste e atualizadoEm
 */
data class AjusteSaldo(
    val id: Long = 0,
    val empregoId: Long,
    val data: LocalDate,
    val minutos: Int,
    val tipo: TipoAjusteSaldo = TipoAjusteSaldo.MANUAL,
    val justificativa: String,
    val criadoEm: LocalDateTime = LocalDateTime.now(),
    val atualizadoEm: LocalDateTime = LocalDateTime.now()
) {
    // ========================================================================
    // PROPRIEDADES CALCULADAS
    // ========================================================================

    /** Verifica se é um ajuste positivo (adiciona horas) */
    val isPositivo: Boolean
        get() = minutos > 0

    /** Verifica se é um ajuste negativo (subtrai horas) */
    val isNegativo: Boolean
        get() = minutos < 0

    /** Verifica se é ajuste zero (neutro) */
    val isNeutro: Boolean
        get() = minutos == 0

    /** Horas do ajuste (parte inteira) */
    val horas: Int
        get() = abs(minutos) / 60

    /** Minutos restantes (após horas) */
    val minutosRestantes: Int
        get() = abs(minutos) % 60

    // ========================================================================
    // FORMATADORES
    // ========================================================================

    private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    /** Ajuste formatado com sinal (ex: "+01:30" ou "-00:45") */
    val minutosFormatados: String
        get() {
            val sinal = if (minutos >= 0) "+" else "-"
            return "$sinal${String.format("%02d:%02d", horas, minutosRestantes)}"
        }

    /** Ajuste formatado extenso (ex: "+1h 30min" ou "-45min") */
    val minutosFormatadosExtenso: String
        get() {
            val sinal = if (minutos >= 0) "+" else "-"
            return when {
                horas == 0 -> "$sinal${minutosRestantes}min"
                minutosRestantes == 0 -> "$sinal${horas}h"
                else -> "$sinal${horas}h ${minutosRestantes}min"
            }
        }

    /** Data formatada (ex: "25/02/2026") */
    val dataFormatada: String
        get() = data.format(dateFormatter)

    /** Descrição do tipo com emoji */
    val tipoDescricao: String
        get() = "${tipo.emoji} ${tipo.descricao}"

    /** Descrição resumida para listas (ex: "✏️ +1h 30min") */
    val descricaoResumida: String
        get() = "${tipo.emoji} $minutosFormatadosExtenso"

    // ========================================================================
    // MÉTODOS AUXILIARES
    // ========================================================================

    /**
     * Cria uma cópia atualizada com novo timestamp.
     */
    fun atualizar(
        minutos: Int = this.minutos,
        tipo: TipoAjusteSaldo = this.tipo,
        justificativa: String = this.justificativa
    ): AjusteSaldo = copy(
        minutos = minutos,
        tipo = tipo,
        justificativa = justificativa,
        atualizadoEm = LocalDateTime.now()
    )

    /**
     * Converte para mapa de auditoria.
     */
    fun toAuditMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "empregoId" to empregoId,
        "data" to dataFormatada,
        "minutos" to minutos,
        "minutosFormatado" to minutosFormatadosExtenso,
        "tipo" to tipo.name,
        "tipoDescricao" to tipo.descricao,
        "justificativa" to justificativa
    )

    companion object {
        /**
         * Cria ajuste de saldo inicial (ex: ao cadastrar emprego com saldo existente).
         */
        fun criarSaldoInicial(
            empregoId: Long,
            minutos: Int,
            justificativa: String = "Saldo inicial do emprego"
        ): AjusteSaldo = AjusteSaldo(
            empregoId = empregoId,
            data = LocalDate.now(),
            minutos = minutos,
            tipo = TipoAjusteSaldo.SALDO_INICIAL,
            justificativa = justificativa
        )

        /**
         * Cria ajuste manual.
         */
        fun criarManual(
            empregoId: Long,
            data: LocalDate,
            minutos: Int,
            justificativa: String
        ): AjusteSaldo = AjusteSaldo(
            empregoId = empregoId,
            data = data,
            minutos = minutos,
            tipo = TipoAjusteSaldo.MANUAL,
            justificativa = justificativa
        )

        /**
         * Cria ajuste de correção.
         */
        fun criarCorrecao(
            empregoId: Long,
            data: LocalDate,
            minutos: Int,
            justificativa: String
        ): AjusteSaldo = AjusteSaldo(
            empregoId = empregoId,
            data = data,
            minutos = minutos,
            tipo = TipoAjusteSaldo.CORRECAO,
            justificativa = justificativa
        )

        /**
         * Cria ajuste de migração.
         */
        fun criarMigracao(
            empregoId: Long,
            minutos: Int,
            origemDescricao: String
        ): AjusteSaldo = AjusteSaldo(
            empregoId = empregoId,
            data = LocalDate.now(),
            minutos = minutos,
            tipo = TipoAjusteSaldo.MIGRACAO,
            justificativa = "Migração: $origemDescricao"
        )
    }
}
