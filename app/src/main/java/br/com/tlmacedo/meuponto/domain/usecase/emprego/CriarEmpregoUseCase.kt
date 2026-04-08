// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/usecase/emprego/CriarEmpregoUseCase.kt
package br.com.tlmacedo.meuponto.domain.usecase.emprego

import br.com.tlmacedo.meuponto.domain.model.ConfiguracaoEmprego
import br.com.tlmacedo.meuponto.domain.model.DiaSemana
import br.com.tlmacedo.meuponto.domain.model.Emprego
import br.com.tlmacedo.meuponto.domain.model.FotoFormato
import br.com.tlmacedo.meuponto.domain.model.HorarioDiaSemana
import br.com.tlmacedo.meuponto.domain.model.HistoricoCargo
import br.com.tlmacedo.meuponto.domain.model.TipoNsr
import br.com.tlmacedo.meuponto.domain.model.VersaoJornada
import br.com.tlmacedo.meuponto.domain.repository.ConfiguracaoEmpregoRepository
import br.com.tlmacedo.meuponto.domain.repository.EmpregoRepository
import br.com.tlmacedo.meuponto.domain.repository.HistoricoCargoRepository
import br.com.tlmacedo.meuponto.domain.repository.HorarioDiaSemanaRepository
import br.com.tlmacedo.meuponto.domain.repository.VersaoJornadaRepository
import br.com.tlmacedo.meuponto.domain.usecase.validacao.ValidarEmpregoUseCase
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject

/**
 * Caso de uso para criar um novo emprego com suas configurações completas.
 *
 * @author Thiago
 * @since 1.0.0
 * @updated 11.0.0 - Suporte total a configurações de jornada, banco e RH conforme requisitos do usuário.
 */
