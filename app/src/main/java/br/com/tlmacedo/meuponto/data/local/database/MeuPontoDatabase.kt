// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/data/local/database/MeuPontoDatabase.kt
package br.com.tlmacedo.meuponto.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import br.com.tlmacedo.meuponto.data.local.database.converter.Converters
import br.com.tlmacedo.meuponto.data.local.database.dao.PontoDao
import br.com.tlmacedo.meuponto.data.local.database.entity.AjusteSaldoEntity
import br.com.tlmacedo.meuponto.data.local.database.entity.AuditLogEntity
import br.com.tlmacedo.meuponto.data.local.database.entity.ConfiguracaoEmpregoEntity
import br.com.tlmacedo.meuponto.data.local.database.entity.EmpregoEntity
import br.com.tlmacedo.meuponto.data.local.database.entity.FechamentoPeriodoEntity
import br.com.tlmacedo.meuponto.data.local.database.entity.HorarioDiaSemanaEntity
import br.com.tlmacedo.meuponto.data.local.database.entity.MarcadorEntity
import br.com.tlmacedo.meuponto.data.local.database.entity.PontoEntity

/**
 * Classe principal do banco de dados Room.
 *
 * @author Thiago
 * @since 1.0.0
 * @updated 2.0.0 - Adicionado suporte a múltiplos empregos e novas entidades
 */
@Database(
    entities = [
        PontoEntity::class,
        EmpregoEntity::class,
        ConfiguracaoEmpregoEntity::class,
        HorarioDiaSemanaEntity::class,
        AjusteSaldoEntity::class,
        FechamentoPeriodoEntity::class,
        MarcadorEntity::class,
        AuditLogEntity::class
    ],
    version = 2,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class MeuPontoDatabase : RoomDatabase() {

    abstract fun pontoDao(): PontoDao

    companion object {
        const val DATABASE_NAME = "meuponto.db"
    }
}
