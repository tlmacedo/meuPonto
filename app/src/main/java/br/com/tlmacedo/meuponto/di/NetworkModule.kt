// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/di/NetworkModule.kt
package br.com.tlmacedo.meuponto.di

import br.com.tlmacedo.meuponto.BuildConfig
import br.com.tlmacedo.meuponto.data.remote.api.BrasilApiService
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * Módulo Hilt para configuração de dependências de rede.
 *
 * ## Correção aplicada (12.0.0):
 * [HttpLoggingInterceptor] estava configurado com [HttpLoggingInterceptor.Level.BODY]
 * incondicionalmente, expondo tokens e payloads completos em produção.
 * Corrigido para usar [HttpLoggingInterceptor.Level.BODY] apenas em debug
 * e [HttpLoggingInterceptor.Level.NONE] em release.
 *
 * @author Thiago
 * @since 3.0.0
 * @updated 12.0.0 - HttpLoggingInterceptor protegido por BuildConfig.DEBUG
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    /**
     * Provê instância configurada do [Gson].
     */
    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder()
            .setLenient()
            .create()
    }

    /**
     * Provê instância configurada do [OkHttpClient].
     *
     * O interceptor de log é ativo apenas em debug para evitar
     * exposição de dados sensíveis (tokens, payloads) em produção.
     *
     * @return Cliente HTTP configurado com timeouts e interceptor condicional
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        // ✅ Correto: Level.BODY apenas em debug
        // ❌ Errado (original): Level.BODY incondicional em produção
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    /**
     * Provê instância configurada do [Retrofit].
     *
     * @param okHttpClient Cliente HTTP injetado
     * @param gson Instância do Gson injetada
     * @return Retrofit configurado para a BrasilAPI
     */
    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        gson: Gson
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BrasilApiService.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    /**
     * Provê implementação do [BrasilApiService] via Retrofit.
     *
     * @param retrofit Instância do Retrofit injetada
     * @return Implementação gerada pelo Retrofit
     */
    @Provides
    @Singleton
    fun provideBrasilApiService(retrofit: Retrofit): BrasilApiService {
        return retrofit.create(BrasilApiService::class.java)
    }
}