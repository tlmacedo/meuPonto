package br.com.tlmacedo.meuponto.data.local.database.migration

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import br.com.tlmacedo.meuponto.data.local.database.MeuPontoDatabase
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class MigrationTest {
    private val TEST_DB = "migration-test"

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        MeuPontoDatabase::class.java.canonicalName,
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    @Throws(IOException::class)
    fun migrate22To23() {
        var db = helper.createDatabase(TEST_DB, 22)
        db.close()

        // Executar migração
        db = helper.runMigrationsAndValidate(TEST_DB, 23, true, MIGRATION_22_23)

        // Verificar se as colunas foram adicionadas na tabela pontos
        val cursorPontos = db.query("PRAGMA table_info(pontos)")
        var foundIsDeleted = false
        var foundDeletedAt = false
        var foundUpdatedAt = false
        while (cursorPontos.moveToNext()) {
            val name = cursorPontos.getString(cursorPontos.getColumnIndexOrThrow("name"))
            if (name == "is_deleted") foundIsDeleted = true
            if (name == "deleted_at") foundDeletedAt = true
            if (name == "updated_at") foundUpdatedAt = true
        }
        cursorPontos.close()
        assert(foundIsDeleted) { "Coluna 'is_deleted' não encontrada em pontos após migração 22->23" }
        assert(foundDeletedAt) { "Coluna 'deleted_at' não encontrada em pontos após migração 22->23" }
        assert(foundUpdatedAt) { "Coluna 'updated_at' não encontrada em pontos após migração 22->23" }
        
        db.close()
    }

    @Test
    @Throws(IOException::class)
    fun migrate23To24() {
        var db = helper.createDatabase(TEST_DB, 23)
        db.close()

        // Executar migração
        db = helper.runMigrationsAndValidate(TEST_DB, 24, true, MIGRATION_23_24)

        // Verificar se as colunas foram adicionadas na tabela ajustes_saldo
        val cursor = db.query("PRAGMA table_info(ajustes_saldo)")
        var foundTipo = false
        var foundAtualizadoEm = false
        while (cursor.moveToNext()) {
            val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
            if (name == "tipo") foundTipo = true
            if (name == "atualizadoEm") foundAtualizadoEm = true
        }
        cursor.close()
        assert(foundTipo) { "Coluna 'tipo' não encontrada em ajustes_saldo após migração 23->24" }
        assert(foundAtualizadoEm) { "Coluna 'atualizadoEm' não encontrada em ajustes_saldo após migração 23->24" }
        
        db.close()
    }

    @Test
    @Throws(IOException::class)
    fun migrate24To25() {
        var db = helper.createDatabase(TEST_DB, 24)
        db.close()

        // Executar migração vazia
        db = helper.runMigrationsAndValidate(TEST_DB, 25, true, MIGRATION_24_25)
        db.close()
    }

    @Test
    @Throws(IOException::class)
    fun migrate22To27() {
        // Teste de migração completa desde a 22
        helper.createDatabase(TEST_DB, 22).close()

        helper.runMigrationsAndValidate(
            TEST_DB,
            27,
            true,
            MIGRATION_22_23,
            MIGRATION_23_24,
            MIGRATION_24_25,
            MIGRATION_25_26,
            MIGRATION_26_27
        )
    }

    @Test
    @Throws(IOException::class)
    fun migrate26To27() {
        helper.createDatabase(TEST_DB, 26).close()

        // Executar migração e validar schema
        helper.runMigrationsAndValidate(TEST_DB, 27, true, MIGRATION_26_27)
    }
}
