package br.com.tlmacedo.meuponto.data.local.database.migration

import androidx.room.migration.Migration

/**
 * Migração da versão 27 para 28:
 * - Adição de tabelas de histórico de cargos e ajustes salariais.
 * - Adição de colunas em 'empregos'.
 * - Adição de colunas em 'configuracoes_emprego'.
 * - Adição de colunas em 'versoes_jornada'.
 */
val MIGRATION_27_28 = Migration(27, 28) { database ->
    // 1. Criar novas tabelas
    database.execSQL(
        """
        CREATE TABLE IF NOT EXISTS `historico_cargos` (
            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
            `empregoId` INTEGER NOT NULL, 
            `funcao` TEXT NOT NULL, 
            `salarioInicial` REAL NOT NULL, 
            `dataInicio` TEXT NOT NULL, 
            `dataFim` TEXT, 
            `criadoEm` TEXT NOT NULL, 
            `atualizadoEm` TEXT NOT NULL, 
            FOREIGN KEY(`empregoId`) REFERENCES `empregos`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE 
        )
        """.trimIndent()
    )
    database.execSQL("CREATE INDEX IF NOT EXISTS `index_historico_cargos_empregoId` ON `historico_cargos` (`empregoId`)")

    database.execSQL(
        """
        CREATE TABLE IF NOT EXISTS `ajustes_salariais` (
            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
            `historicoCargoId` INTEGER NOT NULL, 
            `dataAjuste` TEXT NOT NULL, 
            `novoSalario` REAL NOT NULL, 
            `observacao` TEXT, 
            `criadoEm` TEXT NOT NULL, 
            FOREIGN KEY(`historicoCargoId`) REFERENCES `historico_cargos`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE 
        )
        """.trimIndent()
    )
    database.execSQL("CREATE INDEX IF NOT EXISTS `index_ajustes_salariais_historicoCargoId` ON `ajustes_salariais` (`historicoCargoId`)")

    // 2. Atualizar tabela 'empregos'
    database.execSQL("ALTER TABLE `empregos` ADD COLUMN `apelido` TEXT")
    database.execSQL("ALTER TABLE `empregos` ADD COLUMN `endereco` TEXT")
    database.execSQL("ALTER TABLE `empregos` ADD COLUMN `dataTerminoTrabalho` TEXT")

    // 3. Atualizar tabela 'configuracoes_emprego'
    database.execSQL("ALTER TABLE `configuracoes_emprego` ADD COLUMN `fotoLocalArmazenamento` TEXT")
    database.execSQL("ALTER TABLE `configuracoes_emprego` ADD COLUMN `fotoRegistrarPontoOcr` INTEGER NOT NULL DEFAULT 0")
    database.execSQL("ALTER TABLE `configuracoes_emprego` ADD COLUMN `fotoValidarComprovante` INTEGER NOT NULL DEFAULT 0")
    database.execSQL("ALTER TABLE `configuracoes_emprego` ADD COLUMN `diaInicioFechamentoRH` INTEGER NOT NULL DEFAULT 11")
    database.execSQL("ALTER TABLE `configuracoes_emprego` ADD COLUMN `bancoHorasHabilitado` INTEGER NOT NULL DEFAULT 0")
    database.execSQL("ALTER TABLE `configuracoes_emprego` ADD COLUMN `bancoHorasCicloMeses` INTEGER NOT NULL DEFAULT 6")
    database.execSQL("ALTER TABLE `configuracoes_emprego` ADD COLUMN `bancoHorasDataInicioCiclo` TEXT")
    database.execSQL("ALTER TABLE `configuracoes_emprego` ADD COLUMN `bancoHorasZerarAoFinalCiclo` INTEGER NOT NULL DEFAULT 0")
    database.execSQL("ALTER TABLE `configuracoes_emprego` ADD COLUMN `exigeJustificativaInconsistencia` INTEGER NOT NULL DEFAULT 0")
    database.execSQL("ALTER TABLE `configuracoes_emprego` ADD COLUMN `exibirDuracaoTurno` INTEGER NOT NULL DEFAULT 1")
    database.execSQL("ALTER TABLE `configuracoes_emprego` ADD COLUMN `exibirDuracaoIntervalo` INTEGER NOT NULL DEFAULT 1")

    // 4. Atualizar tabela 'versoes_jornada' (Recriação para aplicar DefaultValues e novas colunas)
    database.execSQL(
        """
        CREATE TABLE IF NOT EXISTS `versoes_jornada_new` (
            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
            `empregoId` INTEGER NOT NULL, 
            `dataInicio` TEXT NOT NULL, 
            `dataFim` TEXT, 
            `descricao` TEXT, 
            `numeroVersao` INTEGER NOT NULL DEFAULT 1, 
            `vigente` INTEGER NOT NULL DEFAULT 1, 
            `jornadaMaximaDiariaMinutos` INTEGER NOT NULL DEFAULT 600, 
            `intervaloMinimoInterjornadaMinutos` INTEGER NOT NULL DEFAULT 660, 
            `intervaloMinimoAlmocoMinutos` INTEGER NOT NULL DEFAULT 60, 
            `toleranciaIntervaloMaisMinutos` INTEGER NOT NULL DEFAULT 0, 
            `turnoMaximoMinutos` INTEGER NOT NULL DEFAULT 360, 
            `cargaHorariaDiariaMinutos` INTEGER NOT NULL DEFAULT 480, 
            `acrescimoMinutosDiasPontes` INTEGER NOT NULL DEFAULT 12, 
            `cargaHorariaSemanalMinutos` INTEGER NOT NULL DEFAULT 2460, 
            `primeiroDiaSemana` TEXT NOT NULL DEFAULT 'SEGUNDA', 
            `diaInicioFechamentoRH` INTEGER NOT NULL DEFAULT 1, 
            `zerarSaldoSemanal` INTEGER NOT NULL DEFAULT 0, 
            `zerarSaldoPeriodoRH` INTEGER NOT NULL DEFAULT 0, 
            `ocultarSaldoTotal` INTEGER NOT NULL DEFAULT 0, 
            `bancoHorasHabilitado` INTEGER NOT NULL DEFAULT 0, 
            `periodoBancoDias` INTEGER NOT NULL DEFAULT 0, 
            `periodoBancoSemanas` INTEGER NOT NULL DEFAULT 0, 
            `periodoBancoMeses` INTEGER NOT NULL DEFAULT 0, 
            `periodoBancoAnos` INTEGER NOT NULL DEFAULT 0,
            `dataInicioCicloBancoAtual` TEXT, 
            `diasUteisLembreteFechamento` INTEGER NOT NULL DEFAULT 3, 
            `habilitarSugestaoAjuste` INTEGER NOT NULL DEFAULT 0, 
            `zerarBancoAntesPeriodo` INTEGER NOT NULL DEFAULT 0, 
            `exigeJustificativaInconsistencia` INTEGER NOT NULL DEFAULT 0, 
            `criadoEm` TEXT NOT NULL, 
            `atualizadoEm` TEXT NOT NULL, 
            FOREIGN KEY(`empregoId`) REFERENCES `empregos`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE 
        )
        """.trimIndent()
    )

    // Copiar dados existentes (apenas colunas que já existiam na v27)
    // Note: Use os nomes de colunas que existiam na versão anterior. 
    // Se novas colunas foram adicionadas, elas receberão o DEFAULT.
    database.execSQL(
        """
        INSERT INTO `versoes_jornada_new` (
            id, empregoId, dataInicio, dataFim, descricao, numeroVersao, vigente, 
            jornadaMaximaDiariaMinutos, intervaloMinimoInterjornadaMinutos, 
            toleranciaIntervaloMaisMinutos, turnoMaximoMinutos, 
            cargaHorariaDiariaMinutos, acrescimoMinutosDiasPontes, cargaHorariaSemanalMinutos, 
            primeiroDiaSemana, diaInicioFechamentoRH, zerarSaldoSemanal, zerarSaldoPeriodoRH, 
            ocultarSaldoTotal, bancoHorasHabilitado, periodoBancoSemanas, periodoBancoMeses, 
            dataInicioCicloBancoAtual, diasUteisLembreteFechamento, habilitarSugestaoAjuste, 
            zerarBancoAntesPeriodo, exigeJustificativaInconsistencia, criadoEm, atualizadoEm
        )
        SELECT 
            id, empregoId, dataInicio, dataFim, descricao, numeroVersao, vigente, 
            jornadaMaximaDiariaMinutos, intervaloMinimoInterjornadaMinutos, 
            toleranciaIntervaloMaisMinutos, turnoMaximoMinutos, 
            cargaHorariaDiariaMinutos, acrescimoMinutosDiasPontes, cargaHorariaSemanalMinutos, 
            primeiroDiaSemana, diaInicioFechamentoRH, zerarSaldoSemanal, zerarSaldoPeriodoRH, 
            ocultarSaldoTotal, bancoHorasHabilitado, periodoBancoSemanas, periodoBancoMeses, 
            dataInicioCicloBancoAtual, diasUteisLembreteFechamento, habilitarSugestaoAjuste, 
            zerarBancoAntesPeriodo, exigeJustificativaInconsistencia, criadoEm, atualizadoEm
        FROM `versoes_jornada`
        """.trimIndent()
    )

    database.execSQL("DROP TABLE `versoes_jornada`")
    database.execSQL("ALTER TABLE `versoes_jornada_new` RENAME TO `versoes_jornada`")

    // Recriar índices
    database.execSQL("CREATE INDEX IF NOT EXISTS `index_versoes_jornada_empregoId` ON `versoes_jornada` (`empregoId`)")
    database.execSQL("CREATE INDEX IF NOT EXISTS `index_versoes_jornada_empregoId_dataInicio` ON `versoes_jornada` (`empregoId`, `dataInicio`)")
    database.execSQL("CREATE INDEX IF NOT EXISTS `index_versoes_jornada_empregoId_dataFim` ON `versoes_jornada` (`empregoId`, `dataFim`)")
}
