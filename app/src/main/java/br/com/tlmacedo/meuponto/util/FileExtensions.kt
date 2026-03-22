// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/util/FileExtensions.kt
package br.com.tlmacedo.meuponto.util

/**
 * Extensões centralizadas para manipulação de arquivos e formatação de tamanhos.
 *
 * Este arquivo centraliza toda a lógica de formatação de bytes que estava
 * duplicada em [AdaptiveCompressionResult], [ImageProcessingResult],
 * [SavePhotoResult], [TrashItem], [TrashStats] e [ComprovanteImageStorage].
 *
 * ## Antes (duplicado em 6 lugares com implementações ligeiramente diferentes):
 * ```kotlin
 * val sizeFormatted: String get() = when {
 *     sizeBytes < 1024 -> "$sizeBytes B"
 *     sizeBytes < 1024 * 1024 -> String.format("%.1f KB", sizeBytes / 1024.0)
 *     else -> String.format("%.2f MB", sizeBytes / (1024.0 * 1024.0))
 * }
 * ```
 *
 * ## Depois (único ponto de verdade):
 * ```kotlin
 * val sizeFormatted: String get() = sizeBytes.formatarTamanho()
 * ```
 *
 * @author Thiago
 * @since 12.0.0
 */

/**
 * Formata um tamanho em bytes para representação legível por humanos.
 *
 * Exemplos de saída:
 * - `512L.formatarTamanho()` → "512 B"
 * - `1536L.formatarTamanho()` → "1.5 KB"
 * - `2097152L.formatarTamanho()` → "2.0 MB"
 * - `1610612736L.formatarTamanho()` → "1.50 GB"
 *
 * @return String formatada com unidade de medida (B, KB, MB ou GB)
 */
fun Long.formatarTamanho(): String = when {
    this < 1_024L              -> "$this B"
    this < 1_048_576L          -> String.format("%.1f KB", this / 1_024.0)
    this < 1_073_741_824L      -> String.format("%.1f MB", this / 1_048_576.0)
    else                       -> String.format("%.2f GB", this / 1_073_741_824.0)
}

/**
 * Formata um tamanho em bytes (Int) para representação legível.
 * Delega para a versão [Long].
 *
 * @return String formatada com unidade de medida (B, KB, MB ou GB)
 */
fun Int.formatarTamanho(): String = this.toLong().formatarTamanho()