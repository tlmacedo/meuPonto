package br.com.tlmacedo.meuponto.data.local.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migração da versão 37 para 38:
 * - Nenhuma mudança estrutural no banco de dados.
 */
val MIGRATION_37_38 = object : Migration(37, 38) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // 0. Remover a tabela temporária caso ela já exista de uma tentativa anterior
        db.execSQL("DROP TABLE IF EXISTS `ausencias_new`")

        // 1. Criar nova tabela com a estrutura EXATA (conforme MeuPontoDatabase_Impl.java)
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `ausencias_new` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                `empregoId` INTEGER NOT NULL, 
                `tipo` TEXT NOT NULL, 
                `tipoFolga` TEXT, 
                `dataInicio` TEXT NOT NULL, 
                `dataFim` TEXT NOT NULL, 
                `descricao` TEXT, 
                `observacao` TEXT, 
                `horaInicio` TEXT, 
                `duracaoDeclaracaoMinutos` INTEGER, 
                `duracaoAbonoMinutos` INTEGER, 
                `subTipoFolga` TEXT, 
                `dataInicioPeriodoAquisitivo` TEXT, 
                `dataFimPeriodoAquisitivo` TEXT, 
                `periodoAquisitivo` TEXT, 
                `imagemUri` TEXT, 
                `ativo` INTEGER NOT NULL, 
                `criadoEm` TEXT NOT NULL, 
                `atualizadoEm` TEXT NOT NULL, 
                FOREIGN KEY(`empregoId`) REFERENCES `empregos`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE 
            )
        """.trimIndent()
        )

        // 2. Copiar dados da tabela antiga para a nova
        // Primeiro, obter as colunas que realmente existem na tabela atual
        val cursor = db.query("PRAGMA table_info(`ausencias`)")
        val existingColumns = mutableSetOf<String>()
        val nameIndex = cursor.getColumnIndexOrThrow("name")
        while (cursor.moveToNext()) {
            existingColumns.add(cursor.getString(nameIndex))
        }
        cursor.close()

        val columnsToCopy = listOf(
            "id", "empregoId", "tipo", "tipoFolga", "dataInicio", "dataFim",
            "descricao", "observacao", "horaInicio", "duracaoDeclaracaoMinutos",
            "duracaoAbonoMinutos", "subTipoFolga", "dataInicioPeriodoAquisitivo",
            "dataFimPeriodoAquisitivo", "periodoAquisitivo", "imagemUri",
            "ativo", "criadoEm", "atualizadoEm"
        ).filter { it in existingColumns }

        val columnsString = columnsToCopy.joinToString(", ")

        db.execSQL(
            """
            INSERT INTO `ausencias_new` ($columnsString)
            SELECT $columnsString FROM `ausencias`
        """.trimIndent()
        )

        // 3. Remover a tabela antiga
        db.execSQL("DROP TABLE `ausencias`")

        // 4. Renomear a nova tabela
        db.execSQL("ALTER TABLE `ausencias_new` RENAME TO `ausencias`")

        // 5. Recriar os índices
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_ausencias_empregoId` ON `ausencias` (`empregoId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_ausencias_tipo` ON `ausencias` (`tipo`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_ausencias_dataInicio` ON `ausencias` (`dataInicio`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_ausencias_dataFim` ON `ausencias` (`dataFim`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_ausencias_ativo` ON `ausencias` (`ativo`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_ausencias_empregoId_dataInicio_dataFim` ON `ausencias` (`empregoId`, `dataInicio`, `dataFim`)")
    }
}

