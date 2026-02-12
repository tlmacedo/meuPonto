package br.com.tlmacedo.meuponto.util

/**
 * Constantes globais da aplicação.
 *
 * @author Thiago
 * @since 1.0.0
 */
object Constants {

    // ========================================================================
    // Configurações padrão de jornada
    // ========================================================================

    /**
     * Carga horária diária padrão em minutos (8 horas).
     */
    const val CARGA_HORARIA_DIARIA_PADRAO = 480

    /**
     * Carga horária semanal padrão em minutos (44 horas).
     */
    const val CARGA_HORARIA_SEMANAL_PADRAO = 2640

    /**
     * Tolerância padrão em minutos para batida de ponto.
     */
    const val TOLERANCIA_PADRAO_MINUTOS = 10

    /**
     * Duração padrão do intervalo de almoço em minutos.
     */
    const val INTERVALO_ALMOCO_PADRAO = 60

    // ========================================================================
    // Horários padrão
    // ========================================================================

    /**
     * Hora padrão de entrada (08:00).
     */
    const val HORA_ENTRADA_PADRAO = 8

    /**
     * Hora padrão de saída para almoço (12:00).
     */
    const val HORA_SAIDA_ALMOCO_PADRAO = 12

    /**
     * Hora padrão de retorno do almoço (13:00).
     */
    const val HORA_RETORNO_ALMOCO_PADRAO = 13

    /**
     * Hora padrão de saída (17:00).
     */
    const val HORA_SAIDA_PADRAO = 17

    // ========================================================================
    // Navegação
    // ========================================================================

    /**
     * Rotas de navegação do app.
     */
    object Routes {
        const val HOME = "home"
        const val HISTORICO = "historico"
        const val CONFIGURACOES = "configuracoes"
        const val RELATORIOS = "relatorios"
        const val EDITAR_PONTO = "editar_ponto/{pontoId}"
        const val LANCAMENTO = "lancamento"
    }

    // ========================================================================
    // DataStore Keys
    // ========================================================================

    /**
     * Chaves para DataStore de preferências.
     */
    object DataStoreKeys {
        const val PREFERENCES_NAME = "meuponto_preferences"
        const val CARGA_HORARIA_DIARIA = "carga_horaria_diaria"
        const val TOLERANCIA_MINUTOS = "tolerancia_minutos"
        const val INTERVALO_ALMOCO = "intervalo_almoco"
        const val NOTIFICACOES_ATIVAS = "notificacoes_ativas"
        const val TEMA_ESCURO = "tema_escuro"
        const val PRIMEIRO_ACESSO = "primeiro_acesso"
    }
}
