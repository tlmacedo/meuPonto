// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/data/local/database/migration/Migration_1_2.kt
package br.com.tlmacedo.meuponto.data.local.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migration do banco de dados da versão 1 para 2.
 *
 * Principais alterações:
 * - Criação das tabelas: empregos, configuracoes_emprego, horarios_dia_semana,
 *   ajustes_saldo, fechamentos_periodo, marcadores, audit_log
 * - Adição de colunas na tabela pontos: empregoId, nsr, latitude, longitude,
 *   endereco, marcadorId, justificativaInconsistencia, horaConsiderada, criadoEm, atualizadoEm
 * - Criação de índices para otimização de queries
 * - Inserção do emprego padrão (id=1) para dados existentes
 *
 * @author Thiago
 * @since 2.0.0
 */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // ====================================================================
        // 1. Criar tabela de empregos
        // ====================================================================
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `empregos` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `nome` TEXT NOT NULL,
                `descricao` TEXT,
                `ativo` INTEGER NOT NULL DEFAULT 1,
                `arquivado` INTEGER NOT NULL DEFAULT 0,
                `ordem` INTEGER NOT NULL DEFAULT 0,
                `criadoEm` TEXT NOT NULL,
                `atualizadoEm` TEXT NOT NULL
            )
        """.trimIndent())
        
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_empregos_ativo` ON `empregos` (`ativo`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_empregos_arquivado` ON `empregos` (`arquivado`)")

        // ====================================================================
        // 2. Inserir emprego padrão para dados existentes
        // ====================================================================
        val now = java.time.LocalDateTime.now().toString()
        db.execSQL("""
            INSERT INTO `empregos` (`id`, `nome`, `descricao`, `ativo`, `arquivado`, `ordem`, `criadoEm`, `atualizadoEm`)
            VALUES (1, 'Meu Emprego', 'Emprego padrão migrado automaticamente', 1, 0, 0, '$now', '$now')
        """.trimIndent())

        // ====================================================================
        // 3. Criar tabela de configurações do emprego
        // ====================================================================
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `configuracoes_emprego` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `empregoId` INTEGER NOT NULL,
                `cargaHorariaDiaria` INTEGER NOT NULL DEFAULT 480,
                `cargaHorariaSemanal` INTEGER NOT NULL DEFAULT 2400,
                `toleranciaAtraso` INTEGER NOT NULL DEFAULT 10,
                `toleranciaHoraExtra` INTEGER NOT NULL DEFAULT 10,
                `toleranciaIntervalo` INTEGER NOT NULL DEFAULT 0,
                `intervaloMinimo` INTEGER NOT NULL DEFAULT 60,
                `horaExtraAutomatica` INTEGER NOT NULL DEFAULT 0,
                `considerarFeriados` INTEGER NOT NULL DEFAULT 1,
                `considerarPontoFacultativo` INTEGER NOT NULL DEFAULT 0,
                `tipoNsr` TEXT NOT NULL DEFAULT 'NENHUM',
                `prefixoNsr` TEXT,
                `proximoNsr` INTEGER NOT NULL DEFAULT 1,
                `criadoEm` TEXT NOT NULL,
                `atualizadoEm` TEXT NOT NULL,
                FOREIGN KEY(`empregoId`) REFERENCES `empregos`(`id`) ON DELETE CASCADE
            )
        """.trimIndent())
        
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_configuracoes_emprego_empregoId` ON `configuracoes_emprego` (`empregoId`)")

        // ====================================================================
        // 4. Inserir configuração padrão para o emprego migrado
        // ====================================================================
        db.execSQL("""
            INSERT INTO `configuracoes_emprego` (
                `empregoId`, `cargaHorariaDiaria`, `cargaHorariaSemanal`, 
                `toleranciaAtraso`, `toleranciaHoraExtra`, `toleranciaIntervalo`,
                `intervaloMinimo`, `horaExtraAutomatica`, `considerarFeriados`,
                `considerarPontoFacultativo`, `tipoNsr`, `prefixoNsr`, `proximoNsr`,
                `criadoEm`, `atualizadoEm`
            ) VALUES (
                1, 480, 2400, 10, 10, 0, 60, 0, 1, 0, 'NENHUM', NULL, 1, '$now', '$now'
            )
        """.trimIndent())

        // ====================================================================
        // 5. Criar tabela de horários por dia da semana
        // ====================================================================
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `horarios_dia_semana` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `configuracaoId` INTEGER NOT NULL,
                `diaSemana` TEXT NOT NULL,
                `isDiaUtil` INTEGER NOT NULL DEFAULT 1,
                `horaEntrada` TEXT,
                `horaSaidaAlmoco` TEXT,
                `horaRetornoAlmoco` TEXT,
                `horaSaida` TEXT,
                `cargaHorariaDia` INTEGER,
                `criadoEm` TEXT NOT NULL,
                `atualizadoEm` TEXT NOT NULL,
                FOREIGN KEY(`configuracaoId`) REFERENCES `configuracoes_emprego`(`id`) ON DELETE CASCADE
            )
        """.trimIndent())
        
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_horarios_dia_semana_configuracaoId` ON `horarios_dia_semana` (`configuracaoId`)")
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_horarios_dia_semana_configuracaoId_diaSemana` ON `horarios_dia_semana` (`configuracaoId`, `diaSemana`)")

        // ====================================================================
        // 6. Criar tabela de ajustes de saldo
        // ====================================================================
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `ajustes_saldo` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `empregoId` INTEGER NOT NULL,
                `dataReferencia` TEXT NOT NULL,
                `minutos` INTEGER NOT NULL,
                `motivo` TEXT NOT NULL,
                `observacao` TEXT,
                `criadoEm` TEXT NOT NULL,
                `atualizadoEm` TEXT NOT NULL,
                FOREIGN KEY(`empregoId`) REFERENCES `empregos`(`id`) ON DELETE CASCADE
            )
        """.trimIndent())
        
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_ajustes_saldo_empregoId` ON `ajustes_saldo` (`empregoId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_ajustes_saldo_dataReferencia` ON `ajustes_saldo` (`dataReferencia`)")

        // ====================================================================
        // 7. Criar tabela de fechamentos de período
        // ====================================================================
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `fechamentos_periodo` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `empregoId` INTEGER NOT NULL,
                `dataInicio` TEXT NOT NULL,
                `dataFim` TEXT NOT NULL,
                `tipo` TEXT NOT NULL,
                `saldoAnterior` INTEGER NOT NULL DEFAULT 0,
                `horasTrabalhadas` INTEGER NOT NULL DEFAULT 0,
                `horasEsperadas` INTEGER NOT NULL DEFAULT 0,
                `saldoPeriodo` INTEGER NOT NULL DEFAULT 0,
                `saldoAcumulado` INTEGER NOT NULL DEFAULT 0,
                `observacao` TEXT,
                `criadoEm` TEXT NOT NULL,
                `atualizadoEm` TEXT NOT NULL,
                FOREIGN KEY(`empregoId`) REFERENCES `empregos`(`id`) ON DELETE CASCADE
            )
        """.trimIndent())
        
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_fechamentos_periodo_empregoId` ON `fechamentos_periodo` (`empregoId`)")
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_fechamentos_periodo_empregoId_dataInicio_dataFim` ON `fechamentos_periodo` (`empregoId`, `dataInicio`, `dataFim`)")

        // ====================================================================
        // 8. Criar tabela de marcadores
        // ====================================================================
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `marcadores` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `empregoId` INTEGER NOT NULL,
                `nome` TEXT NOT NULL,
                `cor` TEXT NOT NULL DEFAULT '#6200EE',
                `icone` TEXT,
                `ativo` INTEGER NOT NULL DEFAULT 1,
                `criadoEm` TEXT NOT NULL,
                `atualizadoEm` TEXT NOT NULL,
                FOREIGN KEY(`empregoId`) REFERENCES `empregos`(`id`) ON DELETE CASCADE
            )
        """.trimIndent())
        
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_marcadores_empregoId` ON `marcadores` (`empregoId`)")
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_marcadores_empregoId_nome` ON `marcadores` (`empregoId`, `nome`)")

        // ====================================================================
        // 9. Criar tabela de audit log
        // ====================================================================
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `audit_log` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `empregoId` INTEGER NOT NULL,
                `entidade` TEXT NOT NULL,
                `entidadeId` INTEGER NOT NULL,
                `acao` TEXT NOT NULL,
                `valorAnterior` TEXT,
                `valorNovo` TEXT,
                `criadoEm` TEXT NOT NULL,
                FOREIGN KEY(`empregoId`) REFERENCES `empregos`(`id`) ON DELETE CASCADE
            )
        """.trimIndent())
        
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_audit_log_empregoId` ON `audit_log` (`empregoId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_audit_log_entidade_entidadeId` ON `audit_log` (`entidade`, `entidadeId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_audit_log_criadoEm` ON `audit_log` (`criadoEm`)")

        // ====================================================================
        // 10. Atualizar tabela pontos - adicionar novas colunas
        // ====================================================================
        
        // Adicionar coluna empregoId com valor padrão 1 (emprego migrado)
        db.execSQL("ALTER TABLE `pontos` ADD COLUMN `empregoId` INTEGER NOT NULL DEFAULT 1")
        
        // Adicionar colunas de NSR
        db.execSQL("ALTER TABLE `pontos` ADD COLUMN `nsr` TEXT")
        
        // Adicionar colunas de localização
        db.execSQL("ALTER TABLE `pontos` ADD COLUMN `latitude` REAL")
        db.execSQL("ALTER TABLE `pontos` ADD COLUMN `longitude` REAL")
        db.execSQL("ALTER TABLE `pontos` ADD COLUMN `endereco` TEXT")
        
        // Adicionar coluna de marcador
        db.execSQL("ALTER TABLE `pontos` ADD COLUMN `marcadorId` INTEGER")
        
        // Adicionar colunas de inconsistência e tolerância
        db.execSQL("ALTER TABLE `pontos` ADD COLUMN `justificativaInconsistencia` TEXT")
        db.execSQL("ALTER TABLE `pontos` ADD COLUMN `horaConsiderada` TEXT")
        
        // Adicionar colunas de auditoria
        db.execSQL("ALTER TABLE `pontos` ADD COLUMN `criadoEm` TEXT NOT NULL DEFAULT '$now'")
        db.execSQL("ALTER TABLE `pontos` ADD COLUMN `atualizadoEm` TEXT NOT NULL DEFAULT '$now'")

        // ====================================================================
        // 11. Criar índices na tabela pontos
        // ====================================================================
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_pontos_empregoId` ON `pontos` (`empregoId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_pontos_empregoId_data` ON `pontos` (`empregoId`, `data`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_pontos_marcadorId` ON `pontos` (`marcadorId`)")
    }
}
