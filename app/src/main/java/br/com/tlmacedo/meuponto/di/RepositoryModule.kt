package br.com.tlmacedo.meuponto.di

import br.com.tlmacedo.meuponto.data.repository.PontoRepositoryImpl
import br.com.tlmacedo.meuponto.domain.repository.PontoRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo Hilt para injeção de dependências dos repositórios.
 *
 * Vincula as interfaces de repositório às suas implementações concretas.
 *
 * @author Thiago
 * @since 1.0.0
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    /**
     * Vincula a implementação do PontoRepository.
     *
     * @param impl Implementação concreta do repositório
     * @return Interface PontoRepository
     */
    @Binds
    @Singleton
    abstract fun bindPontoRepository(
        impl: PontoRepositoryImpl
    ): PontoRepository
}
