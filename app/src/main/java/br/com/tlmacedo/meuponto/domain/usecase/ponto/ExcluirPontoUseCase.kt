// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/usecase/ponto/ExcluirPontoUseCase.kt
package br.com.tlmacedo.meuponto.domain.usecase.ponto

import br.com.tlmacedo.meuponto.domain.model.AcaoAuditoria
import br.com.tlmacedo.meuponto.domain.model.AuditLog
import br.com.tlmacedo.meuponto.domain.model.Ponto
import br.com.tlmacedo.meuponto.domain.repository.AuditLogRepository
import br.com.tlmacedo.meuponto.domain.repository.PontoRepository
import com.google.gson.Gson
import java.time.LocalDateTime
import javax.inject.Inject

/**
 * Caso de uso para excluir um ponto.
 *
 * Todas as exclusões são registradas no log de auditoria com motivo obrigatório.
 *
 * @author Thiago
 * @since 1.0.0
 * @updated 3.5.0 - Adicionado suporte a auditoria com motivo obrigatório
 */
class ExcluirPontoUseCase @Inject constructor(
    private val pontoRepository: PontoRepository,
    private val auditLogRepository: AuditLogRepository,
    private val gson: Gson
) {
    data class Parametros(
        val pontoId: Long,
        val motivo: String
    )

    sealed class Resultado {
        data object Sucesso : Resultado()
        data class Erro(val mensagem: String) : Resultado()
        data class NaoEncontrado(val pontoId: Long) : Resultado()
        data class Validacao(val erros: List<String>) : Resultado()
    }

    suspend operator fun invoke(parametros: Parametros): Resultado {
        // Validações
        val erros = mutableListOf<String>()

        if (parametros.motivo.isBlank()) {
            erros.add("O motivo da exclusão é obrigatório")
        }

        if (parametros.motivo.length < 5) {
            erros.add("O motivo deve ter pelo menos 5 caracteres")
        }

        if (erros.isNotEmpty()) {
            return Resultado.Validacao(erros)
        }

        val ponto = pontoRepository.buscarPorId(parametros.pontoId)
            ?: return Resultado.NaoEncontrado(parametros.pontoId)

        return try {
            // Registrar auditoria ANTES de excluir
            val auditLog = AuditLog(
                entidade = "pontos",
                entidadeId = parametros.pontoId,
                acao = AcaoAuditoria.DELETE,
                motivo = parametros.motivo,
                dadosAnteriores = gson.toJson(ponto.toAuditMap()),
                dadosNovos = null,
                criadoEm = LocalDateTime.now()
            )
            auditLogRepository.inserir(auditLog)

            // Excluir ponto
            pontoRepository.excluir(ponto)

            Resultado.Sucesso
        } catch (e: Exception) {
            Resultado.Erro("Erro ao excluir ponto: ${e.message}")
        }
    }

    /**
     * Sobrecarga para manter compatibilidade com código existente.
     * @deprecated Use invoke(Parametros) com motivo obrigatório
     */
    @Deprecated("Use invoke(Parametros) com motivo obrigatório", ReplaceWith("invoke(Parametros(pontoId, motivo))"))
    suspend operator fun invoke(pontoId: Long): Resultado {
        return Resultado.Validacao(listOf("O motivo da exclusão é obrigatório"))
    }

    private fun Ponto.toAuditMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "empregoId" to empregoId,
        "dataHora" to dataHora.toString(),
        "nsr" to nsr,
        "latitude" to latitude,
        "longitude" to longitude,
        "endereco" to endereco,
        "observacao" to observacao,
        "isEditadoManualmente" to isEditadoManualmente
    )
}
