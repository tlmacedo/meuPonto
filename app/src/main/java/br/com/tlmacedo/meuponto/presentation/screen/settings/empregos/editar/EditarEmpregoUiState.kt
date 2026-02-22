// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/settings/empregos/editar/EditarEmpregoUiState.kt
package br.com.tlmacedo.meuponto.presentation.screen.settings.empregos.editar

import br.com.tlmacedo.meuponto.domain.model.DiaSemana
import br.com.tlmacedo.meuponto.domain.model.PeriodoRH
import br.com.tlmacedo.meuponto.domain.model.TipoNsr
import java.time.Duration
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Estado da tela de edição/criação de emprego.
 *
 * @author Thiago
 * @since 2.0.0
 * @updated 3.0.0 - Refatoração completa do sistema de ciclos de banco de horas
 */
data class EditarEmpregoUiState(
    val empregoId: Long? = null,
    val isNovoEmprego: Boolean = true,
    val nome: String = "",
    val nomeErro: String? = null,
    val dataInicioTrabalho: LocalDate? = null,

    // JORNADA DE TRABALHO
    val cargaHorariaDiaria: Duration = Duration.ofMinutes(492),
    val jornadaMaximaDiariaMinutos: Int = 600,
    val intervaloMinimoMinutos: Int = 60,
    val intervaloInterjornadaMinutos: Int = 660,

    // TOLERÂNCIAS (apenas intervalo - entrada/saída são por dia)
    val toleranciaIntervaloMaisMinutos: Int = 0,

    // NSR E LOCALIZAÇÃO
    val habilitarNsr: Boolean = false,
    val tipoNsr: TipoNsr = TipoNsr.NUMERICO,
    val habilitarLocalizacao: Boolean = false,
    val localizacaoAutomatica: Boolean = false,

    // VALIDAÇÕES
    val exigeJustificativaInconsistencia: Boolean = false,

    // PERÍODO RH
    val primeiroDiaSemana: DiaSemana = DiaSemana.SEGUNDA,
    val diaInicioFechamentoRH: Int = 1,
    val zerarSaldoPeriodoRH: Boolean = false,

    // BANCO DE HORAS - CICLO
    val bancoHorasHabilitado: Boolean = false,
    val periodoBancoValor: Int = 0, // 0=desabilitado, 1-3=semanas, 4+=meses
    val dataInicioCicloBanco: LocalDate? = null,
    val zerarBancoAntesPeriodo: Boolean = false,

    // ESTADOS DE UI
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val erro: String? = null,
    val secaoExpandida: SecaoFormulario? = SecaoFormulario.DADOS_BASICOS,

    // Pickers
    val showInicioTrabalhoPicker: Boolean = false,
    val showDataInicioCicloPicker: Boolean = false
) {
    private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    // Propriedades computadas básicas
    val tituloTela: String = if (isNovoEmprego) "Novo Emprego" else "Editar Emprego"
    val textoBotaoSalvar: String = if (isNovoEmprego) "Criar Emprego" else "Salvar Alterações"
    val formularioValido: Boolean = nome.isNotBlank() && nomeErro == null

    // Formatações de data
    val dataInicioTrabalhoFormatada: String
        get() = dataInicioTrabalho?.format(dateFormatter) ?: "Não informada"

    val dataInicioCicloFormatada: String
        get() = dataInicioCicloBanco?.format(dateFormatter) ?: "Selecionar data"

    // Conversão do slider para semanas/meses
    val periodoBancoSemanas: Int
        get() = if (periodoBancoValor in 1..3) periodoBancoValor else 0

    val periodoBancoMeses: Int
        get() = if (periodoBancoValor > 3) periodoBancoValor - 3 else 0

    // Verificação se banco está ativo
    val temBancoHoras: Boolean
        get() = bancoHorasHabilitado && periodoBancoValor > 0

    // Descrição do período do banco
    val periodoBancoDescricao: String
        get() = when (periodoBancoValor) {
            0 -> "Desabilitado"
            1 -> "1 semana"
            2 -> "2 semanas"
            3 -> "3 semanas"
            else -> "${periodoBancoValor - 3} mês(es)"
        }

    // Data de fim do ciclo calculada
    val dataFimCicloCalculada: String
        get() {
            if (!temBancoHoras || dataInicioCicloBanco == null) return "—"

            val dataFim = when {
                periodoBancoSemanas > 0 ->
                    dataInicioCicloBanco.plusWeeks(periodoBancoSemanas.toLong()).minusDays(1)
                periodoBancoMeses > 0 ->
                    dataInicioCicloBanco.plusMonths(periodoBancoMeses.toLong()).minusDays(1)
                else -> return "—"
            }

            return dataFim.format(dateFormatter)
        }

    // Descrição do ciclo completo
    val cicloDescricao: String
        get() {
            if (!temBancoHoras || dataInicioCicloBanco == null) return "Não configurado"
            return "${dataInicioCicloFormatada} ~ $dataFimCicloCalculada"
        }

    // Label dinâmico para zerar saldo
    val labelZerarSaldoDinamico: String
        get() = when (periodoBancoValor) {
            0 -> "Zerar saldo ao fim do período"
            1 -> "Zerar saldo a cada semana"
            2 -> "Zerar saldo a cada 2 semanas"
            3 -> "Zerar saldo a cada 3 semanas"
            else -> {
                val meses = periodoBancoValor - 3
                if (meses == 1) "Zerar saldo mensalmente"
                else "Zerar saldo a cada $meses meses"
            }
        }

    // Exemplo do período RH
    val exemploPeriodoRH: String
        get() {
            val hoje = LocalDate.now()
            val periodo = PeriodoRH.criarPara(hoje, diaInicioFechamentoRH)
            return "Ex: ${periodo.periodoDescricao}"
        }

    // Próximo fechamento RH
    val proximoFechamentoRH: String
        get() {
            val hoje = LocalDate.now()
            val periodo = PeriodoRH.criarPara(hoje, diaInicioFechamentoRH)
            return periodo.dataFim.plusDays(1).format(dateFormatter)
        }
}

/**
 * Seções do formulário de edição.
 */
enum class SecaoFormulario {
    DADOS_BASICOS,
    JORNADA,
    TOLERANCIAS,
    NSR_LOCALIZACAO,
    BANCO_HORAS,
    AVANCADO
}
