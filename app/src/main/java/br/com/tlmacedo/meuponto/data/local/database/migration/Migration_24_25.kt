package br.com.tlmacedo.meuponto.data.local.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migration 24 → 25: Migração vazia.
 *
 * A lógica original que alterava a tabela 'ajustes_saldo' foi movida para MIGRATION_23_24
 * para refletir corretamente o schema exportado na versão 24.
 */
val MIGRATION_24_25 = object : Migration(24, 25) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Nada a fazer, alterações movidas para 23->24
    }
}
