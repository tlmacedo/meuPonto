// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/home/HomeUiState.kt
package br.com.tlmacedo.meuponto.presentation.screen.home

import android.net.Uri
import br.com.tlmacedo.meuponto.domain.model.BancoHoras
import br.com.tlmacedo.meuponto.domain.model.CicloBancoHoras
import br.com.tlmacedo.meuponto.domain.model.ConfiguracaoEmprego
import br.com.tlmacedo.meuponto.domain.model.Emprego
import br.com.tlmacedo.meuponto.domain.model.FechamentoPeriodo
import br.com.tlmacedo.meuponto.domain.model.FotoOrigem
import br.com.tlmacedo.meuponto.domain.model.IntervaloPonto
import br.com.tlmacedo.meuponto.domain.model.Ponto
import br.com.tlmacedo.meuponto.domain.model.ResumoDia
import br.com.tlmacedo.meuponto.domain.model.TipoDiaEspecial
import br.com.tlmacedo.meuponto.domain.model.TipoNsr
import br.com.tlmacedo.meuponto.domain.model.TipoPonto
import br.com.tlmacedo.meuponto.domain.model.VersaoJornada
import br.com.tlmacedo.meuponto.domain.model.ausencia.Ausencia
import br.com.tlmacedo.meuponto.domain.model.feriado.Feriado
import br.com.tlmacedo.meuponto.domain.usecase.ponto.ProximoPonto
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Estados possíveis do ciclo de banco de horas.
 *
 * @author Thiago
 * @since 6.2.0
 */
sealed class EstadoCiclo {
    /** Banco de horas não habilitado ou sem configuração */
    data object Nenhum : EstadoCiclo()

    /** Ciclo em andamento normal */
    data class EmAndamento(
        val ciclo: CicloBancoHoras,
        val diasRestantes: Int
    ) : EstadoCiclo()

    /** Ciclo próximo do fim (alerta) */
    data class ProximoDoFim(
        val ciclo: CicloBancoHoras,
        val diasRestantes: Int
    ) : EstadoCiclo() {
        val mensagem: String
            get() = when (diasRestantes) {
                0 -> "Ciclo encerra hoje"
                1 -> "Ciclo encerra amanhã"
                else -> "Ciclo encerra em $diasRestantes dias"
            }
    }

    /** Ciclo pendente de fechamento */
    data class Pendente(
        val ciclo: CicloBancoHoras,
        val diasAposVencimento: Int
    ) : EstadoCiclo() {
        val mensagem: String
            get() = when (diasAposVencimento) {
                1 -> "Ciclo encerrou ontem"
                else -> "Ciclo encerrou há $diasAposVencimento dias"
            }
    }
}

/**
 * Estado do modal de edição de ponto.
 *
 * @author Thiago
 * @since 7.2.0
 */
data class EdicaoModalState(
    val ponto: Ponto,
    val indicePonto: Int = 0,
    val isSaving: Boolean = false,
    val fotoUri: Uri? = null,
    val fotoOrigem: FotoOrigem = FotoOrigem.NENHUMA,
    val fotoRemovida: Boolean = false
) {
    /** Determina o tipo do ponto baseado no índice (ímpar = entrada, par = saída) */
    val tipoPonto: TipoPonto
        get() = TipoPonto.getTipoPorIndice(indicePonto)

    val tipoDescricao: String
        get() = tipoPonto.descricao
}

/**
 * Estado do modal de exclusão de ponto.
 *
 * @author Thiago
 * @since 7.2.0
 */
data class ExclusaoModalState(
    val ponto: Ponto,
    val indicePonto: Int = 0,
    val isDeleting: Boolean = false
) {
    val tipoPonto: TipoPonto
        get() = TipoPonto.getTipoPorIndice(indicePonto)

    val tipoDescricao: String
        get() = tipoPonto.descricao
}

/**
 * Estado do modal de visualização de localização.
 *
 * @author Thiago
 * @since 7.2.0
 */
