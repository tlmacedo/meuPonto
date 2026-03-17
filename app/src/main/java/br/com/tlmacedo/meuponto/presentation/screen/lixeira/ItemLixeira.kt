// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/lixeira/ItemLixeira.kt
package br.com.tlmacedo.meuponto.presentation.screen.lixeira

import br.com.tlmacedo.meuponto.domain.model.Ponto
import java.time.format.DateTimeFormatter

/**
 * Tipos de itens que podem estar na lixeira.
 *
 * @author Thiago
 * @since 11.0.0
 */
enum class TipoItemLixeira {
    PONTO,
    EMPREGO,
    FERIADO,
    AUSENCIA,
    VERSAO_JORNADA
}

/**
 * Representa um item genérico na lixeira.
 *
 * @author Thiago
 * @since 11.0.0
 */
data class ItemLixeira(
    val id: Long,
    val tipo: TipoItemLixeira,
    val titulo: String,
    val subtitulo: String,
    val diasRestantes: Int,
    val deletedAt: Long?,
    val dados: Any? = null
) {
    val expirandoEmBreve: Boolean get() = diasRestantes <= 7

    companion object {
        private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

        /**
         * Cria um ItemLixeira a partir de um PontoLixeiraItem.
         */
        fun fromPontoLixeiraItem(pontoItem: PontoLixeiraItem): ItemLixeira {
            return ItemLixeira(
                id = pontoItem.id,
                tipo = TipoItemLixeira.PONTO,
                titulo = pontoItem.ponto.data.format(dateFormatter),
                subtitulo = "${pontoItem.nomeEmprego} • ${pontoItem.ponto.hora.format(timeFormatter)}",
                diasRestantes = pontoItem.diasRestantes,
                deletedAt = pontoItem.ponto.deletedAt,
                dados = pontoItem.ponto
            )
        }

        /**
         * Cria um ItemLixeira a partir de um Ponto.
         */
        fun fromPonto(ponto: Ponto, nomeEmprego: String, diasRestantes: Int): ItemLixeira {
            return ItemLixeira(
                id = ponto.id,
                tipo = TipoItemLixeira.PONTO,
                titulo = ponto.data.format(dateFormatter),
                subtitulo = "$nomeEmprego • ${ponto.hora.format(timeFormatter)}",
                diasRestantes = diasRestantes,
                deletedAt = ponto.deletedAt,
                dados = ponto
            )
        }
    }
}
