// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/data/local/database/migration/Migration23To24.kt
package br.com.tlmacedo.meuponto.data.local.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migration 23 → 24: Adiciona suporte a Soft Delete na tabela pontos.
 *
 * Alterações:
 * - Adiciona coluna is_deleted (INTEGER DEFAULT 0)
 * - Adiciona coluna deleted_at (INTEGER nullable)
 * - Adiciona coluna updated_at (INTEGER DEFAULT 0)
 * - Cria índice em is_deleted para consultas eficientes
 *
 * @author Thiago
 * @since 11.0.0
 */
val MIGRATION_23_24 = object : Migration(23, 24) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Adicionar coluna is_deleted com valor padrão 0 (false)
        db.execSQL(
            """
            ALTER TABLE pontos 
            ADD COLUMN is_deleted INTEGER NOT NULL DEFAULT 0
            """
        )

        // Adicionar coluna deleted_at (nullable)
        db.execSQL(
            """
            ALTER TABLE pontos 
            ADD COLUMN deleted_at INTEGER DEFAULT NULL
            """
        )

        // Adicionar coluna updated_at
        db.execSQL(
            """
            ALTER TABLE pontos 
            ADD COLUMN updated_at INTEGER NOT NULL DEFAULT 0
            """
        )

        // Criar índice para consultas de soft delete
        db.execSQL(
            """
            CREATE INDEX IF NOT EXISTS index_pontos_is_deleted 
            ON pontos(is_deleted)
            """
        )

        // Atualizar updated_at com timestamp atual para registros existentes
        val currentTime = System.currentTimeMillis()
        db.execSQL(
            """
            UPDATE pontos SET updated_at = $currentTime WHERE updated_at = 0
            """
        )
    }
}
