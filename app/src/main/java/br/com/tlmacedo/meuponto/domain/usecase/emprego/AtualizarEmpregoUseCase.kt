// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/usecase/emprego/AtualizarEmpregoUseCase.kt
package br.com.tlmacedo.meuponto.domain.usecase.emprego

import br.com.tlmacedo.meuponto.domain.model.ConfiguracaoEmprego
import br.com.tlmacedo.meuponto.domain.model.DiaSemana
import br.com.tlmacedo.meuponto.domain.model.Emprego
import br.com.tlmacedo.meuponto.domain.model.TipoNsr
import br.com.tlmacedo.meuponto.domain.model.VersaoJornada
import br.com.tlmacedo.meuponto.domain.repository.ConfiguracaoEmpregoRepository
import br.com.tlmacedo.meuponto.domain.repository.EmpregoRepository
import br.com.tlmacedo.meuponto.domain.repository.VersaoJornadaRepository
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
    private val versaoJornadaRepository: VersaoJornadaRepository
) {
    data class Parametros(
        val empregoId: Long,
        val nome: String,
        val apelido: String? = null,
        val endereco: String? = null,
        val descricao: String? = null,
        val dataInicioTrabalho: LocalDate,
        val dataTerminoTrabalho: LocalDate? = null,
        
        // Configurações Fixas
        val habilitarNsr: Boolean,
        val tipoNsr: TipoNsr,
        val habilitarLocalizacao: Boolean,
        val localizacaoAutomatica: Boolean,
        val fotoHabilitada: Boolean,
        val fotoObrigatoria: Boolean
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

            // 1. Atualizar Emprego
            empregoRepository.atualizar(
                empregoExistente.copy(
                    nome = parametros.nome.trim(),
                    apelido = parametros.apelido?.trim(),
                    endereco = parametros.endereco?.trim(),
                    descricao = parametros.descricao?.trim(),
                    dataInicioTrabalho = parametros.dataInicioTrabalho,
                    dataTerminoTrabalho = parametros.dataTerminoTrabalho,
                    atualizadoEm = agora
                )
            )

            // 2. Atualizar ConfiguracaoEmprego
            val configExistente = configuracaoEmpregoRepository.buscarPorEmpregoId(parametros.empregoId)
            if (configExistente != null) {
                configuracaoEmpregoRepository.atualizar(
                    configExistente.copy(
                        habilitarNsr = parametros.habilitarNsr,
                        tipoNsr = parametros.tipoNsr,
                        habilitarLocalizacao = parametros.habilitarLocalizacao,
                        localizacaoAutomatica = parametros.localizacaoAutomatica,
                        fotoHabilitada = parametros.fotoHabilitada,
                        fotoObrigatoria = parametros.fotoObrigatoria,
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
