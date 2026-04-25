// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/data/local/database/migration/Migration_19_20.kt
package br.com.tlmacedo.meuponto.data.local.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migração 19 → 20: Reorganização de responsabilidades.
 *
 * Move campos de jornada/banco de horas de ConfiguracaoEmprego para VersaoJornada,
 * permitindo versionamento temporal dessas configurações.
 *
 * @author Thiago
 * @since 8.0.0
 */
val MIGRATION_19_20 = object : Migration(19, 20) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // ════════════════════════════════════════════════════════════════════
        // ETAPA 1: Adicionar novas colunas em versoes_jornada
        // ════════════════════════════════════════════════════════════════════

        // Carga horária
        db.execSQL("ALTER TABLE versoes_jornada ADD COLUMN cargaHorariaDiariaMinutos INTEGER NOT NULL DEFAULT 480")
        db.execSQL("ALTER TABLE versoes_jornada ADD COLUMN acrescimoMinutosDiasPontes INTEGER NOT NULL DEFAULT 12")
        db.execSQL("ALTER TABLE versoes_jornada ADD COLUMN cargaHorariaSemanalMinutos INTEGER NOT NULL DEFAULT 2460")

        // Período/Saldo
        db.execSQL("ALTER TABLE versoes_jornada ADD COLUMN primeiroDiaSemana TEXT NOT NULL DEFAULT 'SEGUNDA'")
        db.execSQL("ALTER TABLE versoes_jornada ADD COLUMN diaInicioFechamentoRH INTEGER NOT NULL DEFAULT 1")
        db.execSQL("ALTER TABLE versoes_jornada ADD COLUMN zerarSaldoSemanal INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE versoes_jornada ADD COLUMN zerarSaldoPeriodoRH INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE versoes_jornada ADD COLUMN ocultarSaldoTotal INTEGER NOT NULL DEFAULT 0")

        // Banco de Horas
        db.execSQL("ALTER TABLE versoes_jornada ADD COLUMN bancoHorasHabilitado INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE versoes_jornada ADD COLUMN periodoBancoSemanas INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE versoes_jornada ADD COLUMN periodoBancoMeses INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE versoes_jornada ADD COLUMN dataInicioCicloBancoAtual TEXT")
        db.execSQL("ALTER TABLE versoes_jornada ADD COLUMN diasUteisLembreteFechamento INTEGER NOT NULL DEFAULT 3")
        db.execSQL("ALTER TABLE versoes_jornada ADD COLUMN habilitarSugestaoAjuste INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE versoes_jornada ADD COLUMN zerarBancoAntesPeriodo INTEGER NOT NULL DEFAULT 0")

        // Validação
        db.execSQL("ALTER TABLE versoes_jornada ADD COLUMN exigeJustificativaInconsistencia INTEGER NOT NULL DEFAULT 0")

        // ════════════════════════════════════════════════════════════════════
        // ETAPA 2: Copiar dados de configuracoes_emprego para versoes_jornada
        // ════════════════════════════════════════════════════════════════════

        db.execSQL(
            """
            UPDATE versoes_jornada 
            SET 
                cargaHorariaDiariaMinutos = COALESCE(
                    (SELECT cargaHorariaDiariaMinutos FROM configuracoes_emprego 
                     WHERE configuracoes_emprego.empregoId = versoes_jornada.empregoId),
                    480
                ),
                primeiroDiaSemana = COALESCE(
                    (SELECT primeiroDiaSemana FROM configuracoes_emprego 
                     WHERE configuracoes_emprego.empregoId = versoes_jornada.empregoId),
                    'SEGUNDA'
                ),
                diaInicioFechamentoRH = COALESCE(
                    (SELECT diaInicioFechamentoRH FROM configuracoes_emprego 
                     WHERE configuracoes_emprego.empregoId = versoes_jornada.empregoId),
                    1
                ),
                zerarSaldoSemanal = COALESCE(
                    (SELECT zerarSaldoSemanal FROM configuracoes_emprego 
                     WHERE configuracoes_emprego.empregoId = versoes_jornada.empregoId),
                    0
                ),
                zerarSaldoPeriodoRH = COALESCE(
                    (SELECT zerarSaldoPeriodoRH FROM configuracoes_emprego 
                     WHERE configuracoes_emprego.empregoId = versoes_jornada.empregoId),
                    0
                ),
                ocultarSaldoTotal = COALESCE(
                    (SELECT ocultarSaldoTotal FROM configuracoes_emprego 
                     WHERE configuracoes_emprego.empregoId = versoes_jornada.empregoId),
                    0
                ),
                bancoHorasHabilitado = COALESCE(
                    (SELECT bancoHorasHabilitado FROM configuracoes_emprego 
                     WHERE configuracoes_emprego.empregoId = versoes_jornada.empregoId),
                    0
                ),
                periodoBancoSemanas = COALESCE(
                    (SELECT periodoBancoSemanas FROM configuracoes_emprego 
                     WHERE configuracoes_emprego.empregoId = versoes_jornada.empregoId),
                    0
                ),
                periodoBancoMeses = COALESCE(
                    (SELECT periodoBancoMeses FROM configuracoes_emprego 
                     WHERE configuracoes_emprego.empregoId = versoes_jornada.empregoId),
                    0
                ),
                dataInicioCicloBancoAtual = (
                    SELECT dataInicioCicloBancoAtual FROM configuracoes_emprego 
                    WHERE configuracoes_emprego.empregoId = versoes_jornada.empregoId
                ),
                diasUteisLembreteFechamento = COALESCE(
                    (SELECT diasUteisLembreteFechamento FROM configuracoes_emprego 
                     WHERE configuracoes_emprego.empregoId = versoes_jornada.empregoId),
                    3
                ),
                habilitarSugestaoAjuste = COALESCE(
                    (SELECT habilitarSugestaoAjuste FROM configuracoes_emprego 
                     WHERE configuracoes_emprego.empregoId = versoes_jornada.empregoId),
                    0
                ),
                zerarBancoAntesPeriodo = COALESCE(
                    (SELECT zerarBancoAntesPeriodo FROM configuracoes_emprego 
                     WHERE configuracoes_emprego.empregoId = versoes_jornada.empregoId),
                    0
                ),
                exigeJustificativaInconsistencia = COALESCE(
                    (SELECT exigeJustificativaInconsistencia FROM configuracoes_emprego 
                     WHERE configuracoes_emprego.empregoId = versoes_jornada.empregoId),
                    0
                )
        """.trimIndent()
        )

        // Calcular cargaHorariaSemanalMinutos baseado na cargaHorariaDiariaMinutos * 5
        db.execSQL(
            """
            UPDATE versoes_jornada 
            SET cargaHorariaSemanalMinutos = cargaHorariaDiariaMinutos * 5
        """.trimIndent()
        )

        // ════════════════════════════════════════════════════════════════════
        // ETAPA 3: Recriar configuracoes_emprego SEM as colunas migradas
        // ════════════════════════════════════════════════════════════════════

        // Criar tabela temporária com nova estrutura
        db.execSQL(
            """
            CREATE TABLE configuracoes_emprego_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                empregoId INTEGER NOT NULL,
                habilitarNsr INTEGER NOT NULL DEFAULT 0,
                tipoNsr TEXT NOT NULL DEFAULT 'NUMERICO',
                habilitarLocalizacao INTEGER NOT NULL DEFAULT 0,
                localizacaoAutomatica INTEGER NOT NULL DEFAULT 0,
                exibirLocalizacaoDetalhes INTEGER NOT NULL DEFAULT 1,
                exibirDuracaoTurno INTEGER NOT NULL DEFAULT 1,
                exibirDuracaoIntervalo INTEGER NOT NULL DEFAULT 1,
                criadoEm TEXT NOT NULL,
                atualizadoEm TEXT NOT NULL,
                FOREIGN KEY (empregoId) REFERENCES empregos(id) ON DELETE CASCADE
            )
        """.trimIndent()
        )

        // Copiar dados mantendo apenas colunas relevantes
        db.execSQL(
            """
            INSERT INTO configuracoes_emprego_new (
                id, empregoId, habilitarNsr, tipoNsr, 
                habilitarLocalizacao, localizacaoAutomatica, exibirLocalizacaoDetalhes,
                exibirDuracaoTurno, exibirDuracaoIntervalo, criadoEm, atualizadoEm
            )
            SELECT 
                id, empregoId, habilitarNsr, tipoNsr,
                habilitarLocalizacao, localizacaoAutomatica, exibirLocalizacaoDetalhes,
                exibirDuracaoTurno, exibirDuracaoIntervalo, criadoEm, atualizadoEm
            FROM configuracoes_emprego
        """.trimIndent()
        )

        // Dropar tabela antiga e renomear
        db.execSQL("DROP TABLE configuracoes_emprego")
        db.execSQL("ALTER TABLE configuracoes_emprego_new RENAME TO configuracoes_emprego")

        // Recriar índice
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_configuracoes_emprego_empregoId ON configuracoes_emprego(empregoId)")
    }
}
