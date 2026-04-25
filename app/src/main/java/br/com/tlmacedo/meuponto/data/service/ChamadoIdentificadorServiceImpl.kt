// path: app/src/main/java/br/com/tlmacedo/meuponto/data/service/ChamadoIdentificadorServiceImpl.kt
package br.com.tlmacedo.meuponto.data.service

import br.com.tlmacedo.meuponto.domain.service.ChamadoIdentificadorService
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChamadoIdentificadorServiceImpl @Inject constructor() : ChamadoIdentificadorService {

    private val formatter = DateTimeFormatter.ofPattern("yyyyMMdd")

    override fun gerar(): String {
        val data = LocalDateTime.now().format(formatter)
        val sufixo = UUID.randomUUID().toString()
            .replace("-", "")
            .uppercase()
            .take(8)
        return "CHM-$data-$sufixo"
    }
}