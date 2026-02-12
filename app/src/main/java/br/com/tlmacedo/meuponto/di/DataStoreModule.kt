// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/di/DataStoreModule.kt
package br.com.tlmacedo.meuponto.di

import android.content.Context
import br.com.tlmacedo.meuponto.data.local.datastore.PreferencesDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo Hilt para injeção de dependências do DataStore.
 *
 * Fornece a instância singleton do PreferencesDataStore para
 * gerenciamento de preferências do usuário.
 *
 * @author Thiago
 * @since 1.0.0
 */
@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    /**
     * Fornece a instância do PreferencesDataStore.
     *
     * @param context Contexto da aplicação
     * @return Instância singleton do PreferencesDataStore
     */
    @Provides
    @Singleton
    fun providePreferencesDataStore(
        @ApplicationContext context: Context
    ): PreferencesDataStore {
        return PreferencesDataStore(context)
    }
}