data class LocalizacaoModalState(
    val ponto: Ponto,
    val indicePonto: Int = 0
) {
    val tipoPonto: TipoPonto
        get() = TipoPonto.getTipoPorIndice(indicePonto)

    val tipoDescricao: String
        get() = tipoPonto.descricao
}

/**
 * Estado do modal de visualização de foto.
 *
 * @author Thiago
 * @since 7.2.0
 */
data class FotoModalState(
    val ponto: Ponto,
    val indicePonto: Int = 0,
    val fotoPath: String? = null
) {
    val tipoPonto: TipoPonto
        get() = TipoPonto.getTipoPorIndice(indicePonto)

    val tipoDescricao: String
        get() = tipoPonto.descricao
}

/**
 * Estado do modal de registro de ponto (Unificado).
 *
 * @author Thiago
 * @since 12.0.0
 */
data class RegistrarPontoModalState(
    val dataHora: LocalDateTime,
    val nsr: String = "",
    val observacao: String = "",
    val fotoUri: Uri? = null,
    val fotoOrigem: FotoOrigem = FotoOrigem.NENHUMA,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val endereco: String? = null,
    val isSaving: Boolean = false,
    val isCapturingLocation: Boolean = false,
    val isProcessingOcr: Boolean = false,
    val showTimePicker: Boolean = false,
    val erroLocalizacao: String? = null
) {
    val horaFormatada: String
        get() = dataHora.format(DateTimeFormatter.ofPattern("HH:mm"))

    val dataFormatada: String
        get() = dataHora.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
}

/**
 * Estado da tela Home.
 *
 * @author Thiago
 * @since 2.0.0
 * @updated 4.0.0 - Adicionado suporte a ausências (férias, folga, falta, atestado)
 * @updated 6.0.0 - Adicionado campo motivoExclusao para auditoria obrigatória
 * @updated 6.2.0 - Adicionado suporte a ciclos de banco de horas
 * @updated 6.4.0 - Adicionado fechamentoCicloAnterior para exibir marco de início de ciclo
 * @updated 7.2.0 - Substituída edição inline por modais
 */
