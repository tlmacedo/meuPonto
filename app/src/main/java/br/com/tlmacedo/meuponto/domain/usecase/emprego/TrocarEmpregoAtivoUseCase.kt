// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/usecase/emprego/TrocarEmpregoAtivoUseCase.kt
package br.com.tlmacedo.meuponto.domain.usecase.emprego

import br.com.tlmacedo.meuponto.domain.model.Emprego
import br.com.tlmacedo.meuponto.domain.repository.EmpregoRepository
import br.com.tlmacedo.meuponto.domain.repository.PreferenciasRepository
import javax.inject.Inject

/**
 * Caso de uso para trocar o emprego atualmente ativo.
 *
 * Permite ao usuário alternar entre diferentes empregos cadastrados,
 * validando se o emprego selecionado está disponível (ativo e não arquivado).
 *
 * @property preferenciasRepository Repositório de preferências para salvar a seleção
 * @property empregoRepository Repositório de empregos para validar o emprego
 *
 * @author Thiago
 * @since 2.0.0
 */
class TrocarEmpregoAtivoUseCase @Inject constructor(
    private val preferenciasRepository: PreferenciasRepository,
    private val empregoRepository: EmpregoRepository
) {

    /**
     * Resultado da operação de troca de emprego.
     */
    sealed class Resultado {
        /**
         * Emprego trocado com sucesso.
         *
         * @property emprego Novo emprego ativo
         */
        data class Sucesso(val emprego: Emprego) : Resultado()

        /**
         * Emprego não encontrado no sistema.
         *
         * @property empregoId ID do emprego que não foi encontrado
         */
        data class NaoEncontrado(val empregoId: Long) : Resultado()

        /**
         * Emprego está inativo ou arquivado.
         *
         * @property emprego Emprego que não pode ser selecionado
         */
        data class EmpregoIndisponivel(val emprego: Emprego) : Resultado()

        /**
         * Erro durante a operação.
         *
         * @property mensagem Descrição do erro ocorrido
         */
        data class Erro(val mensagem: String) : Resultado()
    }

    /**
     * Troca o emprego ativo para o emprego especificado.
     *
     * Valida se o emprego existe, está ativo e não está arquivado antes
     * de realizar a troca. Persiste a seleção nas preferências do app.
     *
     * @param empregoId ID do emprego a ser definido como ativo
     * @return Resultado da operação
     */
    suspend operator fun invoke(empregoId: Long): Resultado {
        return try {
            // Busca o emprego para validação
            val emprego = empregoRepository.buscarPorId(empregoId)
                ?: return Resultado.NaoEncontrado(empregoId)

            // Verifica se o emprego pode ser selecionado
            if (!emprego.isVisivel) {
                return Resultado.EmpregoIndisponivel(emprego)
            }

            // Salva o novo emprego ativo nas preferências
            preferenciasRepository.definirEmpregoAtivoId(empregoId)

            Resultado.Sucesso(emprego)
        } catch (e: Exception) {
            Resultado.Erro("Erro ao trocar emprego ativo: ${e.message}")
        }
    }

    /**
     * Troca o emprego ativo usando o objeto Emprego diretamente.
     *
     * Atalho conveniente quando já se tem o objeto Emprego em mãos.
     *
     * @param emprego Emprego a ser definido como ativo
     * @return Resultado da operação
     */
    suspend operator fun invoke(emprego: Emprego): Resultado {
        return invoke(emprego.id)
    }
}
