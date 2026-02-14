// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/di/DatabaseModule.kt
package br.com.tlmacedo.meuponto.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
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
import java.time.LocalDateTime
import javax.inject.Singleton

/**
 * Módulo Hilt para injeção de dependências relacionadas ao banco de dados.
 *
 * Fornece instâncias singleton do banco de dados Room e seus DAOs.
 * Inclui callback para inserir dados iniciais em instalações novas.
 *
 * @author Thiago
 * @since 1.0.0
 * @updated 2.0.0 - Adicionado suporte a migrations, novos DAOs e callback de inicialização
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    // ========================================================================
    // Database
    // ========================================================================

    /**
     * Fornece instância singleton do banco de dados Room.
     *
     * Configura migrations e callback para inserir dados iniciais
     * quando o banco é criado pela primeira vez (instalação nova).
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
            .addCallback(createDatabaseCallback())
            .build()
    }

    /**
     * Cria callback para inicialização do banco de dados.
     *
     * O callback onCreate é executado apenas quando o banco é criado
     * pela primeira vez (instalação nova), inserindo o emprego padrão
     * e sua configuração inicial.
     *
     * Nota: Em upgrades (versão 1 → 2), a migration é responsável
     * por inserir esses dados.
     *
     * @return RoomDatabase.Callback configurado
     */
    private fun createDatabaseCallback(): RoomDatabase.Callback {
        return object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                inserirDadosIniciais(db)
            }
        }
    }

    /**
     * Insere dados iniciais no banco de dados.
     *
     * Cria o emprego padrão (id=1) e sua configuração inicial
     * para que o app funcione corretamente na primeira execução.
     *
     * @param db Instância do banco SQLite
     */
    private fun inserirDadosIniciais(db: SupportSQLiteDatabase) {
        val now = LocalDateTime.now().toString()

        // Inserir emprego padrão
        db.execSQL(
            """
            INSERT INTO empregos (
                id, nome, descricao, ativo, arquivado, ordem, criadoEm, atualizadoEm
            ) VALUES (
                1, 'Meu Emprego', 'Emprego padrão', 1, 0, 0, '$now', '$now'
            )
            """.trimIndent()
        )

        // Inserir configuração padrão do emprego
        db.execSQL(
            """
            INSERT INTO configuracoes_emprego (
                empregoId,
                cargaHorariaDiaria,
                cargaHorariaSemanal,
                toleranciaAtraso,
                toleranciaHoraExtra,
                toleranciaIntervalo,
                intervaloMinimo,
                horaExtraAutomatica,
                considerarFeriados,
                considerarPontoFacultativo,
                tipoNsr,
                prefixoNsr,
                proximoNsr,
                criadoEm,
                atualizadoEm
            ) VALUES (
                1,
                480,
                2400,
                10,
                10,
                0,
                60,
                0,
                1,
                0,
                'NENHUM',
                NULL,
                1,
                '$now',
                '$now'
            )
            """.trimIndent()
        )
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
