// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/usecase/feriado/ObterFeriadosDaDataUseCase.kt
package br.com.tlmacedo.meuponto.domain.usecase.feriado

import br.com.tlmacedo.meuponto.domain.model.feriado.Feriado
import br.com.tlmacedo.meuponto.domain.model.feriado.RecorrenciaFeriado
import br.com.tlmacedo.meuponto.domain.model.feriado.AbrangenciaFeriado
import br.com.tlmacedo.meuponto.domain.repository.FeriadoRepository
import java.time.LocalDate
import javax.inject.Inject

/**
 * UseCase para obter feriados aplicáveis a uma data específica.
 *
 * Considera:
 * - Feriados anuais recorrentes (pelo dia/mês)
 * - Feriados únicos (pela data específica)
 * - Abrangência (global ou emprego específico)
 * - Status ativo do feriado
 *
 * @author Thiago
 * @since 3.4.0
 */
class ObterFeriadosDaDataUseCase @Inject constructor(
    private val feriadoRepository: FeriadoRepository
) {
    /**
     * Busca feriados aplicáveis para uma data e emprego.
     *
     * @param data Data para verificar
     * @param empregoId ID do emprego ativo (para filtrar feriados específicos)
     * @return Lista de feriados aplicáveis à data
     */
    suspend operator fun invoke(data: LocalDate, empregoId: Long?): List<Feriado> {
        val todosAtivos = feriadoRepository.buscarTodosAtivos()

        return todosAtivos.filter { feriado ->
            // Verificar se o feriado se aplica à data
            val aplicavelAData = when (feriado.recorrencia) {
                RecorrenciaFeriado.ANUAL -> {
                    feriado.diaMes?.let { diaMes ->
                        diaMes.dayOfMonth == data.dayOfMonth &&
                                diaMes.month == data.month
                    } ?: false
                }
                RecorrenciaFeriado.UNICO -> {
                    feriado.dataEspecifica == data
                }
            }

            // Verificar abrangência do feriado
            val aplicavelAoEmprego = when (feriado.abrangencia) {
                AbrangenciaFeriado.GLOBAL -> true
                AbrangenciaFeriado.EMPREGO_ESPECIFICO -> {
                    empregoId != null && feriado.empregoId == empregoId
                }
            }

            aplicavelAData && aplicavelAoEmprego
        }.sortedBy { it.tipo.ordinal } // Ordenar por tipo (Nacional, Estadual, Municipal, Facultativo, Ponte)
    }

    /**
     * Verifica se uma data é feriado.
     *
     * @param data Data para verificar
     * @param empregoId ID do emprego ativo
     * @return true se a data é feriado
     */
    suspend fun isFeriado(data: LocalDate, empregoId: Long?): Boolean {
        return invoke(data, empregoId).isNotEmpty()
    }

    /**
     * Retorna o feriado principal da data (para exibição resumida).
     * Prioriza: Nacional > Estadual > Municipal > Facultativo > Ponte
     *
     * @param data Data para verificar
     * @param empregoId ID do emprego ativo
     * @return Feriado principal ou null
     */
    suspend fun getFeriadoPrincipal(data: LocalDate, empregoId: Long?): Feriado? {
        return invoke(data, empregoId).firstOrNull()
    }
}
