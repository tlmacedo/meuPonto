// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/usecase/validacao/ValidarConfiguracaoEmpregoUseCase.kt
package br.com.tlmacedo.meuponto.domain.usecase.validacao

import br.com.tlmacedo.meuponto.domain.model.ConfiguracaoEmprego
import javax.inject.Inject

/**
 * Caso de uso para validar configurações de emprego.
 *
 * @author Thiago
 * @since 2.0.0
 * @updated 3.0.0 - Atualizado para nova estrutura de ciclos
 */
class ValidarConfiguracaoEmpregoUseCase @Inject constructor() {

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
    }

    operator fun invoke(configuracao: ConfiguracaoEmprego): ResultadoValidacao {
        val erros = mutableListOf<ErroValidacao>()

        // Validação de jornada máxima (1 a 24 horas = 60 a 1440 minutos)
        if (configuracao.jornadaMaximaDiariaMinutos !in 60..1440) {
            erros.add(ErroValidacao.JornadaInvalida)
        }

        // Validação de intervalo interjornada (0 a 24 horas = 0 a 1440 minutos)
        if (configuracao.intervaloMinimoInterjornadaMinutos !in 0..1440) {
            erros.add(ErroValidacao.IntervaloInvalido)
        }

        // Validação do dia de início do fechamento RH
        if (configuracao.diaInicioFechamentoRH !in 1..28) {
            erros.add(ErroValidacao.DiaInicioFechamentoInvalido)
        }

        // Validação do período de banco de horas
        val periodoBancoTotal = configuracao.periodoBancoSemanas + configuracao.periodoBancoMeses
        if (configuracao.bancoHorasHabilitado && periodoBancoTotal == 0) {
            erros.add(ErroValidacao.PeriodoBancoInvalido)
        }

        return if (erros.isEmpty()) {
            ResultadoValidacao.Valido
        } else {
            ResultadoValidacao.Invalido(erros)
        }
    }
}
