// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/di/DatabaseModule.kt
package br.com.tlmacedo.meuponto.di

import android.content.Context
import androidx.room.Room
import br.com.tlmacedo.meuponto.data.local.database.MeuPontoDatabase
import br.com.tlmacedo.meuponto.data.local.database.dao.AjusteSaldoDao
import br.com.tlmacedo.meuponto.data.local.database.dao.AuditLogDao
import br.com.tlmacedo.meuponto.data.local.database.dao.ConfiguracaoEmpregoDao
import br.com.tlmacedo.meuponto.data.local.database.dao.EmpregoDao
import br.com.tlmacedo.meuponto.data.local.database.dao.FechamentoPeriodoDao
import br.com.tlmacedo.meuponto.data.local.database.dao.HorarioDiaSemanaDao
import br.com.tlmacedo.meuponto.data.local.database.dao.MarcadorDao
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
 * @updated 2.0.0 - Adicionado suporte a migrations e novos DAOs
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    // ========================================================================
    // Database
    // ========================================================================

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

    // ========================================================================
    // DAOs
    // ========================================================================

    @Provides
    @Singleton
    fun providePontoDao(database: MeuPontoDatabase): PontoDao {
        return database.pontoDao()
    }

    @Provides
    @Singleton
    fun provideEmpregoDao(database: MeuPontoDatabase): EmpregoDao {
        return database.empregoDao()
    }

    @Provides
    @Singleton
    fun provideConfiguracaoEmpregoDao(database: MeuPontoDatabase): ConfiguracaoEmpregoDao {
        return database.configuracaoEmpregoDao()
    }

    @Provides
    @Singleton
    fun provideHorarioDiaSemanaDao(database: MeuPontoDatabase): HorarioDiaSemanaDao {
        return database.horarioDiaSemanaDao()
    }

    @Provides
    @Singleton
    fun provideAjusteSaldoDao(database: MeuPontoDatabase): AjusteSaldoDao {
        return database.ajusteSaldoDao()
    }

    @Provides
    @Singleton
    fun provideFechamentoPeriodoDao(database: MeuPontoDatabase): FechamentoPeriodoDao {
        return database.fechamentoPeriodoDao()
    }

    @Provides
    @Singleton
    fun provideMarcadorDao(database: MeuPontoDatabase): MarcadorDao {
        return database.marcadorDao()
    }

    @Provides
    @Singleton
    fun provideAuditLogDao(database: MeuPontoDatabase): AuditLogDao {
        return database.auditLogDao()
    }
}
