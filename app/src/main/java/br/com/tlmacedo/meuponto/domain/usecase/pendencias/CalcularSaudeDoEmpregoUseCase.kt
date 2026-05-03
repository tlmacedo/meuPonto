package br.com.tlmacedo.meuponto.domain.usecase.pendencias

import br.com.tlmacedo.meuponto.domain.usecase.backup.VerificarBackupSaudavelUseCase
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

/**
 * Calcula um score de saúde para o emprego baseado em pendências e backup.
 *
 * @author Thiago
 * @since 14.0.0
 * @updated 14.1.0 - Adicionado suporte a período específico para o cálculo de pendências
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

    suspend operator fun invoke(
        empregoId: Long,
        mesReferencia: YearMonth = YearMonth.now()
    ): SaudeEmprego {
        val pendencias = listarPendenciasPontoUseCase(
            empregoId = empregoId,
            dataInicio = mesReferencia.atDay(1),
            dataFim = mesReferencia.atEndOfMonth()
        )
        
        val backup = verificarBackupSaudavelUseCase()
        
        // Lógica de Score
        var score = 100
        
        // Cada dia com pendência reduz 5 pontos (máx 40)
        score -= (pendencias.total * 5).coerceAtMost(40)
        
        // Se houver bloqueados tiram 30 pontos fixos
        if (pendencias.bloqueados.isNotEmpty()) {
            score -= 30
        }
        
        // Backup atrasado tira 20 pontos, nunca feito tira 40
        // Nota: O backup é um estado GLOBAL, não do mês.
        when (backup) {
            is VerificarBackupSaudavelUseCase.StatusBackup.Atrasado -> score -= 20
            is VerificarBackupSaudavelUseCase.StatusBackup.NuncaRealizado -> score -= 40
            else -> {}
        }
        
        return SaudeEmprego(
            totalPendenciasMes = pendencias.total,
            temBloqueantes = pendencias.bloqueados.isNotEmpty(),
            backupStatus = backup,
            percentualSaude = score.coerceIn(0, 100)
        )
    }
}
