// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/data/local/database/migration/Migration_9_10.kt
package br.com.tlmacedo.meuponto.data.local.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Migração da versão 9 para 10.
 *
 * Adiciona sistema de versionamento de jornadas:
 * 1. Cria tabela versoes_jornada
 * 2. Recria horarios_dia_semana com versaoJornadaId e foreign key
 * 3. Migra dados existentes para primeira versão
 *
 * @author Thiago
 * @since 2.7.0
 */
val MIGRATION_9_10 = object : Migration(9, 10) {
    override fun migrate(db: SupportSQLiteDatabase) {
        val agora = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        val dataInicioPadrao = LocalDate.of(2020, 1, 1).format(DateTimeFormatter.ISO_LOCAL_DATE)

        // ====================================================================
        // PASSO 1: Criar tabela versoes_jornada
        // ====================================================================
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS versoes_jornada (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                empregoId INTEGER NOT NULL,
                dataInicio TEXT NOT NULL,
                dataFim TEXT,
                descricao TEXT,
                numeroVersao INTEGER NOT NULL DEFAULT 1,
                vigente INTEGER NOT NULL DEFAULT 1,
                jornadaMaximaDiariaMinutos INTEGER NOT NULL DEFAULT 600,
                intervaloMinimoInterjornadaMinutos INTEGER NOT NULL DEFAULT 660,
                toleranciaIntervaloMaisMinutos INTEGER NOT NULL DEFAULT 0,
                criadoEm TEXT NOT NULL,
                atualizadoEm TEXT NOT NULL,
                FOREIGN KEY (empregoId) REFERENCES empregos(id) ON DELETE CASCADE
            )
        """
        )

        // Criar índices para versoes_jornada
        db.execSQL("CREATE INDEX IF NOT EXISTS index_versoes_jornada_empregoId ON versoes_jornada(empregoId)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_versoes_jornada_empregoId_dataInicio ON versoes_jornada(empregoId, dataInicio)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_versoes_jornada_empregoId_dataFim ON versoes_jornada(empregoId, dataFim)")

        // ====================================================================
        // PASSO 2: Criar versão inicial para cada emprego existente
        // ====================================================================
        db.execSQL(
            """
            INSERT INTO versoes_jornada (
                empregoId, 
                dataInicio, 
                dataFim, 
                descricao, 
                numeroVersao, 
                vigente,
                jornadaMaximaDiariaMinutos,
                intervaloMinimoInterjornadaMinutos,
                toleranciaIntervaloMaisMinutos,
                criadoEm, 
                atualizadoEm
            )
            SELECT 
                e.id,
                COALESCE(e.dataInicioTrabalho, '$dataInicioPadrao'),
                NULL,
                'Jornada inicial (migração)',
                1,
                1,
                COALESCE(c.jornadaMaximaDiariaMinutos, 600),
                COALESCE(c.intervaloMinimoInterjornadaMinutos, 660),
                COALESCE(c.toleranciaIntervaloMaisMinutos, 0),
                '$agora',
                '$agora'
            FROM empregos e
            LEFT JOIN configuracoes_emprego c ON c.empregoId = e.id
        """
        )

        // ====================================================================
        // PASSO 3: Recriar horarios_dia_semana com a nova estrutura
        // (SQLite não permite adicionar foreign key em tabela existente)
        // ====================================================================

        // 3.1 Renomear tabela antiga
        db.execSQL("ALTER TABLE horarios_dia_semana RENAME TO horarios_dia_semana_old")

        // 3.2 Criar nova tabela com foreign keys corretas
        db.execSQL(
            """
            CREATE TABLE horarios_dia_semana (
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
                toleranciaEntradaMinutos INTEGER,
                toleranciaSaidaMinutos INTEGER,
                criadoEm TEXT NOT NULL,
                atualizadoEm TEXT NOT NULL,
                FOREIGN KEY (empregoId) REFERENCES empregos(id) ON DELETE CASCADE,
                FOREIGN KEY (versaoJornadaId) REFERENCES versoes_jornada(id) ON DELETE CASCADE
            )
        """
        )

        // 3.3 Copiar dados da tabela antiga para a nova, vinculando com a versão criada
        db.execSQL(
            """
            INSERT INTO horarios_dia_semana (
                id, empregoId, versaoJornadaId, diaSemana, ativo, 
                cargaHorariaMinutos, entradaIdeal, saidaIntervaloIdeal, 
                voltaIntervaloIdeal, saidaIdeal, intervaloMinimoMinutos,
                toleranciaIntervaloMaisMinutos, toleranciaEntradaMinutos,
                toleranciaSaidaMinutos, criadoEm, atualizadoEm
            )
            SELECT 
                h.id, h.empregoId, 
                (SELECT v.id FROM versoes_jornada v WHERE v.empregoId = h.empregoId LIMIT 1),
                h.diaSemana, h.ativo, h.cargaHorariaMinutos, h.entradaIdeal,
                h.saidaIntervaloIdeal, h.voltaIntervaloIdeal, h.saidaIdeal,
                h.intervaloMinimoMinutos, h.toleranciaIntervaloMaisMinutos,
                h.toleranciaEntradaMinutos, h.toleranciaSaidaMinutos,
                h.criadoEm, h.atualizadoEm
            FROM horarios_dia_semana_old h
        """
        )

        // 3.4 Remover tabela antiga
        db.execSQL("DROP TABLE horarios_dia_semana_old")

        // ====================================================================
        // PASSO 4: Criar índices para horarios_dia_semana
        // ====================================================================
        db.execSQL("CREATE INDEX IF NOT EXISTS index_horarios_dia_semana_empregoId ON horarios_dia_semana(empregoId)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_horarios_dia_semana_versaoJornadaId ON horarios_dia_semana(versaoJornadaId)")
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_horarios_dia_semana_versaoJornadaId_diaSemana ON horarios_dia_semana(versaoJornadaId, diaSemana)")
    }
}
