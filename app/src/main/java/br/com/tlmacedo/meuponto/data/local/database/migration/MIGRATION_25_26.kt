package br.com.tlmacedo.meuponto.data.local.database.migration

import androidx.room.migration.Migration

val MIGRATION_25_26 = Migration(25, 26) { database ->
    // Corrigir valores vazios de 'atualizadoEm' usando o valor de 'criadoEm'
    database.execSQL(
        """
        UPDATE ajustes_saldo 
        SET atualizadoEm = criadoEm 
        WHERE atualizadoEm = '' OR atualizadoEm IS NULL
        """.trimIndent()
    )

    // Corrigir valores vazios de 'tipo' com valor padrão 'MANUAL' (alinhado com TipoAjusteSaldo.MANUAL)
    database.execSQL(
        """
        UPDATE ajustes_saldo
        SET tipo = 'MANUAL'
        WHERE tipo = '' OR tipo IS NULL OR tipo = 'CREDITO'
        """.trimIndent()
    )
}
