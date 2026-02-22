// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/data/local/database/migration/Migration_14_15.kt
package br.com.tlmacedo.meuponto.data.local.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migração para refatoração do sistema de ciclos de banco de horas.
 *
 * Alterações:
 * - Renomeia primeiroDiaMes -> diaInicioFechamentoRH
 * - Renomeia zerarSaldoMensal -> zerarSaldoPeriodoRH
 * - Renomeia periodoBancoHorasMeses -> periodoBancoMeses
 * - Renomeia ultimoFechamentoBanco -> dataInicioCicloBancoAtual
 * - Adiciona bancoHorasHabilitado
 * - Adiciona periodoBancoSemanas
 *
 * @author Thiago
 * @since 3.0.0
 */
val MIGRATION_14_15 = object : Migration(14, 15) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // 1. Criar tabela temporária com nova estrutura
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS configuracoes_emprego_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                empregoId INTEGER NOT NULL,
                
                -- JORNADA DE TRABALHO
                cargaHorariaDiariaMinutos INTEGER NOT NULL DEFAULT 492,
                jornadaMaximaDiariaMinutos INTEGER NOT NULL DEFAULT 600,
                intervaloMinimoInterjornadaMinutos INTEGER NOT NULL DEFAULT 660,
                intervaloMinimoMinutos INTEGER NOT NULL DEFAULT 60,
                
                -- TOLERÂNCIAS
                toleranciaIntervaloMaisMinutos INTEGER NOT NULL DEFAULT 0,
                
                -- VALIDAÇÕES
                exigeJustificativaInconsistencia INTEGER NOT NULL DEFAULT 0,
                
                -- NSR
                habilitarNsr INTEGER NOT NULL DEFAULT 0,
                tipoNsr TEXT NOT NULL DEFAULT 'NUMERICO',
                
                -- LOCALIZAÇÃO
                habilitarLocalizacao INTEGER NOT NULL DEFAULT 0,
                localizacaoAutomatica INTEGER NOT NULL DEFAULT 0,
                exibirLocalizacaoDetalhes INTEGER NOT NULL DEFAULT 1,
                
                -- EXIBIÇÃO
                exibirDuracaoTurno INTEGER NOT NULL DEFAULT 1,
                exibirDuracaoIntervalo INTEGER NOT NULL DEFAULT 1,
                
                -- PERÍODO RH (novos nomes)
                primeiroDiaSemana TEXT NOT NULL DEFAULT 'SEGUNDA',
                diaInicioFechamentoRH INTEGER NOT NULL DEFAULT 1,
                
                -- SALDO (novos nomes)
                zerarSaldoSemanal INTEGER NOT NULL DEFAULT 0,
                zerarSaldoPeriodoRH INTEGER NOT NULL DEFAULT 0,
                ocultarSaldoTotal INTEGER NOT NULL DEFAULT 0,
                
                -- BANCO DE HORAS - CICLO (novos campos)
                bancoHorasHabilitado INTEGER NOT NULL DEFAULT 0,
                periodoBancoSemanas INTEGER NOT NULL DEFAULT 0,
                periodoBancoMeses INTEGER NOT NULL DEFAULT 0,
                dataInicioCicloBancoAtual TEXT,
                diasUteisLembreteFechamento INTEGER NOT NULL DEFAULT 3,
                habilitarSugestaoAjuste INTEGER NOT NULL DEFAULT 0,
                zerarBancoAntesPeriodo INTEGER NOT NULL DEFAULT 0,
                
                -- AUDITORIA
                criadoEm TEXT NOT NULL,
                atualizadoEm TEXT NOT NULL,
                
                FOREIGN KEY(empregoId) REFERENCES empregos(id) ON DELETE CASCADE
            )
            """.trimIndent()
        )

        // 2. Migrar dados existentes
        db.execSQL(
            """
            INSERT INTO configuracoes_emprego_new (
                id, empregoId,
                cargaHorariaDiariaMinutos, jornadaMaximaDiariaMinutos,
                intervaloMinimoInterjornadaMinutos, intervaloMinimoMinutos,
                toleranciaIntervaloMaisMinutos, exigeJustificativaInconsistencia,
                habilitarNsr, tipoNsr,
                habilitarLocalizacao, localizacaoAutomatica, exibirLocalizacaoDetalhes,
                exibirDuracaoTurno, exibirDuracaoIntervalo,
                primeiroDiaSemana, diaInicioFechamentoRH,
                zerarSaldoSemanal, zerarSaldoPeriodoRH, ocultarSaldoTotal,
                bancoHorasHabilitado, periodoBancoSemanas, periodoBancoMeses,
                dataInicioCicloBancoAtual, diasUteisLembreteFechamento,
                habilitarSugestaoAjuste, zerarBancoAntesPeriodo,
                criadoEm, atualizadoEm
            )
            SELECT 
                id, empregoId,
                cargaHorariaDiariaMinutos, jornadaMaximaDiariaMinutos,
                intervaloMinimoInterjornadaMinutos, intervaloMinimoMinutos,
                toleranciaIntervaloMaisMinutos, exigeJustificativaInconsistencia,
                habilitarNsr, tipoNsr,
                habilitarLocalizacao, localizacaoAutomatica, exibirLocalizacaoDetalhes,
                exibirDuracaoTurno, exibirDuracaoIntervalo,
                primeiroDiaSemana, primeiroDiaMes,
                zerarSaldoSemanal, zerarSaldoMensal, ocultarSaldoTotal,
                CASE WHEN periodoBancoHorasMeses > 0 THEN 1 ELSE 0 END,
                0,
                periodoBancoHorasMeses,
                ultimoFechamentoBanco,
                diasUteisLembreteFechamento,
                habilitarSugestaoAjuste, zerarBancoAntesPeriodo,
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
