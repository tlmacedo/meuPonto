// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/model/PreferenciasGlobais.kt
package br.com.tlmacedo.meuponto.domain.model

import java.time.DayOfWeek
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Configurações globais do aplicativo.
 * 
 * Separadas das configurações específicas de emprego (VersaoJornada).
 * Aplicam-se a todo o app independente do emprego ativo.
 *
 * @author Thiago
 * @since 8.1.0
 */
data class PreferenciasGlobais(
    // Aparência
    val temaEscuro: TemaEscuro = TemaEscuro.SISTEMA,
    val usarCoresDoSistema: Boolean = true,
    val corDestaque: Long = 0xFF6200EE,
    
    // Notificações
    val lembretePontoAtivo: Boolean = true,
    val alertaFeriadoAtivo: Boolean = true,
    val alertaBancoHorasAtivo: Boolean = true,
    val antecedenciaAlertaFeriadoDias: Int = 7,
    
    // Localização
    val localizacaoPadraoNome: String = "",
    val localizacaoPadraoLatitude: Double? = null,
    val localizacaoPadraoLongitude: Double? = null,
    val raioGeofencingMetros: Int = 100,
    val registroAutomaticoGeofencing: Boolean = false,
    
    // Formatos
    val formatoData: FormatoData = FormatoData.DD_MM_YYYY,
    val formatoHora: FormatoHora = FormatoHora.H24,
    val primeiroDiaSemana: DayOfWeek = DayOfWeek.SUNDAY,
    
    // Backup
    val backupAutomaticoAtivo: Boolean = false,
    val ultimoBackup: Long? = null
) {
    /**
     * Opções de tema escuro.
     */
    enum class TemaEscuro(val descricao: String) {
        SISTEMA("Seguir sistema"),
        ATIVADO("Sempre escuro"),
        DESATIVADO("Sempre claro");
        
        companion object {
            fun fromOrdinal(ordinal: Int): TemaEscuro = 
                entries.getOrElse(ordinal) { SISTEMA }
        }
    }
    
    /**
     * Formatos de data suportados.
     */
    enum class FormatoData(val pattern: String, val descricao: String, val exemplo: String) {
        DD_MM_YYYY("dd/MM/yyyy", "Dia/Mês/Ano", "04/03/2026"),
        MM_DD_YYYY("MM/dd/yyyy", "Mês/Dia/Ano", "03/04/2026"),
        YYYY_MM_DD("yyyy-MM-dd", "Ano-Mês-Dia", "2026-03-04");
        
        val formatter: DateTimeFormatter get() = DateTimeFormatter.ofPattern(pattern)
        
        companion object {
            fun fromOrdinal(ordinal: Int): FormatoData = 
                entries.getOrElse(ordinal) { DD_MM_YYYY }
        }
    }
    
    /**
     * Formatos de hora suportados.
     */
    enum class FormatoHora(val pattern: String, val descricao: String, val exemplo: String) {
        H24("HH:mm", "24 horas", "14:30"),
        H12("hh:mm a", "12 horas", "02:30 PM");
        
        val formatter: DateTimeFormatter get() = DateTimeFormatter.ofPattern(pattern, Locale.getDefault())
        
        companion object {
            fun fromOrdinal(ordinal: Int): FormatoHora = 
                entries.getOrElse(ordinal) { H24 }
        }
    }
    
    // Propriedades computadas
    val temLocalizacaoPadrao: Boolean 
        get() = localizacaoPadraoLatitude != null && localizacaoPadraoLongitude != null
    
    val localizacaoResumo: String
        get() = if (temLocalizacaoPadrao && localizacaoPadraoNome.isNotBlank()) {
            localizacaoPadraoNome
        } else if (temLocalizacaoPadrao) {
            "Lat: ${"%.4f".format(localizacaoPadraoLatitude)}, Long: ${"%.4f".format(localizacaoPadraoLongitude)}"
        } else {
            "Não definida"
        }
    
    val primeiroDiaSemanaDescricao: String
        get() = when (primeiroDiaSemana) {
            DayOfWeek.SUNDAY -> "Domingo"
            DayOfWeek.MONDAY -> "Segunda-feira"
            else -> primeiroDiaSemana.name.lowercase().replaceFirstChar { it.uppercase() }
        }
    
    companion object {
        val DEFAULT = PreferenciasGlobais()
        const val RAIO_GEOFENCING_MIN = 50
        const val RAIO_GEOFENCING_MAX = 500
        const val ANTECEDENCIA_FERIADO_MIN = 1
        const val ANTECEDENCIA_FERIADO_MAX = 30
    }
}
