// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/navigation/MeuPontoDestinations.kt
package br.com.tlmacedo.meuponto.presentation.navigation

/**
 * Constantes de destinos de navegação do aplicativo MeuPonto.
 *
 * @author Thiago
 * @since 1.0.0
 * @updated 3.3.0 - Adicionado suporte a navegação com data para Home
 * @updated 3.4.0 - Adicionado módulo de Feriados
 */
object MeuPontoDestinations {

    // === TELAS PRINCIPAIS ===

    /** Tela inicial com registro de ponto */
    const val HOME = "home?data={data}"

    /** Rota base da Home (sem parâmetros) */
    const val HOME_BASE = "home"

    /** Tela de histórico de pontos */
    const val HISTORY = "history"

    /** Tela principal de configurações */
    const val SETTINGS = "settings"

    /** Tela de edição de ponto */
    const val EDIT_PONTO = "edit_ponto/{pontoId}"

    // === CONFIGURAÇÕES - SUB-TELAS ===

    /** Tela de gerenciamento de empregos */
    const val GERENCIAR_EMPREGOS = "settings/empregos"

    /** Tela de edição/criação de emprego */
    const val EDITAR_EMPREGO = "settings/empregos/{empregoId}"

    /** Tela de configuração de jornada */
    const val CONFIGURACAO_JORNADA = "settings/jornada"

    /** Tela de horários por dia da semana */
    const val HORARIOS_TRABALHO = "settings/horarios"

    /** Tela de ajustes de banco de horas */
    const val AJUSTES_BANCO_HORAS = "settings/banco-horas"

    /** Tela de gerenciamento de marcadores */
    const val MARCADORES = "settings/marcadores"

    /** Tela sobre o aplicativo */
    const val SOBRE = "settings/sobre"

    // === FERIADOS ===

    /** Tela de listagem de feriados */
    const val FERIADOS = "settings/feriados"

    /** Tela de edição/criação de feriado */
    const val EDITAR_FERIADO = "settings/feriados/editar/{feriadoId}"

    /** Tela de criação de novo feriado (sem ID) */
    const val NOVO_FERIADO = "settings/feriados/editar"

    // === ARGUMENTOS DE NAVEGAÇÃO ===

    /** Argumento para ID do ponto */
    const val ARG_PONTO_ID = "pontoId"

    /** Argumento para ID do emprego */
    const val ARG_EMPREGO_ID = "empregoId"

    /** Argumento para data selecionada (formato: yyyy-MM-dd) */
    const val ARG_DATA = "data"

    /** Argumento para ID do feriado */
    const val ARG_FERIADO_ID = "feriadoId"

    // === FUNÇÕES DE CRIAÇÃO DE ROTAS ===

    /**
     * Cria a rota para edição de ponto.
     *
     * @param pontoId ID do ponto a ser editado
     * @return Rota formatada com o ID
     */
    fun editPonto(pontoId: Long): String = "edit_ponto/$pontoId"

    /**
     * Cria a rota para edição/criação de emprego.
     *
     * @param empregoId ID do emprego a ser editado (-1 para novo)
     * @return Rota formatada com o ID
     */
    fun editarEmprego(empregoId: Long = -1L): String = "settings/empregos/$empregoId"

    /**
     * Cria a rota para Home com uma data específica.
     *
     * @param data Data a ser selecionada na Home (formato ISO: yyyy-MM-dd)
     * @return Rota formatada com a data
     */
    fun homeComData(data: String): String = "home?data=$data"

    /**
     * Cria a rota para edição de feriado.
     *
     * @param feriadoId ID do feriado a ser editado
     * @return Rota formatada com o ID
     */
    fun editarFeriado(feriadoId: Long): String = "settings/feriados/editar/$feriadoId"
}
