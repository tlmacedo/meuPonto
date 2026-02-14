package br.com.tlmacedo.meuponto.domain.usecase.emprego

import br.com.tlmacedo.meuponto.domain.model.ConfiguracaoEmprego
import br.com.tlmacedo.meuponto.domain.model.DiaSemana
import br.com.tlmacedo.meuponto.domain.model.Emprego
import br.com.tlmacedo.meuponto.domain.repository.ConfiguracaoEmpregoRepository
import br.com.tlmacedo.meuponto.domain.repository.EmpregoRepository
import br.com.tlmacedo.meuponto.domain.usecase.validacao.ValidarEmpregoUseCase
import java.time.LocalDateTime
import javax.inject.Inject

/**
 * Caso de uso para criar um novo emprego com suas configurações padrão.
 */
class CriarEmpregoUseCase @Inject constructor(
    private val empregoRepository: EmpregoRepository,
    private val configuracaoEmpregoRepository: ConfiguracaoEmpregoRepository,
    private val validarEmpregoUseCase: ValidarEmpregoUseCase
) {
    data class Parametros(
        val nome: String,
        val descricao: String? = null
    )

    sealed class Resultado {
        data class Sucesso(val empregoId: Long) : Resultado()
        data class Erro(val mensagem: String) : Resultado()
        data class Validacao(val erros: List<String>) : Resultado()
    }

    suspend operator fun invoke(parametros: Parametros): Resultado {
        val emprego = Emprego(
            nome = parametros.nome.trim(),
            descricao = parametros.descricao?.trim(),
            ativo = true,
            arquivado = false,
            ordem = empregoRepository.buscarProximaOrdem(),
            criadoEm = LocalDateTime.now(),
            atualizadoEm = LocalDateTime.now()
        )

        // Validação
        val resultadoValidacao = validarEmpregoUseCase(emprego)
        if (resultadoValidacao is ValidarEmpregoUseCase.ResultadoValidacao.Invalido) {
            return Resultado.Validacao(resultadoValidacao.erros.map { it.mensagem })
        }

        return try {
            // Cria o emprego
            val empregoId = empregoRepository.inserir(emprego)

            // Cria configuração padrão
            val configuracao = ConfiguracaoEmprego(
                empregoId = empregoId,
                jornadaMaximaDiariaMinutos = 600,
                intervaloMinimoInterjornadaMinutos = 660,
                primeiroDiaSemana = DiaSemana.SEGUNDA,
                primeiroDiaMes = 1
            )
            configuracaoEmpregoRepository.inserir(configuracao)

            Resultado.Sucesso(empregoId)
        } catch (e: Exception) {
            Resultado.Erro("Erro ao criar emprego: ${e.message}")
        }
    }
}
