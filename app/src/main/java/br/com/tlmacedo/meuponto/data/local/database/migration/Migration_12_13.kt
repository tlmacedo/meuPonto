// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/data/local/database/migration/Migration_12_13.kt
package br.com.tlmacedo.meuponto.data.local.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migration para adicionar tabela de ausências.
 *
 * @author Thiago
 * @since 4.0.0
 */
val MIGRATION_12_13 = object : Migration(12, 13) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Criar tabela de ausências
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `ausencias` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `empregoId` INTEGER NOT NULL,
                `tipo` TEXT NOT NULL,
                `dataInicio` TEXT NOT NULL,
                `dataFim` TEXT NOT NULL,
                `descricao` TEXT,
                `observacao` TEXT,
                `ativo` INTEGER NOT NULL DEFAULT 1,
                `criadoEm` TEXT NOT NULL,
                `atualizadoEm` TEXT NOT NULL,
                FOREIGN KEY(`empregoId`) REFERENCES `empregos`(`id`) ON DELETE CASCADE
            )
        """.trimIndent())

        // Criar índices
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_ausencias_empregoId` ON `ausencias` (`empregoId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_ausencias_tipo` ON `ausencias` (`tipo`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_ausencias_dataInicio` ON `ausencias` (`dataInicio`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_ausencias_dataFim` ON `ausencias` (`dataFim`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_ausencias_ativo` ON `ausencias` (`ativo`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_ausencias_empregoId_dataInicio_dataFim` ON `ausencias` (`empregoId`, `dataInicio`, `dataFim`)")
    }
}
