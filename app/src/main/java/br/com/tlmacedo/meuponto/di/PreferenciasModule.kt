package br.com.tlmacedo.meuponto.di

import br.com.tlmacedo.meuponto.data.repository.PreferenciasRepositoryImpl
import br.com.tlmacedo.meuponto.domain.repository.PreferenciasRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PreferenciasModule {
    @Binds
    @Singleton
    abstract fun bindPreferenciasRepository(impl: PreferenciasRepositoryImpl): PreferenciasRepository
}
