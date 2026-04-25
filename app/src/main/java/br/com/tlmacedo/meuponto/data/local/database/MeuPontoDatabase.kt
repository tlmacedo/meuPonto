// path: app/src/main/java/br/com/tlmacedo/meuponto/data/local/database/MeuPontoDatabase.kt
package br.com.tlmacedo.meuponto.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import br.com.tlmacedo.meuponto.data.local.database.converter.Converters
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
import br.com.tlmacedo.meuponto.data.local.database.entity.AjusteSalarialEntity
import br.com.tlmacedo.meuponto.data.local.database.entity.AjusteSaldoEntity
import br.com.tlmacedo.meuponto.data.local.database.entity.AuditLogEntity
import br.com.tlmacedo.meuponto.data.local.database.entity.AusenciaEntity
import br.com.tlmacedo.meuponto.data.local.database.entity.ChamadoEntity
import br.com.tlmacedo.meuponto.data.local.database.entity.CloudSyncQueueEntity
import br.com.tlmacedo.meuponto.data.local.database.entity.ConfiguracaoEmpregoEntity
import br.com.tlmacedo.meuponto.data.local.database.entity.ConfiguracaoPontesAnoEntity
import br.com.tlmacedo.meuponto.data.local.database.entity.EmpregoEntity
import br.com.tlmacedo.meuponto.data.local.database.entity.FechamentoPeriodoEntity
import br.com.tlmacedo.meuponto.data.local.database.entity.FeriadoEntity
import br.com.tlmacedo.meuponto.data.local.database.entity.FotoComprovanteEntity
import br.com.tlmacedo.meuponto.data.local.database.entity.GeocodificacaoCacheEntity
import br.com.tlmacedo.meuponto.data.local.database.entity.HistoricoCargoEntity
import br.com.tlmacedo.meuponto.data.local.database.entity.HistoricoChamadoEntity
import br.com.tlmacedo.meuponto.data.local.database.entity.HorarioDiaSemanaEntity
import br.com.tlmacedo.meuponto.data.local.database.entity.MarcadorEntity
import br.com.tlmacedo.meuponto.data.local.database.entity.PontoEntity
import br.com.tlmacedo.meuponto.data.local.database.entity.UsuarioEntity
import br.com.tlmacedo.meuponto.data.local.database.entity.VersaoJornadaEntity
import br.com.tlmacedo.meuponto.data.local.database.migration.*

@Database(
    entities = [
        AjusteSalarialEntity::class,
        AjusteSaldoEntity::class,
        AuditLogEntity::class,
        AusenciaEntity::class,
        ChamadoEntity::class,
        CloudSyncQueueEntity::class,
        ConfiguracaoEmpregoEntity::class,
        ConfiguracaoPontesAnoEntity::class,
        EmpregoEntity::class,
        FechamentoPeriodoEntity::class,
        FeriadoEntity::class,
        FotoComprovanteEntity::class,
        GeocodificacaoCacheEntity::class,
        HistoricoCargoEntity::class,
        HistoricoChamadoEntity::class,
        HorarioDiaSemanaEntity::class,
        MarcadorEntity::class,
        PontoEntity::class,
        UsuarioEntity::class,
        VersaoJornadaEntity::class
    ],
    version = 43,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class MeuPontoDatabase : RoomDatabase() {

    abstract fun ajusteSalarialDao(): AjusteSalarialDao
    abstract fun ajusteSaldoDao(): AjusteSaldoDao
    abstract fun auditLogDao(): AuditLogDao
    abstract fun ausenciaDao(): AusenciaDao
    abstract fun chamadoDao(): ChamadoDao
    abstract fun cloudSyncQueueDao(): CloudSyncQueueDao
    abstract fun configuracaoEmpregoDao(): ConfiguracaoEmpregoDao
    abstract fun configuracaoPontesAnoDao(): ConfiguracaoPontesAnoDao
    abstract fun empregoDao(): EmpregoDao
    abstract fun fechamentoPeriodoDao(): FechamentoPeriodoDao
    abstract fun feriadoDao(): FeriadoDao
    abstract fun fotoComprovanteDao(): FotoComprovanteDao
    abstract fun geocodificacaoCacheDao(): GeocodificacaoCacheDao
    abstract fun historicoCargoDao(): HistoricoCargoDao
    abstract fun historicoChamadoDao(): HistoricoChamadoDao
    abstract fun horarioDiaSemanaDao(): HorarioDiaSemanaDao
    abstract fun marcadorDao(): MarcadorDao
    abstract fun pontoDao(): PontoDao
    abstract fun usuarioDao(): UsuarioDao
    abstract fun versaoJornadaDao(): VersaoJornadaDao

    companion object {
        const val DATABASE_NAME = "meuponto.db"
        val MIGRATIONS: Array<Migration> = arrayOf(
            MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6,
            MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10, MIGRATION_10_11,
            MIGRATION_11_12, MIGRATION_12_13, MIGRATION_13_14, MIGRATION_14_15, MIGRATION_15_16,
            MIGRATION_16_17, MIGRATION_17_18, MIGRATION_18_19, MIGRATION_19_20, MIGRATION_20_21,
            MIGRATION_21_22, MIGRATION_22_23, MIGRATION_23_24, MIGRATION_24_25, MIGRATION_25_26,
            MIGRATION_26_27, MIGRATION_27_28, MIGRATION_28_29, MIGRATION_29_30, MIGRATION_30_31,
            MIGRATION_31_32, MIGRATION_32_33, MIGRATION_33_34, MIGRATION_34_35, MIGRATION_35_36,
            MIGRATION_36_37, MIGRATION_37_38, MIGRATION_38_39, MIGRATION_39_40, MIGRATION_40_41,
            MIGRATION_41_42, MIGRATION_42_43
        )
    }
}