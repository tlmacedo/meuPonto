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
 * @updated 14.0.0 - Alinhado com a nova classificação de produto (BLOQUEANTE, PENDENTE, INFO)
 */
enum class Inconsistencia(
    val descricao: String,
    val severidade: Severidade
) {
    // ========================================================================
    // BLOQUEANTES (Impede cálculos precisos ou fechamento)
    // ========================================================================

    ENTRADA_SEM_SAIDA_PASSADO(
        descricao = "Entrada sem saída em dia passado",
        severidade = Severidade.BLOQUEANTE
    ),
    REGISTROS_IMPARES_PASSADO(
        descricao = "Quantidade ímpar de registros em dia passado",
        severidade = Severidade.BLOQUEANTE
    ),
    REGISTROS_IMPARES(
        descricao = "Número ímpar de registros no dia",
        severidade = Severidade.BLOQUEANTE
    ),
    REGISTRO_NO_FUTURO(
        descricao = "Registro com data/hora no futuro",
        severidade = Severidade.BLOQUEANTE
    ),
    SAIDA_SEM_ENTRADA(
        descricao = "Saída registrada sem entrada correspondente",
        severidade = Severidade.BLOQUEANTE
    ),
    ENTRADA_DUPLICADA(
        descricao = "Entrada duplicada sem saída intermediária",
        severidade = Severidade.BLOQUEANTE
    ),
    SAIDA_DUPLICADA(
        descricao = "Saída duplicada sem entrada intermediária",
        severidade = Severidade.BLOQUEANTE
    ),
    ENTRADA_SEM_SAIDA(
        descricao = "Entrada sem saída correspondente",
        severidade = Severidade.BLOQUEANTE
    ),

    // ========================================================================
    // PENDENTE_JUSTIFICATIVA (Exige ação do usuário se ativado)
    // ========================================================================

    INTERVALO_MINIMO_INSUFICIENTE(
        descricao = "Intervalo menor que o mínimo configurado",
        severidade = Severidade.PENDENTE_JUSTIFICATIVA
    ),
    INTERVALO_ALMOCO_INSUFICIENTE(
        descricao = "Intervalo de almoço menor que o mínimo legal",
        severidade = Severidade.PENDENTE_JUSTIFICATIVA
    ),
    TURNO_EXCEDIDO_6H(
        descricao = "Turno de trabalho maior que 6 horas",
        severidade = Severidade.PENDENTE_JUSTIFICATIVA
    ),
    JORNADA_EXCEDIDA_10H(
        descricao = "Jornada diária acima de 10 horas",
        severidade = Severidade.PENDENTE_JUSTIFICATIVA
    ),
    JORNADA_EXCEDIDA(
        descricao = "Jornada diária excedeu o limite permitido",
        severidade = Severidade.PENDENTE_JUSTIFICATIVA
    ),
    DESCANSO_INTERJORNADA_INSUFICIENTE(
        descricao = "Descanso entre jornadas menor que 11 horas",
        severidade = Severidade.PENDENTE_JUSTIFICATIVA
    ),
    INTERVALO_INTERJORNADA_INSUFICIENTE(
        descricao = "Intervalo entre jornadas menor que 11 horas",
        severidade = Severidade.PENDENTE_JUSTIFICATIVA
    ),
    COMPROVANTE_AUSENTE(
        descricao = "Comprovante obrigatório não anexado",
        severidade = Severidade.PENDENTE_JUSTIFICATIVA
    ),
    FORA_DO_GEOFENCING(
        descricao = "Registro fora do raio de geofencing",
        severidade = Severidade.PENDENTE_JUSTIFICATIVA
    ),
    FORA_AREA_PERMITIDA(
        descricao = "Registro fora da área geográfica permitida",
        severidade = Severidade.PENDENTE_JUSTIFICATIVA
    ),

    // ========================================================================
    // INFO (Apenas informativo)
    // ========================================================================

    TRABALHO_EM_DIA_ESPECIAL(
        descricao = "Trabalho em feriado ou dia de descanso",
        severidade = Severidade.INFO
    ),
    SALDO_NEGATIVO(
        descricao = "Saldo do dia está negativo",
        severidade = Severidade.INFO
    ),
    JORNADA_REDUZIDA(
        descricao = "Jornada menor que 8 horas",
        severidade = Severidade.INFO
    ),
    REGISTRO_EDITADO(
        descricao = "Registro foi editado manualmente",
        severidade = Severidade.INFO
    ),
    REGISTRO_RETROATIVO(
        descricao = "Registro inserido retroativamente",
        severidade = Severidade.INFO
    ),
    FORA_HORARIO_ESPERADO(
        descricao = "Registro fora do horário esperado de trabalho",
        severidade = Severidade.INFO
    ),
    INTERVALO_MUITO_CURTO(
        descricao = "Intervalo de trabalho muito curto",
        severidade = Severidade.INFO
    ),
    INTERVALO_MUITO_LONGO(
        descricao = "Intervalo de trabalho muito longo",
        severidade = Severidade.INFO
    ),
    REGISTRO_MUITO_ANTIGO(
        descricao = "Registro anterior à data permitida",
        severidade = Severidade.INFO
    ),
    LOCALIZACAO_NAO_CAPTURADA(
        descricao = "Localização não foi capturada",
        severidade = Severidade.INFO
    ),
    FALTA_SEM_JUSTIFICATIVA(
        descricao = "Ausência de registros em dia útil",
        severidade = Severidade.INFO
    );

    /**
     * Níveis de severidade das inconsistências.
     */
    enum class Severidade {
        BLOQUEANTE,
        PENDENTE_JUSTIFICATIVA,
        INFO
    }

    val isBloqueante: Boolean get() = severidade == Severidade.BLOQUEANTE
    val isPendente: Boolean get() = severidade == Severidade.PENDENTE_JUSTIFICATIVA
    val isInfo: Boolean get() = severidade == Severidade.INFO

    companion object {
        fun bloqueantes() = entries.filter { it.isBloqueante }
    }
}
