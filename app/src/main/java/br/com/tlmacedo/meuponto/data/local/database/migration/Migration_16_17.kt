// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/data/local/database/migration/Migration_16_17.kt
package br.com.tlmacedo.meuponto.data.local.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import br.com.tlmacedo.meuponto.core.security.CryptoHelper
import timber.log.Timber

/**
 * Migration 16 → 17: Criptografa dados de localização existentes.
 * 
 * Esta migration:
 * 1. Cria colunas temporárias para dados criptografados
 * 2. Criptografa os dados existentes
 * 3. Remove colunas antigas (via recriação da tabela)
 * 4. Renomeia colunas novas
 *
 * IMPORTANTE: Esta migration é irreversível. Faça backup antes!
 *
 * @author Thiago
 * @since 6.1.0
 */
val MIGRATION_16_17 = object : Migration(16, 17) {
    override fun migrate(db: SupportSQLiteDatabase) {
        Timber.i("Iniciando migration 16→17: Criptografia de localização")

        // 1. Adicionar colunas criptografadas temporárias
        db.execSQL("ALTER TABLE pontos ADD COLUMN latitudeEnc TEXT")
        db.execSQL("ALTER TABLE pontos ADD COLUMN longitudeEnc TEXT")
        db.execSQL("ALTER TABLE pontos ADD COLUMN enderecoEnc TEXT")

        // 2. Criptografar dados existentes
        var registrosCriptografados = 0
        val cursor = db.query(
            "SELECT id, latitude, longitude, endereco FROM pontos " +
                    "WHERE latitude IS NOT NULL OR longitude IS NOT NULL OR endereco IS NOT NULL"
        )

        cursor.use {
            while (it.moveToNext()) {
                val id = it.getLong(0)
                val latitude = if (it.isNull(1)) null else it.getDouble(1)
                val longitude = if (it.isNull(2)) null else it.getDouble(2)
                val endereco = it.getString(3)

                val latEnc = CryptoHelper.encryptDouble(latitude)
                val lonEnc = CryptoHelper.encryptDouble(longitude)
                val endEnc = CryptoHelper.encrypt(endereco)

                db.execSQL(
                    "UPDATE pontos SET latitudeEnc = ?, longitudeEnc = ?, enderecoEnc = ? WHERE id = ?",
                    arrayOf(latEnc, lonEnc, endEnc, id)
                )
                registrosCriptografados++
            }
        }

        Timber.d("Criptografados $registrosCriptografados registros com localização")

        // 3. Recriar tabela com novo schema (campos TEXT em vez de REAL)
        db.execSQL("""
            CREATE TABLE pontos_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                empregoId INTEGER NOT NULL DEFAULT 1,
                dataHora TEXT NOT NULL,
                observacao TEXT,
                isEditadoManualmente INTEGER NOT NULL DEFAULT 0,
                nsr TEXT,
                latitude TEXT,
                longitude TEXT,
                endereco TEXT,
                marcadorId INTEGER,
                justificativaInconsistencia TEXT,
                horaConsiderada TEXT,
                criadoEm TEXT NOT NULL,
                atualizadoEm TEXT NOT NULL,
                data TEXT NOT NULL,
                hora TEXT NOT NULL,
                FOREIGN KEY(empregoId) REFERENCES empregos(id) ON DELETE CASCADE,
                FOREIGN KEY(marcadorId) REFERENCES marcadores(id) ON DELETE SET NULL
            )
        """)

        // 4. Copiar dados com colunas criptografadas
        db.execSQL("""
            INSERT INTO pontos_new (
                id, empregoId, dataHora, observacao, isEditadoManualmente, nsr,
                latitude, longitude, endereco,
                marcadorId, justificativaInconsistencia, horaConsiderada,
                criadoEm, atualizadoEm, data, hora
            )
            SELECT 
                id, empregoId, dataHora, observacao, isEditadoManualmente, nsr,
                latitudeEnc, longitudeEnc, enderecoEnc,
                marcadorId, justificativaInconsistencia, horaConsiderada,
                criadoEm, atualizadoEm, data, hora
            FROM pontos
        """)

        // 5. Remover tabela antiga e renomear
        db.execSQL("DROP TABLE pontos")
        db.execSQL("ALTER TABLE pontos_new RENAME TO pontos")

        // 6. Recriar índices
        db.execSQL("CREATE INDEX index_pontos_empregoId ON pontos(empregoId)")
        db.execSQL("CREATE INDEX index_pontos_data ON pontos(data)")
        db.execSQL("CREATE INDEX index_pontos_empregoId_data ON pontos(empregoId, data)")
        db.execSQL("CREATE INDEX index_pontos_marcadorId ON pontos(marcadorId)")

        Timber.i("Migration 16→17 concluída com sucesso")
    }
}
