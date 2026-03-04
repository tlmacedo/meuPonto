// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/usecase/emprego/CriarEmpregoUseCase.kt
package br.com.tlmacedo.meuponto.domain.usecase.emprego

import br.com.tlmacedo.meuponto.domain.model.ConfiguracaoEmprego
import br.com.tlmacedo.meuponto.domain.model.DiaSemana
import br.com.tlmacedo.meuponto.domain.model.Emprego
import br.com.tlmacedo.meuponto.domain.model.HorarioDiaSemana
import br.com.tlmacedo.meuponto.domain.model.VersaoJornada
import br.com.tlmacedo.meuponto.domain.repository.ConfiguracaoEmpregoRepository
import br.com.tlmacedo.meuponto.domain.repository.EmpregoRepository
import br.com.tlmacedo.meuponto.domain.repository.HorarioDiaSemanaRepository
import br.com.tlmacedo.meuponto.domain.repository.VersaoJornadaRepository
import br.com.tlmacedo.meuponto.domain.usecase.validacao.ValidarEmpregoUseCase
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

/**
 * Caso de uso para criar um novo emprego com suas configurações padrão.
 *
 * @author Thiago
 * @since 1.0.0
 * @updated 8.0.0 - Atualizado para criar VersaoJornada com campos de jornada/banco
 */
class CriarEmpregoUseCase @Inject constructor(
    private val empregoRepository: EmpregoRepository,
    private val configuracaoEmpregoRepository: ConfiguracaoEmpregoRepository,
    private val versaoJornadaRepository: VersaoJornadaRepository,
    private val horarioDiaSemanaRepository: HorarioDiaSemanaRepository,
    private val validarEmpregoUseCase: ValidarEmpregoUseCase
) {
    data class Parametros(
        val nome: String,
        val descricao: String? = null,
        val dataInicioTrabalho: LocalDate = LocalDate.now()
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
            dataInicioTrabalho = parametros.dataInicioTrabalho,
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
            val agora = LocalDateTime.now()

            // 1. Cria o emprego
            val empregoId = empregoRepository.inserir(emprego)

            // 2. Cria configuração de exibição/comportamento (simplificada)
            val configuracao = ConfiguracaoEmprego(
                empregoId = empregoId,
                criadoEm = agora,
                atualizadoEm = agora
            )
            configuracaoEmpregoRepository.inserir(configuracao)

            // 3. Cria versão de jornada inicial (com campos de jornada e banco)
            val versaoJornada = VersaoJornada(
                empregoId = empregoId,
                dataInicio = parametros.dataInicioTrabalho,
                descricao = "Configuração inicial",
                numeroVersao = 1,
                vigente = true,
                // Jornada
                jornadaMaximaDiariaMinutos = 600,
                intervaloMinimoInterjornadaMinutos = 660,
                turnoMaximoMinutos = 360,
                // Carga horária
                cargaHorariaDiariaMinutos = 480,
                acrescimoMinutosDiasPontes = 12,
                cargaHorariaSemanalMinutos = 2460,
                // Período
                primeiroDiaSemana = DiaSemana.SEGUNDA,
                diaInicioFechamentoRH = 1,
                // Banco de horas (desabilitado por padrão)
                bancoHorasHabilitado = false,
                criadoEm = agora,
                atualizadoEm = agora
            )
            val versaoJornadaId = versaoJornadaRepository.inserir(versaoJornada)

            // 4. Cria horários padrão para cada dia da semana
            DiaSemana.entries.forEach { diaSemana ->
                val horario = HorarioDiaSemana.criarPadrao(
                    empregoId = empregoId,
                    diaSemana = diaSemana,
                    versaoJornadaId = versaoJornadaId
                )
                horarioDiaSemanaRepository.inserir(horario)
            }

            Resultado.Sucesso(empregoId)
        } catch (e: Exception) {
            Resultado.Erro("Erro ao criar emprego: ${e.message}")
        }
    }
}
