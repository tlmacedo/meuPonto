// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/usecase/emprego/DesativarEmpregoUseCase.kt
package br.com.tlmacedo.meuponto.domain.usecase.emprego

import br.com.tlmacedo.meuponto.domain.repository.EmpregoRepository
import br.com.tlmacedo.meuponto.domain.repository.PreferenciasRepository
import javax.inject.Inject

/**
 * Caso de uso para desativar temporariamente um emprego.
 *
 * A desativação é uma operação reversível que oculta o emprego da lista
 * principal sem excluir seus dados. Útil para empregos que o usuário
 * não está utilizando no momento, mas pode querer reativar futuramente.
 *
 * @property empregoRepository Repositório de empregos para atualizar o status
 * @property preferenciasRepository Repositório de preferências para gerenciar emprego ativo
 *
 * @author Thiago
 * @since 2.0.0
 */
class DesativarEmpregoUseCase @Inject constructor(
    private val empregoRepository: EmpregoRepository,
    private val preferenciasRepository: PreferenciasRepository
) {

    /**
     * Resultado da operação de desativação.
     */
    sealed class Resultado {
        /**
         * Emprego desativado com sucesso.
         */
        data object Sucesso : Resultado()

        /**
         * Emprego não encontrado no sistema.
         *
         * @property empregoId ID do emprego que não foi encontrado
         */
        data class NaoEncontrado(val empregoId: Long) : Resultado()

        /**
         * Não é possível desativar o único emprego ativo.
         * O usuário deve ter pelo menos um emprego ativo no sistema.
         */
        data object UltimoEmpregoAtivo : Resultado()

        /**
         * Erro durante a operação.
         *
         * @property mensagem Descrição do erro ocorrido
         */
        data class Erro(val mensagem: String) : Resultado()
    }

    /**
     * Desativa o emprego especificado.
     *
     * Verifica se existem outros empregos ativos antes de desativar,
     * pois o usuário deve manter pelo menos um emprego ativo.
     * Se o emprego desativado era o ativo, troca automaticamente para outro.
     *
     * @param empregoId ID do emprego a ser desativado
     * @return Resultado da operação
     */
    suspend operator fun invoke(empregoId: Long): Resultado {
        return try {
            // Verifica se o emprego existe
            val emprego = empregoRepository.buscarPorId(empregoId)
                ?: return Resultado.NaoEncontrado(empregoId)

            // Verifica se é o único emprego ativo
            val quantidadeAtivos = empregoRepository.contarAtivos()
            if (quantidadeAtivos <= 1 && emprego.ativo) {
                return Resultado.UltimoEmpregoAtivo
            }

            // Desativa o emprego
            empregoRepository.atualizarStatus(empregoId, ativo = false)

            // Se era o emprego ativo, troca para outro
            val empregoAtivoId = preferenciasRepository.obterEmpregoAtivoId()
            if (empregoAtivoId == empregoId) {
                // Busca outro emprego ativo para selecionar
                val outrosAtivos = empregoRepository.buscarAtivos()
                if (outrosAtivos.isNotEmpty()) {
                    preferenciasRepository.definirEmpregoAtivoId(outrosAtivos.first().id)
                } else {
                    preferenciasRepository.limparEmpregoAtivo()
                }
            }

            Resultado.Sucesso
        } catch (e: Exception) {
            Resultado.Erro("Erro ao desativar emprego: ${e.message}")
        }
    }

    /**
     * Reativa um emprego previamente desativado.
     *
     * @param empregoId ID do emprego a ser reativado
     * @return Resultado da operação
     */
    suspend fun reativar(empregoId: Long): Resultado {
        return try {
            // Verifica se o emprego existe
            empregoRepository.buscarPorId(empregoId)
                ?: return Resultado.NaoEncontrado(empregoId)

            // Reativa o emprego
            empregoRepository.atualizarStatus(empregoId, ativo = true)

            Resultado.Sucesso
        } catch (e: Exception) {
            Resultado.Erro("Erro ao reativar emprego: ${e.message}")
        }
    }
}
