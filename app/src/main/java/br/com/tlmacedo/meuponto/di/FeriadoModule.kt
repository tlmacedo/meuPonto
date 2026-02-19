// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/di/FeriadoModule.kt
package br.com.tlmacedo.meuponto.di

import br.com.tlmacedo.meuponto.data.repository.FeriadoRepositoryImpl
import br.com.tlmacedo.meuponto.domain.repository.FeriadoRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo Hilt para injeção de dependências relacionadas a Feriados.
 *
 * @author Thiago
 * @since 3.0.0
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class FeriadoModule {

    @Binds
    @Singleton
    abstract fun bindFeriadoRepository(
        impl: FeriadoRepositoryImpl
    ): FeriadoRepository
}
