// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/usecase/ponto/EditarPontoUseCase.kt
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
import java.time.LocalTime
import javax.inject.Inject

/**
 * Caso de uso para editar um ponto existente.
 *
 * Suporta edição de:
 * - Horário do ponto (com recálculo automático de horaConsiderada)
 * - NSR (Número Sequencial de Registro)
 * - Localização (latitude, longitude, endereço)
 * - Observação
 * - Foto de comprovante (com versionamento na lixeira)
 *
 * Funcionalidades de auditoria e rollback:
 * - Todas as edições são registradas no log de auditoria com motivo obrigatório
 * - Imagens antigas são movidas para lixeira (não deletadas) quando substituídas
 * - Dados completos antes/depois são salvos para rollback
 *
 * @author Thiago
 * @since 1.0.0
 * @updated 7.0.0 - Integração com CalcularHoraConsideradaUseCase para recalcular tolerância
 * @updated 11.0.0 - Integração com lixeira de imagens e suporte a rollback de foto
 */
class EditarPontoUseCase @Inject constructor(
    private val pontoRepository: PontoRepository,
    private val auditLogRepository: AuditLogRepository,
    private val calcularHoraConsideradaUseCase: CalcularHoraConsideradaUseCase,
    private val imageTrashManager: ImageTrashManager,
    private val gson: Gson
) {
    data class Parametros(
        val pontoId: Long,
        val dataHora: LocalDateTime? = null,
        val nsr: String? = null,
        val latitude: Double? = null,
        val longitude: Double? = null,
        val endereco: String? = null,
        val observacao: String? = null,
        val novaFotoComprovantePath: String? = null,
        val removerFotoComprovante: Boolean = false,
        val motivo: String
    )

    sealed class Resultado {
        data class Sucesso(
            val ponto: Ponto,
            val imagemAntigaMovidaParaLixeira: Boolean = false,
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
            erros.add("O motivo da edição é obrigatório")
        }

        if (parametros.motivo.length < 5) {
            erros.add("O motivo deve ter pelo menos 5 caracteres")
        }

        if (erros.isNotEmpty()) {
            return Resultado.Validacao(erros)
        }

        val pontoExistente = pontoRepository.buscarPorId(parametros.pontoId)
            ?: return Resultado.NaoEncontrado(parametros.pontoId)

        // Validar horário se foi alterado
        val novaDataHora = parametros.dataHora ?: pontoExistente.dataHora
        val horarioFoiAlterado =
            parametros.dataHora != null && parametros.dataHora != pontoExistente.dataHora

        if (horarioFoiAlterado) {
            val pontosDoDia = pontoRepository.buscarPorEmpregoEData(
                pontoExistente.empregoId,
                novaDataHora.toLocalDate()
            ).filter { it.id != parametros.pontoId }

            // Verifica conflito de horário
            for (ponto in pontosDoDia) {
                if (novaDataHora == ponto.dataHora) {
                    return Resultado.Validacao(listOf("Já existe um ponto neste horário"))
                }
            }
        }

        // *** RECALCULAR horaConsiderada se horário foi alterado ***
        val novaHoraConsiderada: LocalTime = if (horarioFoiAlterado) {
            val pontosDoDia = pontoRepository.buscarPorEmpregoEData(
                pontoExistente.empregoId,
                novaDataHora.toLocalDate()
            ).sortedBy { it.dataHora }

            val indicePonto = pontosDoDia.indexOfFirst { it.id == parametros.pontoId }
                .takeIf { it >= 0 } ?: 0

            calcularHoraConsideradaUseCase(
                empregoId = pontoExistente.empregoId,
                dataHora = novaDataHora,
                indicePonto = indicePonto
            )
        } else {
            pontoExistente.horaConsiderada
        }

        return try {
            var imagemAntigaMovidaParaLixeira = false
            var trashPath: String? = null

            // ═══════════════════════════════════════════════════════════════════
            // GESTÃO DE FOTO DE COMPROVANTE
            // ═══════════════════════════════════════════════════════════════════

            val novaFotoPath: String? = when {
                // Caso 1: Remover foto existente
                parametros.removerFotoComprovante -> {
                    if (pontoExistente.temFotoComprovante && pontoExistente.fotoComprovantePath != null) {
                        val result = imageTrashManager.moveToTrash(
                            relativePath = pontoExistente.fotoComprovantePath,
                            pontoId = pontoExistente.id,
                            motivo = "Remoção de foto: ${parametros.motivo}"
                        )
                        if (result is TrashResult.Success) {
                            imagemAntigaMovidaParaLixeira = true
                            trashPath = result.trashPath
                            Timber.d("Foto removida e movida para lixeira: ${result.trashPath}")
                        }
                    }
                    null
                }

                // Caso 2: Substituir foto existente por nova
                parametros.novaFotoComprovantePath != null -> {
                    // Mover foto antiga para lixeira antes de substituir
                    if (pontoExistente.temFotoComprovante && pontoExistente.fotoComprovantePath != null) {
                        val result = imageTrashManager.moveToTrash(
                            relativePath = pontoExistente.fotoComprovantePath,
                            pontoId = pontoExistente.id,
                            motivo = "Substituição de foto: ${parametros.motivo}"
                        )
                        if (result is TrashResult.Success) {
                            imagemAntigaMovidaParaLixeira = true
                            trashPath = result.trashPath
                            Timber.d("Foto antiga movida para lixeira: ${result.trashPath}")
                        }
                    }
                    parametros.novaFotoComprovantePath
                }

                // Caso 3: Manter foto existente
                else -> pontoExistente.fotoComprovantePath
            }

            // Criar ponto atualizado
            val pontoAtualizado = pontoExistente.copy(
                dataHora = novaDataHora,
                horaConsiderada = novaHoraConsiderada,
                nsr = parametros.nsr ?: pontoExistente.nsr,
                latitude = parametros.latitude ?: pontoExistente.latitude,
                longitude = parametros.longitude ?: pontoExistente.longitude,
                endereco = parametros.endereco ?: pontoExistente.endereco,
                observacao = parametros.observacao ?: pontoExistente.observacao,
                fotoComprovantePath = novaFotoPath,
                isEditadoManualmente = true,
                atualizadoEm = LocalDateTime.now()
            )

            // ═══════════════════════════════════════════════════════════════════
            // REGISTRAR AUDITORIA
            // ═══════════════════════════════════════════════════════════════════

            val dadosAnteriores = pontoExistente.toAuditMapLocal().toMutableMap()
            val dadosNovos = pontoAtualizado.toAuditMapLocal().toMutableMap()

            // Adiciona metadata de lixeira para rollback
            if (trashPath != null) {
                dadosAnteriores["_fotoTrashPath"] = trashPath
            }

            val auditLog = AuditLog(
                entidade = "pontos",
                entidadeId = parametros.pontoId,
                acao = AcaoAuditoria.UPDATE,
                motivo = parametros.motivo,
                dadosAnteriores = gson.toJson(dadosAnteriores),
                dadosNovos = gson.toJson(dadosNovos),
                criadoEm = LocalDateTime.now()
            )
            auditLogRepository.inserir(auditLog)

            // Atualizar ponto
            pontoRepository.atualizar(pontoAtualizado)

            Timber.i("Ponto editado com sucesso: id=${pontoAtualizado.id}")

            Resultado.Sucesso(
                ponto = pontoAtualizado,
                imagemAntigaMovidaParaLixeira = imagemAntigaMovidaParaLixeira,
                trashPath = trashPath
            )
        } catch (e: Exception) {
            Timber.e(e, "Erro ao atualizar ponto: ${parametros.pontoId}")
            Resultado.Erro("Erro ao atualizar ponto: ${e.message}")
        }
    }

    /**
     * Converte Ponto para Map para serialização no audit log.
     */
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
