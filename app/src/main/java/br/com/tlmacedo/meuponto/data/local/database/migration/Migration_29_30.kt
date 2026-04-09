package br.com.tlmacedo.meuponto.data.local.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migração da versão 29 para 30:
 * - Esta migração é uma medida corretiva (self-healing) para a tabela 'versoes_jornada'.
 * - Corrige o erro de esquema onde colunas foram perdidas ou não adicionadas corretamente
 *   nas migrações anteriores (27->28 e 28->29).
 */
val MIGRATION_29_30 = Migration(29, 30) { database ->
    // 1. Criar tabela temporária com o esquema correto (v29/v30)
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

    // 2. Identificar colunas existentes na tabela atual
    val cursor = database.query("PRAGMA table_info(`versoes_jornada`)")
    val existingColumns = mutableSetOf<String>()
    while (cursor.moveToNext()) {
        existingColumns.add(cursor.getString(cursor.getColumnIndexOrThrow("name")))
    }
    cursor.close()

    // 3. Montar a lista de colunas para o INSERT (apenas as que existem)
    val columnsToCopy = listOf(
        "id", "empregoId", "dataInicio", "dataFim", "descricao", "numeroVersao", "vigente",
        "jornadaMaximaDiariaMinutos", "intervaloMinimoInterjornadaMinutos", 
        "intervaloMinimoAlmocoMinutos", "toleranciaIntervaloMaisMinutos", "turnoMaximoMinutos",
        "cargaHorariaDiariaMinutos", "acrescimoMinutosDiasPontes", "cargaHorariaSemanalMinutos",
        "primeiroDiaSemana", "diaInicioFechamentoRH", "zerarSaldoSemanal", "zerarSaldoPeriodoRH",
        "ocultarSaldoTotal", "bancoHorasHabilitado", "periodoBancoDias", "periodoBancoSemanas",
        "periodoBancoMeses", "periodoBancoAnos", "dataInicioCicloBancoAtual",
        "diasUteisLembreteFechamento", "habilitarSugestaoAjuste", "zerarBancoAntesPeriodo",
        "exigeJustificativaInconsistencia", "criadoEm", "atualizadoEm"
    ).filter { existingColumns.contains(it) }

    val columnsCsv = columnsToCopy.joinToString(", ")
    
    if (columnsCsv.isNotEmpty()) {
        database.execSQL(
            """
            INSERT INTO `versoes_jornada_new` ($columnsCsv)
            SELECT $columnsCsv FROM `versoes_jornada`
            """.trimIndent()
        )
    }

    // 4. Substituir a tabela antiga pela nova
    database.execSQL("DROP TABLE `versoes_jornada`")
    database.execSQL("ALTER TABLE `versoes_jornada_new` RENAME TO `versoes_jornada`")

    // 5. Recriar índices
    database.execSQL("CREATE INDEX IF NOT EXISTS `index_versoes_jornada_empregoId` ON `versoes_jornada` (`empregoId`)")
    database.execSQL("CREATE INDEX IF NOT EXISTS `index_versoes_jornada_empregoId_dataInicio` ON `versoes_jornada` (`empregoId`, `dataInicio`)")
    database.execSQL("CREATE INDEX IF NOT EXISTS `index_versoes_jornada_empregoId_dataFim` ON `versoes_jornada` (`empregoId`, `dataFim`)")
}
