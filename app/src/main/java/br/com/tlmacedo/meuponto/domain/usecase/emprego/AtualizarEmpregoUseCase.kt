// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/usecase/emprego/AtualizarEmpregoUseCase.kt
package br.com.tlmacedo.meuponto.domain.usecase.emprego

import androidx.core.net.toUri
import br.com.tlmacedo.meuponto.domain.model.TipoNsr
import br.com.tlmacedo.meuponto.domain.repository.ConfiguracaoEmpregoRepository
import br.com.tlmacedo.meuponto.domain.repository.EmpregoRepository
import br.com.tlmacedo.meuponto.domain.repository.VersaoJornadaRepository
import br.com.tlmacedo.meuponto.util.LogoImageStorage
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

/**
 * Caso de uso para atualizar um emprego e suas configurações relacionadas.
 *
 * @author Thiago
 * @since 11.0.0
 */
class AtualizarEmpregoUseCase @Inject constructor(
    private val empregoRepository: EmpregoRepository,
    private val configuracaoEmpregoRepository: ConfiguracaoEmpregoRepository,
    private val versaoJornadaRepository: VersaoJornadaRepository,
    private val logoImageStorage: LogoImageStorage
) {
    data class Parametros(
        val empregoId: Long,
        val nome: String,
        val apelido: String? = null,
        val endereco: String? = null,
        val descricao: String? = null,
        val dataInicioTrabalho: LocalDate,
        val dataTerminoTrabalho: LocalDate? = null,
        val logo: String? = null,

        // Configurações Fixas
        val habilitarNsr: Boolean,
        val tipoNsr: TipoNsr,
        val habilitarLocalizacao: Boolean,
        val localizacaoAutomatica: Boolean,
        val exibirLocalizacaoDetalhes: Boolean,
        val latitude: Double? = null,
        val longitude: Double? = null,
        val raioGeofencing: Int = 200,
        val fotoHabilitada: Boolean,
        val fotoObrigatoria: Boolean,
        val fotoValidarComprovante: Boolean,

        // RH e Banco de Horas
        val diaInicioFechamentoRH: Int,
        val bancoHorasHabilitado: Boolean,
        val bancoHorasCicloMeses: Int,
        val bancoHorasDataInicioCiclo: LocalDate? = null,
        val bancoHorasZerarAoFinalCiclo: Boolean,
        val exigeJustificativaInconsistencia: Boolean,

        // Comentários e Exibição
        val comentarioHabilitado: Boolean,
        val comentarioObrigatorioHoraExtra: Boolean,
        val exibirDuracaoTurno: Boolean,
        val exibirDuracaoIntervalo: Boolean
    )

    sealed class Resultado {
        data object Sucesso : Resultado()
        data class Erro(val mensagem: String) : Resultado()
        data object NaoEncontrado : Resultado()
    }

    suspend operator fun invoke(parametros: Parametros): Resultado {
        return try {
            val empregoExistente = empregoRepository.buscarPorId(parametros.empregoId)
                ?: return Resultado.NaoEncontrado

            val agora = LocalDateTime.now()

            // 1. Processar Logo se necessário
            val logoFinal = if (parametros.logo != null && parametros.logo.startsWith("content://")) {
                logoImageStorage.saveFromUri(parametros.logo.toUri(), parametros.empregoId)
                    ?: parametros.logo
            } else {
                parametros.logo
            }

            // 2. Atualizar Emprego
            empregoRepository.atualizar(
                empregoExistente.copy(
                    nome = parametros.nome.trim(),
                    apelido = parametros.apelido?.trim(),
                    endereco = parametros.endereco?.trim(),
                    descricao = parametros.descricao?.trim(),
                    dataInicioTrabalho = parametros.dataInicioTrabalho,
                    dataTerminoTrabalho = parametros.dataTerminoTrabalho,
                    logo = logoFinal,
                    atualizadoEm = agora
                )
            )

            // 3. Atualizar ConfiguracaoEmprego
            val configExistente = configuracaoEmpregoRepository.buscarPorEmpregoId(parametros.empregoId)
            if (configExistente != null) {
                configuracaoEmpregoRepository.atualizar(
                    configExistente.copy(
                        habilitarNsr = parametros.habilitarNsr,
                        tipoNsr = parametros.tipoNsr,
                        habilitarLocalizacao = parametros.habilitarLocalizacao,
                        localizacaoAutomatica = parametros.localizacaoAutomatica,
                        exibirLocalizacaoDetalhes = parametros.exibirLocalizacaoDetalhes,
                        latitude = parametros.latitude,
                        longitude = parametros.longitude,
                        raioGeofencing = parametros.raioGeofencing,
                        fotoHabilitada = parametros.fotoHabilitada,
                        fotoObrigatoria = parametros.fotoObrigatoria,
                        fotoValidarComprovante = parametros.fotoValidarComprovante,
                        diaInicioFechamentoRH = parametros.diaInicioFechamentoRH,
                        bancoHorasHabilitado = parametros.bancoHorasHabilitado,
                        bancoHorasCicloMeses = parametros.bancoHorasCicloMeses,
                        bancoHorasDataInicioCiclo = parametros.bancoHorasDataInicioCiclo,
                        bancoHorasZerarAoFinalCiclo = parametros.bancoHorasZerarAoFinalCiclo,
                        exigeJustificativaInconsistencia = parametros.exigeJustificativaInconsistencia,
                        comentarioHabilitado = parametros.comentarioHabilitado,
                        comentarioObrigatorioHoraExtra = parametros.comentarioObrigatorioHoraExtra,
                        exibirDuracaoTurno = parametros.exibirDuracaoTurno,
                        exibirDuracaoIntervalo = parametros.exibirDuracaoIntervalo,
                        atualizadoEm = agora
                    )
                )
            }

            Resultado.Sucesso
        } catch (e: Exception) {
            Resultado.Erro("Erro ao atualizar emprego: ${e.message}")
        }
    }
}
