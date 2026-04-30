package br.com.tlmacedo.meuponto.data.local.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migração da versão 43 para 44.
 * Corrige discrepâncias na tabela 'chamados' e 'historico_chamados' 
 * que podem ter ocorrido durante o desenvolvimento da versão 43.
 */
val MIGRATION_43_44 = object : Migration(43, 44) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // 1. Garantir que a tabela cloud_sync_queue exista (pode ter faltado em migrações anteriores)
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `cloud_sync_queue` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                `entityType` TEXT NOT NULL, 
                `entityId` INTEGER NOT NULL, 
                `operation` TEXT NOT NULL, 
                `payload` TEXT, 
                `tentativas` INTEGER NOT NULL, 
                `ultimoErro` TEXT, 
                `criadoEm` TEXT NOT NULL, 
                `sincronizadoEm` TEXT
            )
            """.trimIndent()
        )

        // 2. Adicionar colunas faltantes na tabela 'chamados'
        // SQLite não suporta IF NOT EXISTS em ALTER TABLE ADD COLUMN diretamente em todas as versões,
        // mas o Room usa uma versão que geralmente suporta ou podemos usar try-catch.

        try {
            db.execSQL("ALTER TABLE `chamados` ADD COLUMN `passosParaReproduzir` TEXT")
        } catch (e: Exception) {
            // Coluna já existe
        }

        try {
            db.execSQL("ALTER TABLE `chamados` ADD COLUMN `deviceInfo` TEXT")
        } catch (e: Exception) {
            // Coluna já existe
        }

        try {
            db.execSQL("ALTER TABLE `chamados` ADD COLUMN `usuarioId` INTEGER")
        } catch (e: Exception) {
            // Coluna já existe
        }

        // 3. Adicionar colunas faltantes na tabela 'historico_chamados'
        try {
            db.execSQL("ALTER TABLE `historico_chamados` ADD COLUMN `chamadoIdentificador` TEXT NOT NULL DEFAULT ''")
        } catch (e: Exception) {
            // Coluna já existe
        }
    }
}
