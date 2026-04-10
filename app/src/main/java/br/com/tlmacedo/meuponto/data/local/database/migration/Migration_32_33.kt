package br.com.tlmacedo.meuponto.data.local.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migração da versão 32 para 33:
 * - Nenhuma alteração de esquema necessária. Esta migração foi criada para resolver
 *   um IllegalStateException, mas as colunas que tentava adicionar já existiam.
 */
val MIGRATION_32_33 = object : Migration(32, 33) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // 1. Criar a nova tabela com o esquema correto (conforme v33.json)
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `pontos_new` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                `empregoId` INTEGER NOT NULL, 
                `dataHora` TEXT NOT NULL, 
                `data` TEXT NOT NULL, 
                `hora` TEXT NOT NULL, 
                `horaConsiderada` TEXT NOT NULL, 
                `nsr` TEXT, 
                `observacao` TEXT, 
                `isEditadoManualmente` INTEGER NOT NULL, 
                `latitude` REAL, 
                `longitude` REAL, 
                `endereco` TEXT, 
                `marcadorId` INTEGER, 
                `justificativaInconsistencia` TEXT, 
                `fotoComprovantePath` TEXT, 
                `foto_origem` INTEGER NOT NULL DEFAULT 0, 
                `criadoEm` TEXT NOT NULL, 
                `atualizadoEm` TEXT NOT NULL, 
                `is_deleted` INTEGER NOT NULL DEFAULT 0, 
                `deleted_at` INTEGER, 
                `updated_at` INTEGER NOT NULL DEFAULT 0, 
                FOREIGN KEY(`empregoId`) REFERENCES `empregos`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE, 
                FOREIGN KEY(`marcadorId`) REFERENCES `marcadores`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL
            )
            """.trimIndent()
        )

        // 2. Copiar dados da tabela antiga para a nova
        // Verificamos se a coluna 'foto_origem' existe na tabela atual para evitar erro no SELECT
        val cursor = db.query("PRAGMA table_info(pontos)")
        var hasFotoOrigem = false
        while (cursor.moveToNext()) {
            if (cursor.getString(cursor.getColumnIndexOrThrow("name")) == "foto_origem") {
                hasFotoOrigem = true
                break
            }
        }
        cursor.close()

        val selectColumns = if (hasFotoOrigem) {
            "id, empregoId, dataHora, data, hora, horaConsiderada, nsr, observacao, isEditadoManualmente, latitude, longitude, endereco, marcadorId, justificativaInconsistencia, fotoComprovantePath, foto_origem, criadoEm, atualizadoEm, is_deleted, deleted_at, updated_at"
        } else {
            "id, empregoId, dataHora, data, hora, horaConsiderada, nsr, observacao, isEditadoManualmente, latitude, longitude, endereco, marcadorId, justificativaInconsistencia, fotoComprovantePath, 0 as foto_origem, criadoEm, atualizadoEm, is_deleted, deleted_at, updated_at"
        }

        db.execSQL(
            """
            INSERT INTO `pontos_new` (
                id, empregoId, dataHora, data, hora, horaConsiderada, nsr, observacao, 
                isEditadoManualmente, latitude, longitude, endereco, marcadorId, 
                justificativaInconsistencia, fotoComprovantePath, foto_origem, 
                criadoEm, atualizadoEm, is_deleted, deleted_at, updated_at
            )
            SELECT $selectColumns FROM `pontos`
            """.trimIndent()
        )

        // 3. Remover tabela antiga e renomear a nova
        db.execSQL("DROP TABLE `pontos`")
        db.execSQL("ALTER TABLE `pontos_new` RENAME TO `pontos`")

        // 4. Recriar os índices (essencial para o Room validar o TableInfo)
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_pontos_empregoId` ON `pontos` (`empregoId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_pontos_marcadorId` ON `pontos` (`marcadorId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_pontos_dataHora` ON `pontos` (`dataHora`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_pontos_data` ON `pontos` (`data`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_pontos_empregoId_data` ON `pontos` (`empregoId`, `data`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_pontos_empregoId_dataHora` ON `pontos` (`empregoId`, `dataHora`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_pontos_is_deleted` ON `pontos` (`is_deleted`)")
    }
}

