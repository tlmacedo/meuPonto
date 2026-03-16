// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/di/TrashModule.kt
package br.com.tlmacedo.meuponto.di

import android.content.Context
import br.com.tlmacedo.meuponto.util.foto.ImageTrashManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo Hilt para injeção do gerenciador de lixeira.
 *
 * @author Thiago
 * @since 11.0.0
 */
@Module
@InstallIn(SingletonComponent::class)
object TrashModule {

    @Provides
    @Singleton
    fun provideImageTrashManager(
        @ApplicationContext context: Context
    ): ImageTrashManager {
        return ImageTrashManager(context)
    }
}
