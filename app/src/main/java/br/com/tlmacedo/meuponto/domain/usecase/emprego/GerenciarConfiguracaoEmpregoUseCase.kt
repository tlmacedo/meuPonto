package br.com.tlmacedo.meuponto.domain.usecase.emprego

import br.com.tlmacedo.meuponto.domain.model.ConfiguracaoEmprego
import br.com.tlmacedo.meuponto.domain.model.DiaSemana
import br.com.tlmacedo.meuponto.domain.repository.ConfiguracaoEmpregoRepository
import br.com.tlmacedo.meuponto.domain.usecase.validacao.ValidarConfiguracaoEmpregoUseCase
import java.time.LocalDateTime
import javax.inject.Inject

class GerenciarConfiguracaoEmpregoUseCase @Inject constructor(
    private val configuracaoEmpregoRepository: ConfiguracaoEmpregoRepository,
    private val validarConfiguracaoUseCase: ValidarConfiguracaoEmpregoUseCase
) {
    sealed class Resultado {
        data class Sucesso(val configuracao: ConfiguracaoEmprego) : Resultado()
        data class Erro(val mensagem: String) : Resultado()
        data class Validacao(val erros: List<String>) : Resultado()
    }

    suspend fun obter(empregoId: Long): ConfiguracaoEmprego {
        return configuracaoEmpregoRepository.buscarPorEmpregoId(empregoId)
            ?: ConfiguracaoEmprego(empregoId = empregoId)
    }

    suspend fun atualizar(configuracao: ConfiguracaoEmprego): Resultado {
        val resultadoValidacao = validarConfiguracaoUseCase(configuracao)
        if (resultadoValidacao is ValidarConfiguracaoEmpregoUseCase.ResultadoValidacao.Invalido) {
            return Resultado.Validacao(resultadoValidacao.erros.map { it.mensagem })
        }

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
