package br.com.tlmacedo.meuponto.domain.model.confiabilidade

data class ConfiabilidadeRegistro(
    val pontuacao: Int,
    val fatores: List<FatorConfiabilidade>
) {
    init {
        require(pontuacao in 0..100) {
            "A pontuação de confiabilidade deve estar entre 0 e 100."
        }
    }

    val nivel: NivelConfiabilidade
        get() = when {
            pontuacao >= 85 -> NivelConfiabilidade.ALTA
            pontuacao >= 60 -> NivelConfiabilidade.MEDIA
            else -> NivelConfiabilidade.BAIXA
        }

    val descricao: String
        get() = "${nivel.descricao} ($pontuacao/100)"
}