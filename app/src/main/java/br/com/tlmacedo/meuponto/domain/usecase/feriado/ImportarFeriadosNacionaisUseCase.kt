// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/usecase/feriado/ImportarFeriadosNacionaisUseCase.kt
package br.com.tlmacedo.meuponto.domain.usecase.feriado

import br.com.tlmacedo.meuponto.data.remote.api.BrasilApiService
import br.com.tlmacedo.meuponto.domain.model.feriado.Feriado
import br.com.tlmacedo.meuponto.domain.model.feriado.RecorrenciaFeriado
import br.com.tlmacedo.meuponto.domain.repository.FeriadoRepository
import java.time.LocalDate
import javax.inject.Inject

/**
 * Caso de uso para importar feriados nacionais da Brasil API.
 *
 * Importa feriados do ano atual e do próximo ano, evitando duplicatas.
 * Feriados fixos são salvos como recorrentes (ANUAL).
 * Feriados móveis são salvos como únicos (UNICO) para cada ano.
 *
 * @author Thiago
 * @since 3.0.0
 */
class ImportarFeriadosNacionaisUseCase @Inject constructor(
    private val brasilApiService: BrasilApiService,
    private val feriadoRepository: FeriadoRepository
) {

    /**
     * Resultado da importação.
     */
    sealed class Resultado {
        data class Sucesso(
            val feriadosImportados: Int,
            val feriadosIgnorados: Int,
            val anos: List<Int>
        ) : Resultado()

        data class Erro(val mensagem: String, val exception: Throwable? = null) : Resultado()
        data object SemConexao : Resultado()
    }

    /**
     * Importa feriados nacionais do ano atual e próximo.
     *
     * @param forcarReimportacao Se true, reimporta mesmo se já existirem feriados
     * @return Resultado da importação
     */
    suspend operator fun invoke(forcarReimportacao: Boolean = false): Resultado {
        return try {
            // Verifica se já existem feriados importados
            if (!forcarReimportacao && feriadoRepository.existemFeriadosNacionaisImportados()) {
                return Resultado.Sucesso(
                    feriadosImportados = 0,
                    feriadosIgnorados = 0,
                    anos = emptyList()
                )
            }

            val anoAtual = LocalDate.now().year
            val anosParaImportar = listOf(anoAtual, anoAtual + 1)

            var totalImportados = 0
            var totalIgnorados = 0
            val anosProcessados = mutableListOf<Int>()

            for (ano in anosParaImportar) {
                val resultado = importarAno(ano)
                totalImportados += resultado.first
                totalIgnorados += resultado.second
                if (resultado.first > 0 || resultado.second > 0) {
                    anosProcessados.add(ano)
                }
            }

            Resultado.Sucesso(
                feriadosImportados = totalImportados,
                feriadosIgnorados = totalIgnorados,
                anos = anosProcessados
            )
        } catch (e: java.net.UnknownHostException) {
            Resultado.SemConexao
        } catch (e: java.net.SocketTimeoutException) {
            Resultado.SemConexao
        } catch (e: Exception) {
            Resultado.Erro("Erro ao importar feriados: ${e.message}", e)
        }
    }

    /**
     * Importa feriados de um ano específico.
     *
     * @param ano Ano para importar
     * @return Pair com (importados, ignorados)
     */
    suspend fun importarAno(ano: Int): Pair<Int, Int> {
        val response = brasilApiService.buscarFeriadosNacionais(ano)

        if (!response.isSuccessful) {
            throw Exception("Erro na API: ${response.code()} - ${response.message()}")
        }

        val feriadosDto = response.body() ?: emptyList()
        val feriadosExistentes = feriadoRepository.buscarPorAno(ano)

        var importados = 0
        var ignorados = 0

        for (dto in feriadosDto) {
            val feriado = dto.toDomain(ano)

            // Verifica se já existe um feriado com o mesmo nome
            val jaExiste = feriadosExistentes.any { existente ->
                existente.nome.equals(feriado.nome, ignoreCase = true) &&
                        (existente.recorrencia == RecorrenciaFeriado.ANUAL ||
                                existente.anoReferencia == ano)
            }

            if (jaExiste) {
                ignorados++
            } else {
                feriadoRepository.inserir(feriado)
                importados++
            }
        }

        return Pair(importados, ignorados)
    }

    /**
     * Atualiza feriados móveis para um ano específico.
     * Útil para atualizar Carnaval, Páscoa, etc. que mudam de data.
     *
     * @param ano Ano para atualizar
     * @return Quantidade de feriados atualizados
     */
    suspend fun atualizarFeriadosMoveis(ano: Int): Int {
        val response = brasilApiService.buscarFeriadosNacionais(ano)

        if (!response.isSuccessful) return 0

        val feriadosDto = response.body() ?: return 0
        var atualizados = 0

        for (dto in feriadosDto) {
            val feriado = dto.toDomain(ano)

            // Só atualiza feriados móveis (únicos)
            if (feriado.recorrencia == RecorrenciaFeriado.UNICO) {
                // Remove o antigo se existir
                val existentes = feriadoRepository.buscarPorAno(ano)
                existentes.filter {
                    it.nome.equals(feriado.nome, ignoreCase = true) &&
                            it.anoReferencia == ano
                }.forEach {
                    feriadoRepository.excluir(it)
                }

                // Insere o novo
                feriadoRepository.inserir(feriado)
                atualizados++
            }
        }

        return atualizados
    }
}
