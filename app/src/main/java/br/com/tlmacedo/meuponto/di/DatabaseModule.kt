// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/di/DatabaseModule.kt
package br.com.tlmacedo.meuponto.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import br.com.tlmacedo.meuponto.data.local.database.MeuPontoDatabase
import br.com.tlmacedo.meuponto.data.local.database.dao.*
import br.com.tlmacedo.meuponto.data.local.database.migration.*
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
 * @author Thiago
 * @since 1.0.0
 * @updated 6.0.0 - Adicionada migração 14->15 para novo sistema de ciclos de banco
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

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
            .addMigrations(
                MIGRATION_1_2,
                MIGRATION_2_3,
                MIGRATION_3_4,
                MIGRATION_4_5,
                MIGRATION_5_6,
                MIGRATION_6_7,
                MIGRATION_7_8,
                MIGRATION_8_9,
                MIGRATION_9_10,
                MIGRATION_10_11,
                MIGRATION_11_12,
                MIGRATION_12_13,
                MIGRATION_13_14,
                MIGRATION_14_15  // Nova migração - sistema de ciclos
            )
            .addCallback(createDatabaseCallback())
            .build()
    }

    private fun createDatabaseCallback(): RoomDatabase.Callback {
        return object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                inserirDadosIniciais(db)
            }
        }
    }

    /**
     * Insere dados iniciais de teste para desenvolvimento.
     * NOTA: Usa os novos nomes de campos (versão 15)
     */
    private fun inserirDadosIniciais(db: SupportSQLiteDatabase) {
        val now = LocalDateTime.now().toString()
        val dataAdmissao = "2021-11-10"

        // ========================================================================
        // 1. EMPREGO
        // ========================================================================
        db.execSQL(
            """
            INSERT INTO empregos (
                id, nome, dataInicioTrabalho, descricao, ativo, arquivado, ordem, criadoEm, atualizadoEm
            ) VALUES (
                1, 
                'SIDIA Teste', 
                '$dataAdmissao',
                'Emprego para desenvolvimento',
                1, 
                0, 
                0, 
                '$now', 
                '$now'
            )
            """.trimIndent()
        )

        // ========================================================================
        // 2. CONFIGURAÇÃO DO EMPREGO (novos nomes de campos - versão 15)
        // ========================================================================
        db.execSQL(
            """
            INSERT INTO configuracoes_emprego (
                empregoId,
                cargaHorariaDiariaMinutos,
                jornadaMaximaDiariaMinutos,
                intervaloMinimoInterjornadaMinutos,
                intervaloMinimoMinutos,
                toleranciaIntervaloMaisMinutos,
                exigeJustificativaInconsistencia,
                habilitarNsr,
                tipoNsr,
                habilitarLocalizacao,
                localizacaoAutomatica,
                exibirLocalizacaoDetalhes,
                exibirDuracaoTurno,
                exibirDuracaoIntervalo,
                primeiroDiaSemana,
                diaInicioFechamentoRH,
                zerarSaldoSemanal,
                zerarSaldoPeriodoRH,
                ocultarSaldoTotal,
                bancoHorasHabilitado,
                periodoBancoSemanas,
                periodoBancoMeses,
                dataInicioCicloBancoAtual,
                diasUteisLembreteFechamento,
                habilitarSugestaoAjuste,
                zerarBancoAntesPeriodo,
                criadoEm,
                atualizadoEm
            ) VALUES (
                1,
                492,
                600,
                660,
                60,
                20,
                0,
                0,
                'NUMERICO',
                0,
                0,
                1,
                1,
                1,
                'SEGUNDA',
                1,
                0,
                0,
                0,
                0,
                0,
                0,
                NULL,
                3,
                0,
                0,
                '$now',
                '$now'
            )
            """.trimIndent()
        )

        // ========================================================================
        // 3. HORÁRIOS POR DIA DA SEMANA
        // ========================================================================
        val diasUteis = listOf("SEGUNDA", "TERCA", "QUARTA", "QUINTA", "SEXTA")
        diasUteis.forEach { dia ->
            db.execSQL(
                """
                INSERT INTO horarios_dia_semana (
                    empregoId,
                    diaSemana,
                    ativo,
                    cargaHorariaMinutos,
                    entradaIdeal,
                    saidaIntervaloIdeal,
                    voltaIntervaloIdeal,
                    saidaIdeal,
                    intervaloMinimoMinutos,
                    toleranciaIntervaloMaisMinutos,
                    toleranciaEntradaMinutos,
                    toleranciaSaidaMinutos,
                    criadoEm,
                    atualizadoEm
                ) VALUES (
                    1,
                    '$dia',
                    1,
                    492,
                    '08:00',
                    '12:00',
                    '13:00',
                    '17:12',
                    60,
                    20,
                    NULL,
                    NULL,
                    '$now',
                    '$now'
                )
                """.trimIndent()
            )
        }

        val diasFolga = listOf("SABADO", "DOMINGO")
        diasFolga.forEach { dia ->
            db.execSQL(
                """
                INSERT INTO horarios_dia_semana (
                    empregoId,
                    diaSemana,
                    ativo,
                    cargaHorariaMinutos,
                    entradaIdeal,
                    saidaIntervaloIdeal,
                    voltaIntervaloIdeal,
                    saidaIdeal,
                    intervaloMinimoMinutos,
                    toleranciaIntervaloMaisMinutos,
                    toleranciaEntradaMinutos,
                    toleranciaSaidaMinutos,
                    criadoEm,
                    atualizadoEm
                ) VALUES (
                    1,
                    '$dia',
                    0,
                    0,
                    NULL,
                    NULL,
                    NULL,
                    NULL,
                    60,
                    0,
                    NULL,
                    NULL,
                    '$now',
                    '$now'
                )
                """.trimIndent()
            )
        }
    }

    // ========================================================================
    // PROVIDERS DOS DAOs
    // ========================================================================

    @Provides
    @Singleton
    fun providePontoDao(database: MeuPontoDatabase): PontoDao = database.pontoDao()

    @Provides
    @Singleton
    fun provideEmpregoDao(database: MeuPontoDatabase): EmpregoDao = database.empregoDao()

    @Provides
    @Singleton
    fun provideConfiguracaoEmpregoDao(database: MeuPontoDatabase): ConfiguracaoEmpregoDao = database.configuracaoEmpregoDao()

    @Provides
    @Singleton
    fun provideHorarioDiaSemanaDao(database: MeuPontoDatabase): HorarioDiaSemanaDao = database.horarioDiaSemanaDao()

    @Provides
    @Singleton
    fun provideAjusteSaldoDao(database: MeuPontoDatabase): AjusteSaldoDao = database.ajusteSaldoDao()

    @Provides
    @Singleton
    fun provideFechamentoPeriodoDao(database: MeuPontoDatabase): FechamentoPeriodoDao = database.fechamentoPeriodoDao()

    @Provides
    @Singleton
    fun provideMarcadorDao(database: MeuPontoDatabase): MarcadorDao = database.marcadorDao()

    @Provides
    @Singleton
    fun provideAuditLogDao(database: MeuPontoDatabase): AuditLogDao = database.auditLogDao()

    @Provides
    @Singleton
    fun provideVersaoJornadaDao(database: MeuPontoDatabase): VersaoJornadaDao = database.versaoJornadaDao()

    @Provides
    @Singleton
    fun provideFeriadoDao(database: MeuPontoDatabase): FeriadoDao = database.feriadoDao()

    @Provides
    @Singleton
    fun provideConfiguracaoPontesAnoDao(database: MeuPontoDatabase): ConfiguracaoPontesAnoDao = database.configuracaoPontesAnoDao()

    @Provides
    @Singleton
    fun provideAusenciaDao(database: MeuPontoDatabase): AusenciaDao = database.ausenciaDao()
}
