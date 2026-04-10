package br.com.tlmacedo.meuponto.data.local.database.migration

import androidx.room.migration.Migration

/**
 * Migração da versão 28 para 29:
 * - Esta migração inclui uma verificação "self-healing" para a tabela 'versoes_jornada'.
 * - Alguns dispositivos podem estar na versão 28 com colunas faltando ou sem valores padrão
 *   devido a um erro na migração 27 -> 28 original.
 * - Também garante a existência das tabelas 'historico_cargos' e 'ajustes_salariais'.
 */
val MIGRATION_28_29 = Migration(28, 29) { database ->
    // 1. Garantir que as tabelas adicionadas na v28 existam (Medida de segurança)
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

    // 2. Recriação "self-healing" da tabela 'versoes_jornada'
    // Isso garante que todos os DefaultValues e colunas estejam presentes conforme o esquema v28/v29.
    
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
            `turnoMaximoMinutos` INTEGER NOT NULL DEFAULT 360, 
            `intervaloMinimoAlmocoMinutos` INTEGER NOT NULL DEFAULT 60, 
            `toleranciaIntervaloMaisMinutos` INTEGER NOT NULL DEFAULT 0, 
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

    // Identificar colunas existentes na tabela atual para evitar erros no INSERT
    val cursor = database.query("PRAGMA table_info(`versoes_jornada`)")
    val existingColumns = mutableSetOf<String>()
    while (cursor.moveToNext()) {
        existingColumns.add(cursor.getString(cursor.getColumnIndexOrThrow("name")))
    }
    cursor.close()

    // Lista de todas as colunas esperadas na v29 (seguindo a ordem do 29.json)
    val expectedColumns = listOf(
        "id", "empregoId", "dataInicio", "dataFim", "descricao", "numeroVersao", "vigente",
        "jornadaMaximaDiariaMinutos", "intervaloMinimoInterjornadaMinutos", "turnoMaximoMinutos",
        "intervaloMinimoAlmocoMinutos", "toleranciaIntervaloMaisMinutos", "cargaHorariaDiariaMinutos",
        "acrescimoMinutosDiasPontes", "cargaHorariaSemanalMinutos", "primeiroDiaSemana",
        "diaInicioFechamentoRH", "zerarSaldoSemanal", "zerarSaldoPeriodoRH", "ocultarSaldoTotal",
        "bancoHorasHabilitado", "periodoBancoDias", "periodoBancoSemanas", "periodoBancoMeses",
        "periodoBancoAnos", "dataInicioCicloBancoAtual", "diasUteisLembreteFechamento",
        "habilitarSugestaoAjuste", "zerarBancoAntesPeriodo", "exigeJustificativaInconsistencia",
        "criadoEm", "atualizadoEm"
    )

    // Filtrar apenas as colunas que realmente existem na tabela física
    val columnsToCopy = expectedColumns.filter { existingColumns.contains(it) }
    val columnsCsv = columnsToCopy.joinToString(", ")

    if (columnsCsv.isNotEmpty()) {
        database.execSQL(
            """
            INSERT INTO `versoes_jornada_new` ($columnsCsv)
            SELECT $columnsCsv FROM `versoes_jornada`
            """.trimIndent()
        )
    }

    // Substituir a tabela antiga pela nova
    database.execSQL("DROP TABLE `versoes_jornada`")
    database.execSQL("ALTER TABLE `versoes_jornada_new` RENAME TO `versoes_jornada`")

    // Recriar índices (conforme 29.json)
    database.execSQL("CREATE INDEX IF NOT EXISTS `index_versoes_jornada_empregoId` ON `versoes_jornada` (`empregoId`)")
    database.execSQL("CREATE INDEX IF NOT EXISTS `index_versoes_jornada_empregoId_dataInicio` ON `versoes_jornada` (`empregoId`, `dataInicio`)")
    database.execSQL("CREATE INDEX IF NOT EXISTS `index_versoes_jornada_empregoId_dataFim` ON `versoes_jornada` (`empregoId`, `dataFim`)")
}
