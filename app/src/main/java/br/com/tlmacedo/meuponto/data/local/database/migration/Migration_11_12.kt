// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/data/local/database/migration/Migration_11_12.kt
package br.com.tlmacedo.meuponto.data.local.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migração do banco de dados versão 11 para 12.
 * Adiciona o campo 'motivo' na tabela audit_logs.
 *
 * @author Thiago
 * @since 3.5.0
 */
val MIGRATION_11_12 = object : Migration(11, 12) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Adicionar campo motivo na tabela audit_logs
        db.execSQL("ALTER TABLE audit_logs ADD COLUMN motivo TEXT DEFAULT NULL")
    }
}
