// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/ausencias/AusenciasUiState.kt
package br.com.tlmacedo.meuponto.presentation.screen.ausencias

import br.com.tlmacedo.meuponto.domain.model.Emprego
import br.com.tlmacedo.meuponto.domain.model.ausencia.Ausencia
import br.com.tlmacedo.meuponto.domain.model.ausencia.TipoAusencia
import br.com.tlmacedo.meuponto.domain.model.ausencia.TipoFolga
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

/**
 * Ordenação por data.
 */
enum class OrdemData {
    CRESCENTE,    // Próximas primeiro
    DECRESCENTE   // Mais antigas primeiro
}

/**
 * Estado da tela de listagem de ausências.
 *
 * @author Thiago
 * @since 4.0.0
 * @updated 5.6.0 - Filtros múltiplos e lista unificada
 */
data class AusenciasUiState(
    // Dados
    val ausencias: List<Ausencia> = emptyList(),
    val empregoAtivo: Emprego? = null,

    // Filtros
    val filtroTipos: Set<TipoAusencia> = emptySet(),
    val filtroAno: Int? = null,
    val ordemData: OrdemData = OrdemData.CRESCENTE,

    // Estados de UI
    val isLoading: Boolean = false,
    val erro: String? = null,

    // Dialog de exclusão
    val showDeleteDialog: Boolean = false,
    val ausenciaParaExcluir: Ausencia? = null
) {
    companion object {
        private val localeBR = Locale("pt", "BR")
    }

    // ========================================================================
    // PROPRIEDADES CALCULADAS
    // ========================================================================

    val temAusencias: Boolean
        get() = ausencias.isNotEmpty()

    val temEmpregoAtivo: Boolean
        get() = empregoAtivo != null

    val temFiltrosAtivos: Boolean
        get() = filtroTipos.isNotEmpty() || filtroAno != null

    /**
     * Anos disponíveis para filtro (extraídos das ausências).
     */
    val anosDisponiveis: List<Int>
        get() {
            val anos = ausencias.flatMap { ausencia ->
                listOf(ausencia.dataInicio.year, ausencia.dataFim.year)
            }.distinct().sorted()

            // Adiciona ano atual se não existir
            val anoAtual = LocalDate.now().year
            return if (anoAtual in anos) anos else (anos + anoAtual).sorted()
        }

    /**
     * Ausências filtradas e ordenadas.
     */
    val ausenciasFiltradas: List<Ausencia>
        get() {
            var resultado = ausencias

            // Filtro por tipos
            if (filtroTipos.isNotEmpty()) {
                resultado = resultado.filter { it.tipo in filtroTipos }
            }

            // Filtro por ano
            filtroAno?.let { ano ->
                resultado = resultado.filter { ausencia ->
                    ausencia.dataInicio.year == ano || ausencia.dataFim.year == ano
                }
            }

            // Ordenação
            resultado = when (ordemData) {
                OrdemData.CRESCENTE -> resultado.sortedBy { it.dataInicio }
                OrdemData.DECRESCENTE -> resultado.sortedByDescending { it.dataInicio }
            }

            return resultado
        }

    val totalAusenciasFiltradas: Int
        get() = ausenciasFiltradas.size

    val totalDiasAusencia: Int
        get() = ausenciasFiltradas.sumOf { it.quantidadeDias }

    val totalDiasPorTipo: Map<TipoAusencia, Int>
        get() = ausenciasFiltradas
            .groupBy { it.tipo }
            .mapValues { (_, lista) -> lista.sumOf { it.quantidadeDias } }
}

/**
 * Modo de definição do período.
 */
enum class ModoPeriodo {
    DATA_FINAL,
    QUANTIDADE_DIAS
}

/**
 * Estado do formulário de ausência.
 *
 * @updated 6.1.0 - Adicionado TipoFolga
 */
