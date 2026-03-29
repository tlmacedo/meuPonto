package br.com.tlmacedo.meuponto.presentation.screen.settings.feriados.editar

import br.com.tlmacedo.meuponto.domain.model.Emprego
import br.com.tlmacedo.meuponto.domain.model.feriado.AbrangenciaFeriado
import br.com.tlmacedo.meuponto.domain.model.feriado.RecorrenciaFeriado
import br.com.tlmacedo.meuponto.domain.model.feriado.TipoFeriado
import java.time.LocalDate
import java.time.MonthDay

data class EditarFeriadoUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isDeleting: Boolean = false,
    val feriadoId: Long? = null,
    val nome: String = "",
    val tipo: TipoFeriado = TipoFeriado.NACIONAL,
    val recorrencia: RecorrenciaFeriado = RecorrenciaFeriado.ANUAL,
    val abrangencia: AbrangenciaFeriado = AbrangenciaFeriado.GLOBAL,
    val diaMes: MonthDay? = null,
    val dataEspecifica: LocalDate? = null,
    val uf: String? = null,
    val municipio: String? = null,
    val empregoSelecionado: Emprego? = null,
    val observacao: String = "",
    val ativo: Boolean = true,
    val showDatePicker: Boolean = false,
    val showEmpregoSelector: Boolean = false,
    val showUfSelector: Boolean = false,
    val showDeleteConfirmation: Boolean = false,
    val showDiscardConfirmation: Boolean = false,
    val nomeError: String? = null,
    val dataError: String? = null,
    val ufError: String? = null,
    val municipioError: String? = null,
    val empregoError: String? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val shouldNavigateBack: Boolean = false,
    val empregosDisponiveis: List<Emprego> = emptyList(),
    val hasChanges: Boolean = false
) {
    val isEditing: Boolean get() = feriadoId != null

    val screenTitle: String get() = if (isEditing) "Editar Feriado" else "Novo Feriado"

    val tipoDescricao: String get() = when (tipo.name) {
        "NACIONAL" -> "Válido em todo o território nacional"
        "ESTADUAL" -> "Válido apenas no estado selecionado"
        "MUNICIPAL" -> "Válido apenas no município selecionado"
        "PONTO_FACULTATIVO" -> "Ponto facultativo (opcional)"
        else -> "Feriado"
    }

    val showUfField: Boolean get() = tipo.name == "ESTADUAL" || tipo.name == "MUNICIPAL"

    val showMunicipioField: Boolean get() = tipo.name == "MUNICIPAL"

    val showEmpregoField: Boolean get() = abrangencia.name == "EMPREGO_ESPECIFICO"

    val isFormValid: Boolean get() {
        if (nome.isBlank()) return false
        if (recorrencia.name == "ANUAL" && diaMes == null) return false
        if (recorrencia.name == "UNICO" && dataEspecifica == null) return false
        if ((tipo.name == "ESTADUAL" || tipo.name == "MUNICIPAL") && uf.isNullOrBlank()) return false
        if (tipo.name == "MUNICIPAL" && municipio.isNullOrBlank()) return false
        if (abrangencia.name == "EMPREGO_ESPECIFICO" && empregoSelecionado == null) return false
        return true
    }

    val ufList: List<String> = listOf(
        "AC", "AL", "AP", "AM", "BA", "CE", "DF", "ES", "GO", "MA", "MT", "MS", "MG",
        "PA", "PB", "PR", "PE", "PI", "RJ", "RN", "RS", "RO", "RR", "SC", "SP", "SE", "TO"
    )
}
