package br.com.tlmacedo.meuponto.data.local.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migração da versão 38 para 39:
 * - Criação da tabela geocodificacao_cache para cache de endereços.
 */
val MIGRATION_38_39 = object : Migration(38, 39) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `geocodificacao_cache` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                `latitude` REAL NOT NULL, 
                `longitude` REAL NOT NULL, 
                `endereco` TEXT NOT NULL, 
                `dataCriacao` TEXT NOT NULL
            )
        """.trimIndent()
        )

        db.execSQL(
            """
            CREATE UNIQUE INDEX IF NOT EXISTS `index_geocodificacao_cache_latitude_longitude` 
            ON `geocodificacao_cache` (`latitude`, `longitude`)
        """.trimIndent()
        )
    }
}
