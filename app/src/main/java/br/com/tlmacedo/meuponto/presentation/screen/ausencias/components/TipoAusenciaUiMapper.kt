// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/ausencias/components/TipoAusenciaUiMapper.kt
package br.com.tlmacedo.meuponto.presentation.screen.ausencias.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BeachAccess
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.WorkOff
import androidx.compose.ui.graphics.vector.ImageVector
import br.com.tlmacedo.meuponto.domain.model.ausencia.TipoAusencia

fun TipoAusencia.toIcon(): ImageVector {
    return when (this) {
        TipoAusencia.Folga -> Icons.Default.Home
        TipoAusencia.Ferias -> Icons.Default.BeachAccess

        TipoAusencia.Feriado.Oficial,
        TipoAusencia.Feriado.DiaPonte,
        TipoAusencia.Feriado.Facultativo -> Icons.Default.EventBusy

        TipoAusencia.Atestado -> Icons.Default.LocalHospital
        TipoAusencia.Declaracao -> Icons.Default.Receipt
        TipoAusencia.DayOff -> Icons.Default.WorkOff
        TipoAusencia.CompensacaoBanco -> Icons.Default.Timer
        TipoAusencia.Falta.Justificada -> Icons.Default.Schedule
        TipoAusencia.Falta.Injustificada -> Icons.Default.EventBusy
    }
}

fun TipoAusencia.toLabel(): String {
    return when (this) {
        TipoAusencia.Folga -> "Folga"
        TipoAusencia.Ferias -> "Férias"
        TipoAusencia.Feriado.Oficial -> "Feriado"
        TipoAusencia.Feriado.DiaPonte -> "Dia ponte"
        TipoAusencia.Feriado.Facultativo -> "Facultativo"
        TipoAusencia.Atestado -> "Atestado"
        TipoAusencia.Declaracao -> "Declaração"
        TipoAusencia.DayOff -> "Day off"
        TipoAusencia.CompensacaoBanco -> "Diminuir banco"
        TipoAusencia.Falta.Justificada -> "Falta justificada"
        TipoAusencia.Falta.Injustificada -> "Falta injustificada"
    }
}