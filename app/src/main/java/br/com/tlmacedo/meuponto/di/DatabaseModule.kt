// Arquivo: DatabaseModule.kt
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
 * @updated 2.4.0 - Dados de teste SIDIA para desenvolvimento
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
                MIGRATION_7_8
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
     *
     * Emprego: SIDIA Teste
     * - Jornada: 44h/semana (8h12 seg-sex)
     * - Horário: 08:00-12:00 / 13:00-17:12
     * - Intervalo mínimo: 60min (tolerância +20min)
     * - Admissão: 10/11/2021
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
        // 2. CONFIGURAÇÃO DO EMPREGO
        // ========================================================================
        db.execSQL(
            """
            INSERT INTO configuracoes_emprego (
                empregoId,
                cargaHorariaDiariaMinutos,
                jornadaMaximaDiariaMinutos,
                intervaloMinimoInterjornadaMinutos,
                intervaloMinimoMinutos,
                toleranciaEntradaMinutos,
                toleranciaSaidaMinutos,
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
                primeiroDiaMes,
                zerarSaldoSemanal,
                zerarSaldoMensal,
                ocultarSaldoTotal,
                periodoBancoHorasMeses,
                diasUteisLembreteFechamento,
                habilitarSugestaoAjuste,
                zerarBancoAntesPeriodo,
                criadoEm,
                atualizadoEm
            ) VALUES (
                1,
                492,    -- 8h12 = 8*60 + 12 = 492 minutos
                600,    -- 10h máximo
                660,    -- 11h interjornada
                60,     -- 1h intervalo mínimo
                0,      -- sem tolerância entrada
                0,      -- sem tolerância saída
                20,     -- +20min tolerância no retorno do intervalo
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
        
        // Segunda a Sexta - Dias úteis com horário padrão
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
                    492,        -- 8h12
                    '08:00',    -- entrada
                    '12:00',    -- saída almoço
                    '13:00',    -- volta almoço
                    '17:12',    -- saída
                    60,         -- 1h intervalo mínimo
                    20,         -- +20min tolerância retorno
                    NULL,       -- usa tolerância global
                    NULL,       -- usa tolerância global
                    '$now',
                    '$now'
                )
                """.trimIndent()
            )
        }

        // Sábado e Domingo - Folga
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
                    0,          -- inativo (folga)
                    0,          -- sem carga horária
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
}
