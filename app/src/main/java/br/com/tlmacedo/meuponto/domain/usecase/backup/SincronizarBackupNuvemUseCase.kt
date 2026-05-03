package br.com.tlmacedo.meuponto.domain.usecase.backup

import br.com.tlmacedo.meuponto.domain.repository.CloudBackupRepository
import javax.inject.Inject

/**
 * Sincroniza o estado do backup na nuvem com o banco de dados local.
 * Garante que a data do último backup esteja correta mesmo após reinstalação.
 */
class SincronizarBackupNuvemUseCase @Inject constructor(
    private val cloudBackupRepository: CloudBackupRepository
) {
    suspend operator fun invoke(): Result<Long> {
        return cloudBackupRepository.sincronizarStatusUltimoBackup()
    }
}
