// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/data/local/database/migration/Migration_10_11.kt
package br.com.tlmacedo.meuponto.data.local.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migração da versão 10 para 11.
 *
 * Adiciona tabelas para gerenciamento de feriados:
 * - feriados: Armazena feriados (nacionais, estaduais, municipais, pontes)
 * - configuracao_pontes_ano: Armazena cálculo de distribuição de pontes por ano
 *
 * @author Thiago
 * @since 3.0.0
 */
val MIGRATION_10_11 = object : Migration(10, 11) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // ========================================================================
        // 1. TABELA FERIADOS
        // ========================================================================
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS feriados (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                nome TEXT NOT NULL,
                tipo TEXT NOT NULL,
                recorrencia TEXT NOT NULL,
                abrangencia TEXT NOT NULL,
                diaMes TEXT,
                dataEspecifica TEXT,
                anoReferencia INTEGER,
                uf TEXT,
                municipio TEXT,
                empregoId INTEGER,
                ativo INTEGER NOT NULL DEFAULT 1,
                observacao TEXT,
                criadoEm TEXT NOT NULL,
                atualizadoEm TEXT NOT NULL,
                FOREIGN KEY (empregoId) REFERENCES empregos(id) ON DELETE CASCADE
            )
            """.trimIndent()
        )

        // Índices para feriados
        db.execSQL("CREATE INDEX IF NOT EXISTS index_feriados_empregoId ON feriados(empregoId)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_feriados_tipo ON feriados(tipo)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_feriados_anoReferencia ON feriados(anoReferencia)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_feriados_diaMes ON feriados(diaMes)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_feriados_dataEspecifica ON feriados(dataEspecifica)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_feriados_ativo ON feriados(ativo)")

        // ========================================================================
        // 2. TABELA CONFIGURACAO_PONTES_ANO
        // ========================================================================
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS configuracao_pontes_ano (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                empregoId INTEGER NOT NULL,
                ano INTEGER NOT NULL,
                diasPonte INTEGER NOT NULL,
                cargaHorariaPonteMinutos INTEGER NOT NULL,
                diasUteisAno INTEGER NOT NULL,
                adicionalDiarioMinutos INTEGER NOT NULL,
                observacao TEXT,
                calculadoEm TEXT NOT NULL,
                FOREIGN KEY (empregoId) REFERENCES empregos(id) ON DELETE CASCADE
            )
            """.trimIndent()
        )

        // Índices para configuracao_pontes_ano
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_configuracao_pontes_ano_empregoId_ano ON configuracao_pontes_ano(empregoId, ano)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_configuracao_pontes_ano_ano ON configuracao_pontes_ano(ano)")
    }
}
