package br.com.tlmacedo.meuponto.data.local.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migração da versão 39 para 40:
 * - Adição dos campos latitude, longitude e raioGeofencing na tabela configuracoes_emprego.
 */
val MIGRATION_39_40 = object : Migration(39, 40) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `configuracoes_emprego` ADD COLUMN `latitude` REAL")
        db.execSQL("ALTER TABLE `configuracoes_emprego` ADD COLUMN `longitude` REAL")
        db.execSQL("ALTER TABLE `configuracoes_emprego` ADD COLUMN `raioGeofencing` INTEGER NOT NULL DEFAULT 200")
    }
}
