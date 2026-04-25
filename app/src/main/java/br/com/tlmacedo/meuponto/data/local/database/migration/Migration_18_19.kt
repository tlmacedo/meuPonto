// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/data/local/database/migration/Migration_18_19.kt
package br.com.tlmacedo.meuponto.data.local.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migração 18 → 19:
 *
 * 1. Remove toleranciaEntradaMinutos e toleranciaSaidaMinutos da tabela horarios_dia_semana
 * 2. Adiciona turnoMaximoMinutos na tabela versoes_jornada (default: 360 = 6h)
 *
 * @author Thiago
 * @since 7.2.0
 */
val MIGRATION_18_19 = object : Migration(18, 19) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // ====================================================================
        // PARTE 1: Remover toleranciaEntradaMinutos e toleranciaSaidaMinutos
        // de horarios_dia_semana
        // ====================================================================

        // 1.1 Criar tabela temporária sem as colunas removidas
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS horarios_dia_semana_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                empregoId INTEGER NOT NULL,
                versaoJornadaId INTEGER,
                diaSemana TEXT NOT NULL,
                ativo INTEGER NOT NULL DEFAULT 1,
                cargaHorariaMinutos INTEGER NOT NULL DEFAULT 492,
                entradaIdeal TEXT,
                saidaIntervaloIdeal TEXT,
                voltaIntervaloIdeal TEXT,
                saidaIdeal TEXT,
                intervaloMinimoMinutos INTEGER NOT NULL DEFAULT 60,
                toleranciaIntervaloMaisMinutos INTEGER NOT NULL DEFAULT 0,
                criadoEm TEXT NOT NULL,
                atualizadoEm TEXT NOT NULL,
                FOREIGN KEY (empregoId) REFERENCES empregos(id) ON DELETE CASCADE,
                FOREIGN KEY (versaoJornadaId) REFERENCES versoes_jornada(id) ON DELETE CASCADE
            )
        """.trimIndent()
        )

        // 1.2 Copiar dados (exceto as colunas removidas)
        db.execSQL(
            """
            INSERT INTO horarios_dia_semana_new (
                id, empregoId, versaoJornadaId, diaSemana, ativo, cargaHorariaMinutos,
                entradaIdeal, saidaIntervaloIdeal, voltaIntervaloIdeal, saidaIdeal,
                intervaloMinimoMinutos, toleranciaIntervaloMaisMinutos,
                criadoEm, atualizadoEm
            )
            SELECT 
                id, empregoId, versaoJornadaId, diaSemana, ativo, cargaHorariaMinutos,
                entradaIdeal, saidaIntervaloIdeal, voltaIntervaloIdeal, saidaIdeal,
                intervaloMinimoMinutos, toleranciaIntervaloMaisMinutos,
                criadoEm, atualizadoEm
            FROM horarios_dia_semana
        """.trimIndent()
        )

        // 1.3 Remover tabela antiga
        db.execSQL("DROP TABLE horarios_dia_semana")

        // 1.4 Renomear nova tabela
        db.execSQL("ALTER TABLE horarios_dia_semana_new RENAME TO horarios_dia_semana")

        // 1.5 Recriar índices
        db.execSQL("CREATE INDEX IF NOT EXISTS index_horarios_dia_semana_empregoId ON horarios_dia_semana(empregoId)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_horarios_dia_semana_versaoJornadaId ON horarios_dia_semana(versaoJornadaId)")
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_horarios_dia_semana_versaoJornadaId_diaSemana ON horarios_dia_semana(versaoJornadaId, diaSemana)")

        // ====================================================================
        // PARTE 2: Adicionar turnoMaximoMinutos em versoes_jornada
        // Default: 360 minutos (6 horas)
        // ====================================================================
        db.execSQL("ALTER TABLE versoes_jornada ADD COLUMN turnoMaximoMinutos INTEGER NOT NULL DEFAULT 360")
    }
}
