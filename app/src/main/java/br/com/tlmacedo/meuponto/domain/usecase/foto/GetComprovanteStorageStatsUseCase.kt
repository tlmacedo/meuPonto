// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/usecase/foto/GetComprovanteStorageStatsUseCase.kt
package br.com.tlmacedo.meuponto.domain.usecase.foto

import br.com.tlmacedo.meuponto.util.foto.FotoStorageManager
import br.com.tlmacedo.meuponto.util.foto.StorageStats
import javax.inject.Inject

/**
 * Use case para obtenção de estatísticas de armazenamento de fotos de comprovante.
 *
 * Encapsula a consulta ao [FotoStorageManager] para total de imagens e
 * tamanho ocupado em disco, expondo os dados para a camada de apresentação
 * sem acoplamento direto com a camada de dados.
 *
 * ## Correção aplicada (12.0.0):
 * O operador `invoke` foi marcado como `suspend` pois [FotoStorageManager.getStorageStats]
 * é uma função suspensa (executa em [kotlinx.coroutines.Dispatchers.IO] internamente).
 * Sem o `suspend`, o compilador rejeita a chamada com:
 * "Suspend function 'getStorageStats' should be called only from a coroutine
 * or another suspend function."
 *
 * @param storageManager Gerenciador de armazenamento de fotos
 *
 * @author Thiago
 * @since 10.0.0
 * @updated 12.0.0 - Adicionado `suspend` no operador invoke
 */
class GetComprovanteStorageStatsUseCase @Inject constructor(
    private val storageManager: FotoStorageManager
) {
    /**
     * Retorna as estatísticas atuais de armazenamento de fotos.
     *
     * Deve ser chamado dentro de uma coroutine ou outra função suspensa.
     * O [FotoStorageManager.getStorageStats] executa em [kotlinx.coroutines.Dispatchers.IO]
     * internamente, portanto esta função é segura para chamar da main thread
     * via [androidx.lifecycle.ViewModel.viewModelScope].
     *
     * @return [StorageStats] com total de imagens, tamanho em bytes e tamanho formatado
     */
    suspend operator fun invoke(): StorageStats {
        return storageManager.getStorageStats()
    }
}