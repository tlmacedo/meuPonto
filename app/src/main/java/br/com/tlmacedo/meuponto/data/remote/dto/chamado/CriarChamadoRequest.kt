// path: app/src/main/java/br/com/tlmacedo/meuponto/data/remote/dto/chamado/CriarChamadoRequest.kt
package br.com.tlmacedo.meuponto.data.remote.dto.chamado

import br.com.tlmacedo.meuponto.domain.model.chamado.CategoriaChamado
import br.com.tlmacedo.meuponto.domain.model.chamado.PrioridadeChamado
import com.google.gson.annotations.SerializedName

data class CriarChamadoRequest(
    @SerializedName("titulo") val titulo: String,
    @SerializedName("descricao") val descricao: String,
    @SerializedName("categoria") val categoria: CategoriaChamado,
    @SerializedName("prioridade") val prioridade: PrioridadeChamado,
    @SerializedName("emprego_id") val empregoId: Long?,
    @SerializedName("usuario_email") val usuarioEmail: String,
    @SerializedName("usuario_nome") val usuarioNome: String
)