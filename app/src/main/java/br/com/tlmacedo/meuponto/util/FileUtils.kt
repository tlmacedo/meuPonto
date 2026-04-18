package br.com.tlmacedo.meuponto.util

import java.util.Locale

object FileUtils {
    /**
     * Formata o tamanho em bytes para uma string legível (B, KB, MB, GB).
     */
    fun formatarTamanho(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB", "PB", "EB")
        var digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
        if (digitGroups >= units.size) digitGroups = units.size - 1
        return String.format(
            Locale.getDefault(),
            "%.2f %s",
            bytes / Math.pow(1024.0, digitGroups.toDouble()),
            units[digitGroups]
        )
    }
}
