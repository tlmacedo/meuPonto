// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/data/repository/AuditedRepositoryBase.kt
package br.com.tlmacedo.meuponto.data.repository

import br.com.tlmacedo.meuponto.domain.service.AuditService

/**
 * Classe base para repositórios com auditoria automática de CRUD.
 *
 * Elimina a repetição do padrão: buscar-anterior → operar DAO → logAudit
 * presente em todos os repositórios do projeto.
 *
 * Uso:
 *   1. Estender esta classe passando o tipo de domínio [D] e o [entityName].
 *   2. Implementar os 5 métodos abstratos de ponte com o DAO.
 *   3. Chamar [inserirComAuditoria], [atualizarComAuditoria] e [excluirComAuditoria]
 *      nas implementações dos métodos da interface de repositório.
 *   4. Sobrescrever [motivoInserir], [motivoAtualizar] ou [motivoExcluir] para
 *      mensagens de auditoria específicas do domínio.
 *
 * @param D Tipo do modelo de domínio
 * @param auditService Serviço centralizado de auditoria
 * @param entityName Nome da entidade usado nos logs (ex: "Ponto", "Emprego")
 */
abstract class AuditedRepositoryBase<D : Any>(
    protected val auditService: AuditService,
    protected val entityName: String
) {

    // ========================================================================
    // PONTE COM O DAO — implementações fazem a conversão domain ↔ entity
    // ========================================================================

    protected abstract suspend fun daoInserir(domain: D): Long
    protected abstract suspend fun daoBuscarPorId(id: Long): D?
    protected abstract suspend fun daoAtualizar(domain: D)
    protected abstract suspend fun daoExcluir(domain: D)
    protected abstract fun getEntityId(domain: D): Long

    // ========================================================================
    // SERIALIZAÇÃO PARA AUDITORIA
    // ========================================================================

    protected abstract fun D.toAuditMap(): Map<String, Any?>

    // ========================================================================
    // MOTIVOS DE AUDITORIA — sobrescreva para mensagens específicas do domínio
    // ========================================================================

    protected open fun motivoInserir(domain: D): String = "$entityName criado"
    protected open fun motivoAtualizar(domain: D): String = "$entityName atualizado"
    protected open fun motivoExcluir(domain: D): String = "$entityName excluído"

    // ========================================================================
    // CRUD COM AUDITORIA
    // ========================================================================

    /**
     * Insere a entidade no DAO e registra log de criação.
     * Retorna o ID gerado.
     */
    protected suspend fun inserirComAuditoria(domain: D): Long {
        val id = daoInserir(domain)
        auditService.logCreate(
            entidade = entityName,
            entidadeId = id,
            motivo = motivoInserir(domain),
            novoValor = domain,
            serializer = { auditService.toJson(it.toAuditMap()) }
        )
        return id
    }

    /**
     * Busca o estado anterior, atualiza no DAO e registra log de atualização.
     */
    protected suspend fun atualizarComAuditoria(domain: D) {
        val id = getEntityId(domain)
        val anterior = daoBuscarPorId(id)
        daoAtualizar(domain)
        auditService.logUpdate(
            entidade = entityName,
            entidadeId = id,
            motivo = motivoAtualizar(domain),
            valorAntigo = anterior,
            valorNovo = domain,
            serializer = { auditService.toJson(it.toAuditMap()) }
        )
    }

    /**
     * Registra log de exclusão permanente e remove a entidade do DAO.
     * O log é feito antes da exclusão para preservar o contexto.
     */
    protected suspend fun excluirComAuditoria(domain: D) {
        auditService.logPermanentDelete(
            entidade = entityName,
            entidadeId = getEntityId(domain),
            motivo = motivoExcluir(domain)
        )
        daoExcluir(domain)
    }

    /**
     * Variante de exclusão por ID: busca a entidade antes de excluir
     * para compor uma mensagem de auditoria mais descritiva.
     */
    protected suspend fun excluirPorIdComAuditoria(
        id: Long,
        daoExcluirPorId: suspend (Long) -> Unit
    ) {
        val domain = daoBuscarPorId(id)
        auditService.logPermanentDelete(
            entidade = entityName,
            entidadeId = id,
            motivo = domain?.let { motivoExcluir(it) } ?: "$entityName excluído"
        )
        daoExcluirPorId(id)
    }
}
