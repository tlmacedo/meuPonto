// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/navigation/MeuPontoDestinations.kt
package br.com.tlmacedo.meuponto.presentation.navigation

/**
 * Destinos de navegação do aplicativo.
 *
 * @updated 8.3.0 - Adicionadas rotas para Notificações e Privacidade
 * @updated 9.2.0 - Adicionada rota para Lixeira
 * @updated 11.0.0 - Adicionada rota para Auditoria
 * @updated 12.0.0 - Adicionadas rotas para Autenticação (Fase 1)
 */
object MeuPontoDestinations {
    // Autenticação (Fase 1)
    const val AUTH_GRAPH = "auth_graph"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val FORGOT_PASSWORD = "forgot_password"

    // Telas principais
    const val HOME_BASE = "home"
    const val HOME = "home?data={data}"
    const val HISTORY = "history"
    const val HISTORICO_CICLOS = "historico_ciclos"
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
    const val EMPREGO_SETTINGS = "emprego/{$ARG_EMPREGO_ID}/settings"

    // Jornada (legacy - sem emprego específico)
    const val CONFIGURACAO_JORNADA = "configuracao_jornada"
    const val HORARIOS_TRABALHO = "horarios_trabalho"
    const val VERSOES_JORNADA = "versoes_jornada"
    const val EDITAR_VERSAO = "editar_versao/{$ARG_VERSAO_ID}"

    // Jornada (por emprego)
    const val VERSOES_JORNADA_EMPREGO = "emprego/{$ARG_EMPREGO_ID}/versoes"
    const val EDITAR_VERSAO_EMPREGO = "emprego/{$ARG_EMPREGO_ID}/versoes/{$ARG_VERSAO_ID}"

    // Ajustes de saldo (por emprego)
    const val AJUSTES_SALDO_EMPREGO = "emprego/{$ARG_EMPREGO_ID}/ajustes"

    // Ausências (por emprego)
    const val AUSENCIAS_EMPREGO = "emprego/{$ARG_EMPREGO_ID}/ausencias"

    // Feriados
    const val FERIADOS = "feriados"
    const val NOVO_FERIADO = "novo_feriado"
    const val EDITAR_FERIADO = "editar_feriado/{$ARG_FERIADO_ID}"

    // Ausências (globais)
    const val AUSENCIAS = "ausencias"
    const val NOVA_AUSENCIA_BASE = "nova_ausencia"
    const val NOVA_AUSENCIA = "nova_ausencia?tipo={$ARG_TIPO}&data={$ARG_DATA}"
    const val EDITAR_AUSENCIA = "editar_ausencia/{$ARG_AUSENCIA_ID}"

    // Banco de horas (legacy)
    const val AJUSTES_BANCO_HORAS = "ajustes_banco_horas"

    // Personalização e Configurações
    const val MARCADORES = "marcadores"
    const val APARENCIA = "aparencia"
    const val NOTIFICACOES = "notificacoes"
    const val PRIVACIDADE = "privacidade"
    const val BACKUP = "backup"

    // Lixeira (Soft Delete)
    const val LIXEIRA = "lixeira"

    // Auditoria
    const val AUDITORIA = "auditoria"

    // Configurações globais
    const val CONFIGURACOES_GLOBAIS = "configuracoes_globais"

    // Sobre
    const val SOBRE = "sobre"

    // ===== Funções auxiliares =====

    fun homeComData(data: String) = "home?data=$data"
    fun editPonto(pontoId: Long) = "edit_ponto/$pontoId"
    fun editarEmprego(empregoId: Long) = "editar_emprego/$empregoId"
    fun editarFeriado(feriadoId: Long) = "editar_feriado/$feriadoId"
    fun editarAusencia(ausenciaId: Long) = "editar_ausencia/$ausenciaId"

    // Versão legacy (sem emprego)
    fun editarVersao(versaoId: Long) = "editar_versao/$versaoId"

    // Novas rotas por emprego
    fun empregoSettings(empregoId: Long) = "emprego/$empregoId/settings"
    fun versoesJornada(empregoId: Long) = "emprego/$empregoId/versoes"
    fun editarVersaoEmprego(empregoId: Long, versaoId: Long) = "emprego/$empregoId/versoes/$versaoId"
    fun ajustesSaldo(empregoId: Long) = "emprego/$empregoId/ajustes"
    fun ausenciasEmprego(empregoId: Long) = "emprego/$empregoId/ausencias"

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
