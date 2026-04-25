package br.com.tlmacedo.meuponto.data.local.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migração da versão 34 para 35:
 * - Garante que as colunas 'apelido', 'endereco' e 'logo' existam na tabela 'empregos'.
 * - Garante que as colunas 'fotoLocalArmazenamento', 'fotoValidarComprovante', 'exibirDuracaoTurno' 
 *   e 'exibirDuracaoIntervalo' existam na tabela 'configuracoes_emprego'.
 */
val MIGRATION_34_35 = object : Migration(34, 35) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Reparar tabela 'empregos'
        val cursorEmpregos = db.query("PRAGMA table_info(empregos)")
        var hasApelido = false
        var hasEndereco = false
        var hasLogo = false

        val nameIndexEmpregos = cursorEmpregos.getColumnIndexOrThrow("name")
        while (cursorEmpregos.moveToNext()) {
            val name = cursorEmpregos.getString(nameIndexEmpregos)
            if (name == "apelido") hasApelido = true
            if (name == "endereco") hasEndereco = true
            if (name == "logo") hasLogo = true
        }
        cursorEmpregos.close()

        if (!hasApelido) {
            db.execSQL("ALTER TABLE `empregos` ADD COLUMN `apelido` TEXT")
        }
        if (!hasEndereco) {
            db.execSQL("ALTER TABLE `empregos` ADD COLUMN `endereco` TEXT")
        }
        if (!hasLogo) {
            db.execSQL("ALTER TABLE `empregos` ADD COLUMN `logo` TEXT")
        }

        // Reparar tabela 'configuracoes_emprego'
        val cursorConfig = db.query("PRAGMA table_info(configuracoes_emprego)")
        var hasFotoLocalArmazenamento = false
        var hasFotoValidarComprovante = false
        var hasExibirDuracaoTurno = false
        var hasExibirDuracaoIntervalo = false

        val nameIndexConfig = cursorConfig.getColumnIndexOrThrow("name")
        while (cursorConfig.moveToNext()) {
            val name = cursorConfig.getString(nameIndexConfig)
            if (name == "fotoLocalArmazenamento") hasFotoLocalArmazenamento = true
            if (name == "fotoValidarComprovante") hasFotoValidarComprovante = true
            if (name == "exibirDuracaoTurno") hasExibirDuracaoTurno = true
            if (name == "exibirDuracaoIntervalo") hasExibirDuracaoIntervalo = true
        }
        cursorConfig.close()

        if (!hasFotoLocalArmazenamento) {
            db.execSQL("ALTER TABLE `configuracoes_emprego` ADD COLUMN `fotoLocalArmazenamento` TEXT")
        }
        if (!hasFotoValidarComprovante) {
            db.execSQL("ALTER TABLE `configuracoes_emprego` ADD COLUMN `fotoValidarComprovante` INTEGER NOT NULL DEFAULT 0")
        }
        if (!hasExibirDuracaoTurno) {
            db.execSQL("ALTER TABLE `configuracoes_emprego` ADD COLUMN `exibirDuracaoTurno` INTEGER NOT NULL DEFAULT 1")
        }
        if (!hasExibirDuracaoIntervalo) {
            db.execSQL("ALTER TABLE `configuracoes_emprego` ADD COLUMN `exibirDuracaoIntervalo` INTEGER NOT NULL DEFAULT 1")
        }
    }
}
