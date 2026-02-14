// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/di/PreferenciasModule.kt
package br.com.tlmacedo.meuponto.di

import br.com.tlmacedo.meuponto.data.repository.PreferenciasRepositoryImpl
import br.com.tlmacedo.meuponto.domain.repository.PreferenciasRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo Hilt para injeção de dependências relacionadas às preferências.
 *
 * Vincula a implementação do repositório de preferências à sua interface,
 * permitindo a inversão de dependência e facilitando testes unitários.
 *
 * @author Thiago
 * @since 2.0.0
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class PreferenciasModule {

    /**
     * Vincula a implementação do PreferenciasRepository à sua interface.
     *
     * O Hilt usará esta vinculação para fornecer a implementação correta
     * sempre que a interface for requisitada como dependência.
     *
     * @param impl Implementação concreta do repositório
     * @return Interface do repositório para injeção
     */
    @Binds
    @Singleton
    abstract fun bindPreferenciasRepository(
        impl: PreferenciasRepositoryImpl
    ): PreferenciasRepository
}