data class AusenciaFormUiState(
    // Identificação
    val id: Long = 0,
    val empregoId: Long = 0,
    val isEdicao: Boolean = false,

    // Tipo
    val tipo: TipoAusencia = TipoAusencia.FERIAS,
    val tipoFolga: TipoFolga = TipoFolga.COMPENSACAO, // Novo campo

    // Período (para tipos que usam período)
    val modoPeriodo: ModoPeriodo = ModoPeriodo.DATA_FINAL,
    val dataInicio: LocalDate = LocalDate.now(),
    val dataFim: LocalDate = LocalDate.now(),
    val quantidadeDias: Int = 1,

    // Horários (para DECLARACAO)
    val horaInicio: LocalTime = LocalTime.of(8, 0),
    val duracaoDeclaracaoHoras: Int = 1,
    val duracaoDeclaracaoMinutos: Int = 0,
    val duracaoAbonoHoras: Int = 1,
    val duracaoAbonoMinutos: Int = 0,

    // Textos
    val descricao: String = "",
    val observacao: String = "",
    val periodoAquisitivo: String = "", // Para FERIAS

    // Anexo de imagem
    val imagemUri: String? = null,
    val imagemNome: String? = null,

    // Estados de UI
    val isLoading: Boolean = false,
    val isSalvando: Boolean = false,
    val erro: String? = null,

    // Dialogs e Bottom Sheets
    val showTipoSelector: Boolean = false,
    val showDatePickerInicio: Boolean = false,
    val showDatePickerFim: Boolean = false,
    val showTimePickerInicio: Boolean = false,
    val showDuracaoDeclaracaoPicker: Boolean = false,
    val showDuracaoAbonoPicker: Boolean = false,
    val showImagePicker: Boolean = false
) {
    companion object {
        private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

        /**
         * Cria um UiState a partir de uma Ausencia existente (modo edição).
         */
        fun fromAusencia(ausencia: Ausencia): AusenciaFormUiState {
            val totalDias = ChronoUnit.DAYS.between(ausencia.dataInicio, ausencia.dataFim).toInt() + 1

            return AusenciaFormUiState(
                id = ausencia.id,
                empregoId = ausencia.empregoId,
                isEdicao = true,
                tipo = ausencia.tipo,
                tipoFolga = ausencia.tipoFolga ?: TipoFolga.COMPENSACAO,
                modoPeriodo = ModoPeriodo.DATA_FINAL,
                dataInicio = ausencia.dataInicio,
                dataFim = ausencia.dataFim,
                quantidadeDias = totalDias,
                horaInicio = ausencia.horaInicio ?: LocalTime.of(8, 0),
                duracaoDeclaracaoHoras = (ausencia.duracaoDeclaracaoMinutos ?: 60) / 60,
                duracaoDeclaracaoMinutos = (ausencia.duracaoDeclaracaoMinutos ?: 60) % 60,
                duracaoAbonoHoras = (ausencia.duracaoAbonoMinutos ?: 60) / 60,
                duracaoAbonoMinutos = (ausencia.duracaoAbonoMinutos ?: 60) % 60,
                descricao = ausencia.descricao ?: "",
                observacao = ausencia.observacao ?: "",
                periodoAquisitivo = ausencia.periodoAquisitivo ?: "",
                imagemUri = ausencia.imagemUri
            )
        }
    }

    // ========================================================================
    // PROPRIEDADES CALCULADAS
    // ========================================================================

    val tituloTela: String
        get() = if (isEdicao) "Editar ${tipo.descricao}" else "Nova ${tipo.descricao}"

    val textoBotaoSalvar: String
        get() = if (isEdicao) "Atualizar" else "Salvar"

    val totalDias: Int
        get() = when (modoPeriodo) {
            ModoPeriodo.DATA_FINAL -> ChronoUnit.DAYS.between(dataInicio, dataFim).toInt() + 1
            ModoPeriodo.QUANTIDADE_DIAS -> quantidadeDias
        }

    val dataFimCalculada: LocalDate
        get() = when (modoPeriodo) {
            ModoPeriodo.DATA_FINAL -> dataFim
            ModoPeriodo.QUANTIDADE_DIAS -> dataInicio.plusDays((quantidadeDias - 1).toLong())
        }

    val duracaoDeclaracaoTotalMinutos: Int
        get() = duracaoDeclaracaoHoras * 60 + duracaoDeclaracaoMinutos

    val duracaoAbonoTotalMinutos: Int
        get() = duracaoAbonoHoras * 60 + duracaoAbonoMinutos

    val horaFimDeclaracao: LocalTime
        get() = horaInicio.plusMinutes(duracaoDeclaracaoTotalMinutos.toLong())

    /** Indica se deve mostrar o seletor de tipo de folga */
    val mostrarTipoFolga: Boolean
        get() = tipo == TipoAusencia.FOLGA

    // ========================================================================
    // FORMATAÇÕES
    // ========================================================================

    val dataInicioFormatada: String
        get() = dataInicio.format(dateFormatter)

    val dataFimFormatada: String
        get() = dataFimCalculada.format(dateFormatter)

    val horaInicioFormatada: String
        get() = horaInicio.format(timeFormatter)

    val horaFimFormatada: String
        get() = horaFimDeclaracao.format(timeFormatter)

    val duracaoDeclaracaoFormatada: String
        get() = formatarDuracao(duracaoDeclaracaoHoras, duracaoDeclaracaoMinutos)

    val duracaoAbonoFormatada: String
        get() = formatarDuracao(duracaoAbonoHoras, duracaoAbonoMinutos)

    private fun formatarDuracao(horas: Int, minutos: Int): String {
        return when {
            horas > 0 && minutos > 0 -> "${horas}h${minutos}min"
            horas > 0 -> "${horas}h"
            else -> "${minutos}min"
        }
    }

    // ========================================================================
    // VALIDAÇÕES
    // ========================================================================

    val isFormValido: Boolean
        get() = when (tipo) {
            TipoAusencia.FERIAS -> empregoId > 0 && periodoAquisitivo.isNotBlank()
            TipoAusencia.ATESTADO -> empregoId > 0 && observacao.isNotBlank()
            TipoAusencia.DECLARACAO -> {
                empregoId > 0 &&
                        observacao.isNotBlank() &&
                        duracaoDeclaracaoTotalMinutos > 0 &&
                        duracaoAbonoTotalMinutos > 0 &&
                        duracaoAbonoTotalMinutos <= duracaoDeclaracaoTotalMinutos
            }
            TipoAusencia.FALTA_JUSTIFICADA -> empregoId > 0 && observacao.isNotBlank()
            TipoAusencia.FOLGA -> empregoId > 0
            TipoAusencia.FALTA_INJUSTIFICADA -> empregoId > 0
        }

    val mensagemValidacao: String?
        get() = when {
            empregoId <= 0 -> "Nenhum emprego ativo encontrado"
            tipo == TipoAusencia.FERIAS && periodoAquisitivo.isBlank() ->
                "Informe o período aquisitivo das férias"
            tipo == TipoAusencia.ATESTADO && observacao.isBlank() ->
                "Informe o motivo do atestado"
            tipo == TipoAusencia.DECLARACAO && observacao.isBlank() ->
                "Informe o motivo da declaração"
            tipo == TipoAusencia.DECLARACAO && duracaoDeclaracaoTotalMinutos <= 0 ->
                "Informe a duração da declaração"
            tipo == TipoAusencia.DECLARACAO && duracaoAbonoTotalMinutos <= 0 ->
                "Informe o tempo de abono"
            tipo == TipoAusencia.DECLARACAO && duracaoAbonoTotalMinutos > duracaoDeclaracaoTotalMinutos ->
                "O tempo de abono não pode ser maior que a duração da declaração"
            tipo == TipoAusencia.FALTA_JUSTIFICADA && observacao.isBlank() ->
                "Informe o motivo da falta justificada"
            else -> null
        }

    // ========================================================================
    // LABELS DINÂMICOS
    // ========================================================================

    val labelObservacao: String
        get() = tipo.labelObservacao

    val placeholderObservacao: String
        get() = tipo.placeholderObservacao

    val permiteAnexo: Boolean
        get() = tipo.permiteAnexo

    val usaPeriodo: Boolean
        get() = tipo.usaPeriodo

    val usaIntervaloHoras: Boolean
        get() = tipo.usaIntervaloHoras

    // ========================================================================
    // CONVERSÃO
    // ========================================================================

    fun toAusencia(): Ausencia {
        val dataFimFinal = dataFimCalculada

        return Ausencia(
            id = id,
            empregoId = empregoId,
            tipo = tipo,
            tipoFolga = if (tipo == TipoAusencia.FOLGA) tipoFolga else null,
            dataInicio = dataInicio,
            dataFim = if (tipo.usaPeriodo) dataFimFinal else dataInicio,
            descricao = descricao.ifBlank { tipo.descricao },
            observacao = observacao.ifBlank { null },
            horaInicio = if (tipo == TipoAusencia.DECLARACAO) horaInicio else null,
            duracaoDeclaracaoMinutos = if (tipo == TipoAusencia.DECLARACAO) duracaoDeclaracaoTotalMinutos else null,
            duracaoAbonoMinutos = if (tipo == TipoAusencia.DECLARACAO) duracaoAbonoTotalMinutos else null,
            periodoAquisitivo = if (tipo == TipoAusencia.FERIAS) periodoAquisitivo.ifBlank { null } else null,
            imagemUri = imagemUri
        )
    }
}
