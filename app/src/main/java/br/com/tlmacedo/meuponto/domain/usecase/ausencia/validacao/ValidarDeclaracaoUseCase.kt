// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/usecase/ausencia/validacao/ValidarDeclaracaoUseCase.kt
package br.com.tlmacedo.meuponto.domain.usecase.ausencia.validacao

import java.time.Duration
import java.time.LocalTime
import javax.inject.Inject

sealed class ResultadoValidacaoDeclaracao {
    data object Valida : ResultadoValidacaoDeclaracao()
    data class Invalida(val mensagem: String) : ResultadoValidacaoDeclaracao()
}

class ValidarDeclaracaoUseCase @Inject constructor() {

    operator fun invoke(
        horaInicio: LocalTime?,
        horaFim: LocalTime?,
        jornadaPrevistaMinutos: Int
    ): ResultadoValidacaoDeclaracao {
        if (horaInicio == null || horaFim == null) {
            return ResultadoValidacaoDeclaracao.Invalida(
                "Informe o horário inicial e final da declaração."
            )
        }

        if (!horaFim.isAfter(horaInicio)) {
            return ResultadoValidacaoDeclaracao.Invalida(
                "O horário final deve ser maior que o horário inicial."
            )
        }

        val minutosDeclaracao = Duration.between(horaInicio, horaFim).toMinutes().toInt()

        if (minutosDeclaracao > jornadaPrevistaMinutos) {
            return ResultadoValidacaoDeclaracao.Invalida(
                "A declaração não pode exceder o tempo total da jornada."
            )
        }

        return ResultadoValidacaoDeclaracao.Valida
    }
}