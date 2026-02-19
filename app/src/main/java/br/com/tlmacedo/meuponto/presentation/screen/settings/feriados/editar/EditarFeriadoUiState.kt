// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/settings/feriados/editar/EditarFeriadoUiState.kt
package br.com.tlmacedo.meuponto.presentation.screen.settings.feriados.editar

import br.com.tlmacedo.meuponto.domain.model.Emprego
import br.com.tlmacedo.meuponto.domain.model.feriado.AbrangenciaFeriado
import br.com.tlmacedo.meuponto.domain.model.feriado.RecorrenciaFeriado
import br.com.tlmacedo.meuponto.domain.model.feriado.TipoFeriado
import java.time.LocalDate
import java.time.MonthDay

/**
 * Estado da UI para a tela de edição/criação de feriados.
 *
 * @author Thiago
 * @since 3.4.0
 */
data class EditarFeriadoUiState(
    // === Estado de carregamento ===
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isDeleting: Boolean = false,

    // === Dados do formulário ===
    val feriadoId: Long? = null,
    val nome: String = "",
    val tipo: TipoFeriado = TipoFeriado.NACIONAL,
    val recorrencia: RecorrenciaFeriado = RecorrenciaFeriado.ANUAL,
    val abrangencia: AbrangenciaFeriado = AbrangenciaFeriado.GLOBAL,

    /** Dia e mês para feriados anuais */
    val diaMes: MonthDay? = null,

    /** Data específica para feriados únicos */
    val dataEspecifica: LocalDate? = null,

    /** UF para feriados estaduais */
    val uf: String? = null,

    /** Município para feriados municipais */
    val municipio: String? = null,

    /** Emprego vinculado (quando abrangência é EMPREGO_ESPECIFICO) */
    val empregoSelecionado: Emprego? = null,

    /** Observação adicional */
    val observacao: String = "",

    /** Se o feriado está ativo */
    val ativo: Boolean = true,

    // === Validação ===
    val nomeError: String? = null,
    val dataError: String? = null,
    val ufError: String? = null,
    val municipioError: String? = null,
    val empregoError: String? = null,

    // === Listas auxiliares ===
    val empregosDisponiveis: List<Emprego> = emptyList(),
    val ufList: List<String> = UF_BRASIL,

    // === Diálogos ===
    val showDatePicker: Boolean = false,
    val showDeleteConfirmation: Boolean = false,
    val showDiscardConfirmation: Boolean = false,
    val showEmpregoSelector: Boolean = false,
    val showUfSelector: Boolean = false,

    // === Navegação ===
    val shouldNavigateBack: Boolean = false,

    // === Mensagens ===
    val errorMessage: String? = null,
    val successMessage: String? = null
) {
    /** Indica se estamos editando (true) ou criando (false) */
    val isEditing: Boolean get() = feriadoId != null

    /** Título da tela baseado no modo */
    val screenTitle: String get() = if (isEditing) "Editar Feriado" else "Novo Feriado"

    /** Indica se o formulário foi modificado */
    val hasChanges: Boolean get() = nome.isNotBlank() || diaMes != null || dataEspecifica != null

    /** Indica se deve mostrar seleção de UF */
    val showUfField: Boolean get() = tipo == TipoFeriado.ESTADUAL || tipo == TipoFeriado.MUNICIPAL

    /** Indica se deve mostrar seleção de município */
    val showMunicipioField: Boolean get() = tipo == TipoFeriado.MUNICIPAL

    /** Indica se deve mostrar seleção de emprego */
    val showEmpregoField: Boolean get() = abrangencia == AbrangenciaFeriado.EMPREGO_ESPECIFICO

    /** Texto descritivo do tipo selecionado */
    val tipoDescricao: String get() = when (tipo) {
        TipoFeriado.NACIONAL -> "Feriado válido em todo o país"
        TipoFeriado.ESTADUAL -> "Feriado válido apenas no estado selecionado"
        TipoFeriado.MUNICIPAL -> "Feriado válido apenas no município selecionado"
        TipoFeriado.FACULTATIVO -> "Ponto facultativo, pode não ser folga"
        TipoFeriado.PONTE -> "Dia útil entre feriado e fim de semana"
    }

    /** Valida se o formulário está pronto para salvar */
    val isFormValid: Boolean get() {
        if (nome.isBlank()) return false

        // Validar data
        when (recorrencia) {
            RecorrenciaFeriado.ANUAL -> if (diaMes == null) return false
            RecorrenciaFeriado.UNICO -> if (dataEspecifica == null) return false
        }

        // Validar campos geográficos
        if (tipo == TipoFeriado.ESTADUAL && uf.isNullOrBlank()) return false
        if (tipo == TipoFeriado.MUNICIPAL && (uf.isNullOrBlank() || municipio.isNullOrBlank())) return false

        // Validar emprego
        if (abrangencia == AbrangenciaFeriado.EMPREGO_ESPECIFICO && empregoSelecionado == null) return false

        return true
    }

    companion object {
        /** Lista de UFs do Brasil */
        val UF_BRASIL = listOf(
            "AC", "AL", "AP", "AM", "BA", "CE", "DF", "ES", "GO", "MA",
            "MT", "MS", "MG", "PA", "PB", "PR", "PE", "PI", "RJ", "RN",
            "RS", "RO", "RR", "SC", "SP", "SE", "TO"
        )
    }
}
