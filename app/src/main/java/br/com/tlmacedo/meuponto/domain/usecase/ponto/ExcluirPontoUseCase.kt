// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/usecase/ponto/ExcluirPontoUseCase.kt
package br.com.tlmacedo.meuponto.domain.usecase.ponto

import br.com.tlmacedo.meuponto.domain.model.AcaoAuditoria
import br.com.tlmacedo.meuponto.domain.model.AuditLog
import br.com.tlmacedo.meuponto.domain.model.Ponto
import br.com.tlmacedo.meuponto.domain.repository.AuditLogRepository
import br.com.tlmacedo.meuponto.domain.repository.PontoRepository
import br.com.tlmacedo.meuponto.util.foto.ImageTrashManager
import br.com.tlmacedo.meuponto.util.foto.TrashResult
import com.google.gson.Gson
import timber.log.Timber
import java.time.LocalDateTime
import javax.inject.Inject

/**
 * Caso de uso para excluir um ponto.
 *
 * Funcionalidades:
 * - Todas as exclusões são registradas no log de auditoria com motivo obrigatório
 * - Imagens de comprovante são movidas para lixeira (não deletadas permanentemente)
 * - Dados completos do ponto são salvos no log para possível rollback
 *
 * @author Thiago
 * @since 1.0.0
 * @updated 3.5.0 - Adicionado suporte a auditoria com motivo obrigatório
 * @updated 11.0.0 - Integração com lixeira de imagens e suporte a rollback
 */
class ExcluirPontoUseCase @Inject constructor(
    private val pontoRepository: PontoRepository,
    private val auditLogRepository: AuditLogRepository,
    private val imageTrashManager: ImageTrashManager,
    private val gson: Gson
) {
    data class Parametros(
        val pontoId: Long,
        val motivo: String
    )

    sealed class Resultado {
        data class Sucesso(
            val pontoId: Long,
            val imagemMovidaParaLixeira: Boolean = false,
            val trashPath: String? = null
        ) : Resultado()
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
            var imagemMovidaParaLixeira = false
            var trashPath: String? = null

            // 1. Mover imagem para lixeira (se existir)
            if (ponto.temFotoComprovante && ponto.fotoComprovantePath != null) {
                val trashResult = imageTrashManager.moveToTrash(
                    relativePath = ponto.fotoComprovantePath,
                    pontoId = ponto.id,
                    motivo = parametros.motivo
                )

                when (trashResult) {
                    is TrashResult.Success -> {
                        imagemMovidaParaLixeira = true
                        trashPath = trashResult.trashPath
                        Timber.d("Imagem do ponto ${ponto.id} movida para lixeira: ${trashResult.trashPath}")
                    }
                    is TrashResult.FileNotFound -> {
                        Timber.w("Imagem não encontrada para ponto ${ponto.id}: ${ponto.fotoComprovantePath}")
                    }
                    is TrashResult.Error -> {
                        Timber.e("Erro ao mover imagem para lixeira: ${trashResult.message}")
                        // Não falha a operação, apenas loga
                    }
                }
            }

            // 2. Registrar auditoria ANTES de excluir (inclui trashPath para rollback)
            val auditData = ponto.toAuditMapLocal().toMutableMap()
            if (trashPath != null) {
                auditData["_trashPath"] = trashPath
            }

            val auditLog = AuditLog(
                entidade = "pontos",
                entidadeId = parametros.pontoId,
                acao = AcaoAuditoria.DELETE,
                motivo = parametros.motivo,
                dadosAnteriores = gson.toJson(auditData),
                dadosNovos = null,
                criadoEm = LocalDateTime.now()
            )
            auditLogRepository.inserir(auditLog)

            // 3. Excluir ponto do banco
            pontoRepository.excluir(ponto)

            Timber.i("Ponto excluído com sucesso: id=${ponto.id}, imagem na lixeira=$imagemMovidaParaLixeira")

            Resultado.Sucesso(
                pontoId = ponto.id,
                imagemMovidaParaLixeira = imagemMovidaParaLixeira,
                trashPath = trashPath
            )
        } catch (e: Exception) {
            Timber.e(e, "Erro ao excluir ponto: ${parametros.pontoId}")
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

    private fun Ponto.toAuditMapLocal(): Map<String, Any?> = mapOf(
        "id" to id,
        "empregoId" to empregoId,
        "dataHora" to dataHora.toString(),
        "horaConsiderada" to horaConsiderada.toString(),
        "nsr" to nsr,
        "latitude" to latitude,
        "longitude" to longitude,
        "endereco" to endereco,
        "observacao" to observacao,
        "isEditadoManualmente" to isEditadoManualmente,
        "marcadorId" to marcadorId,
        "justificativaInconsistencia" to justificativaInconsistencia,
        "fotoComprovantePath" to fotoComprovantePath,
        "criadoEm" to criadoEm.toString(),
        "atualizadoEm" to atualizadoEm.toString()
    )
}
