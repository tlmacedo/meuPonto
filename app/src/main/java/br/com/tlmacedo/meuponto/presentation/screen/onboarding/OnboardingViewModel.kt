package br.com.tlmacedo.meuponto.presentation.screen.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.tlmacedo.meuponto.data.local.database.MeuPontoDatabase
import br.com.tlmacedo.meuponto.data.local.database.entity.ConfiguracaoEmpregoEntity
import br.com.tlmacedo.meuponto.data.local.database.entity.EmpregoEntity
import br.com.tlmacedo.meuponto.data.local.database.entity.HorarioDiaSemanaEntity
import br.com.tlmacedo.meuponto.data.local.database.entity.VersaoJornadaEntity
import br.com.tlmacedo.meuponto.domain.model.DiaSemana
import br.com.tlmacedo.meuponto.domain.repository.PreferenciasRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

data class OnboardingUiState(
    val currentPage: Int = 0,
    val nomeEmprego: String = "",
    val diasTrabalho: Set<DiaSemana> = setOf(
        DiaSemana.SEGUNDA, DiaSemana.TERCA, DiaSemana.QUARTA, DiaSemana.QUINTA, DiaSemana.SEXTA
    ),
    val cargaHorariaDiaria: Int = 480,
    
    // Opções de Registro
    val fotoHabilitada: Boolean = true,
    val localizacaoHabilitada: Boolean = true,
    val nsrHabilitado: Boolean = false,
    
    // Info RH e Banco de Horas
    val diaFechamentoRH: Int = 1,
    val bancoHorasHabilitado: Boolean = false,
    
    // Sincronização
    val backupNuvemHabilitado: Boolean = true,
    
    val isConcluido: Boolean = false
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val preferenciasRepository: PreferenciasRepository,
    private val database: MeuPontoDatabase
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun onNomeEmpregoChange(novoNome: String) {
        _uiState.update { it.copy(nomeEmprego = novoNome) }
    }

    fun onDiaTrabalhoToggle(dia: DiaSemana) {
        _uiState.update { state ->
            val novosDias = if (state.diasTrabalho.contains(dia)) {
                state.diasTrabalho - dia
            } else {
                state.diasTrabalho + dia
            }
            state.copy(diasTrabalho = novosDias)
        }
    }

    fun onCargaHorariaChange(minutos: Int) {
        _uiState.update { it.copy(cargaHorariaDiaria = minutos) }
    }

    fun onFotoHabilitadaChange(habilitada: Boolean) {
        _uiState.update { it.copy(fotoHabilitada = habilitada) }
    }

    fun onLocalizacaoHabilitadaChange(habilitada: Boolean) {
        _uiState.update { it.copy(localizacaoHabilitada = habilitada) }
    }

    fun onNsrHabilitadoChange(habilitado: Boolean) {
        _uiState.update { it.copy(nsrHabilitado = habilitado) }
    }

    fun onDiaFechamentoRHChange(dia: Int) {
        _uiState.update { it.copy(diaFechamentoRH = dia) }
    }

    fun onBancoHorasHabilitadoChange(habilitado: Boolean) {
        _uiState.update { it.copy(bancoHorasHabilitado = habilitado) }
    }

    fun onBackupNuvemHabilitadoChange(habilitado: Boolean) {
        _uiState.update { it.copy(backupNuvemHabilitado = habilitado) }
    }

    fun nextStep() {
        if (_uiState.value.currentPage < 6) {
            _uiState.update { it.copy(currentPage = it.currentPage + 1) }
        } else {
            concluirOnboarding()
        }
    }

    fun prevStep() {
        if (_uiState.value.currentPage > 0) {
            _uiState.update { it.copy(currentPage = it.currentPage - 1) }
        }
    }

    private fun concluirOnboarding() {
        viewModelScope.launch {
            // 1. Criar o primeiro emprego no banco de dados
            val novoEmprego = EmpregoEntity(
                nome = _uiState.value.nomeEmprego,
                ativo = true,
                criadoEm = LocalDateTime.now(),
                atualizadoEm = LocalDateTime.now()
            )
            val idInserido = database.empregoDao().inserir(novoEmprego)

            // 1.1 Criar configuração padrão para o novo emprego
            val novaConfiguracao = ConfiguracaoEmpregoEntity(
                empregoId = idInserido,
                fotoHabilitada = _uiState.value.fotoHabilitada,
                habilitarLocalizacao = _uiState.value.localizacaoHabilitada,
                habilitarNsr = _uiState.value.nsrHabilitado,
                fotoBackupNuvemHabilitado = _uiState.value.backupNuvemHabilitado,
                criadoEm = LocalDateTime.now(),
                atualizadoEm = LocalDateTime.now()
            )
            database.configuracaoEmpregoDao().inserir(novaConfiguracao)

            // 1.2 Criar uma versão de jornada padrão (configurável no onboarding)
            val novaVersao = VersaoJornadaEntity(
                empregoId = idInserido,
                dataInicio = LocalDate.now().withDayOfMonth(1), // Início do mês atual
                descricao = "Jornada Inicial",
                vigente = true,
                cargaHorariaDiariaMinutos = _uiState.value.cargaHorariaDiaria,
                cargaHorariaSemanalMinutos = _uiState.value.cargaHorariaDiaria * _uiState.value.diasTrabalho.size,
                diaInicioFechamentoRH = _uiState.value.diaFechamentoRH,
                bancoHorasHabilitado = _uiState.value.bancoHorasHabilitado,
                criadoEm = LocalDateTime.now(),
                atualizadoEm = LocalDateTime.now()
            )
            val versaoId = database.versaoJornadaDao().inserir(novaVersao)

            // 1.3 Criar horários padrão para a versão baseados na carga horária escolhida
            val cargaMinutos = _uiState.value.cargaHorariaDiaria
            val entrada = LocalTime.of(8, 0)
            val saidaIntervalo = entrada.plusHours(4)
            val voltaIntervalo = saidaIntervalo.plusHours(1)
            val saida = voltaIntervalo.plusMinutes((cargaMinutos - 240).toLong())

            DiaSemana.entries.forEach { dia ->
                val isDiaTrabalho = _uiState.value.diasTrabalho.contains(dia)
                database.horarioDiaSemanaDao().inserir(
                    HorarioDiaSemanaEntity(
                        empregoId = idInserido,
                        versaoJornadaId = versaoId,
                        diaSemana = dia,
                        ativo = isDiaTrabalho,
                        cargaHorariaMinutos = if (isDiaTrabalho) cargaMinutos else 0,
                        entradaIdeal = if (isDiaTrabalho) entrada else null,
                        saidaIntervaloIdeal = if (isDiaTrabalho) saidaIntervalo else null,
                        voltaIntervaloIdeal = if (isDiaTrabalho) voltaIntervalo else null,
                        saidaIdeal = if (isDiaTrabalho) saida else null,
                        criadoEm = LocalDateTime.now(),
                        atualizadoEm = LocalDateTime.now()
                    )
                )
            }
            
            // 2. Definir este emprego como o ativo nas preferências
            preferenciasRepository.definirEmpregoAtivoId(idInserido)
            
            // 3. Marcar onboarding como concluído
            preferenciasRepository.marcarOnboardingConcluido()
            _uiState.update { it.copy(isConcluido = true) }
        }
    }
}
