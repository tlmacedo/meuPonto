// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/data/local/database/migration/Migration_3_4.kt
package br.com.tlmacedo.meuponto.data.local.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migration da versão 3 para 4 do banco de dados.
 * Restaura as colunas de tolerância de entrada e saída que foram removidas anteriormente.
 *
 * @author Thiago
 * @since 2.1.1
 */
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Verifica se as colunas já existem para evitar erro de duplicidade
        
        // Configuracoes_emprego
        if (!columnExists(db, "configuracoes_emprego", "toleranciaEntradaMinutos")) {
            db.execSQL("ALTER TABLE configuracoes_emprego ADD COLUMN toleranciaEntradaMinutos INTEGER NOT NULL DEFAULT 10")
        }
        if (!columnExists(db, "configuracoes_emprego", "toleranciaSaidaMinutos")) {
            db.execSQL("ALTER TABLE configuracoes_emprego ADD COLUMN toleranciaSaidaMinutos INTEGER NOT NULL DEFAULT 10")
        }
        
        // Horarios_dia_semana
        if (!columnExists(db, "horarios_dia_semana", "toleranciaEntradaMinutos")) {
            db.execSQL("ALTER TABLE horarios_dia_semana ADD COLUMN toleranciaEntradaMinutos INTEGER DEFAULT NULL")
        }
        if (!columnExists(db, "horarios_dia_semana", "toleranciaSaidaMinutos")) {
            db.execSQL("ALTER TABLE horarios_dia_semana ADD COLUMN toleranciaSaidaMinutos INTEGER DEFAULT NULL")
        }
    }
}
