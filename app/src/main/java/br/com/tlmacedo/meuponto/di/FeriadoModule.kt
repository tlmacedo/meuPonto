package br.com.tlmacedo.meuponto.di

import br.com.tlmacedo.meuponto.data.repository.FeriadoRepositoryImpl
import br.com.tlmacedo.meuponto.domain.repository.FeriadoRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class FeriadoModule {
    @Binds
    @Singleton
    abstract fun bindFeriadoRepository(impl: FeriadoRepositoryImpl): FeriadoRepository
}
