// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/settings/global/GlobalSettingsViewModel.kt
package br.com.tlmacedo.meuponto.presentation.screen.settings.global

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.tlmacedo.meuponto.data.service.GeofenceManager
import br.com.tlmacedo.meuponto.domain.model.PreferenciasGlobais.FormatoData
import br.com.tlmacedo.meuponto.domain.model.PreferenciasGlobais.FormatoHora
import br.com.tlmacedo.meuponto.domain.model.PreferenciasGlobais.TemaEscuro
import br.com.tlmacedo.meuponto.domain.usecase.preferencias.ObterPreferenciasGlobaisUseCase
import br.com.tlmacedo.meuponto.domain.usecase.preferencias.SalvarPreferenciasGlobaisUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.DayOfWeek
import javax.inject.Inject

/**
 * ViewModel da tela de configurações globais.
 *
 * @author Thiago
 * @since 8.1.0
 */
@HiltViewModel
class GlobalSettingsViewModel @Inject constructor(
    private val obterPreferencias: ObterPreferenciasGlobaisUseCase,
    private val salvarPreferencias: SalvarPreferenciasGlobaisUseCase,
    private val geofenceManager: GeofenceManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(GlobalSettingsUiState())
    val uiState: StateFlow<GlobalSettingsUiState> = _uiState.asStateFlow()

    private val _eventos = MutableSharedFlow<GlobalSettingsEvent>()
    val eventos: SharedFlow<GlobalSettingsEvent> = _eventos.asSharedFlow()

    init {
        carregarPreferencias()
    }

    private fun carregarPreferencias() {
        viewModelScope.launch {
            obterPreferencias()
                .catch { e ->
                    Timber.e(e, "Erro ao carregar preferências")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            mensagemErro = "Erro ao carregar configurações"
                        )
                    }
                }
                .collect { prefs ->
                    _uiState.update { it.copy(isLoading = false, preferencias = prefs) }
                }
        }
    }

    fun onAction(action: GlobalSettingsAction) {
        when (action) {
            is GlobalSettingsAction.Voltar -> emitirEvento(GlobalSettingsEvent.Voltar)
            is GlobalSettingsAction.AbrirDialog -> abrirDialog(action.tipo)
            is GlobalSettingsAction.FecharDialog -> fecharDialog()
            is GlobalSettingsAction.AlterarTema -> alterarTema(action.tema)
            is GlobalSettingsAction.AlterarCoresSistema -> alterarCoresSistema(action.usar)
            is GlobalSettingsAction.AlterarFormatoData -> alterarFormatoData(action.formato)
            is GlobalSettingsAction.AlterarFormatoHora -> alterarFormatoHora(action.formato)
            is GlobalSettingsAction.AlterarPrimeiroDiaSemana -> alterarPrimeiroDia(action.dia)
            is GlobalSettingsAction.AlterarLembretePonto -> alterarLembretePonto(action.ativo)
            is GlobalSettingsAction.AlterarAlertaFeriado -> alterarAlertaFeriado(action.ativo)
            is GlobalSettingsAction.AlterarAlertaBancoHoras -> alterarAlertaBancoHoras(action.ativo)
            is GlobalSettingsAction.AlterarAntecedenciaFeriado -> alterarAntecedencia(action.dias)
            is GlobalSettingsAction.AlterarLocalizacaoPadrao -> alterarLocalizacao(
                action.nome,
                action.latitude,
                action.longitude
            )

            is GlobalSettingsAction.AlterarRaioGeofencing -> alterarRaioGeofencing(action.metros)
            is GlobalSettingsAction.AlterarRegistroAutomatico -> alterarRegistroAutomatico(action.ativo)
            is GlobalSettingsAction.AlterarBackupAutomatico -> alterarBackupAutomatico(action.ativo)
            is GlobalSettingsAction.LimparMensagem -> limparMensagens()
        }
    }

    private fun abrirDialog(tipo: DialogTipo) {
        _uiState.update { it.copy(dialogAtivo = tipo) }
    }

    private fun fecharDialog() {
        _uiState.update { it.copy(dialogAtivo = null) }
    }

    private fun alterarTema(tema: TemaEscuro) {
        val prefs = _uiState.value.preferencias
        salvarComFeedback("Tema alterado") {
            salvarPreferencias.salvarAparencia(tema, prefs.usarCoresDoSistema, prefs.corDestaque)
        }
        fecharDialog()
    }

    private fun alterarCoresSistema(usar: Boolean) {
        val prefs = _uiState.value.preferencias
        salvarComFeedback("Configuração salva") {
            salvarPreferencias.salvarAparencia(prefs.temaEscuro, usar, prefs.corDestaque)
        }
    }

    private fun alterarFormatoData(formato: FormatoData) {
        val prefs = _uiState.value.preferencias
        salvarComFeedback("Formato de data alterado") {
            salvarPreferencias.salvarFormatos(formato, prefs.formatoHora, prefs.primeiroDiaSemana)
        }
        fecharDialog()
    }

    private fun alterarFormatoHora(formato: FormatoHora) {
        val prefs = _uiState.value.preferencias
        salvarComFeedback("Formato de hora alterado") {
            salvarPreferencias.salvarFormatos(prefs.formatoData, formato, prefs.primeiroDiaSemana)
        }
        fecharDialog()
    }

    private fun alterarPrimeiroDia(dia: DayOfWeek) {
        val prefs = _uiState.value.preferencias
        salvarComFeedback("Primeiro dia da semana alterado") {
            salvarPreferencias.salvarFormatos(prefs.formatoData, prefs.formatoHora, dia)
        }
        fecharDialog()
    }

    private fun alterarLembretePonto(ativo: Boolean) {
        val prefs = _uiState.value.preferencias
        salvarComFeedback(if (ativo) "Lembrete ativado" else "Lembrete desativado") {
            salvarPreferencias.salvarNotificacoes(
                ativo,
                prefs.alertaFeriadoAtivo,
                prefs.alertaBancoHorasAtivo,
                prefs.antecedenciaAlertaFeriadoDias
            )
        }
    }

    private fun alterarAlertaFeriado(ativo: Boolean) {
        val prefs = _uiState.value.preferencias
        salvarComFeedback(if (ativo) "Alerta de feriado ativado" else "Alerta desativado") {
            salvarPreferencias.salvarNotificacoes(
                prefs.lembretePontoAtivo,
                ativo,
                prefs.alertaBancoHorasAtivo,
                prefs.antecedenciaAlertaFeriadoDias
            )
        }
    }

    private fun alterarAlertaBancoHoras(ativo: Boolean) {
        val prefs = _uiState.value.preferencias
        salvarComFeedback(if (ativo) "Alerta ativado" else "Alerta desativado") {
            salvarPreferencias.salvarNotificacoes(
                prefs.lembretePontoAtivo,
                prefs.alertaFeriadoAtivo,
                ativo,
                prefs.antecedenciaAlertaFeriadoDias
            )
        }
    }

    private fun alterarAntecedencia(dias: Int) {
        val prefs = _uiState.value.preferencias
        salvarComFeedback("Antecedência alterada para $dias dias") {
            salvarPreferencias.salvarNotificacoes(
                prefs.lembretePontoAtivo,
                prefs.alertaFeriadoAtivo,
                prefs.alertaBancoHorasAtivo,
                dias
            )
        }
    }

    private fun alterarLocalizacao(nome: String, lat: Double?, lng: Double?) {
        val prefs = _uiState.value.preferencias
        salvarComFeedback("Localização atualizada") {
            salvarPreferencias.salvarLocalizacao(
                nome, lat, lng,
                prefs.raioGeofencingMetros,
                prefs.registroAutomaticoGeofencing
            )
        }
        fecharDialog()
    }

    private fun alterarRaioGeofencing(metros: Int) {
        val prefs = _uiState.value.preferencias
        salvarComFeedback("Raio alterado para ${metros}m") {
            salvarPreferencias.salvarLocalizacao(
                prefs.localizacaoPadraoNome,
                prefs.localizacaoPadraoLatitude,
                prefs.localizacaoPadraoLongitude,
                metros,
                prefs.registroAutomaticoGeofencing
            )
        }
    }

    private fun alterarRegistroAutomatico(ativo: Boolean) {
        val prefs = _uiState.value.preferencias
        salvarComFeedback(if (ativo) "Registro automático ativado" else "Registro automático desativado") {
            salvarPreferencias.salvarLocalizacao(
                prefs.localizacaoPadraoNome,
                prefs.localizacaoPadraoLatitude,
                prefs.localizacaoPadraoLongitude,
                prefs.raioGeofencingMetros,
                ativo
            )

            // Ativa ou desativa o monitoramento real de Geofence
            if (ativo && prefs.localizacaoPadraoLatitude != null && prefs.localizacaoPadraoLongitude != null) {
                geofenceManager.monitorarTrabalho(
                    prefs.localizacaoPadraoLatitude,
                    prefs.localizacaoPadraoLongitude,
                    prefs.raioGeofencingMetros.toFloat()
                )
            } else {
                geofenceManager.pararMonitoramento()
            }
        }
    }

    private fun alterarBackupAutomatico(ativo: Boolean) {
        salvarComFeedback(if (ativo) "Backup automático ativado" else "Backup automático desativado") {
            salvarPreferencias.salvarBackup(ativo)
        }
    }

    private fun salvarComFeedback(mensagemSucesso: String, bloco: suspend () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            try {
                bloco()
                _uiState.update { it.copy(isSaving = false, mensagemSucesso = mensagemSucesso) }
            } catch (e: Exception) {
                Timber.e(e, "Erro ao salvar preferência")
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        mensagemErro = "Erro ao salvar: ${e.message}"
                    )
                }
            }
        }
    }

    private fun limparMensagens() {
        _uiState.update { it.copy(mensagemSucesso = null, mensagemErro = null) }
    }

    private fun emitirEvento(evento: GlobalSettingsEvent) {
        viewModelScope.launch { _eventos.emit(evento) }
    }
}
