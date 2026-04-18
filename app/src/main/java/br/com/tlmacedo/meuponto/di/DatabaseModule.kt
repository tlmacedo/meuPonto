// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/di/DatabaseModule.kt
package br.com.tlmacedo.meuponto.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import br.com.tlmacedo.meuponto.BuildConfig
import br.com.tlmacedo.meuponto.data.local.database.MeuPontoDatabase
import br.com.tlmacedo.meuponto.data.local.database.dao.AjusteSalarialDao
import br.com.tlmacedo.meuponto.data.local.database.dao.AjusteSaldoDao
import br.com.tlmacedo.meuponto.data.local.database.dao.AuditLogDao
import br.com.tlmacedo.meuponto.data.local.database.dao.AusenciaDao
import br.com.tlmacedo.meuponto.data.local.database.dao.ConfiguracaoEmpregoDao
import br.com.tlmacedo.meuponto.data.local.database.dao.ConfiguracaoPontesAnoDao
import br.com.tlmacedo.meuponto.data.local.database.dao.EmpregoDao
import br.com.tlmacedo.meuponto.data.local.database.dao.FechamentoPeriodoDao
import br.com.tlmacedo.meuponto.data.local.database.dao.FeriadoDao
import br.com.tlmacedo.meuponto.data.local.database.dao.FotoComprovanteDao
import br.com.tlmacedo.meuponto.data.local.database.dao.GeocodificacaoCacheDao
import br.com.tlmacedo.meuponto.data.local.database.dao.HistoricoCargoDao
import br.com.tlmacedo.meuponto.data.local.database.dao.HorarioDiaSemanaDao
import br.com.tlmacedo.meuponto.data.local.database.dao.MarcadorDao
import br.com.tlmacedo.meuponto.data.local.database.dao.PontoDao
import br.com.tlmacedo.meuponto.data.local.database.dao.UsuarioDao
import br.com.tlmacedo.meuponto.data.local.database.dao.VersaoJornadaDao
import br.com.tlmacedo.meuponto.data.local.database.migration.MIGRATION_10_11
import br.com.tlmacedo.meuponto.data.local.database.migration.MIGRATION_11_12
import br.com.tlmacedo.meuponto.data.local.database.migration.MIGRATION_12_13
import br.com.tlmacedo.meuponto.data.local.database.migration.MIGRATION_13_14
import br.com.tlmacedo.meuponto.data.local.database.migration.MIGRATION_14_15
import br.com.tlmacedo.meuponto.data.local.database.migration.MIGRATION_15_16
import br.com.tlmacedo.meuponto.data.local.database.migration.MIGRATION_16_17
import br.com.tlmacedo.meuponto.data.local.database.migration.MIGRATION_17_18
import br.com.tlmacedo.meuponto.data.local.database.migration.MIGRATION_18_19
import br.com.tlmacedo.meuponto.data.local.database.migration.MIGRATION_19_20
import br.com.tlmacedo.meuponto.data.local.database.migration.MIGRATION_1_2
import br.com.tlmacedo.meuponto.data.local.database.migration.MIGRATION_20_21
import br.com.tlmacedo.meuponto.data.local.database.migration.MIGRATION_21_22
import br.com.tlmacedo.meuponto.data.local.database.migration.MIGRATION_22_23
import br.com.tlmacedo.meuponto.data.local.database.migration.MIGRATION_23_24
import br.com.tlmacedo.meuponto.data.local.database.migration.MIGRATION_24_25
import br.com.tlmacedo.meuponto.data.local.database.migration.MIGRATION_25_26
import br.com.tlmacedo.meuponto.data.local.database.migration.MIGRATION_26_27
import br.com.tlmacedo.meuponto.data.local.database.migration.MIGRATION_27_28
import br.com.tlmacedo.meuponto.data.local.database.migration.MIGRATION_28_29
import br.com.tlmacedo.meuponto.data.local.database.migration.MIGRATION_29_30
import br.com.tlmacedo.meuponto.data.local.database.migration.MIGRATION_2_3
import br.com.tlmacedo.meuponto.data.local.database.migration.MIGRATION_30_31
import br.com.tlmacedo.meuponto.data.local.database.migration.MIGRATION_31_32
import br.com.tlmacedo.meuponto.data.local.database.migration.MIGRATION_32_33
import br.com.tlmacedo.meuponto.data.local.database.migration.MIGRATION_33_34
import br.com.tlmacedo.meuponto.data.local.database.migration.MIGRATION_34_35
import br.com.tlmacedo.meuponto.data.local.database.migration.MIGRATION_35_36
import br.com.tlmacedo.meuponto.data.local.database.migration.MIGRATION_36_37
import br.com.tlmacedo.meuponto.data.local.database.migration.MIGRATION_37_38
import br.com.tlmacedo.meuponto.data.local.database.migration.MIGRATION_38_39
import br.com.tlmacedo.meuponto.data.local.database.migration.MIGRATION_3_4
import br.com.tlmacedo.meuponto.data.local.database.migration.MIGRATION_4_5
import br.com.tlmacedo.meuponto.data.local.database.migration.MIGRATION_5_6
import br.com.tlmacedo.meuponto.data.local.database.migration.MIGRATION_6_7
import br.com.tlmacedo.meuponto.data.local.database.migration.MIGRATION_7_8
import br.com.tlmacedo.meuponto.data.local.database.migration.MIGRATION_8_9
import br.com.tlmacedo.meuponto.data.local.database.migration.MIGRATION_9_10
import br.com.tlmacedo.meuponto.data.local.database.util.DatabaseCheckpointManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.time.LocalDateTime
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideMeuPontoDatabase(@ApplicationContext context: Context): MeuPontoDatabase {
        return Room.databaseBuilder(
            context,
            MeuPontoDatabase::class.java,
            MeuPontoDatabase.DATABASE_NAME
        )
            .addMigrations(
                MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6,
                MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10, MIGRATION_10_11,
                MIGRATION_11_12, MIGRATION_12_13, MIGRATION_13_14, MIGRATION_14_15, MIGRATION_15_16,
                MIGRATION_16_17, MIGRATION_17_18, MIGRATION_18_19, MIGRATION_19_20, MIGRATION_20_21,
                MIGRATION_21_22, MIGRATION_22_23, MIGRATION_23_24, MIGRATION_24_25, MIGRATION_25_26,
                MIGRATION_26_27, MIGRATION_27_28, MIGRATION_28_29, MIGRATION_29_30, MIGRATION_30_31,
                MIGRATION_31_32, MIGRATION_32_33, MIGRATION_33_34, MIGRATION_34_35, MIGRATION_35_36,
                MIGRATION_36_37, MIGRATION_37_38, MIGRATION_38_39
            )
            .addCallback(createDatabaseCallback())
            .build()
    }

    @Provides
    @Singleton
    fun provideDatabaseCheckpointManager(database: MeuPontoDatabase): DatabaseCheckpointManager {
        return DatabaseCheckpointManager(database)
    }

    private fun createDatabaseCallback(): RoomDatabase.Callback {
        return object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // ✅ Correção aplicada: Dados de seed apenas em DEBUG
                if (BuildConfig.DEBUG) {
                    inserirDadosIniciais(db)
                }
            }
        }
    }

    private fun inserirDadosIniciais(db: SupportSQLiteDatabase) {
        val now = LocalDateTime.now().toString()
        val dataAdmissao = "2025-01-01"

        db.execSQL("INSERT INTO empregos (id, nome, dataInicioTrabalho, descricao, ativo, arquivado, ordem, criadoEm, atualizadoEm) VALUES (1, 'Emprego Teste', '$dataAdmissao', 'Emprego para desenvolvimento', 1, 0, 0, '$now', '$now')")
        db.execSQL("INSERT INTO configuracoes_emprego (empregoId, habilitarNsr, tipoNsr, habilitarLocalizacao, localizacaoAutomatica, exibirLocalizacaoDetalhes, fotoHabilitada, fotoObrigatoria, fotoFormato, fotoQualidade, fotoResolucaoMaxima, fotoTamanhoMaximoKb, fotoCorrecaoOrientacao, fotoApenasCamera, fotoIncluirLocalizacaoExif, fotoBackupNuvemHabilitado, fotoBackupApenasWifi, exibirDuracaoTurno, exibirDuracaoIntervalo, criadoEm, atualizadoEm) VALUES (1, 0, 'NUMERICO', 0, 0, 1, 0, 0, 'JPEG', 85, 1920, 1024, 1, 0, 1, 0, 1, 1, 1, '$now', '$now')")
        db.execSQL("INSERT INTO versoes_jornada (empregoId, dataInicio, dataFim, descricao, numeroVersao, vigente, jornadaMaximaDiariaMinutos, intervaloMinimoInterjornadaMinutos, toleranciaIntervaloMaisMinutos, turnoMaximoMinutos, cargaHorariaDiariaMinutos, acrescimoMinutosDiasPontes, cargaHorariaSemanalMinutos, primeiroDiaSemana, diaInicioFechamentoRH, zerarSaldoSemanal, zerarSaldoPeriodoRH, ocultarSaldoTotal, bancoHorasHabilitado, periodoBancoSemanas, periodoBancoMeses, dataInicioCicloBancoAtual, diasUteisLembreteFechamento, habilitarSugestaoAjuste, zerarBancoAntesPeriodo, exigeJustificativaInconsistencia, criadoEm, atualizadoEm) VALUES (1, '$dataAdmissao', NULL, 'Configuração inicial', 1, 1, 600, 660, 20, 360, 480, 12, 2460, 'SEGUNDA', 1, 0, 0, 0, 0, 0, 0, NULL, 3, 0, 0, 0, '$now', '$now')")

        listOf("SEGUNDA", "TERCA", "QUARTA", "QUINTA", "SEXTA").forEach { dia ->
            db.execSQL("INSERT INTO horarios_dia_semana (empregoId, versaoJornadaId, diaSemana, ativo, cargaHorariaMinutos, entradaIdeal, saidaIntervaloIdeal, voltaIntervaloIdeal, saidaIdeal, intervaloMinimoMinutos, toleranciaIntervaloMaisMinutos, criadoEm, atualizadoEm) VALUES (1, 1, '$dia', 1, 492, '08:00', '12:00', '13:00', '17:12', 60, 20, '$now', '$now')")
        }
        listOf("SABADO", "DOMINGO").forEach { dia ->
            db.execSQL("INSERT INTO horarios_dia_semana (empregoId, versaoJornadaId, diaSemana, ativo, cargaHorariaMinutos, entradaIdeal, saidaIntervaloIdeal, voltaIntervaloIdeal, saidaIdeal, intervaloMinimoMinutos, toleranciaIntervaloMaisMinutos, criadoEm, atualizadoEm) VALUES (1, 1, '$dia', 0, 0, NULL, NULL, NULL, NULL, 60, 0, '$now', '$now')")
        }
    }

    @Provides @Singleton fun providePontoDao(db: MeuPontoDatabase): PontoDao = db.pontoDao()
    @Provides @Singleton fun provideEmpregoDao(db: MeuPontoDatabase): EmpregoDao = db.empregoDao()
    @Provides @Singleton fun provideConfiguracaoEmpregoDao(db: MeuPontoDatabase): ConfiguracaoEmpregoDao = db.configuracaoEmpregoDao()
    @Provides @Singleton fun provideHorarioDiaSemanaDao(db: MeuPontoDatabase): HorarioDiaSemanaDao = db.horarioDiaSemanaDao()
    @Provides @Singleton fun provideAjusteSaldoDao(db: MeuPontoDatabase): AjusteSaldoDao = db.ajusteSaldoDao()
    @Provides @Singleton fun provideFechamentoPeriodoDao(db: MeuPontoDatabase): FechamentoPeriodoDao = db.fechamentoPeriodoDao()
    @Provides @Singleton fun provideMarcadorDao(db: MeuPontoDatabase): MarcadorDao = db.marcadorDao()
    @Provides @Singleton fun provideAuditLogDao(db: MeuPontoDatabase): AuditLogDao = db.auditLogDao()
    @Provides @Singleton fun provideVersaoJornadaDao(db: MeuPontoDatabase): VersaoJornadaDao = db.versaoJornadaDao()
    @Provides @Singleton fun provideFeriadoDao(db: MeuPontoDatabase): FeriadoDao = db.feriadoDao()
    @Provides @Singleton fun provideConfiguracaoPontesAnoDao(db: MeuPontoDatabase): ConfiguracaoPontesAnoDao = db.configuracaoPontesAnoDao()
    @Provides @Singleton fun provideAusenciaDao(db: MeuPontoDatabase): AusenciaDao = db.ausenciaDao()
    @Provides @Singleton fun provideFotoComprovanteDao(db: MeuPontoDatabase): FotoComprovanteDao = db.fotoComprovanteDao()
    @Provides @Singleton fun provideGeocodificacaoCacheDao(db: MeuPontoDatabase): GeocodificacaoCacheDao = db.geocodificacaoCacheDao()
    @Provides @Singleton fun provideUsuarioDao(db: MeuPontoDatabase): UsuarioDao = db.usuarioDao()
    @Provides @Singleton fun provideHistoricoCargoDao(db: MeuPontoDatabase): HistoricoCargoDao = db.historicoCargoDao()
    @Provides @Singleton fun provideAjusteSalarialDao(db: MeuPontoDatabase): AjusteSalarialDao = db.ajusteSalarialDao()
}