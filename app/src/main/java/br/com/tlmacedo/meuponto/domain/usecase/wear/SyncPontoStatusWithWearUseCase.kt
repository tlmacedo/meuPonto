package br.com.tlmacedo.meuponto.domain.usecase.wear

import br.com.tlmacedo.meuponto.domain.model.Ponto
import br.com.tlmacedo.meuponto.domain.repository.PreferenciasRepository
import br.com.tlmacedo.meuponto.domain.service.wear.WearSyncService
import br.com.tlmacedo.meuponto.domain.usecase.ponto.CalcularBancoHorasUseCase
import br.com.tlmacedo.meuponto.domain.usecase.ponto.ObterResumoDiaCompletoUseCase
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class SyncPontoStatusWithWearUseCase @Inject constructor(
    private val wearSyncService: WearSyncService,
    private val preferenciasRepository: PreferenciasRepository,
    private val obterResumoDiaCompletoUseCase: ObterResumoDiaCompletoUseCase,
    private val calcularBancoHorasUseCase: CalcularBancoHorasUseCase
) {
    suspend operator fun invoke() {
        val empregoId = preferenciasRepository.obterEmpregoAtivoId() ?: return
        val hoje = LocalDate.now()

        val resumoDiaCompleto = obterResumoDiaCompletoUseCase(empregoId, hoje)
        val bancoHoras = calcularBancoHorasUseCase(empregoId, hoje).first().bancoHoras

        val pontosOrdenados = resumoDiaCompleto.pontos.sortedBy { it.dataHora }
        val ultimoPonto = pontosOrdenados.lastOrNull()
        val emExpediente = pontosOrdenados.size % 2 == 1

        wearSyncService.syncPontoStatus(
            saldoAtual = bancoHoras.formatarSaldo(),
            ultimoPonto = ultimoPonto?.horaFormatadaCompat(),
            emExpediente = emExpediente
        )
    }
}

private fun Ponto.horaFormatadaCompat(): String {
    return dataHora.format(DateTimeFormatter.ofPattern("HH:mm"))
}