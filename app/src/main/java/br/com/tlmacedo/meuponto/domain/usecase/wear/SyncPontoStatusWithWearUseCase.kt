package br.com.tlmacedo.meuponto.domain.usecase.wear

import br.com.tlmacedo.meuponto.domain.repository.PreferenciasRepository
import br.com.tlmacedo.meuponto.domain.service.wear.WearSyncService
import br.com.tlmacedo.meuponto.domain.usecase.ponto.CalcularBancoHorasUseCase
import br.com.tlmacedo.meuponto.domain.usecase.ponto.ObterResumoDiaCompletoUseCase
import kotlinx.coroutines.flow.first
import java.time.LocalDate
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

        val resumoDia = obterResumoDiaCompletoUseCase(empregoId, hoje)
        val bancoHoras = calcularBancoHorasUseCase(empregoId, hoje).first().bancoHoras

        wearSyncService.syncPontoStatus(
            saldoAtual = bancoHoras.formatarSaldo(),
            ultimoPonto = resumoDia.resumoDia.ultimoPonto?.horaFormatada,
            emExpediente = resumoDia.resumoDia.temTurnoAberto
        )
    }
}
