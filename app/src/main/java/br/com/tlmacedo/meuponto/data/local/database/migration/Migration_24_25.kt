package br.com.tlmacedo.meuponto.data.local.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

val MIGRATION_24_25 = Migration(24, 25) { database ->
    // Valor padrão válido para LocalDateTime
    val agora = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

    // Adicionar coluna 'atualizadoEm' na tabela ajustes_saldo
    database.execSQL(
        """
        ALTER TABLE ajustes_saldo 
        ADD COLUMN atualizadoEm TEXT NOT NULL DEFAULT '$agora'
        """.trimIndent()
    )

    // Adicionar coluna 'tipo' na tabela ajustes_saldo
    database.execSQL(
        """
        ALTER TABLE ajustes_saldo 
        ADD COLUMN tipo TEXT NOT NULL DEFAULT 'CREDITO'
        """.trimIndent()
    )

    // Criar índice para a coluna 'tipo'
    database.execSQL(
        """
        CREATE INDEX IF NOT EXISTS index_ajustes_saldo_tipo 
        ON ajustes_saldo(tipo)
        """.trimIndent()
    )
}
