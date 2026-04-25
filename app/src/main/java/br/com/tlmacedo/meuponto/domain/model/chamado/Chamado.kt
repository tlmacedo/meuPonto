// path: app/src/main/java/br/com/tlmacedo/meuponto/domain/model/chamado/Chamado.kt
package br.com.tlmacedo.meuponto.domain.model.chamado

import java.time.LocalDateTime

data class Chamado(
    val id: Long = 0,
    val identificador: String,        // ex: "MP-2026-00042"
    val titulo: String,
    val descricao: String,
    val categoria: CategoriaChamado,
    val prioridade: PrioridadeChamado,
    val status: StatusChamado,
    val empregoId: Long?,
    val usuarioEmail: String,
    val usuarioNome: String,
    val resposta: String?,
    val anexos: ArrayList<String>? = null,
    val criadoEm: LocalDateTime,
    val atualizadoEm: LocalDateTime,
    val resolvidoEm: LocalDateTime? = null,
    val avaliacaoNota: AvaliacaoChamado? = null,
    val avaliacaoComentario: String? = null,
    val avaliadoEm: LocalDateTime? = null
)

data class AvaliacaoChamado(
    val nota: Int,                  // 1 a 5
    val comentario: String? = null,
    val avaliadoEm: LocalDateTime
)

enum class CategoriaChamado(val label: String) {
    BUG("Erro / Bug"),
    SUGESTAO("Sugestão"),
    DUVIDA("Dúvida"),
    DESEMPENHO("Desempenho"),
    OUTRO("Outro")
}

enum class PrioridadeChamado(val label: String) {
    BAIXA("Baixa"),
    MEDIA("Média"),
    ALTA("Alta"),
    CRITICA("Crítica")
}

enum class StatusChamado(val label: String) {
    ABERTO("Aberto"),
    EM_ANALISE("Em análise"),
    EM_PROGRESSO("Em progresso"),
    AGUARDANDO_USUARIO("Aguardando usuário"),
    RESOLVIDO("Resolvido"),
    FECHADO("Fechado"),
    CANCELADO("Cancelado")
}
