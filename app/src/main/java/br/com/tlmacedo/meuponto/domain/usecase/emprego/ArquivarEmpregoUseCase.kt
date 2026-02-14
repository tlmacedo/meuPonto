// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/usecase/emprego/ArquivarEmpregoUseCase.kt
package br.com.tlmacedo.meuponto.domain.usecase.emprego

import br.com.tlmacedo.meuponto.domain.repository.EmpregoRepository
import br.com.tlmacedo.meuponto.domain.repository.PreferenciasRepository
import javax.inject.Inject

/**
 * Caso de uso para arquivar permanentemente um emprego.
 *
 * O arquivamento é uma operação semi-permanente que move o emprego para
 * uma seção de arquivados, mantendo todos os dados históricos intactos.
 * Diferente da desativação, o arquivamento indica que o emprego está
 * encerrado (ex: demissão, fim de contrato) e provavelmente não será
 * reativado.
 *
 * @property empregoRepository Repositório de empregos para atualizar o status
 * @property preferenciasRepository Repositório de preferências para gerenciar emprego ativo
 *
 * @author Thiago
 * @since 2.0.0
 */
class ArquivarEmpregoUseCase @Inject constructor(
    private val empregoRepository: EmpregoRepository,
    private val preferenciasRepository: PreferenciasRepository
) {

    /**
     * Resultado da operação de arquivamento.
     */
    sealed class Resultado {
        /**
         * Emprego arquivado com sucesso.
         */
        data object Sucesso : Resultado()

        /**
         * Emprego não encontrado no sistema.
         *
         * @property empregoId ID do emprego que não foi encontrado
         */
        data class NaoEncontrado(val empregoId: Long) : Resultado()

        /**
         * Não é possível arquivar o único emprego.
         * O usuário deve ter pelo menos um emprego ativo no sistema.
         */
        data object UltimoEmprego : Resultado()

        /**
         * Erro durante a operação.
         *
         * @property mensagem Descrição do erro ocorrido
         */
        data class Erro(val mensagem: String) : Resultado()
    }

    /**
     * Arquiva o emprego especificado.
     *
     * Move o emprego para a seção de arquivados, mantendo todos os dados
     * históricos. Se o emprego arquivado era o ativo, troca automaticamente
     * para outro emprego disponível.
     *
     * @param empregoId ID do emprego a ser arquivado
     * @return Resultado da operação
     */
    suspend operator fun invoke(empregoId: Long): Resultado {
        return try {
            // Verifica se o emprego existe
            val emprego = empregoRepository.buscarPorId(empregoId)
                ?: return Resultado.NaoEncontrado(empregoId)

            // Verifica se é o único emprego (ativo ou não)
            val quantidadeTotal = empregoRepository.contarTodos()
            val quantidadeAtivos = empregoRepository.contarAtivos()
            
            // Não pode arquivar se for o único emprego não arquivado
            if (quantidadeTotal <= 1 || (quantidadeAtivos <= 1 && emprego.ativo && !emprego.arquivado)) {
                return Resultado.UltimoEmprego
            }

            // Arquiva o emprego
            empregoRepository.arquivar(empregoId)

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
            Resultado.Erro("Erro ao arquivar emprego: ${e.message}")
        }
    }

    /**
     * Desarquiva um emprego previamente arquivado.
     *
     * Restaura o emprego para a lista de empregos ativos, permitindo
     * novamente o registro de pontos.
     *
     * @param empregoId ID do emprego a ser desarquivado
     * @return Resultado da operação
     */
    suspend fun desarquivar(empregoId: Long): Resultado {
        return try {
            // Verifica se o emprego existe
            empregoRepository.buscarPorId(empregoId)
                ?: return Resultado.NaoEncontrado(empregoId)

            // Desarquiva o emprego
            empregoRepository.desarquivar(empregoId)

            Resultado.Sucesso
        } catch (e: Exception) {
            Resultado.Erro("Erro ao desarquivar emprego: ${e.message}")
        }
    }
}
