package br.com.tlmacedo.meuponto.domain.usecase.relatorio

import br.com.tlmacedo.meuponto.domain.usecase.ponto.ResumoDiaCompleto
import br.com.tlmacedo.meuponto.util.helper.minutosParaHoraMinuto
import br.com.tlmacedo.meuponto.util.helper.minutosParaSaldoFormatado
import java.time.format.DateTimeFormatter
import javax.inject.Inject

/**
 * Caso de uso para exportar os dados do relatório para formato CSV.
 *
 * @author Thiago
 * @since 10.0.0
 */
class ExportarRelatorioCsvUseCase @Inject constructor() {

    private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    operator fun invoke(resumo: GerarResumoPeriodoUseCase.ResumoPeriodo): String {
        val sb = StringBuilder()

        // Cabeçalho
        sb.append("Data;Dia;Pontos;Jornada;Trabalhado;Abonado;Saldo;Ocorrência\n")

        resumo.resumos.forEach { dia ->
            sb.append(formatarLinha(dia))
            sb.append("\n")
        }

        // Rodapé de Totais
        sb.append("\n")
        sb.append("TOTAL;;;${resumo.totalEsperadoMinutos.minutosParaHoraMinuto()};")
        sb.append("${resumo.totalTrabalhadoMinutos.minutosParaHoraMinuto()};")
        sb.append("${resumo.totalAbonadoMinutos.minutosParaHoraMinuto()};")
        sb.append("${resumo.saldoPeriodoMinutos.minutosParaSaldoFormatado()};\n")

        return sb.toString()
    }

    private fun formatarLinha(dia: ResumoDiaCompleto): String {
        val data = dia.data.format(dateFormatter)
        val diaSemana = dia.data.dayOfWeek.name // Poderia ser traduzido
        val pontos = dia.pontos.joinToString(" ") { it.dataHora.toLocalTime().toString() }
        val jornada = dia.cargaHorariaEfetivaMinutos.minutosParaHoraMinuto()
        val trabalhado = dia.horasTrabalhadasMinutos.minutosParaHoraMinuto()
        val abonado = dia.tempoAbonadoMinutos.minutosParaHoraMinuto()
        val saldo = dia.saldoDiaMinutos.minutosParaSaldoFormatado()
        val ocorrencia = dia.descricaoDiaEspecial ?: ""

        return "$data;$diaSemana;$pontos;$jornada;$trabalhado;$abonado;$saldo;$ocorrencia"
    }
}
