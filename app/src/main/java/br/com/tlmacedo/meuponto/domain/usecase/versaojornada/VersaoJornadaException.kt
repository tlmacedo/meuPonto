// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/usecase/versaojornada/VersaoJornadaException.kt
package br.com.tlmacedo.meuponto.domain.usecase.versaojornada

import br.com.tlmacedo.meuponto.domain.model.VersaoJornada
import java.time.LocalDate

/**
 * Exceções específicas para operações com versões de jornada.
 *
 * @author Thiago
 * @since 4.0.0
 */
sealed class VersaoJornadaException(override val message: String) : Exception(message) {

    class EmpregoNaoEncontrado : VersaoJornadaException(
        "Nenhum emprego ativo encontrado. Configure um emprego antes de criar versões de jornada."
    )

    class VersaoNaoEncontrada : VersaoJornadaException(
        "Versão de jornada não encontrada."
    )

    class UnicaVersao : VersaoJornadaException(
        "Não é possível excluir a única versão de jornada. Crie outra versão antes de excluir esta."
    )

    data class SobreposicaoDatas(val versaoConflitante: VersaoJornada) : VersaoJornadaException(
        "O período informado conflita com a versão ${versaoConflitante.numeroVersao} " +
                "(${versaoConflitante.periodoFormatado})."
    )

    data class DataInicioInvalida(val motivo: String) : VersaoJornadaException(
        "Data de início inválida: $motivo"
    )

    data class DataFimInvalida(val motivo: String) : VersaoJornadaException(
        "Data de fim inválida: $motivo"
    )

    data class ErroInterno(override val message: String) : VersaoJornadaException(message)
}

/**
 * Verifica se há sobreposição de datas entre versões.
 *
 * @param dataInicio Data de início da nova versão/atualização
 * @param dataFim Data de fim (null = sem fim)
 * @param versoesExistentes Lista de versões a verificar
 * @param versaoIdIgnorar ID da versão a ignorar (para atualização)
 * @return VersaoJornada conflitante ou null
 */
fun verificarSobreposicao(
    dataInicio: LocalDate,
    dataFim: LocalDate?,
    versoesExistentes: List<VersaoJornada>,
    versaoIdIgnorar: Long?
): VersaoJornada? {
    return versoesExistentes
        .filter { versaoIdIgnorar == null || it.id != versaoIdIgnorar }
        .firstOrNull { versao ->
            periodosSeOverpoem(
                inicio1 = dataInicio,
                fim1 = dataFim,
                inicio2 = versao.dataInicio,
                fim2 = versao.dataFim
            )
        }
}

/**
 * Verifica se dois períodos se sobrepõem.
 */
private fun periodosSeOverpoem(
    inicio1: LocalDate,
    fim1: LocalDate?,
    inicio2: LocalDate,
    fim2: LocalDate?
): Boolean {
    // Se ambos têm fim definido
    if (fim1 != null && fim2 != null) {
        return inicio1 <= fim2 && inicio2 <= fim1
    }

    // Se apenas o primeiro tem fim
    if (fim1 != null && fim2 == null) {
        return inicio2 <= fim1
    }

    // Se apenas o segundo tem fim
    if (fim1 == null && fim2 != null) {
        return inicio1 <= fim2
    }

    // Se nenhum tem fim (ambos vigentes)
    return true
}
