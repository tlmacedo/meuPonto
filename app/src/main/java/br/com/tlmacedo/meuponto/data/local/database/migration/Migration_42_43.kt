// path: app/src/main/java/br/com/tlmacedo/meuponto/data/local/database/migration/Migration_42_43.kt
package br.com.tlmacedo.meuponto.data.local.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_42_43 = object : Migration(42, 43) {
    override fun migrate(db: SupportSQLiteDatabase) {

        // Tabela da fila de sincronização na nuvem
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `cloud_sync_queue` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                `entityType` TEXT NOT NULL, 
                `entityId` INTEGER NOT NULL, 
                `operation` TEXT NOT NULL, 
                `payload` TEXT, 
                `tentativas` INTEGER NOT NULL, 
                `ultimoErro` TEXT, 
                `criadoEm` TEXT NOT NULL, 
                `sincronizadoEm` TEXT
            )
            """.trimIndent()
        )

        // Tabela de chamados
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `chamados` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                `identificador` TEXT NOT NULL, 
                `titulo` TEXT NOT NULL, 
                `descricao` TEXT NOT NULL, 
                `categoria` TEXT NOT NULL, 
                `status` TEXT NOT NULL, 
                `prioridade` TEXT NOT NULL, 
                `empregoId` INTEGER, 
                `usuarioId` INTEGER, 
                `usuarioNome` TEXT NOT NULL, 
                `usuarioEmail` TEXT NOT NULL, 
                `resposta` TEXT, 
                `anexos` TEXT, 
                `avaliacaoNota` TEXT, 
                `avaliacaoComentario` TEXT, 
                `avaliadoEm` TEXT, 
                `resolvidoEm` TEXT, 
                `criadoEm` TEXT NOT NULL, 
                `atualizadoEm` TEXT NOT NULL
            )
            """.trimIndent()
        )

        // Tabela de histórico de chamados
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `historico_chamados` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                `chamadoId` INTEGER NOT NULL, 
                `chamadoIdentificador` TEXT NOT NULL, 
                `statusAnterior` TEXT, 
                `statusNovo` TEXT NOT NULL, 
                `mensagem` TEXT NOT NULL, 
                `autor` TEXT NOT NULL, 
                `criadoEm` TEXT NOT NULL, 
                FOREIGN KEY(`chamadoId`) REFERENCES `chamados`(`id`) 
                    ON UPDATE NO ACTION ON DELETE CASCADE 
            )
            """.trimIndent()
        )

        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_historico_chamados_chamadoId` " +
                    "ON `historico_chamados` (`chamadoId`)"
        )
    }
}
