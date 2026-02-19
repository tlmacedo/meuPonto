// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/data/remote/api/BrasilApiService.kt
package br.com.tlmacedo.meuponto.data.remote.api

import br.com.tlmacedo.meuponto.data.remote.dto.FeriadoNacionalDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Interface Retrofit para a Brasil API.
 *
 * Documentação: https://brasilapi.com.br/docs#tag/Feriados-Nacionais
 *
 * @author Thiago
 * @since 3.0.0
 */
interface BrasilApiService {

    companion object {
        const val BASE_URL = "https://brasilapi.com.br/api/"
    }

    /**
     * Busca feriados nacionais de um ano específico.
     *
     * @param ano Ano para buscar os feriados (ex: 2025)
     * @return Lista de feriados do ano
     */
    @GET("feriados/v1/{ano}")
    suspend fun buscarFeriadosNacionais(
        @Path("ano") ano: Int
    ): Response<List<FeriadoNacionalDto>>
}
