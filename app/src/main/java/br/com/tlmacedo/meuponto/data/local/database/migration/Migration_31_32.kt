package br.com.tlmacedo.meuponto.data.local.database.migration

import androidx.room.migration.Migration

/**
 * Migração da versão 31 para 32:
 * - Adiciona a coluna 'logo' à tabela 'empregos' para armazenar a URI da logo da empresa.
 */
val MIGRATION_31_32 = Migration(31, 32) { database ->
    database.execSQL("ALTER TABLE `empregos` ADD COLUMN `logo` TEXT")
    database.execSQL("ALTER TABLE `pontos` ADD COLUMN `foto_origem` INTEGER NOT NULL DEFAULT 0")
}
