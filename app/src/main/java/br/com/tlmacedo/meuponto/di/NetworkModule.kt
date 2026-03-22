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
 * O [HttpLoggingInterceptor] é configurado com [HttpLoggingInterceptor.Level.BODY]
 * apenas em builds de debug. Em produção, o logging é completamente desabilitado
 * para evitar exposição de dados sensíveis (tokens, payloads).
 *
 * @author Thiago
 * @since 3.0.0
 * @updated 12.0.0 - Adicionado guard BuildConfig.DEBUG no HttpLoggingInterceptor
 *                   para não expor dados sensíveis em builds de produção
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    /**
     * Fornece instância configurada do [Gson] para serialização/deserialização.
     *
     * Configurado com modo lenient para aceitar JSON malformado de APIs externas.
     *
     * @return Instância singleton do Gson
     */
    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder()
            .setLenient()
            .create()
    }

    /**
     * Fornece instância configurada do [OkHttpClient].
     *
     * O nível de log é controlado por [BuildConfig.DEBUG]:
     * - Debug: [HttpLoggingInterceptor.Level.BODY] (log completo de headers e body)
     * - Release: [HttpLoggingInterceptor.Level.NONE] (sem logs de rede)
     *
     * @return Instância singleton do OkHttpClient
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
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
     * Fornece instância configurada do [Retrofit].
     *
     * @param okHttpClient Cliente HTTP configurado
     * @param gson Instância do Gson para conversão de tipos
     * @return Instância singleton do Retrofit
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
     * Fornece instância do [BrasilApiService] criada pelo Retrofit.
     *
     * @param retrofit Instância configurada do Retrofit
     * @return Implementação da interface de serviço gerada pelo Retrofit
     */
    @Provides
    @Singleton
    fun provideBrasilApiService(retrofit: Retrofit): BrasilApiService {
        return retrofit.create(BrasilApiService::class.java)
    }
}