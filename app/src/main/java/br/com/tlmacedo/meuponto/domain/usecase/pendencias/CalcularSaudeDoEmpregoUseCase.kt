package br.com.tlmacedo.meuponto.domain.usecase.pendencias

import br.com.tlmacedo.meuponto.domain.usecase.backup.VerificarBackupSaudavelUseCase
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

/**
 * Calcula um score de saúde para o emprego baseado em pendências e backup.
 */
class CalcularSaudeDoEmpregoUseCase @Inject constructor(
    private val listarPendenciasPontoUseCase: ListarPendenciasPontoUseCase,
    private val verificarBackupSaudavelUseCase: VerificarBackupSaudavelUseCase
) {
    data class SaudeEmprego(
        val totalPendenciasMes: Int,
        val temBloqueantes: Boolean,
        val backupStatus: VerificarBackupSaudavelUseCase.StatusBackup,
        val percentualSaude: Int // 0-100
    )

    suspend operator fun invoke(empregoId: Long): SaudeEmprego {
        val hoje = LocalDate.now()
        val mesAtual = YearMonth.from(hoje)
        
        val pendencias = listarPendenciasPontoUseCase(
            empregoId = empregoId,
            dataInicio = mesAtual.atDay(1),
            dataFim = mesAtual.atEndOfMonth()
        )
        
        val backup = verificarBackupSaudavelUseCase()
        
        // Lógica de Score
        var score = 100
        
        // Cada pendência reduz 5 pontos (máx 40)
        score -= (pendencias.total * 5).coerceAtMost(40)
        
        // Bloqueantes tiram 30 pontos fixos
        if (pendencias.bloqueantes.isNotEmpty()) {
            score -= 30
        }
        
        // Backup atrasado tira 20 pontos, nunca feito tira 40
        when (backup) {
            is VerificarBackupSaudavelUseCase.StatusBackup.Atrasado -> score -= 20
            is VerificarBackupSaudavelUseCase.StatusBackup.NuncaRealizado -> score -= 40
            else -> {}
        }
        
        return SaudeEmprego(
            totalPendenciasMes = pendencias.total,
            temBloqueantes = pendencias.bloqueantes.isNotEmpty(),
            backupStatus = backup,
            percentualSaude = score.coerceIn(0, 100)
        )
    }
}
