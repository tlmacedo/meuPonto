// path: app/src/main/java/br/com/tlmacedo/meuponto/di/DatabaseModule.kt
package br.com.tlmacedo.meuponto.di

import android.content.Context
import androidx.room.Room
import br.com.tlmacedo.meuponto.data.local.database.MeuPontoDatabase
import br.com.tlmacedo.meuponto.data.local.database.dao.AjusteSalarialDao
import br.com.tlmacedo.meuponto.data.local.database.dao.AjusteSaldoDao
import br.com.tlmacedo.meuponto.data.local.database.dao.AuditLogDao
import br.com.tlmacedo.meuponto.data.local.database.dao.AusenciaDao
import br.com.tlmacedo.meuponto.data.local.database.dao.ChamadoDao
import br.com.tlmacedo.meuponto.data.local.database.dao.CloudSyncQueueDao
import br.com.tlmacedo.meuponto.data.local.database.dao.ConfiguracaoEmpregoDao
import br.com.tlmacedo.meuponto.data.local.database.dao.ConfiguracaoPontesAnoDao
import br.com.tlmacedo.meuponto.data.local.database.dao.EmpregoDao
import br.com.tlmacedo.meuponto.data.local.database.dao.FechamentoPeriodoDao
import br.com.tlmacedo.meuponto.data.local.database.dao.FeriadoDao
import br.com.tlmacedo.meuponto.data.local.database.dao.FotoComprovanteDao
import br.com.tlmacedo.meuponto.data.local.database.dao.GeocodificacaoCacheDao
import br.com.tlmacedo.meuponto.data.local.database.dao.HistoricoCargoDao
import br.com.tlmacedo.meuponto.data.local.database.dao.HistoricoChamadoDao
import br.com.tlmacedo.meuponto.data.local.database.dao.HorarioDiaSemanaDao
import br.com.tlmacedo.meuponto.data.local.database.dao.MarcadorDao
import br.com.tlmacedo.meuponto.data.local.database.dao.PontoDao
import br.com.tlmacedo.meuponto.data.local.database.dao.UsuarioDao
import br.com.tlmacedo.meuponto.data.local.database.dao.VersaoJornadaDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): MeuPontoDatabase {
        return Room.databaseBuilder(
            context,
            MeuPontoDatabase::class.java,
            "meuponto.db"
        )
            .addMigrations(*MeuPontoDatabase.MIGRATIONS)
            .build()
    }

    @Provides fun provideEmpregoDao(db: MeuPontoDatabase): EmpregoDao = db.empregoDao()
    @Provides fun providePontoDao(db: MeuPontoDatabase): PontoDao = db.pontoDao()
    @Provides fun provideMarcadorDao(db: MeuPontoDatabase): MarcadorDao = db.marcadorDao()
    @Provides fun provideAusenciaDao(db: MeuPontoDatabase): AusenciaDao = db.ausenciaDao()
    @Provides fun provideAjusteSaldoDao(db: MeuPontoDatabase): AjusteSaldoDao = db.ajusteSaldoDao()
    @Provides fun provideVersaoJornadaDao(db: MeuPontoDatabase): VersaoJornadaDao = db.versaoJornadaDao()
    @Provides fun provideFotoComprovanteDao(db: MeuPontoDatabase): FotoComprovanteDao = db.fotoComprovanteDao()
    @Provides fun provideHistoricoCargoDao(db: MeuPontoDatabase): HistoricoCargoDao = db.historicoCargoDao()
    @Provides fun provideCloudSyncQueueDao(db: MeuPontoDatabase): CloudSyncQueueDao = db.cloudSyncQueueDao()
    @Provides fun provideChamadoDao(db: MeuPontoDatabase): ChamadoDao = db.chamadoDao()
    @Provides fun provideHistoricoChamadoDao(db: MeuPontoDatabase): HistoricoChamadoDao = db.historicoChamadoDao()
    @Provides fun provideAuditLogDao(db: MeuPontoDatabase): AuditLogDao = db.auditLogDao()
    @Provides fun provideAjusteSalarialDao(db: MeuPontoDatabase): AjusteSalarialDao = db.ajusteSalarialDao()
    @Provides fun provideConfiguracaoEmpregoDao(db: MeuPontoDatabase): ConfiguracaoEmpregoDao = db.configuracaoEmpregoDao()
    @Provides fun provideConfiguracaoPontesAnoDao(db: MeuPontoDatabase): ConfiguracaoPontesAnoDao = db.configuracaoPontesAnoDao()
    @Provides fun provideFechamentoPeriodoDao(db: MeuPontoDatabase): FechamentoPeriodoDao = db.fechamentoPeriodoDao()
    @Provides fun provideFeriadoDao(db: MeuPontoDatabase): FeriadoDao = db.feriadoDao()
    @Provides fun provideGeocodificacaoCacheDao(db: MeuPontoDatabase): GeocodificacaoCacheDao = db.geocodificacaoCacheDao()
    @Provides fun provideHorarioDiaSemanaDao(db: MeuPontoDatabase): HorarioDiaSemanaDao = db.horarioDiaSemanaDao()
    @Provides fun provideUsuarioDao(db: MeuPontoDatabase): UsuarioDao = db.usuarioDao()
}