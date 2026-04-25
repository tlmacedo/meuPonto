// path:
package br.com.tlmacedo.meuponto.data.remote.dto.chamado

import com.google.gson.annotations.SerializedName

data class AvaliacaoRequest(
    @SerializedName("nota") val nota: Int,           // 1 a 5
    @SerializedName("comentario") val comentario: String?
)
