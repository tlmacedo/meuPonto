// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/data/local/database/migration/Migration_2_3.kt
package br.com.tlmacedo.meuponto.data.local.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migration da versão 2 para 3 do banco de dados.
 */
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Configuracoes_emprego
        if (!columnExists(db, "configuracoes_emprego", "cargaHorariaDiariaMinutos")) {
            db.execSQL("ALTER TABLE configuracoes_emprego ADD COLUMN cargaHorariaDiariaMinutos INTEGER NOT NULL DEFAULT 492")
        }
        if (!columnExists(db, "configuracoes_emprego", "intervaloMinimoMinutos")) {
            db.execSQL("ALTER TABLE configuracoes_emprego ADD COLUMN intervaloMinimoMinutos INTEGER NOT NULL DEFAULT 60")
        }

        // As colunas de tolerância de entrada/saída não devem ser adicionadas aqui 
        // se não estiverem no schema da versão 3. Se estiverem, devem ter o check.
        // Dado o histórico, seremos defensivos em tudo.

        if (!columnExists(db, "configuracoes_emprego", "toleranciaIntervaloMaisMinutos")) {
            db.execSQL("ALTER TABLE configuracoes_emprego ADD COLUMN toleranciaIntervaloMaisMinutos INTEGER NOT NULL DEFAULT 0")
        }
        if (!columnExists(db, "configuracoes_emprego", "toleranciaIntervaloMenosMinutos")) {
            db.execSQL("ALTER TABLE configuracoes_emprego ADD COLUMN toleranciaIntervaloMenosMinutos INTEGER NOT NULL DEFAULT 0")
        }
    }
}

/**
 * Auxiliar compartilhado para verificar se uma coluna existe.
 */
fun columnExists(db: SupportSQLiteDatabase, tableName: String, columnName: String): Boolean {
    return try {
        db.query("SELECT * FROM $tableName LIMIT 0").use { cursor ->
            cursor.getColumnIndex(columnName) != -1
        }
    } catch (e: Exception) {
        false
    }
}
