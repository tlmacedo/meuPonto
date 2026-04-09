package br.com.tlmacedo.meuponto.data.local.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migração da versão 30 para 31:
 * - Adiciona novas colunas à tabela 'versoes_jornada':
 *   - 'intervaloMinimoDescansoMinutos' (Default: 15)
 *   - 'toleranciaRetornoIntervaloMinutos' (Default: 5)
 */
val MIGRATION_30_31 = Migration(30, 31) { database ->
    database.execSQL("ALTER TABLE `versoes_jornada` ADD COLUMN `intervaloMinimoDescansoMinutos` INTEGER NOT NULL DEFAULT 15")
    database.execSQL("ALTER TABLE `versoes_jornada` ADD COLUMN `toleranciaRetornoIntervaloMinutos` INTEGER NOT NULL DEFAULT 5")
}
