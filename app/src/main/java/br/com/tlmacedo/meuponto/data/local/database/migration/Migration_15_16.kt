// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/data/local/database/migration/Migration_15_16.kt
package br.com.tlmacedo.meuponto.data.local.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migração para adicionar tipoFolga na tabela ausencias.
 *
 * Alterações:
 * - Adiciona coluna tipoFolga (DAY_OFF ou COMPENSACAO)
 * - Migra folgas existentes para COMPENSACAO (comportamento anterior)
 * - Remove coluna deprecated subTipoFolga
 *
 * @author Thiago
 * @since 6.1.0
 */
val MIGRATION_15_16 = object : Migration(15, 16) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // 1. Adiciona nova coluna tipoFolga
        db.execSQL(
            "ALTER TABLE ausencias ADD COLUMN tipoFolga TEXT DEFAULT NULL"
        )

        // 2. Migra folgas existentes para COMPENSACAO (mantém comportamento anterior)
        db.execSQL(
            "UPDATE ausencias SET tipoFolga = 'COMPENSACAO' WHERE tipo = 'FOLGA'"
        )

        // Nota: A coluna subTipoFolga será ignorada pelo Room (campo @Deprecated)
        // Não é necessário removê-la, pois SQLite não suporta DROP COLUMN facilmente
    }
}
