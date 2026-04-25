// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/data/local/database/migration/Migration_8_9.kt
package br.com.tlmacedo.meuponto.data.local.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migração 8 → 9: Remove toleranciaEntradaMinutos e toleranciaSaidaMinutos
 * da tabela configuracoes_emprego.
 *
 * As tolerâncias de entrada/saída agora são configuradas apenas por dia da semana
 * na tabela horarios_dia_semana, simplificando o modelo.
 *
 * @author Thiago
 * @since 2.5.0
 */
val MIGRATION_8_9 = object : Migration(8, 9) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // SQLite não suporta DROP COLUMN diretamente
        // Precisamos recriar a tabela sem as colunas

        // 1. Criar tabela temporária sem as colunas removidas
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS configuracoes_emprego_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                empregoId INTEGER NOT NULL,
                cargaHorariaDiariaMinutos INTEGER NOT NULL DEFAULT 492,
                jornadaMaximaDiariaMinutos INTEGER NOT NULL DEFAULT 600,
                intervaloMinimoInterjornadaMinutos INTEGER NOT NULL DEFAULT 660,
                intervaloMinimoMinutos INTEGER NOT NULL DEFAULT 60,
                toleranciaIntervaloMaisMinutos INTEGER NOT NULL DEFAULT 0,
                exigeJustificativaInconsistencia INTEGER NOT NULL DEFAULT 0,
                habilitarNsr INTEGER NOT NULL DEFAULT 0,
                tipoNsr TEXT NOT NULL DEFAULT 'NUMERICO',
                habilitarLocalizacao INTEGER NOT NULL DEFAULT 0,
                localizacaoAutomatica INTEGER NOT NULL DEFAULT 0,
                exibirLocalizacaoDetalhes INTEGER NOT NULL DEFAULT 1,
                exibirDuracaoTurno INTEGER NOT NULL DEFAULT 1,
                exibirDuracaoIntervalo INTEGER NOT NULL DEFAULT 1,
                primeiroDiaSemana TEXT NOT NULL DEFAULT 'SEGUNDA',
                primeiroDiaMes INTEGER NOT NULL DEFAULT 1,
                zerarSaldoSemanal INTEGER NOT NULL DEFAULT 0,
                zerarSaldoMensal INTEGER NOT NULL DEFAULT 0,
                ocultarSaldoTotal INTEGER NOT NULL DEFAULT 0,
                periodoBancoHorasMeses INTEGER NOT NULL DEFAULT 0,
                ultimoFechamentoBanco TEXT,
                diasUteisLembreteFechamento INTEGER NOT NULL DEFAULT 3,
                habilitarSugestaoAjuste INTEGER NOT NULL DEFAULT 0,
                zerarBancoAntesPeriodo INTEGER NOT NULL DEFAULT 0,
                criadoEm TEXT NOT NULL,
                atualizadoEm TEXT NOT NULL,
                FOREIGN KEY (empregoId) REFERENCES empregos(id) ON DELETE CASCADE
            )
        """.trimIndent()
        )

        // 2. Copiar dados (exceto as colunas removidas)
        db.execSQL(
            """
            INSERT INTO configuracoes_emprego_new (
                id, empregoId, cargaHorariaDiariaMinutos, jornadaMaximaDiariaMinutos,
                intervaloMinimoInterjornadaMinutos, intervaloMinimoMinutos,
                toleranciaIntervaloMaisMinutos, exigeJustificativaInconsistencia,
                habilitarNsr, tipoNsr, habilitarLocalizacao, localizacaoAutomatica,
                exibirLocalizacaoDetalhes, exibirDuracaoTurno, exibirDuracaoIntervalo,
                primeiroDiaSemana, primeiroDiaMes, zerarSaldoSemanal, zerarSaldoMensal,
                ocultarSaldoTotal, periodoBancoHorasMeses, ultimoFechamentoBanco,
                diasUteisLembreteFechamento, habilitarSugestaoAjuste, zerarBancoAntesPeriodo,
                criadoEm, atualizadoEm
            )
            SELECT 
                id, empregoId, cargaHorariaDiariaMinutos, jornadaMaximaDiariaMinutos,
                intervaloMinimoInterjornadaMinutos, intervaloMinimoMinutos,
                toleranciaIntervaloMaisMinutos, exigeJustificativaInconsistencia,
                habilitarNsr, tipoNsr, habilitarLocalizacao, localizacaoAutomatica,
                exibirLocalizacaoDetalhes, exibirDuracaoTurno, exibirDuracaoIntervalo,
                primeiroDiaSemana, primeiroDiaMes, zerarSaldoSemanal, zerarSaldoMensal,
                ocultarSaldoTotal, periodoBancoHorasMeses, ultimoFechamentoBanco,
                diasUteisLembreteFechamento, habilitarSugestaoAjuste, zerarBancoAntesPeriodo,
                criadoEm, atualizadoEm
            FROM configuracoes_emprego
        """.trimIndent()
        )

        // 3. Remover tabela antiga
        db.execSQL("DROP TABLE configuracoes_emprego")

        // 4. Renomear nova tabela
        db.execSQL("ALTER TABLE configuracoes_emprego_new RENAME TO configuracoes_emprego")

        // 5. Recriar índice
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_configuracoes_emprego_empregoId ON configuracoes_emprego(empregoId)")
    }
}
