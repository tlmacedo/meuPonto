package br.com.tlmacedo.meuponto.domain.model.assinatura

enum class PlanoAssinatura(
    val descricao: String,
    val limiteEmpregos: Int,
    val limiteRegistrosMes: Int?,
    val permiteSyncNuvem: Boolean,
    val permiteRelatoriosAvancados: Boolean,
    val permiteInsights: Boolean,
    val permiteFotosComprovante: Boolean,
    val permiteExportacao: Boolean,
    val permiteSuportePrioritario: Boolean
) {
    FREE(
        descricao = "Gratuito",
        limiteEmpregos = 1,
        limiteRegistrosMes = 120,
        permiteSyncNuvem = false,
        permiteRelatoriosAvancados = false,
        permiteInsights = false,
        permiteFotosComprovante = false,
        permiteExportacao = false,
        permiteSuportePrioritario = false
    ),

    PREMIUM(
        descricao = "Premium",
        limiteEmpregos = 5,
        limiteRegistrosMes = null,
        permiteSyncNuvem = true,
        permiteRelatoriosAvancados = true,
        permiteInsights = true,
        permiteFotosComprovante = true,
        permiteExportacao = true,
        permiteSuportePrioritario = true
    ),

    PRO(
        descricao = "Profissional",
        limiteEmpregos = Int.MAX_VALUE,
        limiteRegistrosMes = null,
        permiteSyncNuvem = true,
        permiteRelatoriosAvancados = true,
        permiteInsights = true,
        permiteFotosComprovante = true,
        permiteExportacao = true,
        permiteSuportePrioritario = true
    );

    val isPago: Boolean
        get() = this != FREE
}