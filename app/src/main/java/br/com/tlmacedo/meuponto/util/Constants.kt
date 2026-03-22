// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/util/Constants.kt
package br.com.tlmacedo.meuponto.util

/**
 * Constantes globais da aplicação MeuPonto.
 *
 * ## Organização:
 * - Configurações de jornada (valores padrão)
 * - Horários padrão de trabalho
 * - Chaves de DataStore
 *
 * ATENÇÃO: Rotas de navegação devem ser definidas exclusivamente em
 * [br.com.tlmacedo.meuponto.presentation.navigation.MeuPontoDestinations].
 * O objeto [Routes] foi removido desta classe para eliminar duplicidade.
 *
 * @author Thiago
 * @since 1.0.0
 * @updated 12.0.0 - Removido Constants.Routes (duplicava MeuPontoDestinations);
 *                   adicionado KDoc completo
 */
object Constants {

    // ========================================================================
    // CONFIGURAÇÕES PADRÃO DE JORNADA
    // ========================================================================

    /**
     * Carga horária diária padrão em minutos (8 horas = 480 min).
     */
    const val CARGA_HORARIA_DIARIA_PADRAO = 480

    /**
     * Carga horária semanal padrão em minutos (44 horas = 2640 min).
     */
    const val CARGA_HORARIA_SEMANAL_PADRAO = 2640

    /**
     * Tolerância padrão em minutos para batida de ponto.
     */
    const val TOLERANCIA_PADRAO_MINUTOS = 10

    /**
     * Duração padrão do intervalo de almoço em minutos (1 hora = 60 min).
     */
    const val INTERVALO_ALMOCO_PADRAO = 60

    // ========================================================================
    // HORÁRIOS PADRÃO DE TRABALHO
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
    // CHAVES DO DATASTORE
    // ========================================================================

    /**
     * Chaves para o DataStore de preferências.
     *
     * ATENÇÃO: Não alterar os valores das constantes após o primeiro release
     * sem criar migração de DataStore. Alterar a string de uma chave apaga
     * silenciosamente o valor salvo para o usuário.
     */
    object DataStoreKeys {
        /** Nome do arquivo de preferências */
        const val PREFERENCES_NAME = "meuponto_preferences"

        /** Carga horária diária configurada pelo usuário */
        const val CARGA_HORARIA_DIARIA = "carga_horaria_diaria"

        /** Tolerância de ponto configurada */
        const val TOLERANCIA_MINUTOS = "tolerancia_minutos"

        /** Duração do intervalo configurada */
        const val INTERVALO_ALMOCO = "intervalo_almoco"

        /** Flag de notificações habilitadas */
        const val NOTIFICACOES_ATIVAS = "notificacoes_ativas"

        /** Preferência de tema (claro/escuro/sistema) */
        const val TEMA_ESCURO = "tema_escuro"

        /** Flag de primeiro acesso — controla exibição do onboarding */
        const val PRIMEIRO_ACESSO = "primeiro_acesso"
    }

    // ========================================================================
    // CANAIS DE NOTIFICAÇÃO
    // ========================================================================

    /**
     * Identificadores dos canais de notificação do Android.
     * Os valores são imutáveis após o primeiro install — mudar o ID
     * cria um novo canal e o antigo permanece no sistema.
     */
    object NotificationChannels {
        /** Canal de alta prioridade para lembretes de ponto */
        const val CHANNEL_LEMBRETES = "meuponto_lembretes"

        /** Canal de prioridade padrão para alertas de saldo */
        const val CHANNEL_ALERTAS_SALDO = "meuponto_alertas_saldo"
    }
}