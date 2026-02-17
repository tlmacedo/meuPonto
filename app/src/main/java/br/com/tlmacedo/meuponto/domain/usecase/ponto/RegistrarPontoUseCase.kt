// Arquivo: RegistrarPontoUseCase.kt
package br.com.tlmacedo.meuponto.domain.usecase.ponto

import br.com.tlmacedo.meuponto.domain.model.Ponto
import br.com.tlmacedo.meuponto.domain.model.PontoConstants
import br.com.tlmacedo.meuponto.domain.model.proximoPontoDescricao
import br.com.tlmacedo.meuponto.domain.repository.PontoRepository
import br.com.tlmacedo.meuponto.domain.repository.PreferenciasRepository
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import javax.inject.Inject

/**
 * Caso de uso para registrar um novo ponto.
 *
 * O tipo do ponto é determinado automaticamente pela posição na lista
 * (índice par = entrada, ímpar = saída).
 *
 * @author Thiago
 * @since 2.0.0
 * @updated 2.1.0 - Removido campo tipo (calculado por posição)
 */
class RegistrarPontoUseCase @Inject constructor(
    private val pontoRepository: PontoRepository,
    private val preferenciasRepository: PreferenciasRepository
) {

    sealed class Resultado {
        data class Sucesso(
            val ponto: Ponto,
            val mensagem: String
        ) : Resultado() {
            val pontoId: Long get() = ponto.id
        }

        data object SemEmpregoAtivo : Resultado()
        data class HorarioInvalido(val motivo: String) : Resultado()
        data class LimiteAtingido(val limite: Int) : Resultado()
        data class Validacao(val erros: List<String>) : Resultado()
        data class Erro(val mensagem: String) : Resultado()
    }

    data class Parametros(
        val empregoId: Long? = null,
        val dataHora: LocalDateTime? = null,
        val observacao: String? = null,
        val latitude: Double? = null,
        val longitude: Double? = null
    )

    suspend operator fun invoke(parametros: Parametros = Parametros()): Resultado {
        return try {
            val empregoId = parametros.empregoId
                ?: preferenciasRepository.obterEmpregoAtivoId()
                ?: return Resultado.SemEmpregoAtivo

            val dataHoraOriginal = parametros.dataHora ?: LocalDateTime.now()
            val dataHora = dataHoraOriginal.truncatedTo(ChronoUnit.MINUTES)
            val data = dataHora.toLocalDate()

            val pontosNoDia = pontoRepository.buscarPorEmpregoEData(empregoId, data)

            // Verifica limite de pontos
            if (pontosNoDia.size >= PontoConstants.MAX_PONTOS) {
                return Resultado.LimiteAtingido(PontoConstants.MAX_PONTOS)
            }

            // Valida horário
            val ultimoPonto = pontosNoDia.maxByOrNull { it.dataHora }
            if (ultimoPonto != null && dataHora.isBefore(ultimoPonto.dataHora)) {
                return Resultado.HorarioInvalido(
                    "O horário não pode ser anterior ao último registro (${ultimoPonto.horaFormatada})"
                )
            }

            // Determina descrição do tipo baseado na posição
            val tipoDescricao = proximoPontoDescricao(pontosNoDia.size)

            val agora = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES)
            val ponto = Ponto(
                empregoId = empregoId,
                dataHora = dataHora,
                observacao = parametros.observacao,
                latitude = parametros.latitude,
                longitude = parametros.longitude,
                criadoEm = agora,
                atualizadoEm = agora
            )

            val id = pontoRepository.inserir(ponto)
            val pontoSalvo = ponto.copy(id = id)

            val mensagem = "$tipoDescricao registrada às ${pontoSalvo.horaFormatada}"

            Resultado.Sucesso(pontoSalvo, mensagem)
        } catch (e: Exception) {
            Resultado.Erro("Erro ao registrar ponto: ${e.message}")
        }
    }

    suspend fun registrarAgora(empregoId: Long? = null): Resultado {
        return invoke(Parametros(empregoId = empregoId))
    }

    suspend fun registrarManual(
        data: LocalDate,
        hora: LocalTime,
        empregoId: Long? = null
    ): Resultado {
        return invoke(
            Parametros(
                empregoId = empregoId,
                dataHora = LocalDateTime.of(data, hora)
            )
        )
    }
}
