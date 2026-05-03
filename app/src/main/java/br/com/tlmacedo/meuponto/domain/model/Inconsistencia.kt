// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/model/Inconsistencia.kt
package br.com.tlmacedo.meuponto.domain.model

/**
 * Enum que representa os tipos de inconsistências que podem ser detectadas
 * durante a validação de registros de ponto.
 *
 * Cada tipo de inconsistência possui uma descrição amigável para exibição
 * ao usuário e um nível de severidade que indica a gravidade do problema.
 *
 * @property descricao Descrição amigável para exibição ao usuário
 * @property severidade Nível de severidade da inconsistência
 *
 * @author Thiago
 * @since 2.0.0
 */
enum class Inconsistencia(
    val descricao: String,
    val severidade: Severidade
) {
    // ========================================================================
    // Inconsistências de Sequência
    // ========================================================================

    /**
     * Ponto de saída registrado sem entrada correspondente.
     */
    SAIDA_SEM_ENTRADA(
        descricao = "Saída registrada sem entrada correspondente",
        severidade = Severidade.ALTA
    ),

    /**
     * Duas entradas consecutivas sem saída entre elas.
     */
    ENTRADA_DUPLICADA(
        descricao = "Entrada duplicada sem saída intermediária",
        severidade = Severidade.ALTA
    ),

    /**
     * Duas saídas consecutivas sem entrada entre elas.
     */
    SAIDA_DUPLICADA(
        descricao = "Saída duplicada sem entrada intermediária",
        severidade = Severidade.ALTA
    ),

    /**
     * Dia finalizado com entrada aberta (sem saída correspondente).
     */
    ENTRADA_SEM_SAIDA(
        descricao = "Entrada sem saída correspondente",
        severidade = Severidade.ALTA
    ),

    // ========================================================================
    // Inconsistências de Horário
    // ========================================================================

    /**
     * Registro de ponto fora do horário esperado de trabalho.
     */
    FORA_HORARIO_ESPERADO(
        descricao = "Registro fora do horário esperado de trabalho",
        severidade = Severidade.MEDIA
    ),

    /**
     * Registro com data/hora no futuro.
     */
    REGISTRO_NO_FUTURO(
        descricao = "Registro com data/hora no futuro",
        severidade = Severidade.ALTA
    ),

    /**
     * Registro muito antigo (antes da data de admissão ou limite configurado).
     */
    REGISTRO_MUITO_ANTIGO(
        descricao = "Registro anterior à data permitida",
        severidade = Severidade.MEDIA
    ),

    // ========================================================================
    // Inconsistências de Intervalo
    // ========================================================================

    /**
     * Intervalo entre entrada e saída muito curto (possível erro).
     */
    INTERVALO_MUITO_CURTO(
        descricao = "Intervalo de trabalho muito curto",
        severidade = Severidade.BAIXA
    ),

    /**
     * Intervalo entre entrada e saída muito longo (possível esquecimento).
     */
    INTERVALO_MUITO_LONGO(
        descricao = "Intervalo de trabalho muito longo",
        severidade = Severidade.MEDIA
    ),

    /**
     * Intervalo de almoço menor que o mínimo legal (1 hora para jornada > 6h).
     */
    INTERVALO_ALMOCO_INSUFICIENTE(
        descricao = "Intervalo de almoço menor que o mínimo legal",
        severidade = Severidade.ALTA
    ),

    /**
     * Intervalo interjornada menor que 11 horas (CLT).
     */
    INTERVALO_INTERJORNADA_INSUFICIENTE(
        descricao = "Intervalo entre jornadas menor que 11 horas",
        severidade = Severidade.ALTA
    ),

    // ========================================================================
    // Inconsistências de Jornada
    // ========================================================================

    /**
     * Jornada diária excedeu o limite configurado.
     */
    JORNADA_EXCEDIDA(
        descricao = "Jornada diária excedeu o limite permitido",
        severidade = Severidade.MEDIA
    ),

    /**
     * Número ímpar de registros no dia (falta entrada ou saída).
     */
    REGISTROS_IMPARES(
        descricao = "Número ímpar de registros no dia",
        severidade = Severidade.ALTA
    ),

    /**
     * Falta de registros em dia útil sem justificativa.
     */
    FALTA_SEM_JUSTIFICATIVA(
        descricao = "Ausência de registros em dia útil",
        severidade = Severidade.ALTA
    ),

    // ========================================================================
    // Inconsistências de Localização
    // ========================================================================

    /**
     * Registro feito fora da área geográfica permitida.
     */
    FORA_AREA_PERMITIDA(
        descricao = "Registro fora da área geográfica permitida",
        severidade = Severidade.MEDIA
    ),

    /**
     * Localização não capturada quando era obrigatória.
     */
    LOCALIZACAO_NAO_CAPTURADA(
        descricao = "Localização não foi capturada",
        severidade = Severidade.BAIXA
    ),

    // ========================================================================
    // Inconsistências de Edição
    // ========================================================================

    /**
     * Registro foi editado manualmente.
     */
    REGISTRO_EDITADO(
        descricao = "Registro foi editado manualmente",
        severidade = Severidade.BAIXA
    ),

    /**
     * Registro inserido retroativamente.
     */
    REGISTRO_RETROATIVO(
        descricao = "Registro inserido retroativamente",
        severidade = Severidade.BAIXA
    ),

    /**
     * Comprovante obrigatório não anexado ao registro.
     */
    COMPROVANTE_AUSENTE(
        descricao = "Comprovante obrigatório não anexado",
        severidade = Severidade.MEDIA
    ),

    /**
     * Registro realizado fora do raio de geofencing configurado.
     */
    FORA_DO_GEOFENCING(
        descricao = "Fora do raio de geofencing",
        severidade = Severidade.MEDIA
    );

    /**
     * Verifica se a inconsistência é de alta severidade.
     */
    val isAlta: Boolean
        get() = severidade == Severidade.ALTA

    /**
     * Verifica se a inconsistência é de média severidade.
     */
    val isMedia: Boolean
        get() = severidade == Severidade.MEDIA

    /**
     * Verifica se a inconsistência é de baixa severidade.
     */
    val isBaixa: Boolean
        get() = severidade == Severidade.BAIXA

    /**
     * Verifica se a inconsistência bloqueia o registro.
     * Inconsistências de alta severidade impedem o salvamento.
     */
    val isBloqueante: Boolean
        get() = severidade == Severidade.ALTA

    /**
     * Níveis de severidade das inconsistências.
     */
    enum class Severidade {
        /**
         * Inconsistência grave que impede o registro.
         */
        ALTA,

        /**
         * Inconsistência que gera alerta mas permite o registro.
         */
        MEDIA,

        /**
         * Inconsistência informativa apenas.
         */
        BAIXA
    }

    companion object {
        /**
         * Retorna todas as inconsistências de uma determinada severidade.
         *
         * @param severidade Nível de severidade para filtrar
         * @return Lista de inconsistências do nível especificado
         */
        fun porSeveridade(severidade: Severidade): List<Inconsistencia> {
            return entries.filter { it.severidade == severidade }
        }

        /**
         * Retorna todas as inconsistências bloqueantes.
         *
         * @return Lista de inconsistências que impedem o registro
         */
        fun bloqueantes(): List<Inconsistencia> {
            return entries.filter { it.isBloqueante }
        }
    }
}
