// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/usecase/ausencia/validacao/ValidarDayOffUseCase.kt
package br.com.tlmacedo.meuponto.domain.usecase.ausencia.validacao

import br.com.tlmacedo.meuponto.domain.model.ausencia.TipoAusencia
import br.com.tlmacedo.meuponto.domain.repository.AusenciaRepository
import java.time.LocalDate
import javax.inject.Inject

sealed class ResultadoValidacaoDayOff {
    data object Valido : ResultadoValidacaoDayOff()
    data class Invalido(val mensagem: String) : ResultadoValidacaoDayOff()
}

class ValidarDayOffUseCase @Inject constructor(
    private val ausenciaRepository: AusenciaRepository
) {

    suspend operator fun invoke(
        empregoId: Long,
        dataNascimento: LocalDate,
        dataDayOff: LocalDate
    ): ResultadoValidacaoDayOff {
        val inicioPermitido = dataNascimento.withYear(dataDayOff.year)
        val fimPermitido = inicioPermitido.plusDays(90)

        if (dataDayOff.isBefore(inicioPermitido) || dataDayOff.isAfter(fimPermitido)) {
            return ResultadoValidacaoDayOff.Invalido(
                "O day off deve ser usado entre o aniversário e até 90 dias depois."
            )
        }

        val jaExisteNoAno = ausenciaRepository.buscarPorPeriodo(
            empregoId = empregoId,
            dataInicio = LocalDate.of(dataDayOff.year, 1, 1),
            dataFim = LocalDate.of(dataDayOff.year, 12, 31)
        ).any {
            it.ativo && it.tipo == TipoAusencia.DayOff
        }

        if (jaExisteNoAno) {
            return ResultadoValidacaoDayOff.Invalido(
                "Já existe um day off registrado neste ano."
            )
        }

        return ResultadoValidacaoDayOff.Valido
    }
}