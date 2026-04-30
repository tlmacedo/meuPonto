package br.com.tlmacedo.meuponto.domain.model.confiabilidade

enum class NivelConfiabilidade(
    val descricao: String
) {
    ALTA("Alta confiabilidade"),
    MEDIA("Confiabilidade média"),
    BAIXA("Baixa confiabilidade")
}