// path: app/src/main/java/br/com/tlmacedo/meuponto/di/ChamadoModule.kt
package br.com.tlmacedo.meuponto.di

import android.content.Context
import br.com.tlmacedo.meuponto.data.local.database.dao.ChamadoDao
import br.com.tlmacedo.meuponto.data.remote.api.ChamadoApiService
import br.com.tlmacedo.meuponto.data.repository.ChamadoRepositoryImpl
import br.com.tlmacedo.meuponto.data.service.ChamadoIdentificadorServiceImpl
import br.com.tlmacedo.meuponto.data.service.EmailNotificacaoServiceImpl
import br.com.tlmacedo.meuponto.domain.repository.ChamadoRepository
import br.com.tlmacedo.meuponto.domain.service.ChamadoIdentificadorService
import br.com.tlmacedo.meuponto.domain.service.EmailNotificacaoService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ChamadoModule {

    @Provides
    @Singleton
    fun provideChamadoApiService(retrofit: Retrofit): ChamadoApiService {
        return retrofit.create(ChamadoApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideEmailNotificacaoService(
        chamadoApiService: ChamadoApiService
    ): EmailNotificacaoService {
        return EmailNotificacaoServiceImpl(chamadoApiService)
    }

    @Provides
    @Singleton
    fun provideChamadoIdentificadorService(): ChamadoIdentificadorService {
        return ChamadoIdentificadorServiceImpl()
    }

    @Provides
    @Singleton
    fun provideChamadoRepository(
        @ApplicationContext context: Context,
        chamadoDao: ChamadoDao,
        chamadoApiService: ChamadoApiService
    ): ChamadoRepository {
        return ChamadoRepositoryImpl(context, chamadoDao, chamadoApiService)
    }
}