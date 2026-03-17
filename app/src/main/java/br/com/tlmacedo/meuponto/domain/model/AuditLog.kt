// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/model/AuditLog.kt
package br.com.tlmacedo.meuponto.domain.model

import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Modelo de domínio que representa um registro de auditoria.
 *
 * @author Thiago
 * @since 2.0.0
 * @updated 3.5.0 - Adicionado campo motivo
 * @updated 11.0.0 - Adicionadas propriedades para compatibilidade com UI
 */
data class AuditLog(
    val id: Long = 0,
    val entidade: String,
    val entidadeId: Long,
    val acao: AcaoAuditoria,
    val motivo: String? = null,
    val dadosAnteriores: String? = null,
    val dadosNovos: String? = null,
    val criadoEm: LocalDateTime = LocalDateTime.now()
) {
    // ========================================================================
    // PROPRIEDADES PARA UI (mapeamento de nomes)
    // ========================================================================

    /** Alias para entidade - usado pela UI */
    val entityType: String get() = entidade

    /** Alias para entidadeId - usado pela UI */
    val entityId: Long get() = entidadeId

    /** Alias para acao - usado pela UI */
    val action: AcaoAuditoria get() = acao

    /** Descrição do log - usa motivo ou gera automaticamente */
    val description: String
        get() = motivo ?: "${acao.descricao} de $entidade #$entidadeId"

    /** Alias para dadosAnteriores - usado pela UI */
    val oldValue: String? get() = dadosAnteriores

    /** Alias para dadosNovos - usado pela UI */
    val newValue: String? get() = dadosNovos

    /** Timestamp em milissegundos - usado pela UI */
    val timestamp: Long
        get() = criadoEm.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

    /** Data do log (para agrupamento) */
    val data: java.time.LocalDate
        get() = criadoEm.toLocalDate()

    // ========================================================================
    // ATALHOS DE VERIFICAÇÃO
    // ========================================================================

    val isInsert: Boolean get() = acao == AcaoAuditoria.INSERT
    val isUpdate: Boolean get() = acao == AcaoAuditoria.UPDATE
    val isDelete: Boolean get() = acao == AcaoAuditoria.DELETE
    val isSoftDelete: Boolean get() = acao == AcaoAuditoria.SOFT_DELETE
    val isRestore: Boolean get() = acao == AcaoAuditoria.RESTORE
    val isPermanentDelete: Boolean get() = acao == AcaoAuditoria.PERMANENT_DELETE

    /** Verifica se tem detalhes para exibir */
    val temDetalhes: Boolean get() = dadosAnteriores != null || dadosNovos != null

    companion object {
        /**
         * Cria um log de criação.
         */
        fun criar(
            entidade: String,
            entidadeId: Long,
            descricao: String,
            dadosNovos: String? = null
        ): AuditLog = AuditLog(
            entidade = entidade,
            entidadeId = entidadeId,
            acao = AcaoAuditoria.INSERT,
            motivo = descricao,
            dadosNovos = dadosNovos
        )

        /**
         * Cria um log de atualização.
         */
        fun atualizar(
            entidade: String,
            entidadeId: Long,
            descricao: String,
            dadosAnteriores: String? = null,
            dadosNovos: String? = null
        ): AuditLog = AuditLog(
            entidade = entidade,
            entidadeId = entidadeId,
            acao = AcaoAuditoria.UPDATE,
            motivo = descricao,
            dadosAnteriores = dadosAnteriores,
            dadosNovos = dadosNovos
        )

        /**
         * Cria um log de soft delete (mover para lixeira).
         */
        fun moverParaLixeira(
            entidade: String,
            entidadeId: Long,
            descricao: String,
            dadosAnteriores: String? = null
        ): AuditLog = AuditLog(
            entidade = entidade,
            entidadeId = entidadeId,
            acao = AcaoAuditoria.SOFT_DELETE,
            motivo = descricao,
            dadosAnteriores = dadosAnteriores
        )

        /**
         * Cria um log de restauração.
         */
        fun restaurar(
            entidade: String,
            entidadeId: Long,
            descricao: String,
            dadosNovos: String? = null
        ): AuditLog = AuditLog(
            entidade = entidade,
            entidadeId = entidadeId,
            acao = AcaoAuditoria.RESTORE,
            motivo = descricao,
            dadosNovos = dadosNovos
        )

        /**
         * Cria um log de exclusão permanente.
         */
        fun excluirPermanente(
            entidade: String,
            entidadeId: Long,
            descricao: String,
            dadosAnteriores: String? = null
        ): AuditLog = AuditLog(
            entidade = entidade,
            entidadeId = entidadeId,
            acao = AcaoAuditoria.PERMANENT_DELETE,
            motivo = descricao,
            dadosAnteriores = dadosAnteriores
        )
    }
}