data class HomeUiState(
    val dataSelecionada: LocalDate = LocalDate.now(),
    val horaAtual: LocalTime = LocalTime.now(),
    val pontosHoje: List<Ponto> = emptyList(),
    val resumoDia: ResumoDia = ResumoDia(data = LocalDate.now()),
    val bancoHoras: BancoHoras = BancoHoras(),
    val proximoTipo: ProximoPonto = ProximoPonto(isEntrada = true, descricao = "Entrada", indice = 0),
    val empregoAtivo: Emprego? = null,
    val empregosDisponiveis: List<Emprego> = emptyList(),
    val versaoJornadaAtual: VersaoJornada? = null,
    val configuracaoEmprego: ConfiguracaoEmprego? = null,
    // Feriados
    val feriadosDoDia: List<Feriado> = emptyList(),
    // Ausências
    val ausenciaDoDia: Ausencia? = null,
    // Loading e dialogs
    val isLoading: Boolean = false,
    val isLoadingEmpregos: Boolean = false,
    val showTimePickerDialog: Boolean = false,
    val showEmpregoSelector: Boolean = false,
    val showEmpregoMenu: Boolean = false,
    val showDatePicker: Boolean = false,
    val cameraUri: Uri? = null,
    val showFotoSourceDialog: Boolean = false,
    val erro: String? = null,
    // Ciclo de banco de horas
    val estadoCiclo: EstadoCiclo = EstadoCiclo.Nenhum,
    val showFechamentoCicloDialog: Boolean = false,
    // Fechamento de ciclo anterior (para exibir marco de início de novo ciclo)
    val fechamentoCicloAnterior: FechamentoPeriodo? = null,
    // ════════════════════════════════════════════════════════════════════
    // MODAIS DE PONTO (Nova implementação 7.2.0)
    // ════════════════════════════════════════════════════════════════════
    val edicaoModal: EdicaoModalState? = null,
    val exclusaoModal: ExclusaoModalState? = null,
    val localizacaoModal: LocalizacaoModalState? = null,
    val fotoModal: FotoModalState? = null,
    // NOVO MODAL DE REGISTRO
    val registrarPontoModal: RegistrarPontoModalState? = null
) {
    companion object {
        private val localeBR = Locale("pt", "BR")
        internal val formatterDiaSemana = DateTimeFormatter.ofPattern("EEEE", localeBR)
        internal val formatterDiaSemanaAbrev = DateTimeFormatter.ofPattern("EEE", localeBR)
        internal val formatterDataCompleta = DateTimeFormatter.ofPattern("EEEE, dd 'de' MMMM", localeBR)
        internal val formatterDataCurta = DateTimeFormatter.ofPattern("dd/MM/yyyy", localeBR)
    }

    val isHoje: Boolean
        get() = dataSelecionada == LocalDate.now()

    val isOntem: Boolean
        get() = dataSelecionada == LocalDate.now().minusDays(1)

    val isAmanha: Boolean
        get() = dataSelecionada == LocalDate.now().plusDays(1)

    val isFuturo: Boolean
        get() = dataSelecionada.isAfter(LocalDate.now())

    val isPassado: Boolean
        get() = dataSelecionada.isBefore(LocalDate.now())

    // ========================================================================
    // NSR
    // ========================================================================

    val nsrHabilitado: Boolean
        get() = configuracaoEmprego?.habilitarNsr == true

    val tipoNsr: TipoNsr
        get() = configuracaoEmprego?.tipoNsr ?: TipoNsr.NUMERICO

    // ========================================================================
    // FOTO DE COMPROVANTE
    // ========================================================================

    val fotoHabilitada: Boolean
        get() = configuracaoEmprego?.fotoHabilitada == true

    val fotoObrigatoria: Boolean
        get() = configuracaoEmprego?.fotoObrigatoria == true

    // ========================================================================
    // LOCALIZAÇÃO
    // ========================================================================

    val localizacaoHabilitada: Boolean
        get() = configuracaoEmprego?.habilitarLocalizacao == true

    // ========================================================================
    // FERIADOS
    // ========================================================================

    val isFeriado: Boolean
        get() = feriadosDoDia.isNotEmpty()

    val feriadoPrincipal: Feriado?
        get() = feriadosDoDia.firstOrNull()

    val temMultiplosFeriados: Boolean
        get() = feriadosDoDia.size > 1

    // ========================================================================
    // AUSÊNCIAS
    // ========================================================================

    val temAusencia: Boolean
        get() = ausenciaDoDia != null

    val isDescanso: Boolean
        get() = resumoDia.tipoDiaEspecial == TipoDiaEspecial.DESCANSO
    val isFerias: Boolean
        get() = resumoDia.tipoDiaEspecial == TipoDiaEspecial.FERIAS

    val isFolga: Boolean
        get() = resumoDia.tipoDiaEspecial == TipoDiaEspecial.FOLGA

    val isFalta: Boolean
        get() = resumoDia.tipoDiaEspecial == TipoDiaEspecial.FALTA_INJUSTIFICADA

    val isAtestado: Boolean
        get() = resumoDia.tipoDiaEspecial == TipoDiaEspecial.ATESTADO

    val isLicenca: Boolean
        get() = resumoDia.tipoDiaEspecial == TipoDiaEspecial.FALTA_JUSTIFICADA

    val isDiaEspecial: Boolean
        get() = resumoDia.tipoDiaEspecial != TipoDiaEspecial.NORMAL

    val descricaoAusencia: String?
        get() = ausenciaDoDia?.descricao ?: ausenciaDoDia?.tipoDescricao

    val emojiDiaEspecial: String
        get() = resumoDia.tipoDiaEspecial.emoji

    // ========================================================================
    // CICLO DE BANCO DE HORAS - PROPRIEDADES COMPUTADAS
    // ========================================================================

    val temCicloPendente: Boolean
        get() = estadoCiclo is EstadoCiclo.Pendente

    val cicloProximoDoFim: Boolean
        get() = estadoCiclo is EstadoCiclo.ProximoDoFim

    val deveExibirBannerCiclo: Boolean
        get() = temCicloPendente || cicloProximoDoFim

    val mensagemBannerCiclo: String?
        get() = when (val estado = estadoCiclo) {
            is EstadoCiclo.Pendente -> estado.mensagem
            is EstadoCiclo.ProximoDoFim -> estado.mensagem
            else -> null
        }

    val saldoCicloFormatado: String?
        get() = when (val estado = estadoCiclo) {
            is EstadoCiclo.Pendente -> estado.ciclo.saldoAtualFormatado
            is EstadoCiclo.ProximoDoFim -> estado.ciclo.saldoAtualFormatado
            is EstadoCiclo.EmAndamento -> estado.ciclo.saldoAtualFormatado
            else -> null
        }

    // ========================================================================
    // FECHAMENTO DE CICLO ANTERIOR (MARCO DE INÍCIO DE NOVO CICLO)
    // ========================================================================

    val isInicioDeCiclo: Boolean
        get() = fechamentoCicloAnterior?.let { fechamento ->
            dataSelecionada == fechamento.dataFimPeriodo.plusDays(1)
        } ?: false

    val deveExibirBannerFechamentoCiclo: Boolean
        get() = isInicioDeCiclo && fechamentoCicloAnterior != null

    // ========================================================================
    // MODAIS - PROPRIEDADES COMPUTADAS
    // ========================================================================

    /** Verifica se há algum modal aberto */
    val temModalAberto: Boolean
        get() = edicaoModal != null || exclusaoModal != null ||
                localizacaoModal != null || fotoModal != null ||
                registrarPontoModal != null

    /** Obtém o índice de um ponto na lista ordenada */
    fun getIndicePonto(pontoId: Long): Int {
        val pontosOrdenados = pontosHoje.sortedBy { it.dataHora }
        return pontosOrdenados.indexOfFirst { it.id == pontoId }
    }

    /** Obtém o tipo do ponto (entrada/saída) baseado no índice */
    fun getTipoPonto(pontoId: Long): TipoPonto {
        val indice = getIndicePonto(pontoId)
        return if (indice >= 0) TipoPonto.getTipoPorIndice(indice) else TipoPonto.ENTRADA
    }

    /** Obtém a descrição do tipo do ponto */
    fun getTipoPontoDescricao(pontoId: Long): String {
        return getTipoPonto(pontoId).descricao
    }

    // ========================================================================
    // FORMATAÇÃO DE DATA
    // ========================================================================

    val dataFormatada: String
        get() {
            val diaSemana = dataSelecionada.format(formatterDiaSemana)
                .replaceFirstChar { it.uppercase() }

            return when {
                isHoje -> "$diaSemana, Hoje"
                isOntem -> "$diaSemana, Ontem"
                isAmanha -> "$diaSemana, Amanhã"
                else -> dataSelecionada.format(formatterDataCompleta)
                    .replaceFirstChar { it.uppercase() }
            }
        }

    val dataFormatadaCurta: String
        get() = dataSelecionada.format(formatterDataCurta)

    val diaSemana: String
        get() = dataSelecionada.format(formatterDiaSemana).replaceFirstChar { it.uppercase() }

    val temPontos: Boolean
        get() = pontosHoje.isNotEmpty()

    val temMultiplosEmpregos: Boolean
        get() = empregosDisponiveis.size > 1

    val temEmpregoAtivo: Boolean
        get() = empregoAtivo != null

    val nomeEmpregoAtivo: String
        get() = empregoAtivo?.nome ?: "Nenhum emprego"

    val podeRegistrarPonto: Boolean
        get() = temEmpregoAtivo &&
                !isFuturo &&
                !temAusencia &&
                (empregoAtivo?.podeRegistrarPonto == true)

    val podeRegistrarPontoAutomatico: Boolean
        get() = podeRegistrarPonto && isHoje

    val podeRegistrarPontoManual: Boolean
        get() = podeRegistrarPonto

    val podeRegistrarEventoEspecial: Boolean
        get() = temEmpregoAtivo

    val podeNavegaAnterior: Boolean
        get() = dataSelecionada.isAfter(LocalDate.now().minusYears(1))

    val podeNavegarProximo: Boolean
        get() = dataSelecionada.isBefore(LocalDate.now().plusMonths(1))

    val temIntervaloAberto: Boolean
        get() = isHoje && resumoDia.intervalos.any { it.aberto }

    val intervaloAberto: IntervaloPonto?
        get() = if (isHoje) resumoDia.intervalos.find { it.aberto } else null

    val dataHoraInicioContador: LocalDateTime?
        get() = intervaloAberto?.entrada?.dataHora

    val deveExibirContador: Boolean
        get() = isHoje && temIntervaloAberto && dataHoraInicioContador != null

    val jornadaEmAndamento: Boolean
        get() = temPontos && !resumoDia.jornadaCompleta && isHoje

    val ultimoPonto: Ponto?
        get() = pontosHoje.maxByOrNull { it.dataHora }

    val statusJornada: String
        get() = when {
            temAusencia -> ausenciaDoDia?.tipoDescricao ?: "Ausência"
            !temPontos -> "Aguardando entrada"
            jornadaEmAndamento -> "Jornada em andamento"
            resumoDia.jornadaCompleta -> "Jornada finalizada"
            else -> "Status indefinido"
        }

    // ========================================================================
    // DIAS ESPECIAIS - CONSOLIDADO
    // ========================================================================

    val isFeriadoEfetivo: Boolean
        get() = resumoDia.isFeriado

    val isFeriadoTrabalhado: Boolean
        get() = resumoDia.isFeriado && resumoDia.pontos.isNotEmpty()

//    val isDescanso: Boolean
//        get() = resumoDia.isDescanso

    val mensagemTipoDia: String?
        get() = when {
            isDescanso -> "Descanso - sem jornada obrigatória"
            isFerias -> "Férias - sem jornada obrigatória"
            isAtestado -> "Atestado médico - sem jornada obrigatória"
            isLicenca -> "Licença - sem jornada obrigatória"
            isFolga -> {
                val tipoFolgaDescricao = ausenciaDoDia?.tipoDescricaoCompleta ?: "Folga"
                val complemento = if (ausenciaDoDia?.zeraJornadaEfetiva == true)
                    "sem jornada obrigatória"
                else
                    "desconta do banco"
                "$tipoFolgaDescricao - $complemento"
            }
            isFalta -> "Falta - dia não trabalhado"
            isFeriadoTrabalhado -> "Feriado trabalhado - horas contam como extra"
            isFeriadoEfetivo -> "Feriado - sem jornada obrigatória"
            else -> null
        }

    val iconeTipoDia: String
        get() = when {
            isDescanso -> "😴"
            isFerias -> "🏖️"
            isAtestado -> "🏥"
            isLicenca -> "📋"
            isFolga -> ausenciaDoDia?.tipoFolga?.emoji ?: "🏠"
            isFalta -> "❌"
            isFeriadoEfetivo -> "🎉"
            else -> ""
        }

    // ========================================================================
    // VERSÃO DE JORNADA
    // ========================================================================

    val temVersaoJornada: Boolean
        get() = versaoJornadaAtual != null

    val periodoVersaoJornadaFormatado: String?
        get() = versaoJornadaAtual?.periodoFormatado

    val tituloVersaoJornada: String?
        get() = versaoJornadaAtual?.titulo
}
