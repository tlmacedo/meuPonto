package br.com.tlmacedo.meuponto.data.local.database.migration

import androidx.room.migration.Migration

/**
 * Migração da versão 26 para 27: Adição da tabela 'usuarios' para o Módulo de Autenticação.
 */
val MIGRATION_26_27 = Migration(26, 27) { database ->
    database.execSQL(
        """
        CREATE TABLE IF NOT EXISTS `usuarios` (
            `id` TEXT NOT NULL, 
            `nome` TEXT NOT NULL, 
            `email` TEXT NOT NULL, 
            `senhaHash` TEXT NOT NULL, 
            `biometriaHabilitada` INTEGER NOT NULL DEFAULT 0, 
            PRIMARY KEY(`id`)
        )
        """.trimIndent()
    )
}
