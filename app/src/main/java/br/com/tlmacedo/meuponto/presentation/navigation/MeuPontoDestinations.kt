// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/navigation/MeuPontoDestinations.kt
// Adicionar novas rotas (se não existir, criar o arquivo)

package br.com.tlmacedo.meuponto.presentation.navigation

/**
 * Destinos de navegação do aplicativo.
 */
object MeuPontoDestinations {
    // Telas principais
    const val HOME_BASE = "home"
    const val HOME = "home?data={data}"
    const val HISTORY = "history"
    const val SETTINGS = "settings"

    // Argumentos
    const val ARG_DATA = "data"
    const val ARG_PONTO_ID = "pontoId"
    const val ARG_EMPREGO_ID = "empregoId"
    const val ARG_FERIADO_ID = "feriadoId"
    const val ARG_AUSENCIA_ID = "ausenciaId"
    const val ARG_TIPO = "tipo"
    const val ARG_VERSAO_ID = "versaoId"

    // Pontos
    const val EDIT_PONTO = "edit_ponto/{$ARG_PONTO_ID}"

    // Empregos
    const val GERENCIAR_EMPREGOS = "gerenciar_empregos"
    const val EDITAR_EMPREGO = "editar_emprego/{$ARG_EMPREGO_ID}"
    const val NOVO_EMPREGO = "editar_emprego/-1"

    // Jornada
    const val CONFIGURACAO_JORNADA = "configuracao_jornada"
    const val HORARIOS_TRABALHO = "horarios_trabalho"
    const val VERSOES_JORNADA = "versoes_jornada"
    const val EDITAR_VERSAO = "editar_versao/{$ARG_VERSAO_ID}"

    // Feriados
    const val FERIADOS = "feriados"
    const val NOVO_FERIADO = "novo_feriado"
    const val EDITAR_FERIADO = "editar_feriado/{$ARG_FERIADO_ID}"

    // Ausências
    const val AUSENCIAS = "ausencias"
    const val NOVA_AUSENCIA_BASE = "nova_ausencia"
    const val NOVA_AUSENCIA = "nova_ausencia?tipo={$ARG_TIPO}&data={$ARG_DATA}"
    const val EDITAR_AUSENCIA = "editar_ausencia/{$ARG_AUSENCIA_ID}"

    // Banco de horas
    const val AJUSTES_BANCO_HORAS = "ajustes_banco_horas"

    // Personalização
    const val MARCADORES = "marcadores"
    const val APARENCIA = "aparencia"
    const val BACKUP = "backup"

    // Sobre
    const val SOBRE = "sobre"

    // Funções auxiliares
    fun homeComData(data: String) = "home?data=$data"
    fun editPonto(pontoId: Long) = "edit_ponto/$pontoId"
    fun editarEmprego(empregoId: Long) = "editar_emprego/$empregoId"
    fun editarFeriado(feriadoId: Long) = "editar_feriado/$feriadoId"
    fun editarAusencia(ausenciaId: Long) = "editar_ausencia/$ausenciaId"
    fun editarVersao(versaoId: Long) = "editar_versao/$versaoId"
    fun novaAusencia(tipo: String? = null, data: String? = null): String {
        return buildString {
            append(NOVA_AUSENCIA_BASE)
            val params = mutableListOf<String>()
            tipo?.let { params.add("tipo=$it") }
            data?.let { params.add("data=$it") }
            if (params.isNotEmpty()) {
                append("?")
                append(params.joinToString("&"))
            }
        }
    }
}
