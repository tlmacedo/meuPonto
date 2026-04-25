// path: app/src/main/java/br/com/tlmacedo/meuponto/data/remote/dto/chamado/ChamadoCriadoResponse.kt
package br.com.tlmacedo.meuponto.data.remote.dto.chamado


import com.google.gson.annotations.SerializedName

data class ChamadoCriadoResponse(
    @SerializedName("id") val id: String,
    @SerializedName("identificador") val identificador: String,
    @SerializedName("criado_em") val criadoEm: String
)
