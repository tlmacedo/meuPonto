// Arquivo: Migration_7_8.kt
package br.com.tlmacedo.meuponto.data.local.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migração 7 → 8: Remove coluna 'tipo' da tabela pontos.
 *
 * O tipo do ponto (ENTRADA/SAÍDA) passa a ser calculado em runtime
 * baseado na posição do ponto na lista ordenada por dataHora:
 * - Índice par (0, 2, 4...) = ENTRADA
 * - Índice ímpar (1, 3, 5...) = SAÍDA
 *
 * @author Thiago
 * @since 2.1.0
 */
val MIGRATION_7_8 = object : Migration(7, 8) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // SQLite não suporta DROP COLUMN diretamente
        // Precisamos recriar a tabela sem a coluna 'tipo'
        
        // 1. Criar tabela temporária sem a coluna tipo
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS pontos_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                empregoId INTEGER NOT NULL DEFAULT 1,
                dataHora TEXT NOT NULL,
                observacao TEXT,
                isEditadoManualmente INTEGER NOT NULL DEFAULT 0,
                nsr TEXT,
                latitude REAL,
                longitude REAL,
                endereco TEXT,
                marcadorId INTEGER,
                justificativaInconsistencia TEXT,
                horaConsiderada TEXT,
                criadoEm TEXT NOT NULL,
                atualizadoEm TEXT NOT NULL,
                data TEXT NOT NULL,
                hora TEXT NOT NULL,
                FOREIGN KEY (empregoId) REFERENCES empregos(id) ON DELETE CASCADE,
                FOREIGN KEY (marcadorId) REFERENCES marcadores(id) ON DELETE SET NULL
            )
        """.trimIndent())
        
        // 2. Copiar dados (exceto coluna tipo)
        db.execSQL("""
            INSERT INTO pontos_new (
                id, empregoId, dataHora, observacao, isEditadoManualmente,
                nsr, latitude, longitude, endereco, marcadorId,
                justificativaInconsistencia, horaConsiderada,
                criadoEm, atualizadoEm, data, hora
            )
            SELECT 
                id, empregoId, dataHora, observacao, isEditadoManualmente,
                nsr, latitude, longitude, endereco, marcadorId,
                justificativaInconsistencia, horaConsiderada,
                criadoEm, atualizadoEm, data, hora
            FROM pontos
        """.trimIndent())
        
        // 3. Remover tabela antiga
        db.execSQL("DROP TABLE pontos")
        
        // 4. Renomear nova tabela
        db.execSQL("ALTER TABLE pontos_new RENAME TO pontos")
        
        // 5. Recriar índices
        db.execSQL("CREATE INDEX IF NOT EXISTS index_pontos_empregoId ON pontos(empregoId)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_pontos_data ON pontos(data)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_pontos_empregoId_data ON pontos(empregoId, data)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_pontos_marcadorId ON pontos(marcadorId)")
    }
}
