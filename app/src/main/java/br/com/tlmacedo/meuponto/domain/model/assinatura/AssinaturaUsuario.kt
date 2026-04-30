package br.com.tlmacedo.meuponto.domain.model.assinatura

import java.time.LocalDateTime

data class AssinaturaUsuario(
    val usuarioId: String,
    val plano: PlanoAssinatura = PlanoAssinatura.FREE,
    val ativa: Boolean = true,
    val origem: OrigemAssinatura = OrigemAssinatura.LOCAL,
    val iniciouEm: LocalDateTime = LocalDateTime.now(),
    val expiraEm: LocalDateTime? = null,
    val atualizadoEm: LocalDateTime = LocalDateTime.now()
) {
    val isPremiumAtivo: Boolean
        get() = ativa && plano.isPago && !isExpirada

    val isExpirada: Boolean
        get() = expiraEm?.isBefore(LocalDateTime.now()) == true

    fun podeUsar(recurso: RecursoPremium): Boolean {
        if (!ativa || isExpirada) return false

        return when (recurso) {
            RecursoPremium.BACKUP_NUVEM -> plano.permiteSyncNuvem
            RecursoPremium.RELATORIO_PDF -> plano.permiteRelatoriosAvancados
            RecursoPremium.EXPORTACAO_CSV_EXCEL -> plano.permiteExportacao
            RecursoPremium.FOTO_COMPROVANTE -> plano.permiteFotosComprovante
            RecursoPremium.INSIGHTS_INTELIGENTES -> plano.permiteInsights
            RecursoPremium.MULTIPLOS_EMPREGOS -> plano.limiteEmpregos > 1
            RecursoPremium.SUPORTE_PRIORITARIO -> plano.permiteSuportePrioritario
        }
    }
}