// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/di/LocationModule.kt
package br.com.tlmacedo.meuponto.di

import android.content.Context
import br.com.tlmacedo.meuponto.data.service.LocationService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo Hilt para injeção de dependências relacionadas à localização.
 *
 * @author Thiago
 * @since 3.5.0
 */
@Module
@InstallIn(SingletonComponent::class)
object LocationModule {

    @Provides
    @Singleton
    fun provideLocationService(
        @ApplicationContext context: Context
    ): LocationService {
        return LocationService(context)
    }
}
