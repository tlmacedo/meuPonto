// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/data/local/database/migration/Migration_4_5.kt
package br.com.tlmacedo.meuponto.data.local.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migration da versão 4 para 5 do banco de dados.
 * Remove a coluna de tolerância de redução de intervalo.
 *
 * @author Thiago
 * @since 2.1.2
 */
val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // SQLite não suporta DROP COLUMN diretamente para todas as versões de forma simples.
        // A abordagem recomendada pelo Room para remover colunas é recriar a tabela.

        // 1. Recriar configuracoes_emprego sem toleranciaIntervaloMenosMinutos
        db.execSQL(
            "CREATE TABLE configuracoes_emprego_new (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, empregoId INTEGER NOT NULL, cargaHorariaDiariaMinutos INTEGER NOT NULL, jornadaMaximaDiariaMinutos INTEGER NOT NULL, intervaloMinimoInterjornadaMinutos INTEGER NOT NULL, intervaloMinimoMinutos INTEGER NOT NULL, toleranciaEntradaMinutos INTEGER NOT NULL, toleranciaSaidaMinutos INTEGER NOT NULL, toleranciaIntervaloMaisMinutos INTEGER NOT NULL, exigeJustificativaInconsistencia INTEGER NOT NULL, habilitarNsr INTEGER NOT NULL, tipoNsr TEXT NOT NULL, habilitarLocalizacao INTEGER NOT NULL, localizacaoAutomatica INTEGER NOT NULL, exibirLocalizacaoDetalhes INTEGER NOT NULL, exibirDuracaoTurno INTEGER NOT NULL, exibirDuracaoIntervalo INTEGER NOT NULL, primeiroDiaSemana TEXT NOT NULL, primeiroDiaMes INTEGER NOT NULL, zerarSaldoSemanal INTEGER NOT NULL, zerarSaldoMensal INTEGER NOT NULL, ocultarSaldoTotal INTEGER NOT NULL, periodoBancoHorasMeses INTEGER NOT NULL, ultimoFechamentoBanco TEXT, diasUteisLembreteFechamento INTEGER NOT NULL, habilitarSugestaoAjuste INTEGER NOT NULL, criadoEm TEXT NOT NULL, atualizadoEm TEXT NOT NULL, FOREIGN KEY(empregoId) REFERENCES empregos(id) ON UPDATE NO ACTION ON DELETE CASCADE )"
        )
        db.execSQL("INSERT INTO configuracoes_emprego_new SELECT id, empregoId, cargaHorariaDiariaMinutos, jornadaMaximaDiariaMinutos, intervaloMinimoInterjornadaMinutos, intervaloMinimoMinutos, toleranciaEntradaMinutos, toleranciaSaidaMinutos, toleranciaIntervaloMaisMinutos, exigeJustificativaInconsistencia, habilitarNsr, tipoNsr, habilitarLocalizacao, localizacaoAutomatica, exibirLocalizacaoDetalhes, exibirDuracaoTurno, exibirDuracaoIntervalo, primeiroDiaSemana, primeiroDiaMes, zerarSaldoSemanal, zerarSaldoMensal, ocultarSaldoTotal, periodoBancoHorasMeses, ultimoFechamentoBanco, diasUteisLembreteFechamento, habilitarSugestaoAjuste, criadoEm, atualizadoEm FROM configuracoes_emprego")
        db.execSQL("DROP TABLE configuracoes_emprego")
        db.execSQL("ALTER TABLE configuracoes_emprego_new RENAME TO configuracoes_emprego")
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_configuracoes_emprego_empregoId ON configuracoes_emprego (empregoId)")

        // 2. Recriar horarios_dia_semana sem toleranciaIntervaloMenosMinutos
        db.execSQL("CREATE TABLE horarios_dia_semana_new (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, empregoId INTEGER NOT NULL, diaSemana TEXT NOT NULL, ativo INTEGER NOT NULL, cargaHorariaMinutos INTEGER NOT NULL, entradaIdeal TEXT, saidaIntervaloIdeal TEXT, voltaIntervaloIdeal TEXT, saidaIdeal TEXT, intervaloMinimoMinutos INTEGER NOT NULL, toleranciaIntervaloMaisMinutos INTEGER NOT NULL, toleranciaEntradaMinutos INTEGER, toleranciaSaidaMinutos INTEGER, criadoEm TEXT NOT NULL, atualizadoEm TEXT NOT NULL, FOREIGN KEY(empregoId) REFERENCES empregos(id) ON UPDATE NO ACTION ON DELETE CASCADE )")
        db.execSQL("INSERT INTO horarios_dia_semana_new SELECT id, empregoId, diaSemana, ativo, cargaHorariaMinutos, entradaIdeal, saidaIntervaloIdeal, voltaIntervaloIdeal, saidaIdeal, intervaloMinimoMinutos, toleranciaIntervaloMaisMinutos, toleranciaEntradaMinutos, toleranciaSaidaMinutos, criadoEm, atualizadoEm FROM horarios_dia_semana")
        db.execSQL("DROP TABLE horarios_dia_semana")
        db.execSQL("ALTER TABLE horarios_dia_semana_new RENAME TO horarios_dia_semana")
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_horarios_dia_semana_empregoId_diaSemana ON horarios_dia_semana (empregoId, diaSemana)")
    }
}
