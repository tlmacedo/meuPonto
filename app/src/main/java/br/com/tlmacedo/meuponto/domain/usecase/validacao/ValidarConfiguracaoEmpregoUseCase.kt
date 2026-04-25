// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/usecase/validacao/ValidarConfiguracaoEmpregoUseCase.kt
package br.com.tlmacedo.meuponto.domain.usecase.validacao

import br.com.tlmacedo.meuponto.domain.model.ConfiguracaoEmprego
import javax.inject.Inject

/**
 * Caso de uso para validar configurações de exibição/comportamento do emprego.
 *
 * NOTA: Validações de jornada, banco de horas e período RH foram movidas
 * para ValidarVersaoJornadaUseCase.
 *
 * @author Thiago
 * @since 2.0.0
 * @updated 8.0.0 - Simplificado após migração de campos para VersaoJornada
 */
class ValidarConfiguracaoEmpregoUseCase @Inject constructor() {

    sealed class ResultadoValidacao {
        data object Valido : ResultadoValidacao()
        data class Invalido(val erros: List<ErroValidacao>) : ResultadoValidacao() {
            val mensagem: String get() = erros.joinToString("\n") { it.mensagem }
        }

        val isValido: Boolean get() = this is Valido
    }

    sealed class ErroValidacao(val mensagem: String, val codigo: String)
    // Campos migrados para VersaoJornada - validações removidas daqui

    operator fun invoke(configuracao: ConfiguracaoEmprego): ResultadoValidacao {
        // ConfiguracaoEmprego agora só contém campos de exibição/comportamento
        // que não precisam de validação complexa (são apenas booleans e enums)
        return ResultadoValidacao.Valido
    }
}
