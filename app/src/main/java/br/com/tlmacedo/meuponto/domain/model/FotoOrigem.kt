package br.com.tlmacedo.meuponto.domain.model

/**
 * Origem da foto do comprovante de ponto.
 *
 * @author Thiago
 * @since 12.1.0
 */
enum class FotoOrigem(val id: Int, val descricao: String) {
    NENHUMA(0, "Nenhuma"),
    CAMERA(1, "Câmera"),
    GALERIA(2, "Galeria"),
    EDITADA(3, "Editada");

    companion object {
        fun fromId(id: Int): FotoOrigem = entries.find { it.id == id } ?: NENHUMA
    }
}
