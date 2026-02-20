// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/ausencias/AusenciasUiState.kt
package br.com.tlmacedo.meuponto.presentation.screen.ausencias

import br.com.tlmacedo.meuponto.domain.model.Emprego
import br.com.tlmacedo.meuponto.domain.model.ausencia.Ausencia
import br.com.tlmacedo.meuponto.domain.model.ausencia.TipoAusencia
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Estado da tela de listagem de ausências.
 *
 * @author Thiago
 * @since 4.0.0
 */
data class AusenciasUiState(
    val ausencias: List<Ausencia> = emptyList(),
    val empregoAtivo: Emprego? = null,
    val mesSelecionado: YearMonth = YearMonth.now(),
    val filtroTipo: TipoAusencia? = null,
    val isLoading: Boolean = false,
    val erro: String? = null,
    // Dialog de exclusão
    val showDeleteDialog: Boolean = false,
    val ausenciaParaExcluir: Ausencia? = null
) {
    companion object {
        private val localeBR = Locale("pt", "BR")
        private val formatterMesAno = DateTimeFormatter.ofPattern("MMMM 'de' yyyy", localeBR)
    }

    val temAusencias: Boolean
        get() = ausencias.isNotEmpty()

    val temEmpregoAtivo: Boolean
        get() = empregoAtivo != null

    val mesFormatado: String
        get() = mesSelecionado.format(formatterMesAno).replaceFirstChar { it.uppercase() }

    val ausenciasFiltradas: List<Ausencia>
        get() = if (filtroTipo != null) {
            ausencias.filter { it.tipo == filtroTipo }
        } else {
            ausencias
        }

    val ausenciasAgrupadas: Map<TipoAusencia, List<Ausencia>>
        get() = ausencias.groupBy { it.tipo }

    val totalDiasAusencia: Int
        get() = ausencias.sumOf { it.quantidadeDias }

    val totalDiasPorTipo: Map<TipoAusencia, Int>
        get() = ausencias.groupBy { it.tipo }
            .mapValues { (_, lista) -> lista.sumOf { it.quantidadeDias } }

    val podeNavegaMesAnterior: Boolean
        get() = mesSelecionado.isAfter(YearMonth.now().minusYears(2))

    val podeNavegarMesProximo: Boolean
        get() = mesSelecionado.isBefore(YearMonth.now().plusYears(1))
}

/**
 * Estado do formulário de ausência.
 */
data class AusenciaFormUiState(
    val id: Long = 0,
    val empregoId: Long = 0,
    val tipo: TipoAusencia = TipoAusencia.FERIAS,
    val dataInicio: LocalDate = LocalDate.now(),
    val dataFim: LocalDate = LocalDate.now(),
    val descricao: String = "",
    val observacao: String = "",
    val isEdicao: Boolean = false,
    val isLoading: Boolean = false,
    val isSalvando: Boolean = false,
    val erro: String? = null,
    // Dialogs
    val showDatePickerInicio: Boolean = false,
    val showDatePickerFim: Boolean = false,
    val showTipoSelector: Boolean = false
) {
    companion object {
        private val formatterData = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    }

    val isFormValido: Boolean
        get() = empregoId > 0 && dataFim >= dataInicio

    val dataInicioFormatada: String
        get() = dataInicio.format(formatterData)

    val dataFimFormatada: String
        get() = dataFim.format(formatterData)

    val totalDias: Int
        get() = (dataFim.toEpochDay() - dataInicio.toEpochDay() + 1).toInt()

    val tituloTela: String
        get() = if (isEdicao) "Editar ${tipo.descricao}" else "Nova ${tipo.descricao}"

    val textoBotaoSalvar: String
        get() = if (isEdicao) "Atualizar" else "Salvar"

    fun toAusencia(): Ausencia {
        return Ausencia(
            id = id,
            empregoId = empregoId,
            tipo = tipo,
            dataInicio = dataInicio,
            dataFim = dataFim,
            descricao = descricao.ifBlank { null },
            observacao = observacao.ifBlank { null }
        )
    }
}
