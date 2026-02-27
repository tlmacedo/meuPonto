// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/usecase/ponto/EditarPontoUseCase.kt
package br.com.tlmacedo.meuponto.domain.usecase.ponto

import br.com.tlmacedo.meuponto.domain.model.AcaoAuditoria
import br.com.tlmacedo.meuponto.domain.model.AuditLog
import br.com.tlmacedo.meuponto.domain.model.Ponto
import br.com.tlmacedo.meuponto.domain.repository.AuditLogRepository
import br.com.tlmacedo.meuponto.domain.repository.PontoRepository
import com.google.gson.Gson
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
 *
 * Todas as edições são registradas no log de auditoria com motivo obrigatório.
 *
 * @author Thiago
 * @since 1.0.0
 * @updated 7.0.0 - Integração com CalcularHoraConsideradaUseCase para recalcular tolerância
 */
class EditarPontoUseCase @Inject constructor(
    private val pontoRepository: PontoRepository,
    private val auditLogRepository: AuditLogRepository,
    private val calcularHoraConsideradaUseCase: CalcularHoraConsideradaUseCase,
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
        val motivo: String
    )

    sealed class Resultado {
        data class Sucesso(val ponto: Ponto) : Resultado()
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
        val horarioFoiAlterado = parametros.dataHora != null && parametros.dataHora != pontoExistente.dataHora

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
            // Busca o índice do ponto no dia para calcular tolerância correta
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

        // Criar ponto atualizado
        val pontoAtualizado = pontoExistente.copy(
            dataHora = novaDataHora,
            horaConsiderada = novaHoraConsiderada,
            nsr = parametros.nsr ?: pontoExistente.nsr,
            latitude = parametros.latitude ?: pontoExistente.latitude,
            longitude = parametros.longitude ?: pontoExistente.longitude,
            endereco = parametros.endereco ?: pontoExistente.endereco,
            observacao = parametros.observacao ?: pontoExistente.observacao,
            isEditadoManualmente = true,
            atualizadoEm = LocalDateTime.now()
        )

        return try {
            // Registrar auditoria ANTES de atualizar
            val auditLog = AuditLog(
                entidade = "pontos",
                entidadeId = parametros.pontoId,
                acao = AcaoAuditoria.UPDATE,
                motivo = parametros.motivo,
                dadosAnteriores = gson.toJson(pontoExistente.toAuditMap()),
                dadosNovos = gson.toJson(pontoAtualizado.toAuditMap()),
                criadoEm = LocalDateTime.now()
            )
            auditLogRepository.inserir(auditLog)

            // Atualizar ponto
            pontoRepository.atualizar(pontoAtualizado)

            Resultado.Sucesso(pontoAtualizado)
        } catch (e: Exception) {
            Resultado.Erro("Erro ao atualizar ponto: ${e.message}")
        }
    }

    /**
     * Converte Ponto para Map para serialização no audit log.
     */
    private fun Ponto.toAuditMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "empregoId" to empregoId,
        "dataHora" to dataHora.toString(),
        "horaConsiderada" to horaConsiderada.toString(),
        "nsr" to nsr,
        "latitude" to latitude,
        "longitude" to longitude,
        "endereco" to endereco,
        "observacao" to observacao,
        "isEditadoManualmente" to isEditadoManualmente
    )
}
