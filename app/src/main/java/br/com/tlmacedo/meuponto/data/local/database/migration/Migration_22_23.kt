// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/data/local/database/migration/Migration_22_23.kt
package br.com.tlmacedo.meuponto.data.local.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migração 22 → 23: Sistema completo de foto de comprovante com metadados.
 *
 * ## Alterações:
 *
 * ### Nova Tabela: fotos_comprovante
 * Armazena metadados completos das fotos de comprovante, incluindo:
 * - Dados do ponto (snapshot): data, hora, índice (tipo dinâmico), NSR
 * - Localização: coordenadas GPS, altitude, precisão, endereço
 * - Jornada (snapshot): versão, tipo do dia, horas trabalhadas, saldos
 * - Foto: path, timestamp, origem, tamanho, hash MD5
 * - Sincronização: status de backup na nuvem
 *
 * ### Novos campos em configuracoes_emprego:
 * - fotoHabilitada: habilita/desabilita a funcionalidade
 * - fotoFormato: JPEG ou PNG
 * - fotoQualidade: 1-100 (apenas JPEG)
 * - fotoResolucaoMaxima: largura máxima em pixels
 * - fotoTamanhoMaximoKb: tamanho máximo do arquivo
 * - fotoCorrecaoOrientacao: corrigir orientação EXIF
 * - fotoApenasCamera: desabilitar seleção da galeria
 * - fotoIncluirLocalizacaoExif: incluir GPS no EXIF
 * - fotoBackupNuvemHabilitado: habilitar backup automático
 * - fotoBackupApenasWifi: sincronizar apenas em Wi-Fi
 *
 * @author Thiago
 * @since 10.0.0
 */
