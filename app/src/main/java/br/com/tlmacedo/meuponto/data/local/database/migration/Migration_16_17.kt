// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/data/local/database/migration/Migration_16_17.kt
package br.com.tlmacedo.meuponto.data.local.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migração 16 → 17: Adiciona coluna horaConsiderada na tabela pontos.
 *
 * A horaConsiderada armazena a hora que será usada nos cálculos (com tolerância aplicada).
 * Para registros existentes, horaConsiderada = hora extraída de dataHora.
 *
 * @author Thiago
 * @since 7.0.0
 */
val MIGRATION_16_17 = object : Migration(16, 17) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Verifica se a coluna já existe
        val cursor = database.query("PRAGMA table_info(pontos)")
        var colunaExiste = false

        cursor.use {
            val nameIndex = it.getColumnIndex("name")
            while (it.moveToNext()) {
                if (it.getString(nameIndex) == "horaConsiderada") {
                    colunaExiste = true
                    break
                }
            }
        }

        if (!colunaExiste) {
            // Adiciona a coluna horaConsiderada com default temporário
            database.execSQL(
                "ALTER TABLE pontos ADD COLUMN horaConsiderada TEXT NOT NULL DEFAULT '00:00:00'"
            )
        }

        // Atualiza registros existentes: horaConsiderada = hora extraída de dataHora (HH:MM:SS)
        // dataHora está no formato ISO: 2026-02-26T14:30:00
        database.execSQL(
            """
            UPDATE pontos 
            SET horaConsiderada = SUBSTR(dataHora, 12, 8) 
            WHERE horaConsiderada = '' OR horaConsiderada = '00:00:00' OR horaConsiderada IS NULL
            """.trimIndent()
        )
    }
}
