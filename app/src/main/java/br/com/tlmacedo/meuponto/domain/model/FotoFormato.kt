// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/model/FotoFormato.kt
package br.com.tlmacedo.meuponto.domain.model

import android.graphics.Bitmap

/**
 * Formato de salvamento da foto de comprovante.
 *
 * @property extensao Extensão do arquivo
 * @property mimeType MIME type para compartilhamento
 * @property compressFormat Formato de compressão do Bitmap
 *
 * @author Thiago
 * @since 10.0.0
 */
enum class FotoFormato(
    val extensao: String,
    val mimeType: String,
    val compressFormat: Bitmap.CompressFormat
) {
    /**
     * JPEG - Joint Photographic Experts Group
     * - Compressão com perdas
     * - Menor tamanho de arquivo
     * - Suporta qualidade variável (1-100)
     * - Recomendado para a maioria dos casos
     */
    JPEG(
        extensao = "jpg",
        mimeType = "image/jpeg",
        compressFormat = Bitmap.CompressFormat.JPEG
    ),

    /**
     * PNG - Portable Network Graphics
     * - Compressão sem perdas
     * - Maior tamanho de arquivo
     * - Suporta transparência
     * - Melhor para imagens com texto
     */
    PNG(
        extensao = "png",
        mimeType = "image/png",
        compressFormat = Bitmap.CompressFormat.PNG
    );

    /**
     * Gera o nome do arquivo com a extensão correta.
     */
    fun gerarNomeArquivo(prefixo: String, identificador: String): String {
        return "${prefixo}_${identificador}.${extensao}"
    }

    companion object {
        /**
         * Retorna o formato baseado na extensão do arquivo.
         */
        fun fromExtensao(extensao: String): FotoFormato? {
            return entries.find {
                it.extensao.equals(extensao, ignoreCase = true) ||
                        it.extensao.equals(extensao.removePrefix("."), ignoreCase = true)
            }
        }

        /**
         * Retorna o formato baseado no MIME type.
         */
        fun fromMimeType(mimeType: String): FotoFormato? {
            return entries.find { it.mimeType.equals(mimeType, ignoreCase = true) }
        }
    }
}
