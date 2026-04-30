package br.com.tlmacedo.meuponto.domain.model.confiabilidade

enum class FatorConfiabilidade(
    val peso: Int,
    val descricao: String
) {
    TEM_FOTO(20, "Possui foto do comprovante"),
    TEM_LOCALIZACAO(20, "Possui localização"),
    TEM_NSR(20, "Possui NSR"),
    NAO_EDITADO(20, "Não foi editado manualmente"),
    HORARIO_COERENTE(20, "Horário coerente com a jornada")
}