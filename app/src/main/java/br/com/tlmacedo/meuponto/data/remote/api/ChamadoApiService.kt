// path: app/src/main/java/br/com/tlmacedo/meuponto/data/remote/api/ChamadoApiService.kt
package br.com.tlmacedo.meuponto.data.remote.api

import br.com.tlmacedo.meuponto.data.remote.dto.chamado.AvaliacaoRequest
import br.com.tlmacedo.meuponto.data.remote.dto.chamado.ChamadoCriadoResponse
import br.com.tlmacedo.meuponto.data.remote.dto.chamado.ChamadoDto
import br.com.tlmacedo.meuponto.data.remote.dto.chamado.CriarChamadoRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ChamadoApiService {

    @POST("chamados")
    suspend fun criarChamado(
        @Body request: CriarChamadoRequest
    ): Response<ChamadoCriadoResponse>

    @GET("chamados/{id}")
    suspend fun buscarChamado(
        @Path("id") id: String
    ): Response<ChamadoDto>

    @PUT("chamados/{id}/avaliar")
    suspend fun avaliarChamado(
        @Path("id") id: String,
        @Body request: AvaliacaoRequest
    ): Response<Unit>
}