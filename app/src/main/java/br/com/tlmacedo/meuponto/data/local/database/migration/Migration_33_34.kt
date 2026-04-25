package br.com.tlmacedo.meuponto.data.local.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migração da versão 33 para 34:
 * - Garante que as colunas 'razaoSocial' e 'cnpj' existam na tabela 'empregos'.
 * - Estas colunas foram introduzidas na versão 33 mas podem estar faltando se a
 *   MIGRATION_32_33 original foi executada.
 */
val MIGRATION_33_34 = object : Migration(33, 34) {
    override fun migrate(db: SupportSQLiteDatabase) {
        val cursor = db.query("PRAGMA table_info(empregos)")
        var hasRazaoSocial = false
        var hasCnpj = false

        val nameIndex = cursor.getColumnIndexOrThrow("name")
        while (cursor.moveToNext()) {
            val name = cursor.getString(nameIndex)
            if (name == "razaoSocial") hasRazaoSocial = true
            if (name == "cnpj") hasCnpj = true
        }
        cursor.close()

        if (!hasRazaoSocial) {
            db.execSQL("ALTER TABLE `empregos` ADD COLUMN `razaoSocial` TEXT")
        }
        if (!hasCnpj) {
            db.execSQL("ALTER TABLE `empregos` ADD COLUMN `cnpj` TEXT")
        }
    }
}
