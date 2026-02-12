package br.com.tlmacedo.meuponto.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import br.com.tlmacedo.meuponto.data.local.database.converter.Converters
import br.com.tlmacedo.meuponto.data.local.database.dao.PontoDao
import br.com.tlmacedo.meuponto.data.local.database.entity.PontoEntity

/**
 * Classe principal do banco de dados Room.
 *
 * @author Thiago
 * @since 1.0.0
 */
@Database(
    entities = [
        PontoEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class MeuPontoDatabase : RoomDatabase() {

    abstract fun pontoDao(): PontoDao

    companion object {
        const val DATABASE_NAME = "meuponto.db"
    }
}
