package br.com.tlmacedo.meuponto.data.local.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migração da versão 35 para 36:
 * - Corrige campos omitidos na migração anterior (34 -> 35) nas tabelas 
 *   'configuracoes_emprego' e 'pontos'.
 */
val MIGRATION_35_36 = object : Migration(35, 36) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // 1. Reparar tabela 'configuracoes_emprego' (campos faltantes da V35)
        val cursorConfig = db.query("PRAGMA table_info(configuracoes_emprego)")
        val existingConfigCols = mutableSetOf<String>()
        val nameIndexConfig = cursorConfig.getColumnIndexOrThrow("name")
        while (cursorConfig.moveToNext()) {
            existingConfigCols.add(cursorConfig.getString(nameIndexConfig))
        }
        cursorConfig.close()

        val configFields = mapOf(
            "fotoRegistrarPontoOcr" to "INTEGER NOT NULL DEFAULT 0",
            "diaInicioFechamentoRH" to "INTEGER NOT NULL DEFAULT 11",
            "bancoHorasHabilitado" to "INTEGER NOT NULL DEFAULT 0",
            "bancoHorasCicloMeses" to "INTEGER NOT NULL DEFAULT 6",
            "bancoHorasDataInicioCiclo" to "TEXT",
            "bancoHorasZerarAoFinalCiclo" to "INTEGER NOT NULL DEFAULT 0",
            "exigeJustificativaInconsistencia" to "INTEGER NOT NULL DEFAULT 0",
            "comentarioHabilitado" to "INTEGER NOT NULL DEFAULT 1",
            "comentarioObrigatorioHoraExtra" to "INTEGER NOT NULL DEFAULT 0",
            "limiteHoraExtraSemComentario" to "INTEGER NOT NULL DEFAULT 0"
        )

        configFields.forEach { (col, type) ->
            if (col !in existingConfigCols) {
                db.execSQL("ALTER TABLE `configuracoes_emprego` ADD COLUMN `$col` $type")
            }
        }

        // 2. Reparar tabela 'pontos' (campos faltantes da V35)
        val cursorPontos = db.query("PRAGMA table_info(pontos)")
        val existingPontosCols = mutableSetOf<String>()
        val nameIndexPontos = cursorPontos.getColumnIndexOrThrow("name")
        while (cursorPontos.moveToNext()) {
            existingPontosCols.add(cursorPontos.getString(nameIndexPontos))
        }
        cursorPontos.close()

        val pontosFields = mapOf(
            "foto_origem" to "INTEGER NOT NULL DEFAULT 0",
            "is_deleted" to "INTEGER NOT NULL DEFAULT 0",
            "deleted_at" to "INTEGER",
            "updated_at" to "INTEGER NOT NULL DEFAULT 0"
        )

        pontosFields.forEach { (col, type) ->
            if (col !in existingPontosCols) {
                db.execSQL("ALTER TABLE `pontos` ADD COLUMN `$col` $type")
            }
        }

        // 3. Adicionar índice de soft delete se não existir
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_pontos_is_deleted` ON `pontos` (`is_deleted`)")
    }
}
