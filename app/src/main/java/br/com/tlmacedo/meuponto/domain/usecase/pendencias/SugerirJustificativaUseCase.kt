// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/usecase/pendencias/SugerirJustificativaUseCase.kt
package br.com.tlmacedo.meuponto.domain.usecase.pendencias

import br.com.tlmacedo.meuponto.domain.model.Inconsistencia
import javax.inject.Inject

/**
 * Retorna sugestões de justificativa com base nos tipos de inconsistência detectados no dia.
 *
 * As sugestões são ordenadas por relevância: as específicas para os tipos encontrados
 * aparecem primeiro, seguidas de uma opção genérica.
 *
 * @since 14.0.0
 */
class SugerirJustificativaUseCase @Inject constructor() {

    operator fun invoke(inconsistencias: Collection<Inconsistencia>): List<String> {
        val sugestoes = linkedSetOf<String>()

        inconsistencias.forEach { tipo ->
            sugestoes.addAll(sugestoesPorTipo(tipo))
        }

        sugestoes.add(SUGESTAO_OUTRO)
        return sugestoes.toList()
    }

    private fun sugestoesPorTipo(tipo: Inconsistencia): List<String> = when (tipo) {
        Inconsistencia.FALTA_SEM_JUSTIFICATIVA -> listOf(
            "Falta justificada por motivo pessoal",
            "Atestado médico",
            "Trabalho em home office não registrado",
            "Licença autorizada"
        )

        Inconsistencia.ENTRADA_SEM_SAIDA -> listOf(
            "Esquecimento de registro de saída",
            "Saída emergencial sem registro",
            "Falha no dispositivo no momento da saída"
        )

        Inconsistencia.SAIDA_SEM_ENTRADA -> listOf(
            "Esquecimento de registro de entrada",
            "Entrada registrada em outro dispositivo",
            "Falha no dispositivo no momento da entrada"
        )

        Inconsistencia.ENTRADA_DUPLICADA,
        Inconsistencia.SAIDA_DUPLICADA,
        Inconsistencia.REGISTROS_IMPARES -> listOf(
            "Registro duplicado por falha técnica",
            "Correção manual de ponto",
            "Ajuste de horário autorizado pelo gestor"
        )

        Inconsistencia.INTERVALO_ALMOCO_INSUFICIENTE -> listOf(
            "Intervalo reduzido por demanda operacional",
            "Intervalo compensado em outro momento do dia"
        )

        Inconsistencia.INTERVALO_INTERJORNADA_INSUFICIENTE -> listOf(
            "Sobreaviso ou escala especial autorizada",
            "Convocação emergencial autorizada pelo gestor"
        )

        Inconsistencia.INTERVALO_MUITO_CURTO -> listOf(
            "Intervalo reduzido por necessidade do serviço",
            "Retorno antecipado autorizado"
        )

        Inconsistencia.INTERVALO_MUITO_LONGO -> listOf(
            "Intervalo estendido por motivo pessoal",
            "Atendimento médico durante o intervalo"
        )

        Inconsistencia.JORNADA_EXCEDIDA -> listOf(
            "Jornada estendida autorizada pelo gestor",
            "Horas extras autorizadas",
            "Demanda urgente fora do horário normal"
        )

        Inconsistencia.FORA_HORARIO_ESPERADO -> listOf(
            "Horário alterado por necessidade do serviço",
            "Compensação de horário autorizada",
            "Plantão ou sobreaviso"
        )

        Inconsistencia.REGISTRO_EDITADO -> listOf(
            "Correção manual de ponto",
            "Ajuste de horário autorizado pelo gestor",
            "Correção de erro de digitação"
        )

        Inconsistencia.REGISTRO_RETROATIVO -> listOf(
            "Registro retroativo autorizado",
            "Correção de registro esquecido"
        )

        Inconsistencia.FORA_AREA_PERMITIDA -> listOf(
            "Trabalho externo autorizado",
            "Visita a cliente ou parceiro",
            "Deslocamento a serviço"
        )

        Inconsistencia.LOCALIZACAO_NAO_CAPTURADA -> listOf(
            "GPS indisponível no momento do registro",
            "Permissão de localização desativada temporariamente"
        )

        Inconsistencia.REGISTRO_NO_FUTURO -> listOf(
            "Erro de data/hora no dispositivo corrigido"
        )

        Inconsistencia.REGISTRO_MUITO_ANTIGO -> listOf(
            "Registro de período anterior ao esperado",
            "Ajuste retroativo autorizado"
        )

        Inconsistencia.COMPROVANTE_AUSENTE -> listOf(
            "Comprovante em posse do gestor",
            "Extravio de comprovante físico",
            "Problema técnico na captura da foto"
        )

        Inconsistencia.FORA_DO_GEOFENCING -> listOf(
            "Trabalho em local externo autorizado",
            "Viagem a serviço",
            "Deslocamento entre unidades"
        )
    }

    companion object {
        const val SUGESTAO_OUTRO = "Outro motivo"
    }
}
