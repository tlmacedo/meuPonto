// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/di/DatabaseModule.kt
package br.com.tlmacedo.meuponto.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import br.com.tlmacedo.meuponto.BuildConfig
import br.com.tlmacedo.meuponto.data.local.database.MeuPontoDatabase
import br.com.tlmacedo.meuponto.data.local.database.dao.*
import br.com.tlmacedo.meuponto.data.local.database.migration.*
import br.com.tlmacedo.meuponto.data.local.database.util.DatabaseCheckpointManager
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
 * Os dados de seed inseridos em [inserirDadosIniciais] são exclusivos
 * para o ambiente de desenvolvimento e protegidos por [BuildConfig.DEBUG].
 * Em produção, o banco é criado vazio.
 *
 * @author Thiago
 * @since 1.0.0
 * @updated 10.0.0 - Migração 22->23: Sistema de foto de comprovante com metadados
 * @updated 12.0.0 - Dados de seed protegidos por BuildConfig.DEBUG
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /**
     * Fornece a instância singleton do banco de dados Room.
     *
     * Aplica todas as migrações incrementais e registra o callback
     * de criação somente se em ambiente de debug.
     *
     * @param context Contexto da aplicação
     * @return Instância singleton do [MeuPontoDatabase]
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
                MIGRATION_14_15,
                MIGRATION_15_16,
                MIGRATION_16_17,
                MIGRATION_17_18,
                MIGRATION_18_19,
                MIGRATION_19_20,
                MIGRATION_20_21,
                MIGRATION_21_22,
                MIGRATION_22_23,
                MIGRATION_23_24,
                MIGRATION_24_25,
                MIGRATION_25_26
            )
            .addCallback(createDatabaseCallback())
            .build()
    }

    /**
     * Fornece o gerenciador de checkpoint WAL.
     *
     * Garante persistência imediata dos dados após operações de escrita,
     * evitando perda de dados em caso de crash logo após uma transação.
     *
     * @param database Instância do banco de dados
     * @return Instância singleton do [DatabaseCheckpointManager]
     */
    @Provides
    @Singleton
    fun provideDatabaseCheckpointManager(
        database: MeuPontoDatabase
    ): DatabaseCheckpointManager {
        return DatabaseCheckpointManager(database)
    }

    /**
     * Cria o callback de inicialização do banco.
     *
     * Os dados iniciais de desenvolvimento são inseridos apenas quando
     * [BuildConfig.DEBUG] é verdadeiro, garantindo que nenhum dado fictício
     * chegue ao ambiente de produção.
     *
     * @return [RoomDatabase.Callback] configurado conforme o build type
     */
    private fun createDatabaseCallback(): RoomDatabase.Callback {
        return object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // Dados de seed exclusivos para ambiente de desenvolvimento
                if (BuildConfig.DEBUG) {
                    inserirDadosIniciais(db)
                }
            }
        }
    }

    /**
     * Insere dados iniciais para facilitar o desenvolvimento e testes manuais.
     *
     * ATENÇÃO: Este método só é chamado quando [BuildConfig.DEBUG] == true.
     * Nunca deve ser chamado em produção.
     *
     * Dados inseridos:
     * - 1 emprego de teste ("SIDIA Teste")
     * - Configuração de emprego com campos de foto
     * - Versão de jornada inicial
     * - Horários por dia da semana (dias úteis + finais de semana)
     *
     * @param db Banco de dados em criação
     */
    private fun inserirDadosIniciais(db: SupportSQLiteDatabase) {
        val now = LocalDateTime.now().toString()
        val dataAdmissao = "2021-11-10"

        // ════════════════════════════════════════════════════════════════════
        // 1. EMPREGO
        // ════════════════════════════════════════════════════════════════════
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

        // ════════════════════════════════════════════════════════════════════
        // 2. CONFIGURAÇÃO DO EMPREGO
        // ════════════════════════════════════════════════════════════════════
        db.execSQL(
            """
            INSERT INTO configuracoes_emprego (
                empregoId,
                habilitarNsr,
                tipoNsr,
                habilitarLocalizacao,
                localizacaoAutomatica,
                exibirLocalizacaoDetalhes,
                fotoHabilitada,
                fotoObrigatoria,
                fotoFormato,
                fotoQualidade,
                fotoResolucaoMaxima,
                fotoTamanhoMaximoKb,
                fotoCorrecaoOrientacao,
                fotoApenasCamera,
                fotoIncluirLocalizacaoExif,
                fotoBackupNuvemHabilitado,
                fotoBackupApenasWifi,
                exibirDuracaoTurno,
                exibirDuracaoIntervalo,
                criadoEm,
                atualizadoEm
            ) VALUES (
                1, 0, 'NUMERICO', 0, 0, 1,
                0, 0, 'JPEG', 85, 1920, 1024,
                1, 0, 1, 0, 1, 1, 1,
                '$now', '$now'
            )
            """.trimIndent()
        )

        // ════════════════════════════════════════════════════════════════════
        // 3. VERSÃO DE JORNADA
        // ════════════════════════════════════════════════════════════════════
        db.execSQL(
            """
            INSERT INTO versoes_jornada (
                empregoId, dataInicio, dataFim, descricao, numeroVersao, vigente,
                jornadaMaximaDiariaMinutos, intervaloMinimoInterjornadaMinutos,
                toleranciaIntervaloMaisMinutos, turnoMaximoMinutos,
                cargaHorariaDiariaMinutos, acrescimoMinutosDiasPontes,
                cargaHorariaSemanalMinutos, primeiroDiaSemana, diaInicioFechamentoRH,
                zerarSaldoSemanal, zerarSaldoPeriodoRH, ocultarSaldoTotal,
                bancoHorasHabilitado, periodoBancoSemanas, periodoBancoMeses,
                dataInicioCicloBancoAtual, diasUteisLembreteFechamento,
                habilitarSugestaoAjuste, zerarBancoAntesPeriodo,
                exigeJustificativaInconsistencia, criadoEm, atualizadoEm
            ) VALUES (
                1, '$dataAdmissao', NULL, 'Configuração inicial', 1, 1,
                600, 660, 20, 360, 480, 12, 2460, 'SEGUNDA', 1,
                0, 0, 0, 0, 0, 0, NULL, 3, 0, 0, 0,
                '$now', '$now'
            )
            """.trimIndent()
        )

        // ════════════════════════════════════════════════════════════════════
        // 4. HORÁRIOS POR DIA DA SEMANA
        // ════════════════════════════════════════════════════════════════════
        val diasUteis = listOf("SEGUNDA", "TERCA", "QUARTA", "QUINTA", "SEXTA")
        diasUteis.forEach { dia ->
            db.execSQL(
                """
                INSERT INTO horarios_dia_semana (
                    empregoId, versaoJornadaId, diaSemana, ativo,
                    cargaHorariaMinutos, entradaIdeal, saidaIntervaloIdeal,
                    voltaIntervaloIdeal, saidaIdeal, intervaloMinimoMinutos,
                    toleranciaIntervaloMaisMinutos, criadoEm, atualizadoEm
                ) VALUES (
                    1, 1, '$dia', 1, 492,
                    '08:00', '12:00', '13:00', '17:12',
                    60, 20, '$now', '$now'
                )
                """.trimIndent()
            )
        }

        val diasFolga = listOf("SABADO", "DOMINGO")
        diasFolga.forEach { dia ->
            db.execSQL(
                """
                INSERT INTO horarios_dia_semana (
                    empregoId, versaoJornadaId, diaSemana, ativo,
                    cargaHorariaMinutos, entradaIdeal, saidaIntervaloIdeal,
                    voltaIntervaloIdeal, saidaIdeal, intervaloMinimoMinutos,
                    toleranciaIntervaloMaisMinutos, criadoEm, atualizadoEm
                ) VALUES (
                    1, 1, '$dia', 0, 0,
                    NULL, NULL, NULL, NULL,
                    60, 0, '$now', '$now'
                )
                """.trimIndent()
            )
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    // PROVIDERS DOS DAOs
    // ════════════════════════════════════════════════════════════════════════

    @Provides @Singleton
    fun providePontoDao(db: MeuPontoDatabase): PontoDao = db.pontoDao()

    @Provides @Singleton
    fun provideEmpregoDao(db: MeuPontoDatabase): EmpregoDao = db.empregoDao()

    @Provides @Singleton
    fun provideConfiguracaoEmpregoDao(db: MeuPontoDatabase): ConfiguracaoEmpregoDao =
        db.configuracaoEmpregoDao()

    @Provides @Singleton
    fun provideHorarioDiaSemanaDao(db: MeuPontoDatabase): HorarioDiaSemanaDao =
        db.horarioDiaSemanaDao()

    @Provides @Singleton
    fun provideAjusteSaldoDao(db: MeuPontoDatabase): AjusteSaldoDao = db.ajusteSaldoDao()

    @Provides @Singleton
    fun provideFechamentoPeriodoDao(db: MeuPontoDatabase): FechamentoPeriodoDao =
        db.fechamentoPeriodoDao()

    @Provides @Singleton
    fun provideMarcadorDao(db: MeuPontoDatabase): MarcadorDao = db.marcadorDao()

    @Provides @Singleton
    fun provideAuditLogDao(db: MeuPontoDatabase): AuditLogDao = db.auditLogDao()

    @Provides @Singleton
    fun provideVersaoJornadaDao(db: MeuPontoDatabase): VersaoJornadaDao = db.versaoJornadaDao()

    @Provides @Singleton
    fun provideFeriadoDao(db: MeuPontoDatabase): FeriadoDao = db.feriadoDao()

    @Provides @Singleton
    fun provideConfiguracaoPontesAnoDao(db: MeuPontoDatabase): ConfiguracaoPontesAnoDao =
        db.configuracaoPontesAnoDao()

    @Provides @Singleton
    fun provideAusenciaDao(db: MeuPontoDatabase): AusenciaDao = db.ausenciaDao()

    @Provides @Singleton
    fun provideFotoComprovanteDao(db: MeuPontoDatabase): FotoComprovanteDao =
        db.fotoComprovanteDao()
}