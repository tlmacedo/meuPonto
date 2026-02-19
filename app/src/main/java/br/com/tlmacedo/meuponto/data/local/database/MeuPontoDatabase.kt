// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/data/local/database/MeuPontoDatabase.kt
package br.com.tlmacedo.meuponto.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import br.com.tlmacedo.meuponto.data.local.database.converter.Converters
import br.com.tlmacedo.meuponto.data.local.database.converter.FeriadoConverters
import br.com.tlmacedo.meuponto.data.local.database.dao.AjusteSaldoDao
import br.com.tlmacedo.meuponto.data.local.database.dao.AuditLogDao
import br.com.tlmacedo.meuponto.data.local.database.dao.ConfiguracaoEmpregoDao
import br.com.tlmacedo.meuponto.data.local.database.dao.ConfiguracaoPontesAnoDao
import br.com.tlmacedo.meuponto.data.local.database.dao.EmpregoDao
import br.com.tlmacedo.meuponto.data.local.database.dao.FechamentoPeriodoDao
import br.com.tlmacedo.meuponto.data.local.database.dao.FeriadoDao
import br.com.tlmacedo.meuponto.data.local.database.dao.HorarioDiaSemanaDao
import br.com.tlmacedo.meuponto.data.local.database.dao.MarcadorDao
import br.com.tlmacedo.meuponto.data.local.database.dao.PontoDao
import br.com.tlmacedo.meuponto.data.local.database.dao.VersaoJornadaDao
import br.com.tlmacedo.meuponto.data.local.database.entity.AjusteSaldoEntity
import br.com.tlmacedo.meuponto.data.local.database.entity.AuditLogEntity
import br.com.tlmacedo.meuponto.data.local.database.entity.ConfiguracaoEmpregoEntity
import br.com.tlmacedo.meuponto.data.local.database.entity.ConfiguracaoPontesAnoEntity
import br.com.tlmacedo.meuponto.data.local.database.entity.EmpregoEntity
import br.com.tlmacedo.meuponto.data.local.database.entity.FechamentoPeriodoEntity
import br.com.tlmacedo.meuponto.data.local.database.entity.FeriadoEntity
import br.com.tlmacedo.meuponto.data.local.database.entity.HorarioDiaSemanaEntity
import br.com.tlmacedo.meuponto.data.local.database.entity.MarcadorEntity
import br.com.tlmacedo.meuponto.data.local.database.entity.PontoEntity
import br.com.tlmacedo.meuponto.data.local.database.entity.VersaoJornadaEntity

/**
 * Classe principal do banco de dados Room.
 *
 * @since 1.0.0
 * @updated 3.0.0 - Versão 11: Adicionadas tabelas de Feriados e ConfiguracaoPontesAno
 */
@Database(
    entities = [
        PontoEntity::class,
        EmpregoEntity::class,
        ConfiguracaoEmpregoEntity::class,
        HorarioDiaSemanaEntity::class,
        VersaoJornadaEntity::class,
        AjusteSaldoEntity::class,
        FechamentoPeriodoEntity::class,
        MarcadorEntity::class,
        AuditLogEntity::class,
        // Novas entidades - Feriados
        FeriadoEntity::class,
        ConfiguracaoPontesAnoEntity::class
    ],
    version = 12,
    exportSchema = true
)
@TypeConverters(Converters::class, FeriadoConverters::class)
abstract class MeuPontoDatabase : RoomDatabase() {

    // DAOs existentes
    abstract fun pontoDao(): PontoDao
    abstract fun empregoDao(): EmpregoDao
    abstract fun configuracaoEmpregoDao(): ConfiguracaoEmpregoDao
    abstract fun horarioDiaSemanaDao(): HorarioDiaSemanaDao
    abstract fun versaoJornadaDao(): VersaoJornadaDao
    abstract fun ajusteSaldoDao(): AjusteSaldoDao
    abstract fun fechamentoPeriodoDao(): FechamentoPeriodoDao
    abstract fun marcadorDao(): MarcadorDao
    abstract fun auditLogDao(): AuditLogDao

    // Novos DAOs - Feriados
    abstract fun feriadoDao(): FeriadoDao
    abstract fun configuracaoPontesAnoDao(): ConfiguracaoPontesAnoDao

    companion object {
        const val DATABASE_NAME = "meuponto.db"
    }
}
