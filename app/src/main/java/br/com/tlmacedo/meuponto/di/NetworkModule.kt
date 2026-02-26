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
import okhttp3.CertificatePinner
import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import okhttp3.TlsVersion
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * Módulo Hilt para configuração de dependências de rede.
 *
 * Responsabilidades:
 * - Configuração do cliente HTTP (OkHttp)
 * - Logging condicional (somente DEBUG)
 * - Segurança: TLS 1.2+, Certificate Pinning e Network Security Config
 * - Serialização/desserialização JSON (Gson)
 * - Configuração do Retrofit
 *
 * @author Thiago
 * @since 3.0.0
 * @updated 25/02/2026 - TLS moderno, Certificate Pinning e retry interceptor
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    // ─── Timeouts ────────────────────────────────────────────────────────────
    private const val TIMEOUT_CONNECT_SECONDS = 30L
    private const val TIMEOUT_READ_SECONDS    = 30L
    private const val TIMEOUT_WRITE_SECONDS   = 30L

    // ─── Certificate Pinning ─────────────────────────────────────────────────
    // Hash gerado via:
    // openssl s_client -connect brasilapi.com.br:443 | openssl x509 -pubkey |
    //   openssl pkey -pubin -outform der | openssl dgst -sha256 -binary | openssl enc -base64
    private const val API_HOST         = "brasilapi.com.br"
    private const val API_CERT_PIN_1   = "uzfhAPevT2UM6PKBGqsUBWF81xzsGU+0TIYTnOcfH4E=" // Certificado ativo
    private const val API_CERT_PIN_2   = "sha256/BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB=" // Backup (fallback)

    // ─── Retry ───────────────────────────────────────────────────────────────
    private const val MAX_RETRIES = 3

    // =========================================================================
    // Gson
    // =========================================================================

    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder()
            .apply {
                // setLenient() apenas em DEBUG para facilitar desenvolvimento;
                // em produção, parsing estrito evita dados malformados silenciosos
                if (BuildConfig.DEBUG) setLenient()
            }
            .create()
    }

    // =========================================================================
    // OkHttp
    // =========================================================================

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(TIMEOUT_CONNECT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_READ_SECONDS,    TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT_WRITE_SECONDS,  TimeUnit.SECONDS)
            .connectionSpecs(buildConnectionSpecs())
            .certificatePinner(buildCertificatePinner())
            .addInterceptor(buildLoggingInterceptor())
            .addInterceptor(buildRetryInterceptor())
            .build()
    }

    // =========================================================================
    // Retrofit
    // =========================================================================

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

    // =========================================================================
    // Services
    // =========================================================================

    @Provides
    @Singleton
    fun provideBrasilApiService(retrofit: Retrofit): BrasilApiService {
        return retrofit.create(BrasilApiService::class.java)
    }

    // =========================================================================
    // Builders privados
    // =========================================================================

    /**
     * Restringe conexões a TLS 1.2 e 1.3 com cipher suites modernas.
     * Em DEBUG, mantém CLEARTEXT para emuladores/proxies locais (ex.: Charles, Proxyman).
     */
    private fun buildConnectionSpecs(): List<ConnectionSpec> {
        val modernTls = ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
            .tlsVersions(TlsVersion.TLS_1_3, TlsVersion.TLS_1_2)
            .build()

        return if (BuildConfig.DEBUG) {
            listOf(modernTls, ConnectionSpec.CLEARTEXT)
        } else {
            listOf(modernTls)
        }
    }

    /**
     * Certificate Pinning com certificado ativo + backup.
     * ATENÇÃO: Atualize os hashes antes de renovar o certificado do servidor
     * para evitar quebra de conectividade em produção.
     */
    private fun buildCertificatePinner(): CertificatePinner {
        return CertificatePinner.Builder()
            .add(API_HOST, API_CERT_PIN_1)
            .add(API_HOST, API_CERT_PIN_2)
            .build()
    }

    /**
     * Logging HTTP:
     * - DEBUG  → BODY (headers + corpo completo)
     * - RELEASE → NONE (nenhum log de rede)
     */
    private fun buildLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor { message ->
            Timber.tag("OkHttp").d(message)
        }.apply {
            level = if (BuildConfig.DEBUG)
                HttpLoggingInterceptor.Level.BODY
            else
                HttpLoggingInterceptor.Level.NONE
        }
    }

    /**
     * Interceptor de retry com backoff linear para falhas de rede transitórias.
     * Tenta até [MAX_RETRIES] vezes antes de propagar o erro.
     */
    private fun buildRetryInterceptor() = okhttp3.Interceptor { chain ->
        var attempt = 0
        var lastException: Exception? = null

        while (attempt < MAX_RETRIES) {
            try {
                return@Interceptor chain.proceed(chain.request())
            } catch (e: Exception) {
                lastException = e
                attempt++
                Timber.w("Tentativa $attempt/$MAX_RETRIES falhou: ${e.message}")
                Thread.sleep(attempt * 500L) // backoff: 500ms, 1000ms, 1500ms
            }
        }

        throw lastException ?: Exception("Falha desconhecida após $MAX_RETRIES tentativas")
    }
}
