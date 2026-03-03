// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/usecase/versaojornada/ExcluirVersaoJornadaUseCase.kt
package br.com.tlmacedo.meuponto.domain.usecase.versaojornada

import br.com.tlmacedo.meuponto.domain.repository.HorarioDiaSemanaRepository
import br.com.tlmacedo.meuponto.domain.repository.VersaoJornadaRepository
import timber.log.Timber
import java.time.LocalDateTime
import javax.inject.Inject

/**
 * UseCase para excluir uma versão de jornada.
 *
 * Responsabilidades:
 * - Verificar se pode excluir (não é a única versão)
 * - Excluir horários associados
 * - Gerenciar vigência ao excluir versão vigente
 *
 * @author Thiago
 * @since 4.0.0
 */
class ExcluirVersaoJornadaUseCase @Inject constructor(
    private val versaoJornadaRepository: VersaoJornadaRepository,
    private val horarioDiaSemanaRepository: HorarioDiaSemanaRepository
) {
    /**
     * Exclui uma versão de jornada.
     *
     * @param versaoId ID da versão a excluir
     * @return Result com Unit ou erro
     */
    suspend operator fun invoke(versaoId: Long): Result<Unit> {
        return try {
            // 1. Buscar versão
            val versao = versaoJornadaRepository.buscarPorId(versaoId)
                ?: return Result.failure(VersaoJornadaException.VersaoNaoEncontrada)

            // 2. Verificar se é a única versão do emprego
            val versoesEmprego = versaoJornadaRepository.buscarPorEmprego(versao.empregoId)
            if (versoesEmprego.size == 1) {
                return Result.failure(VersaoJornadaException.UnicaVersao)
            }

            // 3. Se é vigente, definir outra como vigente
            if (versao.vigente) {
                val outraVersao = versoesEmprego
                    .filter { it.id != versaoId }
                    .maxByOrNull { it.dataInicio }

                if (outraVersao != null) {
                    // Atualizar a versão mais recente para ser vigente
                    versaoJornadaRepository.atualizar(
                        outraVersao.copy(
                            vigente = true,
                            atualizadoEm = LocalDateTime.now()
                        )
                    )
                    Timber.i("Versão ${outraVersao.id} definida como vigente após exclusão")
                }
            }

            // 4. Excluir horários associados
            horarioDiaSemanaRepository.excluirPorVersaoJornada(versaoId)

            // 5. Excluir versão
            versaoJornadaRepository.excluir(versao)

            Timber.i("Versão de jornada excluída: $versaoId")
            Result.success(Unit)

        } catch (e: Exception) {
            Timber.e(e, "Erro ao excluir versão de jornada")
            Result.failure(VersaoJornadaException.ErroInterno(e.message ?: "Erro desconhecido"))
        }
    }
}
