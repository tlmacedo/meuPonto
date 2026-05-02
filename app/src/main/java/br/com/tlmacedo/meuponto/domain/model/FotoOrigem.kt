// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/model/FotoOrigem.kt
package br.com.tlmacedo.meuponto.domain.model

/**
 * Representa a origem da imagem capturada para o comprovante de ponto.
 *
 * @property id Identificador numérico para persistência
 * @property descricao Nome amigável da origem
 *
 * @author Thiago
 * @since 12.1.0
 */
enum class FotoOrigem(val id: Int, val descricao: String) {
    /** Sem foto associada */
    NENHUMA(0, "Nenhuma"),

    /** Foto capturada diretamente pela câmera do app */
    CAMERA(1, "Câmera"),

    /** Foto selecionada da galeria do dispositivo */
    GALERIA(2, "Galeria"),

    /** Foto que sofreu edição ou processamento manual */
    EDITADA(3, "Editada");

    companion object {
        /** Converte ID numérico para o enum correspondente */
        fun fromId(id: Int): FotoOrigem = entries.find { it.id == id } ?: NENHUMA
    }
}
