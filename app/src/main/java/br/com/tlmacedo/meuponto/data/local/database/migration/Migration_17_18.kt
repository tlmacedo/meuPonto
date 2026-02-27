// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/data/local/database/migration/Migration_17_18.kt
package br.com.tlmacedo.meuponto.data.local.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migração 17 → 18: Adiciona colunas 'data' e 'hora' na tabela pontos.
 *
 * - data: apenas a data (YYYY-MM-DD)
 * - hora: apenas a hora (HH:MM:SS)
 * - Corrige horaConsiderada para NOT NULL
 * - Adiciona índices necessários
 *
 * @author Thiago
 * @since 7.0.0
 */
val MIGRATION_17_18 = object : Migration(17, 18) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // 1. Criar tabela temporária com estrutura correta
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS pontos_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                empregoId INTEGER NOT NULL,
                dataHora TEXT NOT NULL,
                horaConsiderada TEXT NOT NULL,
                data TEXT NOT NULL,
                hora TEXT NOT NULL,
                observacao TEXT,
                isEditadoManualmente INTEGER NOT NULL DEFAULT 0,
                nsr TEXT,
                latitude REAL,
                longitude REAL,
                endereco TEXT,
                marcadorId INTEGER,
                justificativaInconsistencia TEXT,
                criadoEm TEXT NOT NULL,
                atualizadoEm TEXT NOT NULL,
                FOREIGN KEY(empregoId) REFERENCES empregos(id) ON DELETE CASCADE,
                FOREIGN KEY(marcadorId) REFERENCES marcadores(id) ON DELETE SET NULL
            )
            """.trimIndent()
        )

        // 2. Migrar dados existentes
        database.execSQL(
            """
            INSERT INTO pontos_new (
                id, empregoId, dataHora, horaConsiderada, data, hora,
                observacao, isEditadoManualmente, nsr, latitude, longitude, 
                endereco, marcadorId, justificativaInconsistencia, criadoEm, atualizadoEm
            )
            SELECT 
                id, empregoId, dataHora,
                COALESCE(horaConsiderada, SUBSTR(dataHora, 12, 8)),
                SUBSTR(dataHora, 1, 10),
                SUBSTR(dataHora, 12, 8),
                observacao, isEditadoManualmente, nsr, latitude, longitude, 
                endereco, marcadorId, justificativaInconsistencia, criadoEm, atualizadoEm
            FROM pontos
            """.trimIndent()
        )

        // 3. Remover tabela antiga
        database.execSQL("DROP TABLE pontos")

        // 4. Renomear nova tabela
        database.execSQL("ALTER TABLE pontos_new RENAME TO pontos")

        // 5. Criar todos os índices necessários
        database.execSQL("CREATE INDEX IF NOT EXISTS index_pontos_empregoId ON pontos(empregoId)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_pontos_dataHora ON pontos(dataHora)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_pontos_data ON pontos(data)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_pontos_marcadorId ON pontos(marcadorId)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_pontos_empregoId_dataHora ON pontos(empregoId, dataHora)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_pontos_empregoId_data ON pontos(empregoId, data)")
    }
}