val MIGRATION_22_23 = object : Migration(22, 23) {
    override fun migrate(db: SupportSQLiteDatabase) {

        // ════════════════════════════════════════════════════════════════════
        // 1. CRIAR TABELA fotos_comprovante
        // ════════════════════════════════════════════════════════════════════

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS fotos_comprovante (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,

                -- Vínculo
                pontoId INTEGER NOT NULL,
                empregoId INTEGER NOT NULL,

                -- Dados do Ponto (Snapshot)
                data TEXT NOT NULL,
                diaSemana TEXT NOT NULL,
                hora TEXT NOT NULL,
                indicePontoDia INTEGER NOT NULL,
                nsr TEXT,

                -- Localização
                latitude REAL,
                longitude REAL,
                altitude REAL,
                precisaoMetros REAL,
                enderecoFormatado TEXT,

                -- Jornada (Snapshot)
                versaoJornada INTEGER NOT NULL,
                tipoJornadaDia TEXT NOT NULL,
                horasTrabalhadasDiaMinutos INTEGER NOT NULL,
                saldoDiaMinutos INTEGER NOT NULL,
                saldoBancoHorasMinutos INTEGER NOT NULL,

                -- Metadados da Foto
                fotoPath TEXT NOT NULL,
                fotoTimestamp TEXT NOT NULL,
                fotoOrigem TEXT NOT NULL,
                fotoTamanhoBytes INTEGER NOT NULL,
                fotoHashMd5 TEXT NOT NULL,

                -- Sincronização
                sincronizadoNuvem INTEGER NOT NULL DEFAULT 0,
                sincronizadoEm TEXT,
                cloudFileId TEXT,

                -- Auditoria
                criadoEm TEXT NOT NULL,
                atualizadoEm TEXT NOT NULL,

                -- Foreign Key
                FOREIGN KEY (pontoId) REFERENCES pontos(id) ON DELETE CASCADE ON UPDATE NO ACTION
            )
        """.trimIndent()
        )

        // ════════════════════════════════════════════════════════════════════
        // 2. CRIAR ÍNDICES
        // ════════════════════════════════════════════════════════════════════

        // Índice único para garantir 1:1 com pontos
        db.execSQL(
            """
            CREATE UNIQUE INDEX IF NOT EXISTS index_fotos_comprovante_pontoId
            ON fotos_comprovante(pontoId)
        """.trimIndent()
        )

        // Índice para consultas por emprego
        db.execSQL(
            """
            CREATE INDEX IF NOT EXISTS index_fotos_comprovante_empregoId
            ON fotos_comprovante(empregoId)
        """.trimIndent()
        )

        // Índice para consultas por data
        db.execSQL(
            """
            CREATE INDEX IF NOT EXISTS index_fotos_comprovante_data
            ON fotos_comprovante(data)
        """.trimIndent()
        )

        // Índice para consultas de sincronização
        db.execSQL(
            """
            CREATE INDEX IF NOT EXISTS index_fotos_comprovante_sincronizadoNuvem
            ON fotos_comprovante(sincronizadoNuvem)
        """.trimIndent()
        )

        // ════════════════════════════════════════════════════════════════════
        // 3. ADICIONAR CAMPOS EM configuracoes_emprego
        // ════════════════════════════════════════════════════════════════════

        // Habilitar funcionalidade de foto
        db.execSQL(
            """
            ALTER TABLE configuracoes_emprego
            ADD COLUMN fotoHabilitada INTEGER NOT NULL DEFAULT 0
        """.trimIndent()
        )

        // Formato da foto (JPEG ou PNG)
        db.execSQL(
            """
            ALTER TABLE configuracoes_emprego
            ADD COLUMN fotoFormato TEXT NOT NULL DEFAULT 'JPEG'
        """.trimIndent()
        )

        // Qualidade de compressão (1-100)
        db.execSQL(
            """
            ALTER TABLE configuracoes_emprego
            ADD COLUMN fotoQualidade INTEGER NOT NULL DEFAULT 85
        """.trimIndent()
        )

        // Resolução máxima em pixels (largura)
        db.execSQL(
            """
            ALTER TABLE configuracoes_emprego
            ADD COLUMN fotoResolucaoMaxima INTEGER NOT NULL DEFAULT 1920
        """.trimIndent()
        )

        // Tamanho máximo em KB
        db.execSQL(
            """
            ALTER TABLE configuracoes_emprego
            ADD COLUMN fotoTamanhoMaximoKb INTEGER NOT NULL DEFAULT 1024
        """.trimIndent()
        )

        // Corrigir orientação EXIF
        db.execSQL(
            """
            ALTER TABLE configuracoes_emprego
            ADD COLUMN fotoCorrecaoOrientacao INTEGER NOT NULL DEFAULT 1
        """.trimIndent()
        )

        // Permitir apenas câmera
        db.execSQL(
            """
            ALTER TABLE configuracoes_emprego
            ADD COLUMN fotoApenasCamera INTEGER NOT NULL DEFAULT 0
        """.trimIndent()
        )

        // Incluir localização no EXIF
        db.execSQL(
            """
            ALTER TABLE configuracoes_emprego
            ADD COLUMN fotoIncluirLocalizacaoExif INTEGER NOT NULL DEFAULT 1
        """.trimIndent()
        )

        // Backup na nuvem habilitado
        db.execSQL(
            """
            ALTER TABLE configuracoes_emprego
            ADD COLUMN fotoBackupNuvemHabilitado INTEGER NOT NULL DEFAULT 0
        """.trimIndent()
        )

        // Backup apenas em Wi-Fi
        db.execSQL(
            """
            ALTER TABLE configuracoes_emprego
            ADD COLUMN fotoBackupApenasWifi INTEGER NOT NULL DEFAULT 1
        """.trimIndent()
        )

        // ════════════════════════════════════════════════════════════════════
        // 4. SUPORTE A SOFT DELETE NA TABELA PONTOS (Adicionado retroativamente)
        // ════════════════════════════════════════════════════════════════════

        // Adicionar coluna is_deleted com valor padrão 0 (false)
        db.execSQL(
            """
            ALTER TABLE pontos 
            ADD COLUMN is_deleted INTEGER NOT NULL DEFAULT 0
            """
        )

        // Adicionar coluna deleted_at (nullable)
        db.execSQL(
            """
            ALTER TABLE pontos 
            ADD COLUMN deleted_at INTEGER DEFAULT NULL
            """
        )

        // Adicionar coluna updated_at
        db.execSQL(
            """
            ALTER TABLE pontos 
            ADD COLUMN updated_at INTEGER NOT NULL DEFAULT 0
            """
        )

        // Criar índice para consultas de soft delete
        db.execSQL(
            """
            CREATE INDEX IF NOT EXISTS index_pontos_is_deleted 
            ON pontos(is_deleted)
            """
        )

        // Atualizar updated_at com timestamp atual para registros existentes
        val currentTime = System.currentTimeMillis()
        db.execSQL(
            """
            UPDATE pontos SET updated_at = $currentTime WHERE updated_at = 0
            """
        )
    }
}
