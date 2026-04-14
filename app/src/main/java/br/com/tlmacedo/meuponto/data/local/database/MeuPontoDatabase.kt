// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/data/local/database/MeuPontoDatabase.kt
package br.com.tlmacedo.meuponto.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import br.com.tlmacedo.meuponto.data.local.database.converter.Converters
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
import br.com.tlmacedo.meuponto.data.local.database.dao.HistoricoCargoDao
import br.com.tlmacedo.meuponto.data.local.database.dao.HorarioDiaSemanaDao
import br.com.tlmacedo.meuponto.data.local.database.dao.MarcadorDao
import br.com.tlmacedo.meuponto.data.local.database.dao.PontoDao
import br.com.tlmacedo.meuponto.data.local.database.dao.UsuarioDao
import br.com.tlmacedo.meuponto.data.local.database.dao.VersaoJornadaDao
import br.com.tlmacedo.meuponto.data.local.database.entity.AjusteSalarialEntity
import br.com.tlmacedo.meuponto.data.local.database.entity.AjusteSaldoEntity
import br.com.tlmacedo.meuponto.data.local.database.entity.AuditLogEntity
import br.com.tlmacedo.meuponto.data.local.database.entity.AusenciaEntity
import br.com.tlmacedo.meuponto.data.local.database.entity.ConfiguracaoEmpregoEntity
import br.com.tlmacedo.meuponto.data.local.database.entity.ConfiguracaoPontesAnoEntity
import br.com.tlmacedo.meuponto.data.local.database.entity.EmpregoEntity
import br.com.tlmacedo.meuponto.data.local.database.entity.FechamentoPeriodoEntity
import br.com.tlmacedo.meuponto.data.local.database.entity.FeriadoEntity
import br.com.tlmacedo.meuponto.data.local.database.entity.FotoComprovanteEntity
import br.com.tlmacedo.meuponto.data.local.database.entity.HistoricoCargoEntity
import br.com.tlmacedo.meuponto.data.local.database.entity.HorarioDiaSemanaEntity
import br.com.tlmacedo.meuponto.data.local.database.entity.MarcadorEntity
import br.com.tlmacedo.meuponto.data.local.database.entity.PontoEntity
import br.com.tlmacedo.meuponto.data.local.database.entity.UsuarioEntity
import br.com.tlmacedo.meuponto.data.local.database.entity.VersaoJornadaEntity

@Database(
    entities = [
        AjusteSalarialEntity::class,
        AjusteSaldoEntity::class,
        AuditLogEntity::class,
        ConfiguracaoEmpregoEntity::class,
        ConfiguracaoPontesAnoEntity::class,
        EmpregoEntity::class,
        FechamentoPeriodoEntity::class,
        FeriadoEntity::class,
        FotoComprovanteEntity::class,
        HistoricoCargoEntity::class,
        HorarioDiaSemanaEntity::class,
        MarcadorEntity::class,
        PontoEntity::class,
        VersaoJornadaEntity::class,
        UsuarioEntity::class,
        AusenciaEntity::class
    ],
    version = 37, // Incremented version to fix IllegalStateException after schema change
    exportSchema = true // Mantenha como true para gerar o schema.json
)
@TypeConverters(Converters::class)
abstract class MeuPontoDatabase : RoomDatabase() {
    abstract fun ajusteSalarialDao(): AjusteSalarialDao
    abstract fun ajusteSaldoDao(): AjusteSaldoDao
    abstract fun auditLogDao(): AuditLogDao
    abstract fun configuracaoEmpregoDao(): ConfiguracaoEmpregoDao
    abstract fun configuracaoPontesAnoDao(): ConfiguracaoPontesAnoDao
    abstract fun empregoDao(): EmpregoDao
    abstract fun fechamentoPeriodoDao(): FechamentoPeriodoDao
    abstract fun feriadoDao(): FeriadoDao
    abstract fun fotoComprovanteDao(): FotoComprovanteDao
    abstract fun historicoCargoDao(): HistoricoCargoDao
    abstract fun horarioDiaSemanaDao(): HorarioDiaSemanaDao
    abstract fun marcadorDao(): MarcadorDao
    abstract fun pontoDao(): PontoDao
    abstract fun versaoJornadaDao(): VersaoJornadaDao
    abstract fun usuarioDao(): UsuarioDao
    abstract fun ausenciaDao(): AusenciaDao

    companion object {
        const val DATABASE_NAME = "meuponto.db"
    }
}
