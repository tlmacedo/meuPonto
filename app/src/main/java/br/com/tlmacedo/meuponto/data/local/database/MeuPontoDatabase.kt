// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/data/local/database/MeuPontoDatabase.kt
package br.com.tlmacedo.meuponto.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import br.com.tlmacedo.meuponto.data.local.database.converter.Converters
import br.com.tlmacedo.meuponto.data.local.database.dao.*
import br.com.tlmacedo.meuponto.data.local.database.entity.*

@Database(
    entities = [
        AjusteSaldoEntity::class,
        AuditLogEntity::class,
        ConfiguracaoEmpregoEntity::class,
        ConfiguracaoPontesAnoEntity::class,
        EmpregoEntity::class,
        FechamentoPeriodoEntity::class,
        FeriadoEntity::class,
        FotoComprovanteEntity::class,
        HorarioDiaSemanaEntity::class,
        MarcadorEntity::class,
        PontoEntity::class,
        VersaoJornadaEntity::class,
        UsuarioEntity::class,
        AusenciaEntity::class
    ],
    version = 27, // Ajuste a versão conforme necessário para suas migrações
    exportSchema = true // Mantenha como true para gerar o schema.json
)
@TypeConverters(Converters::class)
abstract class MeuPontoDatabase : RoomDatabase() {
    abstract fun ajusteSaldoDao(): AjusteSaldoDao
    abstract fun auditLogDao(): AuditLogDao
    abstract fun configuracaoEmpregoDao(): ConfiguracaoEmpregoDao
    abstract fun configuracaoPontesAnoDao(): ConfiguracaoPontesAnoDao
    abstract fun empregoDao(): EmpregoDao
    abstract fun fechamentoPeriodoDao(): FechamentoPeriodoDao
    abstract fun feriadoDao(): FeriadoDao
    abstract fun fotoComprovanteDao(): FotoComprovanteDao
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
