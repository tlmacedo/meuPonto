// path: app/src/main/java/br/com/tlmacedo/meuponto/data/remote/dto/ChamadoDto.kt
package br.com.tlmacedo.meuponto.data.remote.dto.chamado


import br.com.tlmacedo.meuponto.domain.model.chamado.CategoriaChamado
import br.com.tlmacedo.meuponto.domain.model.chamado.Chamado
import br.com.tlmacedo.meuponto.domain.model.chamado.PrioridadeChamado
import br.com.tlmacedo.meuponto.domain.model.chamado.StatusChamado
import com.google.gson.annotations.SerializedName
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class ChamadoDto(
    @SerializedName("id") val id: Long,
    @SerializedName("identificador") val identificador: String,
    @SerializedName("titulo") val titulo: String,
    @SerializedName("descricao") val descricao: String,
    @SerializedName("passos_para_reproduzir") val passosParaReproduzir: String? = null,
    @SerializedName("device_info") val deviceInfo: String? = null,
    @SerializedName("categoria") val categoria: CategoriaChamado,
    @SerializedName("prioridade") val prioridade: PrioridadeChamado,
    @SerializedName("status") val status: StatusChamado,
    @SerializedName("emprego_id") val empregoId: Long?,
    @SerializedName("usuario_email") val usuarioEmail: String,
    @SerializedName("usuario_nome") val usuarioNome: String,
    @SerializedName("resposta") val resposta: String?,
    @SerializedName("criado_em") val criadoEm: String,
    @SerializedName("atualizado_em") val atualizadoEm: String
) {
    fun toDomain(): Chamado {
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
        return Chamado(
            id = id,
            identificador = identificador,
            titulo = titulo,
            descricao = descricao,
            passosParaReproduzir = passosParaReproduzir,
            deviceInfo = deviceInfo,
            categoria = categoria,
            prioridade = prioridade,
            status = status,
            empregoId = empregoId,
            usuarioEmail = usuarioEmail,
            usuarioNome = usuarioNome,
            resposta = resposta,
            criadoEm = LocalDateTime.parse(criadoEm, formatter),
            atualizadoEm = LocalDateTime.parse(atualizadoEm, formatter)
        )
    }

    companion object {
        fun fromDomain(chamado: Chamado): ChamadoDto {
            val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
            return ChamadoDto(
                id = chamado.id,
                identificador = chamado.identificador,
                titulo = chamado.titulo,
                descricao = chamado.descricao,
                passosParaReproduzir = chamado.passosParaReproduzir,
                deviceInfo = chamado.deviceInfo,
                categoria = chamado.categoria,
                prioridade = chamado.prioridade,
                status = chamado.status,
                empregoId = chamado.empregoId,
                usuarioEmail = chamado.usuarioEmail,
                usuarioNome = chamado.usuarioNome,
                resposta = chamado.resposta,
                criadoEm = chamado.criadoEm.format(formatter),
                atualizadoEm = chamado.atualizadoEm.format(formatter)
            )
        }
    }
}