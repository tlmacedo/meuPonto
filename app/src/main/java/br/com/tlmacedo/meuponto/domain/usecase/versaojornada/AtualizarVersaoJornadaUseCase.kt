// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/usecase/versaojornada/AtualizarVersaoJornadaUseCase.kt
package br.com.tlmacedo.meuponto.domain.usecase.versaojornada

import br.com.tlmacedo.meuponto.domain.model.VersaoJornada
import br.com.tlmacedo.meuponto.domain.repository.VersaoJornadaRepository
import timber.log.Timber
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

/**
 * UseCase para atualizar uma versão de jornada existente.
 *
 * Responsabilidades:
 * - Validar dados de entrada
 * - Verificar sobreposição de datas (excluindo a própria versão)
 * - Gerenciar vigência
 *
 * @author Thiago
 * @since 4.0.0
 */
class AtualizarVersaoJornadaUseCase @Inject constructor(
    private val versaoJornadaRepository: VersaoJornadaRepository
) {
    /**
     * Atualiza uma versão de jornada existente.
     *
     * @param params Parâmetros para atualização
     * @return Result com a versão atualizada ou erro
     */
    suspend operator fun invoke(params: Params): Result<VersaoJornada> {
        return try {
            // 1. Buscar versão existente
            val versaoExistente = versaoJornadaRepository.buscarPorId(params.versaoId)
                ?: return Result.failure(VersaoJornadaException.VersaoNaoEncontrada)

            // 2. Buscar outras versões do mesmo emprego
            val outrasVersoes = versaoJornadaRepository
                .buscarPorEmprego(versaoExistente.empregoId)
                .filter { it.id != params.versaoId }

            // 3. Validar sobreposição de datas
            val sobreposicao = verificarSobreposicao(
                dataInicio = params.dataInicio,
                dataFim = params.dataFim,
                versoesExistentes = outrasVersoes,
                versaoIdIgnorar = params.versaoId
            )
            if (sobreposicao != null) {
                return Result.failure(
                    VersaoJornadaException.SobreposicaoDatas(sobreposicao)
                )
            }

            // 4. Se está sendo definida como vigente, desativar outras
            if (params.vigente && !versaoExistente.vigente) {
                outrasVersoes
                    .filter { it.vigente }
                    .forEach { versao ->
                        versaoJornadaRepository.atualizar(
                            versao.copy(
                                vigente = false,
                                dataFim = params.dataInicio.minusDays(1),
                                atualizadoEm = LocalDateTime.now()
                            )
                        )
                    }
            }

            // 5. Atualizar a versão
            val versaoAtualizada = versaoExistente.copy(
                dataInicio = params.dataInicio,
                dataFim = params.dataFim,
                descricao = params.descricao?.ifBlank { null },
                vigente = params.vigente,
                jornadaMaximaDiariaMinutos = params.jornadaMaximaDiariaMinutos,
                intervaloMinimoInterjornadaMinutos = params.intervaloMinimoInterjornadaMinutos,
                toleranciaIntervaloMaisMinutos = params.toleranciaIntervaloMaisMinutos,
                atualizadoEm = LocalDateTime.now()
            )

            versaoJornadaRepository.atualizar(versaoAtualizada)

            Timber.i("Versão de jornada atualizada: ${params.versaoId}")
            Result.success(versaoAtualizada)

        } catch (e: Exception) {
            Timber.e(e, "Erro ao atualizar versão de jornada")
            Result.failure(VersaoJornadaException.ErroInterno(e.message ?: "Erro desconhecido"))
        }
    }

    /**
     * Parâmetros para atualização de versão de jornada.
     */
    data class Params(
        val versaoId: Long,
        val dataInicio: LocalDate,
        val dataFim: LocalDate? = null,
        val descricao: String? = null,
        val vigente: Boolean = true,
        val jornadaMaximaDiariaMinutos: Int = 600,
        val intervaloMinimoInterjornadaMinutos: Int = 660,
        val toleranciaIntervaloMaisMinutos: Int = 0
    )
}
