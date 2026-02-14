// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/usecase/validacao/ValidarRegistroPontoUseCase.kt
package br.com.tlmacedo.meuponto.domain.usecase.validacao

import br.com.tlmacedo.meuponto.domain.model.Ponto
import br.com.tlmacedo.meuponto.domain.model.TipoPonto
import br.com.tlmacedo.meuponto.domain.repository.ConfiguracaoEmpregoRepository
import br.com.tlmacedo.meuponto.domain.repository.PontoRepository
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import javax.inject.Inject

/**
 * Caso de uso para validar um registro de ponto antes de persistir.
 */
class ValidarRegistroPontoUseCase @Inject constructor(
    private val pontoRepository: PontoRepository,
    private val configuracaoRepository: ConfiguracaoEmpregoRepository
) {
    sealed class ResultadoValidacao {
        data object Valido : ResultadoValidacao()
        
        data class Invalido(
            val erros: List<ErroValidacao>
        ) : ResultadoValidacao() {
            val primeiroErro: ErroValidacao get() = erros.first()
            val mensagem: String get() = erros.joinToString("\n") { it.mensagem }
        }

        val isValido: Boolean get() = this is Valido
    }

    sealed class ErroValidacao(val mensagem: String, val codigo: String) {
        data object SequenciaIncorreta : ErroValidacao(
            "Sequência incorreta: esperado outro tipo de registro",
            "SEQUENCIA_INCORRETA"
        )
        
        data class IntervaloMinimo(val minutos: Int) : ErroValidacao(
            "Intervalo mínimo de $minutos minutos não respeitado",
            "INTERVALO_MINIMO"
        )
        
        data class JornadaMaximaExcedida(val maxMinutos: Int) : ErroValidacao(
            "Jornada máxima de ${maxMinutos / 60}h seria excedida",
            "JORNADA_MAXIMA"
        )
        
        data object RegistroDuplicado : ErroValidacao(
            "Já existe um registro neste horário",
            "REGISTRO_DUPLICADO"
        )
        
        data object HorarioFuturo : ErroValidacao(
            "Não é permitido registrar ponto em horário futuro",
            "HORARIO_FUTURO"
        )
        
        data object DataFutura : ErroValidacao(
            "Não é permitido registrar ponto em data futura",
            "DATA_FUTURA"
        )
        
        data object PeriodoFechado : ErroValidacao(
            "Não é possível registrar ponto em período já fechado",
            "PERIODO_FECHADO"
        )
        
        data class LimiteRegistrosDia(val max: Int) : ErroValidacao(
            "Limite máximo de $max registros por dia atingido",
            "LIMITE_REGISTROS"
        )
    }

    suspend operator fun invoke(
        empregoId: Long,
        data: LocalDate,
        hora: LocalTime,
        tipo: TipoPonto,
        ignorarHorarioFuturo: Boolean = false
    ): ResultadoValidacao {
        val erros = mutableListOf<ErroValidacao>()
        val configuracao = configuracaoRepository.buscarPorEmpregoId(empregoId)

        val hoje = LocalDate.now()
        if (data.isAfter(hoje)) {
            erros.add(ErroValidacao.DataFutura)
            return ResultadoValidacao.Invalido(erros)
        }

        if (!ignorarHorarioFuturo && data == hoje && hora.isAfter(LocalTime.now())) {
            erros.add(ErroValidacao.HorarioFuturo)
        }

        val registrosDoDia = pontoRepository.buscarPorEmpregoEData(empregoId, data)

        val maxRegistros = TipoPonto.MAX_PONTOS
        if (registrosDoDia.size >= maxRegistros) {
            erros.add(ErroValidacao.LimiteRegistrosDia(maxRegistros))
        }

        if (registrosDoDia.any { it.hora == hora }) {
            erros.add(ErroValidacao.RegistroDuplicado)
        }

        val erroSequencia = validarSequencia(registrosDoDia, tipo)
        if (erroSequencia != null) {
            erros.add(erroSequencia)
        }

        val intervaloMinimo = INTERVALO_MINIMO_PADRAO
        val erroIntervalo = validarIntervaloMinimo(registrosDoDia, hora, intervaloMinimo)
        if (erroIntervalo != null) {
            erros.add(erroIntervalo)
        }

        val jornadaMaxima = configuracao?.jornadaMaximaDiariaMinutos ?: JORNADA_MAXIMA_PADRAO
        val erroJornada = validarJornadaMaxima(registrosDoDia, hora, tipo, jornadaMaxima)
        if (erroJornada != null) {
            erros.add(erroJornada)
        }

        return if (erros.isEmpty()) {
            ResultadoValidacao.Valido
        } else {
            ResultadoValidacao.Invalido(erros)
        }
    }

    private fun validarSequencia(
        registrosExistentes: List<Ponto>,
        novoTipo: TipoPonto
    ): ErroValidacao? {
        if (registrosExistentes.isEmpty()) {
            return if (novoTipo != TipoPonto.ENTRADA) {
                ErroValidacao.SequenciaIncorreta
            } else null
        }

        val ultimoRegistro = registrosExistentes.maxByOrNull { it.hora }
            ?: return null

        val tipoEsperado = if (ultimoRegistro.tipo == TipoPonto.ENTRADA) {
            TipoPonto.SAIDA
        } else {
            TipoPonto.ENTRADA
        }

        return if (novoTipo != tipoEsperado) {
            ErroValidacao.SequenciaIncorreta
        } else null
    }

    private fun validarIntervaloMinimo(
        registrosExistentes: List<Ponto>,
        novaHora: LocalTime,
        intervaloMinimo: Int
    ): ErroValidacao? {
        if (registrosExistentes.isEmpty() || intervaloMinimo <= 0) {
            return null
        }

        val registroMaisProximo = registrosExistentes.minByOrNull { registro ->
            kotlin.math.abs(ChronoUnit.MINUTES.between(registro.hora, novaHora))
        } ?: return null

        val diferenca = kotlin.math.abs(
            ChronoUnit.MINUTES.between(registroMaisProximo.hora, novaHora)
        )

        return if (diferenca < intervaloMinimo) {
            ErroValidacao.IntervaloMinimo(intervaloMinimo)
        } else null
    }

    private fun validarJornadaMaxima(
        registrosExistentes: List<Ponto>,
        novaHora: LocalTime,
        novoTipo: TipoPonto,
        jornadaMaxima: Int
    ): ErroValidacao? {
        if (registrosExistentes.isEmpty() || novoTipo == TipoPonto.ENTRADA) {
            return null
        }

        var totalMinutos = 0L
        val registrosOrdenados = registrosExistentes.sortedBy { it.hora }
        
        var i = 0
        while (i < registrosOrdenados.size - 1) {
            val entrada = registrosOrdenados[i]
            val saida = registrosOrdenados[i + 1]
            
            if (entrada.tipo == TipoPonto.ENTRADA && saida.tipo == TipoPonto.SAIDA) {
                totalMinutos += ChronoUnit.MINUTES.between(entrada.hora, saida.hora)
            }
            i += 2
        }

        val ultimoRegistro = registrosOrdenados.lastOrNull()
        if (ultimoRegistro?.tipo == TipoPonto.ENTRADA) {
            totalMinutos += ChronoUnit.MINUTES.between(ultimoRegistro.hora, novaHora)
        }

        return if (totalMinutos > jornadaMaxima) {
            ErroValidacao.JornadaMaximaExcedida(jornadaMaxima)
        } else null
    }

    companion object {
        const val INTERVALO_MINIMO_PADRAO = 1
        const val JORNADA_MAXIMA_PADRAO = 600
    }
}
