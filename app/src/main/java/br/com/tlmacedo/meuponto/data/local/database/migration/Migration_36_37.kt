package br.com.tlmacedo.meuponto.data.local.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migração da versão 36 para 37:
 * - Adiciona o campo 'limiteHoraExtraSemComentario' que foi omitido na migração 35 -> 36.
 */
val MIGRATION_36_37 = object : Migration(36, 37) {
    override fun migrate(db: SupportSQLiteDatabase) {
        val cursor = db.query("PRAGMA table_info(configuracoes_emprego)")
        val existingCols = mutableSetOf<String>()
        val nameIndex = cursor.getColumnIndexOrThrow("name")
        while (cursor.moveToNext()) {
            existingCols.add(cursor.getString(nameIndex))
        }
        cursor.close()

        val repairFields = mapOf(
            "fotoLocalArmazenamento" to "TEXT",
            "fotoValidarComprovante" to "INTEGER NOT NULL DEFAULT 0",
            "exibirDuracaoTurno" to "INTEGER NOT NULL DEFAULT 1",
            "exibirDuracaoIntervalo" to "INTEGER NOT NULL DEFAULT 1",
            "limiteHoraExtraSemComentario" to "INTEGER NOT NULL DEFAULT 0"
        )

        repairFields.forEach { (col, type) ->
            if (col !in existingCols) {
                db.execSQL("ALTER TABLE `configuracoes_emprego` ADD COLUMN `$col` $type")
            }
        }
    }
}
