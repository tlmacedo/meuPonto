package br.com.tlmacedo.meuponto.data.local.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migração da versão 40 para 41:
 * - Adição do campo observacao na tabela fotos_comprovante para cachear a observação do ponto.
 */
val MIGRATION_40_41 = object : Migration(40, 41) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `fotos_comprovante` ADD COLUMN `observacao` TEXT")
        
        // Popula as observações dos pontos já existentes
        db.execSQL("""
            UPDATE fotos_comprovante 
            SET observacao = (
                SELECT observacao FROM pontos 
                WHERE pontos.id = fotos_comprovante.pontoId
            )
            WHERE pontoId != 0
        """)
    }
}
