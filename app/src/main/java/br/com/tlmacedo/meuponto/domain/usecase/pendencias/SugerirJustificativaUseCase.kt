// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/usecase/pendencias/SugerirJustificativaUseCase.kt
package br.com.tlmacedo.meuponto.domain.usecase.pendencias

import br.com.tlmacedo.meuponto.domain.model.Inconsistencia
import javax.inject.Inject

/**
 * Retorna sugestões de justificativa com base nos tipos de inconsistência detectados no dia.
 *
 * @author Thiago
 * @since 14.0.0
 */
class SugerirJustificativaUseCase @Inject constructor() {

    operator fun invoke(inconsistencias: Collection<Inconsistencia>): List<String> {
        val sugestoes = linkedSetOf<String>()

        inconsistencias.forEach { tipo ->
            sugestoes.addAll(sugestoesPorTipo(tipo))
        }

        if (sugestoes.isEmpty()) {
            sugestoes.add("Correção manual de registro")
        }

        sugestoes.add(SUGESTAO_OUTRO)
        return sugestoes.toList()
    }

    private fun sugestoesPorTipo(tipo: Inconsistencia): List<String> = when (tipo) {
        Inconsistencia.ENTRADA_SEM_SAIDA_PASSADO,
        Inconsistencia.REGISTROS_IMPARES_PASSADO -> listOf(
            "Esquecimento de registro",
            "Saída antecipada por emergência",
            "Falha técnica no dispositivo"
        )

        Inconsistencia.INTERVALO_MINIMO_INSUFICIENTE -> listOf(
            "Intervalo reduzido por demanda operacional",
            "Retorno antecipado para reunião",
            "Necessidade urgente de atendimento"
        )

        Inconsistencia.TURNO_EXCEDIDO_6H -> listOf(
            "Finalização de tarefa crítica",
            "Cobertura de escala",
            "Atraso em intervalo por demanda"
        )

        Inconsistencia.JORNADA_EXCEDIDA_10H -> listOf(
            "Trabalho extra autorizado",
            "Demanda excepcional de projeto",
            "Plantão estendido"
        )

        Inconsistencia.DESCANSO_INTERJORNADA_INSUFICIENTE -> listOf(
            "Convocação emergencial",
            "Troca de turno autorizada",
            "Escala especial de trabalho"
        )

        Inconsistencia.COMPROVANTE_AUSENTE -> listOf(
            "Extravio de comprovante físico",
            "Problema na câmera do dispositivo",
            "Comprovante em posse da gerência"
        )

        Inconsistencia.FORA_DO_GEOFENCING -> listOf(
            "Trabalho externo autorizado",
            "Visita a cliente/parceiro",
            "Deslocamento entre unidades"
        )

        Inconsistencia.TRABALHO_EM_DIA_ESPECIAL -> listOf(
            "Trabalho em feriado autorizado",
            "Escala de folga alterada",
            "Compensação de horas"
        )

        Inconsistencia.SALDO_NEGATIVO -> listOf(
            "Atraso compensado em outro dia",
            "Saída antecipada autorizada",
            "Problema pessoal"
        )

        Inconsistencia.JORNADA_REDUZIDA -> listOf(
            "Liberação antecipada pela chefia",
            "Compensação de banco de horas",
            "Falta parcial justificada"
        )

        Inconsistencia.REGISTRO_EDITADO -> listOf(
            "Correção de erro de digitação",
            "Ajuste conforme comprovante",
            "Correção manual de horário"
        )

        Inconsistencia.REGISTRO_RETROATIVO -> listOf(
            "Registro de ponto esquecido",
            "Ajuste retroativo autorizado"
        )

        Inconsistencia.REGISTRO_NO_FUTURO -> listOf(
            "Correção de fuso horário/data do aparelho"
        )

        Inconsistencia.REGISTROS_IMPARES,
        Inconsistencia.SAIDA_SEM_ENTRADA,
        Inconsistencia.ENTRADA_DUPLICADA,
        Inconsistencia.SAIDA_DUPLICADA,
        Inconsistencia.ENTRADA_SEM_SAIDA -> listOf(
            "Erro de registro corrigido manualmente",
            "Falha no sistema de ponto",
            "Ajuste autorizado pelo gestor"
        )

        Inconsistencia.INTERVALO_ALMOCO_INSUFICIENTE,
        Inconsistencia.INTERVALO_INTERJORNADA_INSUFICIENTE -> listOf(
            "Necessidade de serviço",
            "Escala de trabalho alterada",
            "Retorno antecipado autorizado"
        )

        Inconsistencia.JORNADA_EXCEDIDA -> listOf(
            "Demanda excessiva de trabalho",
            "Finalização de tarefa urgente",
            "Hora extra autorizada"
        )

        Inconsistencia.FORA_AREA_PERMITIDA,
        Inconsistencia.FORA_HORARIO_ESPERADO,
        Inconsistencia.INTERVALO_MUITO_CURTO,
        Inconsistencia.INTERVALO_MUITO_LONGO,
        Inconsistencia.REGISTRO_MUITO_ANTIGO,
        Inconsistencia.LOCALIZACAO_NAO_CAPTURADA,
        Inconsistencia.FALTA_SEM_JUSTIFICATIVA -> listOf(
            "Ajuste de registro para conformidade",
            "Correção manual",
            "Informação adicional anexada"
        )
    }

    companion object {
        const val SUGESTAO_OUTRO = "Outro motivo"
    }
}
