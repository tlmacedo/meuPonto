package br.com.tlmacedo.meuponto.domain.model.assinatura

enum class RecursoPremium(
    val titulo: String,
    val descricao: String
) {
    BACKUP_NUVEM(
        titulo = "Backup em nuvem",
        descricao = "Sincronização segura dos dados entre dispositivos."
    ),

    RELATORIO_PDF(
        titulo = "Relatório em PDF",
        descricao = "Geração de extratos profissionais para conferência."
    ),

    EXPORTACAO_CSV_EXCEL(
        titulo = "Exportação CSV/Excel",
        descricao = "Exportação dos registros para análise externa."
    ),

    FOTO_COMPROVANTE(
        titulo = "Foto do comprovante",
        descricao = "Anexo de foto com metadados para auditoria."
    ),

    INSIGHTS_INTELIGENTES(
        titulo = "Insights inteligentes",
        descricao = "Análises automáticas sobre atrasos, saldo e padrões."
    ),

    MULTIPLOS_EMPREGOS(
        titulo = "Múltiplos empregos",
        descricao = "Controle separado para mais de um vínculo."
    ),

    SUPORTE_PRIORITARIO(
        titulo = "Suporte prioritário",
        descricao = "Atendimento diferenciado para usuários pagantes."
    )
}