class CriarEmpregoUseCase @Inject constructor(
    private val empregoRepository: EmpregoRepository,
    private val configuracaoEmpregoRepository: ConfiguracaoEmpregoRepository,
    private val versaoJornadaRepository: VersaoJornadaRepository,
    private val horarioDiaSemanaRepository: HorarioDiaSemanaRepository,
    private val historicoCargoRepository: HistoricoCargoRepository,
    private val validarEmpregoUseCase: ValidarEmpregoUseCase
) {
    data class Parametros(
        val nome: String,
        val apelido: String? = null,
        val endereco: String? = null,
        val descricao: String? = null,
        val dataInicioTrabalho: LocalDate = LocalDate.now(),
        val dataTerminoTrabalho: LocalDate? = null,

        // Cargo Inicial
        val funcao: String,
        val salarioInicial: Double,
        
        // Jornada
        val cargaHorariaDiariaMinutos: Int = 480, // 8h
        val acrescimoMinutosDiasPontes: Int = 0,
        val jornadaMaximaDiariaMinutos: Int = 600, // 10h
        val intervaloMinimoMinutos: Int = 60, // 1h
        val intervaloMinimoInterjornadaMinutos: Int = 660, // 11h
        val turnoMaximoMinutos: Int = 360, // 6h
        val toleranciaIntervaloMaisMinutos: Int = 0,
        
        // RH e Banco
        val diaInicioFechamentoRH: Int = 1,
        val zerarSaldoPeriodoRH: Boolean = false,
        val primeiroDiaSemana: DiaSemana = DiaSemana.SEGUNDA,
        val bancoHorasHabilitado: Boolean = false,
        val periodoBancoSemanas: Int = 0,
        val periodoBancoMeses: Int = 0,
        val dataInicioCicloBanco: LocalDate? = null,
        val zerarBancoAoFecharCiclo: Boolean = false,
        
        // Extras
        val habilitarNsr: Boolean = false,
        val tipoNsr: TipoNsr = TipoNsr.NUMERICO,
        val habilitarLocalizacao: Boolean = false,
        val localizacaoAutomatica: Boolean = false,
        val fotoHabilitada: Boolean = false,
        val fotoObrigatoria: Boolean = false,
        val exigeJustificativaInconsistencia: Boolean = false
    )

    sealed class Resultado {
        data class Sucesso(val empregoId: Long) : Resultado()
        data class Erro(val mensagem: String) : Resultado()
        data class Validacao(val erros: List<String>) : Resultado()
    }

    suspend operator fun invoke(parametros: Parametros): Resultado {
        val emprego = Emprego(
            nome = parametros.nome.trim(),
            apelido = parametros.apelido?.trim(),
            endereco = parametros.endereco?.trim(),
            descricao = parametros.descricao?.trim(),
            dataInicioTrabalho = parametros.dataInicioTrabalho,
            dataTerminoTrabalho = parametros.dataTerminoTrabalho,
            ativo = true,
            arquivado = false,
            ordem = empregoRepository.buscarProximaOrdem()
        )

        val resultadoValidacao = validarEmpregoUseCase(emprego)
        if (resultadoValidacao is ValidarEmpregoUseCase.ResultadoValidacao.Invalido) {
            return Resultado.Validacao(resultadoValidacao.erros.map { it.mensagem })
        }

        return try {
            val agora = LocalDateTime.now()

            // 1. Criar Emprego
            val empregoId = empregoRepository.inserir(emprego)

            // 2. Criar ConfiguracaoEmprego (campos fixos do emprego)
            val configuracao = ConfiguracaoEmprego(
                empregoId = empregoId,
                habilitarNsr = parametros.habilitarNsr,
                tipoNsr = parametros.tipoNsr,
                habilitarLocalizacao = parametros.habilitarLocalizacao,
                localizacaoAutomatica = parametros.localizacaoAutomatica,
                fotoHabilitada = parametros.fotoHabilitada,
                fotoObrigatoria = parametros.fotoObrigatoria,
                fotoFormato = FotoFormato.JPEG,
                criadoEm = agora,
                atualizadoEm = agora
            )
            configuracaoEmpregoRepository.inserir(configuracao)

            // 3. Criar VersaoJornada (campos versionáveis)
            val versaoJornada = VersaoJornada(
                empregoId = empregoId,
                dataInicio = parametros.dataInicioTrabalho,
                descricao = "Configuração inicial",
                numeroVersao = 1,
                vigente = true,
                // Jornada
                cargaHorariaDiariaMinutos = parametros.cargaHorariaDiariaMinutos,
                acrescimoMinutosDiasPontes = parametros.acrescimoMinutosDiasPontes,
                jornadaMaximaDiariaMinutos = parametros.jornadaMaximaDiariaMinutos,
                intervaloMinimoInterjornadaMinutos = parametros.intervaloMinimoInterjornadaMinutos,
                turnoMaximoMinutos = parametros.turnoMaximoMinutos,
                toleranciaIntervaloMaisMinutos = parametros.toleranciaIntervaloMaisMinutos,
                // RH e Banco
                primeiroDiaSemana = parametros.primeiroDiaSemana,
                diaInicioFechamentoRH = parametros.diaInicioFechamentoRH,
                zerarSaldoPeriodoRH = parametros.zerarSaldoPeriodoRH,
                bancoHorasHabilitado = parametros.bancoHorasHabilitado,
                periodoBancoSemanas = parametros.periodoBancoSemanas,
                periodoBancoMeses = parametros.periodoBancoMeses,
                dataInicioCicloBancoAtual = parametros.dataInicioCicloBanco,
                zerarBancoAntesPeriodo = parametros.zerarBancoAoFecharCiclo,
                // Validação
                exigeJustificativaInconsistencia = parametros.exigeJustificativaInconsistencia,
                criadoEm = agora,
                atualizadoEm = agora
            )
            val versaoJornadaId = versaoJornadaRepository.inserir(versaoJornada)

            // 4. Criar Horários por Dia da Semana (Sugestão baseada nos parâmetros)
            criarHorariosIniciais(empregoId, versaoJornadaId, parametros)

            // 5. Criar Cargo Inicial
            val historicoCargo = HistoricoCargo(
                empregoId = empregoId,
                funcao = parametros.funcao.trim(),
                salarioInicial = parametros.salarioInicial,
                dataInicio = parametros.dataInicioTrabalho,
                criadoEm = agora,
                atualizadoEm = agora
            )
            historicoCargoRepository.salvar(historicoCargo)

            Resultado.Sucesso(empregoId)
        } catch (e: Exception) {
            Resultado.Erro("Erro ao criar emprego: ${e.message}")
        }
    }

    private suspend fun criarHorariosIniciais(
        empregoId: Long,
        versaoJornadaId: Long,
        parametros: Parametros
    ) {
        val cargaTotalMinutos = parametros.cargaHorariaDiariaMinutos + parametros.acrescimoMinutosDiasPontes
        
        DiaSemana.entries.forEach { dia ->
            val ehDiaUtil = dia.isDiaUtil && dia != DiaSemana.SABADO // Por padrão, Sab e Dom são folga no seu exemplo
            
            val horario = if (ehDiaUtil) {
                // Exemplo Sugerido: 08:00 - 12:30 (4h30) e 13:30 - (fim conforme carga)
                val entrada = LocalTime.of(8, 0)
                val saidaIntervalo = LocalTime.of(12, 30)
                val voltaIntervalo = saidaIntervalo.plusMinutes(parametros.intervaloMinimoMinutos.toLong())
                
                val minutosManha = java.time.Duration.between(entrada, saidaIntervalo).toMinutes()
                val minutosRestantes = cargaTotalMinutos - minutosManha
                val saidaFinal = voltaIntervalo.plusMinutes(minutosRestantes)

                HorarioDiaSemana(
                    empregoId = empregoId,
                    versaoJornadaId = versaoJornadaId,
                    diaSemana = dia,
                    ativo = true,
                    cargaHorariaMinutos = cargaTotalMinutos,
                    entradaIdeal = entrada,
                    saidaIntervaloIdeal = saidaIntervalo,
                    voltaIntervaloIdeal = voltaIntervalo,
                    saidaIdeal = saidaFinal,
                    intervaloMinimoMinutos = parametros.intervaloMinimoMinutos,
                    toleranciaIntervaloMaisMinutos = parametros.toleranciaIntervaloMaisMinutos
                )
            } else {
                HorarioDiaSemana.criarPadrao(empregoId, dia, versaoJornadaId).copy(ativo = false, cargaHorariaMinutos = 0)
            }
            horarioDiaSemanaRepository.inserir(horario)
        }
    }
}
