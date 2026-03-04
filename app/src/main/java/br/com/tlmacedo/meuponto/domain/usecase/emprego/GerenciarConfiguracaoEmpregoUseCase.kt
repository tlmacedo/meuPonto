// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/usecase/emprego/GerenciarConfiguracaoEmpregoUseCase.kt
package br.com.tlmacedo.meuponto.domain.usecase.emprego

import br.com.tlmacedo.meuponto.domain.model.ConfiguracaoEmprego
import br.com.tlmacedo.meuponto.domain.repository.ConfiguracaoEmpregoRepository
import java.time.LocalDateTime
import javax.inject.Inject

/**
 * UseCase para gerenciar configurações de exibição/comportamento do emprego.
 *
 * NOTA: Configurações de jornada, banco de horas e período RH agora são
 * gerenciadas via VersaoJornada (veja GerenciarVersaoJornadaUseCase).
 *
 * @author Thiago
 * @since 2.0.0
 * @updated 8.0.0 - Simplificado após migração de campos para VersaoJornada
 */
class GerenciarConfiguracaoEmpregoUseCase @Inject constructor(
    private val configuracaoEmpregoRepository: ConfiguracaoEmpregoRepository
) {
    sealed class Resultado {
        data class Sucesso(val configuracao: ConfiguracaoEmprego) : Resultado()
        data class Erro(val mensagem: String) : Resultado()
    }

    /**
     * Obtém a configuração de exibição/comportamento do emprego.
     */
    suspend fun obter(empregoId: Long): ConfiguracaoEmprego {
        return configuracaoEmpregoRepository.buscarPorEmpregoId(empregoId)
            ?: ConfiguracaoEmprego(empregoId = empregoId)
    }

    /**
     * Atualiza a configuração de exibição/comportamento.
     */
    suspend fun atualizar(configuracao: ConfiguracaoEmprego): Resultado {
        return try {
            val configAtualizada = configuracao.copy(atualizadoEm = LocalDateTime.now())

            val existente = configuracaoEmpregoRepository.buscarPorEmpregoId(configuracao.empregoId)
            if (existente != null) {
                configuracaoEmpregoRepository.atualizar(configAtualizada)
            } else {
                configuracaoEmpregoRepository.inserir(configAtualizada)
            }

            Resultado.Sucesso(configAtualizada)
        } catch (e: Exception) {
            Resultado.Erro("Erro ao salvar configuração: ${e.message}")
        }
    }
}
