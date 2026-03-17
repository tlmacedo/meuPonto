// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/service/AuditService.kt
package br.com.tlmacedo.meuponto.domain.service

import br.com.tlmacedo.meuponto.domain.model.AcaoAuditoria
import br.com.tlmacedo.meuponto.domain.model.AuditLog
import br.com.tlmacedo.meuponto.domain.repository.AuditLogRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Serviço centralizado para registro de auditoria.
 *
 * Fornece métodos simplificados para logar operações CRUD
 * em qualquer entidade do sistema.
 *
 * @author Thiago
 * @since 11.0.0
 */
@Singleton
class AuditService @Inject constructor(
    private val auditLogRepository: AuditLogRepository
) {

    /**
     * Registra criação de uma entidade.
     */
    suspend fun <T> logCreate(
        entidade: String,
        entidadeId: Long,
        motivo: String,
        novoValor: T,
        serializer: (T) -> String = { it.toString() }
    ) {
        registrar(
            acao = AcaoAuditoria.INSERT,
            entidade = entidade,
            entidadeId = entidadeId,
            motivo = motivo,
            dadosAnteriores = null,
            dadosNovos = serializer(novoValor)
        )
    }

    /**
     * Registra atualização de uma entidade.
     */
    suspend fun <T> logUpdate(
        entidade: String,
        entidadeId: Long,
        motivo: String,
        valorAntigo: T?,
        valorNovo: T,
        serializer: (T) -> String = { it.toString() }
    ) {
        registrar(
            acao = AcaoAuditoria.UPDATE,
            entidade = entidade,
            entidadeId = entidadeId,
            motivo = motivo,
            dadosAnteriores = valorAntigo?.let { serializer(it) },
            dadosNovos = serializer(valorNovo)
        )
    }

    /**
     * Registra exclusão (soft delete) de uma entidade.
     */
    suspend fun logSoftDelete(
        entidade: String,
        entidadeId: Long,
        motivo: String,
        dadosAnteriores: String? = null
    ) {
        registrar(
            acao = AcaoAuditoria.SOFT_DELETE,
            entidade = entidade,
            entidadeId = entidadeId,
            motivo = motivo,
            dadosAnteriores = dadosAnteriores,
            dadosNovos = null
        )
    }

    /**
     * Registra exclusão de uma entidade.
     */
    suspend fun logDelete(
        entidade: String,
        entidadeId: Long,
        motivo: String,
        dadosAnteriores: String? = null
    ) {
        registrar(
            acao = AcaoAuditoria.DELETE,
            entidade = entidade,
            entidadeId = entidadeId,
            motivo = motivo,
            dadosAnteriores = dadosAnteriores,
            dadosNovos = null
        )
    }

    /**
     * Registra restauração de uma entidade.
     */
    suspend fun logRestore(
        entidade: String,
        entidadeId: Long,
        motivo: String
    ) {
        registrar(
            acao = AcaoAuditoria.RESTORE,
            entidade = entidade,
            entidadeId = entidadeId,
            motivo = motivo,
            dadosAnteriores = null,
            dadosNovos = null
        )
    }

    /**
     * Registra exclusão permanente de uma entidade.
     */
    suspend fun logPermanentDelete(
        entidade: String,
        entidadeId: Long,
        motivo: String
    ) {
        registrar(
            acao = AcaoAuditoria.PERMANENT_DELETE,
            entidade = entidade,
            entidadeId = entidadeId,
            motivo = motivo,
            dadosAnteriores = null,
            dadosNovos = null
        )
    }

    /**
     * Serializa um objeto para String (simples).
     */
    fun <T> toJson(value: T): String {
        return value.toString()
    }

    private suspend fun registrar(
        acao: AcaoAuditoria,
        entidade: String,
        entidadeId: Long,
        motivo: String,
        dadosAnteriores: String?,
        dadosNovos: String?
    ) {
        val log = AuditLog(
            acao = acao,
            entidade = entidade,
            entidadeId = entidadeId,
            motivo = motivo,
            dadosAnteriores = dadosAnteriores,
            dadosNovos = dadosNovos
        )
        auditLogRepository.inserir(log)
    }
}
