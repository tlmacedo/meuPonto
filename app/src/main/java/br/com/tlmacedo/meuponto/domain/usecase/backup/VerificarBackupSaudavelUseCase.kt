package br.com.tlmacedo.meuponto.domain.usecase.backup

import br.com.tlmacedo.meuponto.data.local.datastore.PreferenciasGlobaisDataStore
import kotlinx.coroutines.flow.first
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import javax.inject.Inject

/**
 * Verifica se o status do backup do usuário está em dia (saudável).
 */
class VerificarBackupSaudavelUseCase @Inject constructor(
    private val preferencesDataStore: PreferenciasGlobaisDataStore
) {

    sealed class StatusBackup {
        object Saudavel : StatusBackup()
        data class Atrasado(val diasDesdeUltimo: Int) : StatusBackup()
        object NuncaRealizado : StatusBackup()
    }

    suspend operator fun invoke(limiteDias: Int = 7): StatusBackup {
        val prefs = preferencesDataStore.preferenciasGlobais.first()
        val ultimoLocal = prefs.ultimoBackupLocal
        val ultimoNuvem = prefs.ultimoBackupNuvem

        val timestampMaisRecente = maxOf(ultimoLocal, ultimoNuvem)

        if (timestampMaisRecente <= 0L) {
            return StatusBackup.NuncaRealizado
        }

        val dataUltimo = Instant.ofEpochMilli(timestampMaisRecente)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        val hoje = Instant.now()
            .atZone(ZoneId.systemDefault())
            .toLocalDate()

        val diasAtraso = ChronoUnit.DAYS.between(dataUltimo, hoje).toInt()

        return if (diasAtraso <= limiteDias) {
            StatusBackup.Saudavel
        } else {
            StatusBackup.Atrasado(diasAtraso)
        }
    }
}
