// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/usecase/emprego/ObterEmpregoAtivoUseCase.kt
package br.com.tlmacedo.meuponto.domain.usecase.emprego

import br.com.tlmacedo.meuponto.domain.model.Emprego
import br.com.tlmacedo.meuponto.domain.repository.EmpregoRepository
import br.com.tlmacedo.meuponto.domain.repository.PreferenciasRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Caso de uso para obter o emprego atualmente ativo.
 *
 * Gerencia a lógica de recuperação do emprego selecionado pelo usuário,
 * combinando dados do DataStore (ID persistido) com o repositório de empregos.
 * Possui fallback automático para o primeiro emprego disponível caso o
 * emprego salvo não exista mais.
 *
 * @property preferenciasRepository Repositório de preferências para obter o ID salvo
 * @property empregoRepository Repositório de empregos para buscar os dados completos
 *
 * @author Thiago
 * @since 2.0.0
 */
class ObterEmpregoAtivoUseCase @Inject constructor(
    private val preferenciasRepository: PreferenciasRepository,
    private val empregoRepository: EmpregoRepository
) {

    /**
     * Resultado da operação de obtenção do emprego ativo.
     */
    sealed class Resultado {
        /**
         * Emprego ativo encontrado com sucesso.
         *
         * @property emprego Dados do emprego ativo
         */
        data class Sucesso(val emprego: Emprego) : Resultado()

        /**
         * Nenhum emprego cadastrado no sistema.
         * Indica que o usuário precisa criar um emprego primeiro.
         */
        data object NenhumEmpregoCadastrado : Resultado()

        /**
         * Erro durante a operação.
         *
         * @property mensagem Descrição do erro ocorrido
         */
        data class Erro(val mensagem: String) : Resultado()
    }

    /**
     * Obtém o emprego ativo de forma síncrona (suspend).
     *
     * Busca o ID do emprego salvo nas preferências e retorna os dados completos.
     * Se o emprego salvo não existir mais, seleciona automaticamente o primeiro
     * emprego ativo disponível.
     *
     * @return Resultado da operação contendo o emprego ou erro
     */
    suspend operator fun invoke(): Resultado {
        return try {
            // Busca o ID salvo nas preferências
            val empregoIdSalvo = preferenciasRepository.obterEmpregoAtivoId()

            // Se há um ID salvo, tenta buscar o emprego
            if (empregoIdSalvo != null) {
                val emprego = empregoRepository.buscarPorId(empregoIdSalvo)
                if (emprego != null && emprego.isVisivel) {
                    return Resultado.Sucesso(emprego)
                }
            }

            // Fallback: busca o primeiro emprego ativo disponível
            val empregosAtivos = empregoRepository.buscarAtivos()
            if (empregosAtivos.isEmpty()) {
                return Resultado.NenhumEmpregoCadastrado
            }

            // Seleciona o primeiro e salva como ativo
            val primeiroEmprego = empregosAtivos.first()
            preferenciasRepository.definirEmpregoAtivoId(primeiroEmprego.id)

            Resultado.Sucesso(primeiroEmprego)
        } catch (e: Exception) {
            Resultado.Erro("Erro ao obter emprego ativo: ${e.message}")
        }
    }

    /**
     * Observa o emprego ativo de forma reativa.
     *
     * Emite automaticamente quando o emprego ativo muda (seja por troca
     * de seleção ou por alterações nos dados do emprego).
     *
     * @return Flow que emite o emprego ativo ou null se não houver nenhum
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun observar(): Flow<Emprego?> {
        return preferenciasRepository.observarEmpregoAtivoId()
            .flatMapLatest { empregoId ->
                if (empregoId != null) {
                    // Observa o emprego específico
                    empregoRepository.observarPorId(empregoId).map { emprego ->
                        // Se o emprego não existe mais ou foi desativado, retorna null
                        emprego?.takeIf { it.isVisivel }
                    }
                } else {
                    // Sem emprego selecionado, tenta o primeiro ativo
                    empregoRepository.observarAtivos().map { ativos ->
                        ativos.firstOrNull()
                    }
                }
            }
    }

    /**
     * Verifica se há algum emprego ativo selecionado.
     *
     * @return true se há um emprego ativo válido selecionado
     */
    suspend fun temEmpregoAtivo(): Boolean {
        val resultado = invoke()
        return resultado is Resultado.Sucesso
    }
}
