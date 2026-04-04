// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/data/local/database/migration/Migration23To24.kt
package br.com.tlmacedo.meuponto.data.local.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Migration 23 → 24: Atualiza a tabela ajustes_saldo com novas colunas de controle.
 *
 * Alterações (ajustes_saldo):
 * - Adiciona coluna atualizadoEm (TEXT NOT NULL)
 * - Adiciona coluna tipo (TEXT NOT NULL DEFAULT 'MANUAL')
 * - Cria índice em tipo
 *
 * @author Thiago
 * @since 11.0.0
 */
val MIGRATION_23_24 = object : Migration(23, 24) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // --- Tabela ajustes_saldo ---
        // Valor padrão válido para LocalDateTime
        val agora = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

        // Adicionar coluna 'atualizadoEm' na tabela ajustes_saldo
        db.execSQL(
            """
            ALTER TABLE ajustes_saldo 
            ADD COLUMN atualizadoEm TEXT NOT NULL DEFAULT '$agora'
            """
        )

        // Adicionar coluna 'tipo' na tabela ajustes_saldo
        db.execSQL(
            """
            ALTER TABLE ajustes_saldo 
            ADD COLUMN tipo TEXT NOT NULL DEFAULT 'MANUAL'
            """
        )

        // Criar índice para a coluna 'tipo'
        db.execSQL(
            """
            CREATE INDEX IF NOT EXISTS index_ajustes_saldo_tipo 
            ON ajustes_saldo(tipo)
            """
        )
    }
}
