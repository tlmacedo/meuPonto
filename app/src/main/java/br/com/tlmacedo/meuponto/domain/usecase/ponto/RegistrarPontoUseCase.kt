// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/usecase/ponto/RegistrarPontoUseCase.kt
package br.com.tlmacedo.meuponto.domain.usecase.ponto

import br.com.tlmacedo.meuponto.domain.model.Ponto
import br.com.tlmacedo.meuponto.domain.model.PontoConstants
import br.com.tlmacedo.meuponto.domain.model.proximoPontoDescricao
import br.com.tlmacedo.meuponto.domain.repository.ConfiguracaoEmpregoRepository
import br.com.tlmacedo.meuponto.domain.repository.PontoRepository
import br.com.tlmacedo.meuponto.domain.repository.PreferenciasRepository
import br.com.tlmacedo.meuponto.domain.repository.VersaoJornadaRepository
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import javax.inject.Inject

/**
 * Caso de uso para registrar um novo ponto.
 *
 * @author Thiago
 * @since 2.0.0
 * @updated 12.0.0 - Integração com VersaoJornada para validações e tolerância
 */
class RegistrarPontoUseCase @Inject constructor(
    private val pontoRepository: PontoRepository,
    private val preferenciasRepository: PreferenciasRepository,
    private val configuracaoEmpregoRepository: ConfiguracaoEmpregoRepository,
    private val versaoJornadaRepository: VersaoJornadaRepository,
    private val calcularHoraConsideradaUseCase: CalcularHoraConsideradaUseCase
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
        data class NsrObrigatorio(val tipoNsr: br.com.tlmacedo.meuponto.domain.model.TipoNsr) :
            Resultado()

        data object LocalizacaoObrigatoria : Resultado()
        data object VersaoNaoEncontrada : Resultado()
        data class Erro(val mensagem: String) : Resultado()
    }

    data class Parametros(
        val empregoId: Long? = null,
        val dataHora: LocalDateTime? = null,
        val observacao: String? = null,
        val nsr: String? = null,
        val latitude: Double? = null,
        val longitude: Double? = null,
        val endereco: String? = null,
        val nsrAutoFilled: Boolean = false,
        val horaAutoFilled: Boolean = false,
        val dataAutoFilled: Boolean = false
    )

    suspend operator fun invoke(parametros: Parametros = Parametros()): Resultado {
        return try {
            val empregoId = parametros.empregoId
                ?: preferenciasRepository.obterEmpregoAtivoId()
                ?: return Resultado.SemEmpregoAtivo

            val dataHoraOriginal = parametros.dataHora ?: LocalDateTime.now()
            val dataHora = dataHoraOriginal.truncatedTo(ChronoUnit.MINUTES)
            val data = dataHora.toLocalDate()

            // 1. Buscar Versão da Jornada para a data do ponto
            versaoJornadaRepository.buscarPorEmpregoEData(empregoId, data)
                ?: return Resultado.VersaoNaoEncontrada

            // 2. Buscar configurações fixas do emprego
            val configuracao = configuracaoEmpregoRepository.buscarPorEmpregoId(empregoId)

            // 3. Validar NSR se habilitado (ConfiguracaoEmprego)
            if (configuracao?.habilitarNsr == true && parametros.nsr.isNullOrBlank()) {
                return Resultado.NsrObrigatorio(configuracao.tipoNsr)
            }

            // 4. Validar localização se habilitada (ConfiguracaoEmprego)
            if (configuracao?.habilitarLocalizacao == true) {
                if (parametros.latitude == null || parametros.longitude == null) {
                    return Resultado.LocalizacaoObrigatoria
                }
            }

            val pontosNoDia = pontoRepository.buscarPorEmpregoEData(empregoId, data)

            // 5. Verifica limite de pontos
            if (pontosNoDia.size >= PontoConstants.MAX_PONTOS) {
                return Resultado.LimiteAtingido(PontoConstants.MAX_PONTOS)
            }

            // 6. Determina o índice correto baseado na ordem cronológica (mesmo se inserido fora de ordem)
            val indicePonto = pontosNoDia.count { it.dataHora.isBefore(dataHora) }
            val tipoDescricao = proximoPontoDescricao(indicePonto)

            // 7. Calcula hora considerada com tolerância (usa a versão encontrada no passo 1)
            val horaConsiderada: LocalTime = calcularHoraConsideradaUseCase(
                empregoId = empregoId,
                dataHora = dataHora,
                indicePonto = indicePonto
            )

            val agora = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES)
            val ponto = Ponto(
                empregoId = empregoId,
                dataHora = dataHora,
                horaConsiderada = horaConsiderada,
                observacao = parametros.observacao,
                nsr = parametros.nsr,
                latitude = parametros.latitude,
                longitude = parametros.longitude,
                endereco = parametros.endereco,
                nsrAutoFilled = parametros.nsrAutoFilled,
                horaAutoFilled = parametros.horaAutoFilled,
                dataAutoFilled = parametros.dataAutoFilled,
                criadoEm = agora,
                atualizadoEm = agora
            )

            val id = pontoRepository.inserir(ponto)
            val pontoSalvo = ponto.copy(id = id)

            // Mensagem inclui informação de tolerância se aplicada
            val mensagem = buildString {
                append("$tipoDescricao registrada às ${pontoSalvo.horaFormatada}")
                if (pontoSalvo.temAjusteTolerancia) {
                    append(" (considerado: ${pontoSalvo.horaConsideradaFormatada})")
                }
            }

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
        empregoId: Long? = null,
        nsr: String? = null,
        latitude: Double? = null,
        longitude: Double? = null,
        endereco: String? = null
    ): Resultado {
        return invoke(
            Parametros(
                empregoId = empregoId,
                dataHora = LocalDateTime.of(data, hora),
                nsr = nsr,
                latitude = latitude,
                longitude = longitude,
                endereco = endereco
            )
        )
    }
}
