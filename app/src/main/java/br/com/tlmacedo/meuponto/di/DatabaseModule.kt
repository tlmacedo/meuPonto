// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/di/DatabaseModule.kt
package br.com.tlmacedo.meuponto.di

import android.content.Context
import androidx.room.Room
import br.com.tlmacedo.meuponto.data.local.database.MeuPontoDatabase
import br.com.tlmacedo.meuponto.data.local.database.dao.PontoDao
import br.com.tlmacedo.meuponto.data.local.database.migration.MIGRATION_1_2
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo Hilt para injeção de dependências relacionadas ao banco de dados.
 *
 * Fornece instâncias singleton do banco de dados Room e seus DAOs.
 *
 * @author Thiago
 * @since 1.0.0
 * @updated 2.0.0 - Adicionado suporte a migrations
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /**
     * Fornece a instância singleton do banco de dados Room.
     *
     * @param context Contexto da aplicação
     * @return Instância do MeuPontoDatabase
     */
    @Provides
    @Singleton
    fun provideMeuPontoDatabase(
        @ApplicationContext context: Context
    ): MeuPontoDatabase {
        return Room.databaseBuilder(
            context,
            MeuPontoDatabase::class.java,
            MeuPontoDatabase.DATABASE_NAME
        )
            .addMigrations(MIGRATION_1_2)
            .build()
    }

    /**
     * Fornece o DAO de Ponto.
     *
     * @param database Instância do banco de dados
     * @return PontoDao para operações de banco
     */
    @Provides
    @Singleton
    fun providePontoDao(database: MeuPontoDatabase): PontoDao {
        return database.pontoDao()
    }
}
