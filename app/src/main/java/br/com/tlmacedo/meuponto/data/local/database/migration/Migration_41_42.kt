package br.com.tlmacedo.meuponto.data.local.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migração da versão 41 para 42.
 * Adiciona campos de controle de preenchimento automático (OCR) na tabela de pontos.
 *
 * @author Thiago
 * @since 13.0.0
 */
val MIGRATION_41_42 = object : Migration(41, 42) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Adiciona coluna nsr_auto_filled
        db.execSQL("ALTER TABLE pontos ADD COLUMN nsr_auto_filled INTEGER NOT NULL DEFAULT 0")

        // Adiciona coluna hora_auto_filled
        db.execSQL("ALTER TABLE pontos ADD COLUMN hora_auto_filled INTEGER NOT NULL DEFAULT 0")

        // Adiciona coluna data_auto_filled
        db.execSQL("ALTER TABLE pontos ADD COLUMN data_auto_filled INTEGER NOT NULL DEFAULT 0")
    }
}
