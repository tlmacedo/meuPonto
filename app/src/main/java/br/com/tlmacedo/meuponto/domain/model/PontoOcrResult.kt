package br.com.tlmacedo.meuponto.domain.model

import java.time.LocalDate
import java.time.LocalTime

/**
 * Resultado da extração de dados de um comprovante via OCR.
 *
 * @author Thiago
 * @since 10.1.0
 */
data class PontoOcrResult(
    val nsr: String? = null,
    val data: LocalDate? = null,
    val hora: LocalTime? = null,
    val nomeTrabalhador: String? = null,
    val pis: String? = null,
    val cnpj: String? = null,
    val razaoSocial: String? = null,
    val imagemRecortadaPath: String? = null
)
