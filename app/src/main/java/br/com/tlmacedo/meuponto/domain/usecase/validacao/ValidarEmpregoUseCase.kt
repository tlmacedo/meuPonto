package br.com.tlmacedo.meuponto.domain.usecase.validacao

import br.com.tlmacedo.meuponto.domain.model.Emprego
import br.com.tlmacedo.meuponto.domain.repository.EmpregoRepository
import javax.inject.Inject

/**
 * Caso de uso para validar um emprego antes de persistir.
 */
class ValidarEmpregoUseCase @Inject constructor(
    private val empregoRepository: EmpregoRepository
) {
    sealed class ResultadoValidacao {
        data object Valido : ResultadoValidacao()
        data class Invalido(val erros: List<ErroValidacao>) : ResultadoValidacao() {
            val mensagem: String get() = erros.joinToString("\n") { it.mensagem }
        }
        val isValido: Boolean get() = this is Valido
    }

    sealed class ErroValidacao(val mensagem: String, val codigo: String) {
        data object NomeVazio : ErroValidacao("Nome do emprego é obrigatório", "NOME_VAZIO")
        data object NomeMuitoCurto : ErroValidacao("Nome deve ter pelo menos 2 caracteres", "NOME_CURTO")
        data object NomeMuitoLongo : ErroValidacao("Nome deve ter no máximo 100 caracteres", "NOME_LONGO")
        data class NomeDuplicado(val nome: String) : ErroValidacao("Já existe um emprego com o nome '$nome'", "NOME_DUPLICADO")
    }

    suspend operator fun invoke(emprego: Emprego): ResultadoValidacao {
        val erros = mutableListOf<ErroValidacao>()
        
        // Validação do nome
        when {
            emprego.nome.isBlank() -> erros.add(ErroValidacao.NomeVazio)
            emprego.nome.length < 2 -> erros.add(ErroValidacao.NomeMuitoCurto)
            emprego.nome.length > 100 -> erros.add(ErroValidacao.NomeMuitoLongo)
        }
        
        // Verifica duplicidade (exceto para o próprio emprego em edição)
        if (emprego.nome.isNotBlank()) {
            val existentes = empregoRepository.buscarAtivos()
            val duplicado = existentes.find { 
                it.nome.equals(emprego.nome, ignoreCase = true) && it.id != emprego.id 
            }
            if (duplicado != null) {
                erros.add(ErroValidacao.NomeDuplicado(emprego.nome))
            }
        }
        
        return if (erros.isEmpty()) {
            ResultadoValidacao.Valido
        } else {
            ResultadoValidacao.Invalido(erros)
        }
    }
}
