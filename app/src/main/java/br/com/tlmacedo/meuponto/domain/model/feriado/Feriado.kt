// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/model/feriado/Feriado.kt
package br.com.tlmacedo.meuponto.domain.model.feriado

import java.time.LocalDate
import java.time.MonthDay

/**
 * Representa um feriado no sistema.
 *
 * Feriados podem ser:
 * - Globais (aplicados a todos os empregos) ou específicos
 * - Recorrentes (anuais) ou únicos
 * - De diferentes tipos (nacional, estadual, municipal, facultativo, ponte)
 *
 * @property id Identificador único do feriado
 * @property nome Nome do feriado (ex: "Natal", "Dia do Trabalhador")
 * @property tipo Tipo do feriado (nacional, estadual, municipal, etc.)
 * @property recorrencia Se o feriado se repete anualmente ou é único
 * @property abrangencia Se é global ou específico por emprego
 * @property diaMes Dia e mês do feriado (para feriados anuais fixos)
 * @property dataEspecifica Data específica (para feriados únicos ou móveis)
 * @property anoReferencia Ano de referência (para feriados únicos)
 * @property uf Unidade federativa (para feriados estaduais)
 * @property municipio Nome do município (para feriados municipais)
 * @property empregoId ID do emprego (para feriados específicos)
 * @property ativo Se o feriado está ativo
 * @property observacao Observação adicional
 * @property criadoEm Data/hora de criação
 * @property atualizadoEm Data/hora da última atualização
 *
 * @author Thiago
 * @since 3.0.0
 */
data class Feriado(
    val id: Long = 0,
    val nome: String,
    val tipo: TipoFeriado,
    val recorrencia: RecorrenciaFeriado = RecorrenciaFeriado.ANUAL,
    val abrangencia: AbrangenciaFeriado = AbrangenciaFeriado.GLOBAL,
    val diaMes: MonthDay? = null,
    val dataEspecifica: LocalDate? = null,
    val anoReferencia: Int? = null,
    val uf: String? = null,
    val municipio: String? = null,
    val empregoId: Long? = null,
    val ativo: Boolean = true,
    val observacao: String? = null,
    val criadoEm: java.time.LocalDateTime = java.time.LocalDateTime.now(),
    val atualizadoEm: java.time.LocalDateTime = java.time.LocalDateTime.now()
) {
    /**
     * Retorna a data do feriado para um ano específico.
     *
     * @param ano Ano para calcular a data
     * @return Data do feriado no ano especificado, ou null se não aplicável
     */
    fun getDataParaAno(ano: Int): LocalDate? {
        return when (recorrencia) {
            RecorrenciaFeriado.ANUAL -> diaMes?.atYear(ano)
            RecorrenciaFeriado.UNICO -> {
                if (anoReferencia == ano) dataEspecifica else null
            }
        }
    }

    /**
     * Verifica se o feriado ocorre em uma data específica.
     *
     * @param data Data a verificar
     * @return true se o feriado ocorre na data
     */
    fun ocorreEm(data: LocalDate): Boolean {
        return when (recorrencia) {
            RecorrenciaFeriado.ANUAL -> {
                diaMes?.let {
                    it.dayOfMonth == data.dayOfMonth && it.month == data.month
                } ?: false
            }
            RecorrenciaFeriado.UNICO -> dataEspecifica == data
        }
    }

    /**
     * Verifica se o feriado é aplicável a um emprego específico.
     *
     * @param empregoIdVerificar ID do emprego a verificar
     * @param ufEmprego UF do emprego (opcional)
     * @param municipioEmprego Município do emprego (opcional)
     * @return true se o feriado é aplicável ao emprego
     */
    fun aplicavelPara(
        empregoIdVerificar: Long,
        ufEmprego: String? = null,
        municipioEmprego: String? = null
    ): Boolean {
        // Feriado específico de outro emprego
        if (abrangencia == AbrangenciaFeriado.EMPREGO_ESPECIFICO && empregoId != empregoIdVerificar) {
            return false
        }

        // Verificar abrangência geográfica
        return when (tipo) {
            TipoFeriado.NACIONAL, TipoFeriado.FACULTATIVO -> true
            TipoFeriado.ESTADUAL -> uf == null || uf == ufEmprego
            TipoFeriado.MUNICIPAL -> municipio == null || municipio == municipioEmprego
            TipoFeriado.PONTE -> {
                // Ponte pode ser global ou específico
                abrangencia == AbrangenciaFeriado.GLOBAL || empregoId == empregoIdVerificar
            }
        }
    }

    /**
     * Verifica se é um feriado que representa folga efetiva.
     */
    val isFolga: Boolean
        get() = tipo in TipoFeriado.tiposFolga()

    /**
     * Verifica se é um feriado ponte (carga distribuída).
     */
    val isPonte: Boolean
        get() = tipo == TipoFeriado.PONTE

    companion object {
        /**
         * Cria um feriado nacional recorrente.
         */
        fun criarNacional(
            nome: String,
            dia: Int,
            mes: Int,
            observacao: String? = null
        ): Feriado = Feriado(
            nome = nome,
            tipo = TipoFeriado.NACIONAL,
            recorrencia = RecorrenciaFeriado.ANUAL,
            abrangencia = AbrangenciaFeriado.GLOBAL,
            diaMes = MonthDay.of(mes, dia),
            observacao = observacao
        )

        /**
         * Cria um feriado ponte para um ano específico.
         */
        fun criarPonte(
            nome: String,
            data: LocalDate,
            empregoId: Long? = null,
            observacao: String? = null
        ): Feriado = Feriado(
            nome = nome,
            tipo = TipoFeriado.PONTE,
            recorrencia = RecorrenciaFeriado.UNICO,
            abrangencia = if (empregoId != null) AbrangenciaFeriado.EMPREGO_ESPECIFICO else AbrangenciaFeriado.GLOBAL,
            dataEspecifica = data,
            anoReferencia = data.year,
            empregoId = empregoId,
            observacao = observacao
        )
    }
}
