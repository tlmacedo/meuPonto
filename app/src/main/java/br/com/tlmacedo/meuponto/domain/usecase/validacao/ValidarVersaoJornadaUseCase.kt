// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/usecase/validacao/ValidarVersaoJornadaUseCase.kt
package br.com.tlmacedo.meuponto.domain.usecase.validacao

import br.com.tlmacedo.meuponto.domain.model.VersaoJornada
import javax.inject.Inject

/**
 * Caso de uso para validar versões de jornada.
 *
 * Contém validações de jornada, banco de horas e período RH
 * que foram migradas de ValidarConfiguracaoEmpregoUseCase.
 *
 * @author Thiago
 * @since 8.0.0
 */
class ValidarVersaoJornadaUseCase @Inject constructor() {

    sealed class ResultadoValidacao {
        data object Valido : ResultadoValidacao()
        data class Invalido(val erros: List<ErroValidacao>) : ResultadoValidacao() {
            val mensagem: String get() = erros.joinToString("\n") { it.mensagem }
        }

        val isValido: Boolean get() = this is Valido
    }

    sealed class ErroValidacao(val mensagem: String, val codigo: String) {
        data object JornadaInvalida : ErroValidacao(
            "Jornada máxima deve estar entre 1 e 24 horas",
            "JORNADA_INVALIDA"
        )

        data object IntervaloInvalido : ErroValidacao(
            "Intervalo interjornada deve estar entre 0 e 24 horas",
            "INTERVALO_INVALIDO"
        )

        data object DiaInicioFechamentoInvalido : ErroValidacao(
            "Dia de início do fechamento deve estar entre 1 e 28",
            "DIA_INICIO_INVALIDO"
        )

        data object PeriodoBancoInvalido : ErroValidacao(
            "Período do banco de horas inválido",
            "PERIODO_BANCO_INVALIDO"
        )

        data object CargaHorariaInvalida : ErroValidacao(
            "Carga horária diária deve estar entre 1 e 24 horas",
            "CARGA_HORARIA_INVALIDA"
        )

        data object TurnoMaximoInvalido : ErroValidacao(
            "Turno máximo deve estar entre 1 e 12 horas",
            "TURNO_MAXIMO_INVALIDO"
        )

        data object DataInicioObrigatoria : ErroValidacao(
            "Data de início é obrigatória",
            "DATA_INICIO_OBRIGATORIA"
        )

        data object DataFimAnteriorInicio : ErroValidacao(
            "Data de fim deve ser posterior à data de início",
            "DATA_FIM_ANTERIOR_INICIO"
        )
    }

    operator fun invoke(versaoJornada: VersaoJornada): ResultadoValidacao {
        val erros = mutableListOf<ErroValidacao>()

        // Validação de jornada máxima (1 a 24 horas = 60 a 1440 minutos)
        if (versaoJornada.jornadaMaximaDiariaMinutos !in 60..1440) {
            erros.add(ErroValidacao.JornadaInvalida)
        }

        // Validação de intervalo interjornada (0 a 24 horas = 0 a 1440 minutos)
        if (versaoJornada.intervaloMinimoInterjornadaMinutos !in 0..1440) {
            erros.add(ErroValidacao.IntervaloInvalido)
        }

        // Validação do dia de início do fechamento RH
        if (versaoJornada.diaInicioFechamentoRH !in 1..28) {
            erros.add(ErroValidacao.DiaInicioFechamentoInvalido)
        }

        // Validação do período de banco de horas
        val periodoBancoTotal = versaoJornada.periodoBancoSemanas + versaoJornada.periodoBancoMeses
        if (versaoJornada.bancoHorasHabilitado && periodoBancoTotal == 0) {
            erros.add(ErroValidacao.PeriodoBancoInvalido)
        }

        // Validação de carga horária diária (1 a 24 horas = 60 a 1440 minutos)
        if (versaoJornada.cargaHorariaDiariaMinutos !in 60..1440) {
            erros.add(ErroValidacao.CargaHorariaInvalida)
        }

        // Validação de turno máximo (1 a 12 horas = 60 a 720 minutos)
        if (versaoJornada.turnoMaximoMinutos !in 60..720) {
            erros.add(ErroValidacao.TurnoMaximoInvalido)
        }

        // Validação de datas
        if (versaoJornada.dataFim != null && versaoJornada.dataFim.isBefore(versaoJornada.dataInicio)) {
            erros.add(ErroValidacao.DataFimAnteriorInicio)
        }

        return if (erros.isEmpty()) {
            ResultadoValidacao.Valido
        } else {
            ResultadoValidacao.Invalido(erros)
        }
    }
}
