Vou gerar o documento .md completo agora.

Markdown
Copiar
# MeuPonto — Plano de Desenvolvimento e Conclusão

> **Versão do documento:** 1.0.0  
> **Autor:** Thiago  
> **Última atualização:** Março de 2026  
> **Repositório:** https://github.com/tlmacedo/meuPonto

---

## Sumário

1. [Convenções e Boas Práticas](#1-convenções-e-boas-práticas)
2. [Correções Prioritárias](#2-correções-prioritárias)
3. [Funcionalidades Pendentes](#3-funcionalidades-pendentes)
4. [Plano de Implementação](#4-plano-de-implementação)
5. [Arquitetura de Referência](#5-arquitetura-de-referência)

---

## 1. Convenções e Boas Práticas

Todo o código do projeto deve seguir as seguintes convenções antes de qualquer nova implementação.
Estas regras se aplicam a todas as camadas.

### 1.1 Estrutura de Camadas


app/ ├── core/ # Utilitários agnósticos de framework ├── data/ # Implementações de repositórios, DAOs, entidades │ ├── local/ │ │ ├── database/ │ │ │ ├── dao/ │ │ │ ├── entity/ │ │ │ └── migration/ │ │ └── datastore/ │ ├── remote/ │ │ └── api/ │ └── repository/ ├── di/ # Módulos Hilt (somente bindings, sem lógica) ├── domain/ # Regras de negócio puras (sem Android) │ ├── model/ │ ├── repository/ # Interfaces (contratos) │ ├── usecase/ │ └── service/ ├── presentation/ # UI, ViewModels, Navigation │ ├── components/ │ ├── navigation/ │ ├── screen/ │ └── viewmodel/ ├── util/ # Extensões e helpers reutilizáveis └── worker/ # Workers do WorkManager

### 1.2 Regras de Código

- **Comentários KDoc obrigatórios** em todas as classes públicas, interfaces, funções públicas e data classes.
  O mínimo aceitável: `@param`, `@return`, `@throws` quando aplicável, `@since` e `@author`.
- **Sem lógica nos módulos Hilt.** Os módulos em `di/` devem conter apenas bindings e providers simples.
- **Sem `e.printStackTrace()`.** Substituir por `Timber.e(e, "mensagem descritiva")` em toda a base de código.
- **Sem dados hardcoded em produção.** Qualquer dado de seed ou desenvolvimento deve ser guardado atrás de `BuildConfig.DEBUG`.
- **`sealed class Result<T>`** como padrão único de retorno para operações assíncronas nos repositórios e use cases.
- **Funções com responsabilidade única.** Cada função deve ter no máximo 30 linhas. Se ultrapassar, deve ser decomposta.
- **Imutabilidade preferida.** Usar `val` em vez de `var` sempre que possível. `StateFlow` nunca deve expor `MutableStateFlow` publicamente.
- **Coroutines com contexto explícito.** Toda função suspensa no repositório deve executar em `withContext(Dispatchers.IO)`.

### 1.3 Padrão de Logging



Kotlin
Copiar
// ❌ Errado
e.printStackTrace()

// ✅ Correto
Timber.e(e, "Descrição do que falhou ao tentar fazer X")




### 1.4 Padrão de Result



Kotlin
Copiar
// Definição padrão para toda a camada de domínio
sealed class Result<out T> {
data class Success<T>(val data: T) : Result<T>()
data class Error(val exception: Throwable, val message: String? = null) : Result<Nothing>()
object Loading : Result<Nothing>()

    val isSuccess get() = this is Success
    val isError get() = this is Error
    fun getOrNull(): T? = (this as? Success)?.data
}




---

## 2. Correções Prioritárias

As correções abaixo devem ser aplicadas **antes** de qualquer novo desenvolvimento.
Estão ordenadas por impacto e risco.

---

### 2.1 [CRÍTICO] `NetworkModule` — Log de rede exposto em produção

**Arquivo:** `di/NetworkModule.kt`

**Problema:** O `HttpLoggingInterceptor` está configurado com `Level.BODY` incondicionalmente,
expondo payloads completos de requisições e respostas (incluindo tokens e dados sensíveis)
em builds de produção.

**Correção:**



Kotlin
Copiar
// ❌ Antes
val loggingInterceptor = HttpLoggingInterceptor().apply {
level = HttpLoggingInterceptor.Level.BODY
}

// ✅ Depois
val loggingInterceptor = HttpLoggingInterceptor().apply {
level = if (BuildConfig.DEBUG) {
HttpLoggingInterceptor.Level.BODY
} else {
HttpLoggingInterceptor.Level.NONE
}
}




---

### 2.2 [CRÍTICO] `DatabaseModule` — Dados de seed em produção

**Arquivo:** `di/DatabaseModule.kt`

**Problema:** O callback `onCreate` do Room insere um emprego fictício ("SIDIA Teste")
que aparecerá para todos os usuários em produção.

**Correção:**



Kotlin
Copiar
private fun createDatabaseCallback(): RoomDatabase.Callback {
return object : RoomDatabase.Callback() {
override fun onCreate(db: SupportSQLiteDatabase) {
super.onCreate(db)
// Dados de seed apenas em ambiente de desenvolvimento
if (BuildConfig.DEBUG) {
inserirDadosIniciais(db)
}
}
}
}




---

### 2.3 [ALTO] `MeuPontoApplication` — Delay artificial no agendamento do Worker

**Arquivo:** `MeuPontoApplication.kt`

**Problema:** O `delay(1000)` é uma solução frágil para aguardar a inicialização do WorkManager.
Como o app já implementa `Configuration.Provider`, o WorkManager está disponível
antes mesmo do `onCreate` ser chamado — o delay é desnecessário.

**Correção:**



Kotlin
Copiar
// ❌ Antes
applicationScope.launch {
kotlinx.coroutines.delay(1000)
TrashCleanupWorker.schedule(this@MeuPontoApplication)
}

// ✅ Depois
applicationScope.launch {
TrashCleanupWorker.schedule(this@MeuPontoApplication)
}




---

### 2.4 [ALTO] `FotoModule` — Providers redundantes com `@Inject constructor`

**Arquivo:** `di/FotoModule.kt`

**Problema:** Todas as classes do sistema de foto (`ImageCompressor`, `ImageResizer`,
`ImageOrientationCorrector`, `ImageHashCalculator`, `ExifDataWriter`, `ImageProcessor`,
`FotoStorageManager`, `PhotoCaptureManager`) já possuem `@Inject constructor` e `@Singleton`,
o que significa que o Hilt as resolve automaticamente. O `FotoModule` as provê manualmente,
criando duplicidade e risco de inconsistência.

**Correção:** Remover o `FotoModule.kt` por completo. Manter apenas se houver dependências
externas sem `@Inject constructor` (ex: SDKs de terceiros). As classes já anotadas com
`@Singleton` e `@Inject constructor` são resolvidas automaticamente pelo Hilt.

---

### 2.5 [ALTO] Substituição massiva de `e.printStackTrace()` por Timber

**Arquivos afetados:**
- `util/ComprovanteImageStorage.kt`
- `util/foto/ImageCompressor.kt`
- `util/foto/ImageOrientationCorrector.kt`
- `util/foto/ImageHashCalculator.kt`
- `util/foto/ImageProcessor.kt`
- `util/foto/ImageResizer.kt`
- `util/foto/FotoStorageManager.kt`
- `util/foto/PhotoCaptureManager.kt`

**Correção:** Substituir todas as ocorrências de `e.printStackTrace()` por
`Timber.e(e, "Descrição contextual do erro")`.

Script de busca para localizar todas as ocorrências:


Bash
Copiar
grep -rn "e.printStackTrace()" app/src/main/java/




---

### 2.6 [MÉDIO] `MinutosExtensions` — Inconsistência de sinal entre `Int` e `Long`

**Arquivo:** `util/MinutosExtensions.kt`

**Problema:** A versão `Long` de `minutosParaSaldoFormatado` retorna string vazia para zero,
enquanto a versão `Int` retorna `"+"`. Isso causa comportamento visual inconsistente na UI.



Kotlin
Copiar
// ❌ Versão Long atual — zero retorna sem sinal
fun Long.minutosParaSaldoFormatado(): String {
val sinal = if (this > 0) "+" else { if (this < 0) "-" else "" }
...
}

// ✅ Correção — comportamento idêntico ao Int
fun Long.minutosParaSaldoFormatado(): String {
val sinal = if (this >= 0) "+" else "-"
val horas = abs(this) / 60
val minutos = abs(this) % 60
return String.format("%s%02dh %02dmin", sinal, horas, minutos)
}




---

### 2.7 [MÉDIO] `LocationUtils` — Mistura de APIs Java e Kotlin

**Arquivo:** `core/util/LocationUtils.kt`

**Problema:** O código usa `Math.toRadians()` da API Java misturado com funções do
`kotlin.math`. O padrão do projeto deve ser Kotlin puro.



Kotlin
Copiar
// ❌ Antes
val dLat = Math.toRadians(lat2 - lat1)
val dLon = Math.toRadians(lon2 - lon1)

// ✅ Depois
import kotlin.math.PI

val dLat = (lat2 - lat1) * PI / 180.0
val dLon = (lon2 - lon1) * PI / 180.0
// ou usar a extensão: (lat2 - lat1).toRadians() se disponível no kotlin.math




---

### 2.8 [MÉDIO] `ImageTrashManager` — Separador de nome de arquivo frágil

**Arquivo:** `util/foto/ImageTrashManager.kt`

**Problema:** O separador `_##_` pode colidir com nomes de arquivos ou caminhos que
contenham essa sequência, corrompendo o parsing. Além disso, ao substituir `/` por `__`,
caminhos que já contenham `__` nativamente seriam ambíguos na restauração.

**Correção:** Serializar os metadados do arquivo de lixeira em um arquivo `.json` adjacente,
ou usar um índice persistido no DataStore/Room, eliminando a dependência do nome do arquivo
para carregar metadados.



Kotlin
Copiar
// Estrutura alternativa recomendada:
// .trash/comprovantes/{timestamp}_{pontoId}.jpg   <- arquivo de imagem
// .trash/comprovantes/{timestamp}_{pontoId}.json  <- metadados (path original, motivo, etc.)

data class TrashMetadataJson(
val originalPath: String,
val pontoId: Long,
val timestamp: Long,
val motivo: String
)




---

### 2.9 [BAIXO] `ImageProcessor.processAndSave` — `originalBitmap` não reciclado

**Arquivo:** `util/foto/ImageProcessor.kt`

**Problema:** No bloco `finally`, apenas o bitmap redimensionado é reciclado se for diferente
do original. O `originalBitmap` nunca é reciclado dentro da função, deixando essa
responsabilidade implícita para o chamador sem qualquer contrato documentado.

**Correção:** Documentar explicitamente o contrato de ciclo de vida do bitmap no KDoc
e reciclar o `originalBitmap` no `finally`, garantindo que não há leak independente
de quem chama a função.

---

### 2.10 [BAIXO] `strings.xml` — Acentuação ausente

**Arquivo:** `res/values/strings.xml`

**Problema:** Strings como `"Inicio"`, `"Historico"`, `"Configuracoes"` e `"Relatorios"`
estão sem acentuação, o que é incorreto para o português brasileiro e afeta a percepção
de qualidade do produto.

**Correção:**


Xml
Copiar
<string name="nav_home">Início</string>
<string name="nav_historico">Histórico</string>
<string name="nav_configuracoes">Configurações</string>
<string name="nav_relatorios">Relatórios</string>
<string name="msg_ponto_excluido">Ponto excluído</string>
<string name="msg_ponto_atualizado">Ponto atualizado</string>




---

## 3. Funcionalidades Pendentes

Lista de funcionalidades identificadas como incompletas ou ausentes no projeto atual.

| # | Funcionalidade | Camada Principal | Dependências | Complexidade |
|---|---|---|---|---|
| F01 | Integração UI do sistema de foto | Presentation | FotoStorageManager, PhotoCaptureManager | Média |
| F02 | Registro de localização GPS no ponto | Data / Domain | LocationService, LocationModule | Média |
| F03 | Tela de visualização de comprovante | Presentation | FotoStorageManager | Baixa |
| F04 | Tela de lixeira de fotos | Presentation | ImageTrashManager | Média |
| F05 | Geração de extrato em PDF | Domain / Presentation | — | Alta |
| F06 | Exportação CSV | Domain | — | Média |
| F07 | Compartilhamento (e-mail / WhatsApp) | Presentation | PDF / CSV | Baixa |
| F08 | Notificações e lembretes de ponto | Worker / Presentation | WorkManager | Média |
| F09 | Alerta de saldo negativo | Domain / Presentation | — | Baixa |
| F10 | Reset mensal de saldo | Domain / Worker | — | Média |
| F11 | Autenticação Firebase | Data | Firebase Auth SDK | Alta |
| F12 | Backup automático Firestore | Data / Worker | Firebase Firestore | Alta |
| F13 | Sincronização entre dispositivos | Data / Domain | Firestore + estratégia de conflito | Muito Alta |
| F14 | Testes unitários dos Use Cases | Test | — | Alta |
| F15 | Testes unitários das extensões de minutos | Test | — | Baixa |
| F16 | Testes de integração dos repositórios | Test | — | Alta |

---

## 4. Plano de Implementação

O plano está dividido em fases com sequência baseada em dependências técnicas.
**Cada fase deve ser mergeada e testada antes de iniciar a próxima.**

---

### Fase 0 — Higienização (Pré-requisito de tudo)
**Estimativa:** 3–5 dias  
**Objetivo:** Corrigir todos os problemas da seção 2 antes de qualquer novo código.

- [ ] Aplicar correção `BuildConfig.DEBUG` no `NetworkModule` (item 2.1)
- [ ] Aplicar `BuildConfig.DEBUG` no seed do `DatabaseModule` (item 2.2)
- [ ] Remover delay artificial do `MeuPontoApplication` (item 2.3)
- [ ] Avaliar e remover `FotoModule` redundante (item 2.4)
- [ ] Substituir todos os `e.printStackTrace()` por `Timber.e()` (item 2.5)
- [ ] Corrigir inconsistência de sinal em `MinutosExtensions` (item 2.6)
- [ ] Padronizar `LocationUtils` para Kotlin puro (item 2.7)
- [ ] Corrigir acentuação no `strings.xml` (item 2.10)
- [ ] Documentar contrato de bitmap em `ImageProcessor` (item 2.9)

**Critério de conclusão:** Nenhum `e.printStackTrace()`, nenhum dado hardcoded fora de
`BuildConfig.DEBUG`, build de release sem logs de rede.

---

### Fase 1 — Testes da Base Existente
**Estimativa:** 1–2 semanas  
**Objetivo:** Garantir que o código existente está correto e protegido por testes
antes de adicionar novas funcionalidades (itens F14 e F15).

#### 1.1 Testes Unitários — Extensões de Minutos



Kotlin
Copiar
// test/util/MinutosExtensionsTest.kt

/**
* Testes unitários para as extensões de formatação de minutos.
*
* Cobre os casos: zero, positivo, negativo, valores grandes,
* e consistência entre as versões Int e Long.
*
* @author Thiago
* @since 12.0.0
  */
  class MinutosExtensionsTest {

  @Test
  fun `minutosParaHoraMinuto com zero retorna 00h 00min`() {
  assertEquals("00h 00min", 0.minutosParaHoraMinuto())
  }

  @Test
  fun `minutosParaSaldoFormatado com zero retorna sinal positivo`() {
  assertEquals("+00h 00min", 0.minutosParaSaldoFormatado())
  assertEquals("+00h 00min", 0L.minutosParaSaldoFormatado())
  }

  @Test
  fun `minutosParaSaldoFormatado Int e Long sao consistentes`() {
  val valor = 90
  assertEquals(valor.minutosParaSaldoFormatado(), valor.toLong().minutosParaSaldoFormatado())
  }

  @Test
  fun `minutosParaHoraMinuto com 90 minutos retorna 01h 30min`() {
  assertEquals("01h 30min", 90.minutosParaHoraMinuto())
  }

  @Test
  fun `minutosParaSaldoFormatado com negativo retorna sinal negativo`() {
  assertEquals("-01h 30min", (-90).minutosParaSaldoFormatado())
  }
  }




#### 1.2 Testes Unitários — Use Cases de Cálculo de Saldo



Kotlin
Copiar
// test/domain/usecase/CalcularSaldoDiaUseCaseTest.kt

/**
* Testes unitários para o cálculo de saldo diário.
*
* Usa fakes dos repositórios para isolar a lógica de negócio.
*
* @author Thiago
* @since 12.0.0
  */
  class CalcularSaldoDiaUseCaseTest {

  private lateinit var useCase: CalcularSaldoDiaUseCase
  private lateinit var fakePontoRepository: FakePontoRepository

  @Before
  fun setup() {
  fakePontoRepository = FakePontoRepository()
  useCase = CalcularSaldoDiaUseCase(fakePontoRepository)
  }

  @Test
  fun `dia com jornada completa retorna saldo zero`() = runTest {
  // Arrange: 8h trabalhadas, carga de 8h
  fakePontoRepository.inserirPontos(pontosJornadaCompleta())

       // Act
       val resultado = useCase(LocalDate.now(), empregoId = 1L)

       // Assert
       assertTrue(resultado.isSuccess)
       assertEquals(0, resultado.getOrNull()?.saldoMinutos)
  }

  @Test
  fun `dia sem pontos retorna saldo negativo igual a carga horaria`() = runTest {
  val resultado = useCase(LocalDate.now(), empregoId = 1L)
  assertTrue(resultado.isSuccess)
  assertTrue((resultado.getOrNull()?.saldoMinutos ?: 0) < 0)
  }
  }




#### 1.3 Testes de Integração — Room DAO



Kotlin
Copiar
// androidTest/data/local/PontoDaoTest.kt

/**
* Testes de integração para o PontoDao usando banco in-memory.
*
* @author Thiago
* @since 12.0.0
  */
  @RunWith(AndroidJUnit4::class)
  class PontoDaoTest {

  private lateinit var database: MeuPontoDatabase
  private lateinit var pontoDao: PontoDao

  @Before
  fun setup() {
  database = Room.inMemoryDatabaseBuilder(
  ApplicationProvider.getApplicationContext(),
  MeuPontoDatabase::class.java
  ).allowMainThreadQueries().build()

       pontoDao = database.pontoDao()
  }

  @After
  fun tearDown() {
  database.close()
  }

  @Test
  fun inserirERecuperarPonto() = runTest {
  val ponto = PontoEntity(/* ... */)
  pontoDao.inserir(ponto)
  val resultado = pontoDao.buscarPorId(ponto.id).first()
  assertEquals(ponto, resultado)
  }
  }




**Critério de conclusão:** Cobertura mínima de 70% nos Use Cases e 100% nas extensões
de minutos (lógica crítica de negócio).

---

### Fase 2 — Integração UI do Sistema de Foto
**Estimativa:** 1–2 semanas  
**Objetivo:** Conectar a infraestrutura já implementada com a interface do usuário
(itens F01, F03, F04).

#### 2.1 Estrutura de arquivos novos


presentation/ ├── screen/ │ ├── foto/ │ │ ├── FotoComprovanteScreen.kt # Visualização de foto existente │ │ ├── CapturarFotoDialog.kt # Diálogo de captura (câmera/galeria) │ │ └── LixeiraFotosScreen.kt # Tela de lixeira de fotos │ └── lancamento/ │ └── LancamentoPontoScreen.kt # Atualizar para incluir botão de foto ├── viewmodel/ │ └── FotoViewModel.kt # Já existe — revisar e completar └── components/ └── foto/ ├── FotoThumbnailCard.kt # Componente de thumbnail reutilizável └── FotoPreviewDialog.kt # Preview em tela cheia

#### 2.2 Contrato do `FotoViewModel`



Kotlin
Copiar
/**
* ViewModel para gerenciamento do fluxo de captura e exibição de fotos.
*
* Expõe estado imutável via [StateFlow] e recebe eventos da UI via funções
* suspensas ou síncronas conforme a natureza da operação.
*
* ## Estados possíveis:
* - [FotoUiState.Idle]: Aguardando interação
* - [FotoUiState.Capturing]: Fluxo de captura em andamento
* - [FotoUiState.Processing]: Processando imagem capturada
* - [FotoUiState.Success]: Foto salva com sucesso
* - [FotoUiState.Error]: Erro com mensagem para exibição
*
* @param photoCaptureManager Gerenciador de captura de fotos
* @param fotoStorageManager Gerenciador de armazenamento
*
* @author Thiago
* @since 12.0.0
  */
  @HiltViewModel
  class FotoViewModel @Inject constructor(
  private val photoCaptureManager: PhotoCaptureManager,
  private val fotoStorageManager: FotoStorageManager,
  private val imageTrashManager: ImageTrashManager
  ) : ViewModel() {

  /** Estado observável da UI de foto */
  val captureState: StateFlow<CaptureState> = photoCaptureManager.captureState

  /**
    * Prepara a câmera para captura.
    * Deve ser chamado ao abrir o diálogo de captura.
    *
    * @return URI para uso com [ActivityResultLauncher], ou null em caso de erro
      */
      fun prepararCamera(): Uri? = photoCaptureManager.prepareForCameraCapture()

  /**
    * Chamado após retorno bem-sucedido da câmera.
      */
      fun onCameraSuccess() = photoCaptureManager.onCameraCaptureSuccess()

  /**
    * Chamado quando o usuário cancela a câmera.
      */
      fun onCameraCancelado() = photoCaptureManager.onCameraCaptureCancelled()

  /**
    * Chamado quando uma foto é selecionada da galeria.
    *
    * @param uri URI da foto selecionada pelo usuário
      */
      fun onGaleriaSelecionada(uri: Uri) = photoCaptureManager.onGalleryPhotoSelected(uri)

  /**
    * Processa e salva a foto capturada associando ao ponto.
    *
    * @param empregoId ID do emprego
    * @param pontoId ID do ponto a ser vinculado
    * @param data Data do ponto
    * @param config Configurações do emprego para processamento da imagem
      */
      fun salvarFoto(
      empregoId: Long,
      pontoId: Long,
      data: LocalDate,
      config: ConfiguracaoEmprego
      ) {
      viewModelScope.launch {
      photoCaptureManager.processAndSavePhoto(
      empregoId = empregoId,
      pontoId = pontoId,
      data = data,
      config = config
      )
      }
      }

  /**
    * Move foto para a lixeira ao invés de deletar permanentemente.
    *
    * @param relativePath Caminho relativo da foto
    * @param pontoId ID do ponto vinculado
      */
      fun moverParaLixeira(relativePath: String, pontoId: Long) {
      viewModelScope.launch {
      imageTrashManager.moveToTrash(
      relativePath = relativePath,
      pontoId = pontoId,
      motivo = "exclusão pelo usuário"
      )
      }
      }
      }




#### 2.3 Atualizar `file_paths.xml` para câmera



Xml
Copiar
<!-- O diretório correto para o FileProvider da câmera é cache-path -->
<paths>
    <!-- Arquivos temporários da câmera -->
    <cache-path name="temp_camera" path="temp_camera/" />

    <!-- Comprovantes permanentes -->
    <files-path name="comprovantes" path="comprovantes/" />
</paths>




**Critério de conclusão:** Usuário consegue tirar foto ou selecionar da galeria ao
registrar um ponto, e visualizar o comprovante na tela de detalhes do ponto.

---

### Fase 3 — Localização GPS no Ponto
**Estimativa:** 1 semana  
**Objetivo:** Associar coordenadas GPS ao registro de ponto (item F02).

#### 3.1 Fluxo de implementação


[LancamentoPontoViewModel] └── solicita localização → [LocationService] └── recebe GpsData → passa para [PontoUseCase] └── [PontoUseCase] persiste coordenadas junto com o PontoEntity

#### 3.2 Permissões em runtime



Kotlin
Copiar
/**
* Solicita permissão de localização em runtime antes de registrar o ponto.
*
* O fluxo deve ser:
* 1. Verificar se permissão está concedida
* 2. Se não: exibir rationale → solicitar permissão → aguardar resultado
* 3. Se sim: coletar localização e prosseguir
*
* Referência: [ActivityResultContracts.RequestPermission]
  */




#### 3.3 Contrato do `LocationService`



Kotlin
Copiar
/**
* Serviço de localização que abstrai o FusedLocationProviderClient.
*
* Retorna a localização atual com timeout configurável.
* Deve ser suspenso e executar em IO.
*
* @param timeoutMs Tempo máximo de espera pela localização em milissegundos
* @return [GpsData] com coordenadas ou null se timeout/permissão negada
*
* @author Thiago
* @since 12.0.0
  */
  suspend fun obterLocalizacaoAtual(timeoutMs: Long = 10_000): GpsData?




---

### Fase 4 — Relatórios e Exportação
**Estimativa:** 2–3 semanas  
**Objetivo:** Geração de extrato PDF e exportação CSV (itens F05, F06, F07).

#### 4.1 Estrutura da camada de domínio



Kotlin
Copiar
// domain/usecase/relatorio/

/**
* Use case para geração de extrato de pontos em formato PDF.
*
* Responsável apenas pela orquestração dos dados. A formatação
* e renderização do PDF ficam em uma classe de infraestrutura separada.
*
* @param pontoRepository Repositório de pontos
* @param pdfGenerator Gerador de PDF (implementação de infraestrutura)
*
* @author Thiago
* @since 13.0.0
  */
  class GerarExtratoPdfUseCase @Inject constructor(
  private val pontoRepository: PontoRepository,
  private val pdfGenerator: PdfGenerator
  ) {
  /**
    * Gera extrato PDF para o período especificado.
    *
    * @param empregoId ID do emprego
    * @param dataInicio Data de início do período
    * @param dataFim Data de fim do período
    * @return [Result] com o [File] gerado ou erro descritivo
      */
      suspend operator fun invoke(
      empregoId: Long,
      dataInicio: LocalDate,
      dataFim: LocalDate
      ): Result<File>
      }

/**
* Interface de geração de PDF — permite trocar a implementação
* sem alterar a camada de domínio.
*
* @author Thiago
* @since 13.0.0
  */
  interface PdfGenerator {
  /**
    * Gera arquivo PDF a partir dos dados fornecidos.
    *
    * @param dados Dados do extrato agrupados por dia
    * @param outputFile Arquivo de destino
    * @return true se gerado com sucesso
      */
      suspend fun gerar(dados: ExtratoData, outputFile: File): Boolean
      }




#### 4.2 Implementação com PrintedPdfDocument (sem dependência externa)



Kotlin
Copiar
/**
* Implementação de [PdfGenerator] usando a API nativa do Android.
*
* Não requer biblioteca externa. Limitação: sem paginação automática avançada.
* Para relatórios complexos, considerar migração para iText ou PdfBox.
*
* @author Thiago
* @since 13.0.0
  */
  class NativePdfGeneratorImpl @Inject constructor(
  @ApplicationContext private val context: Context
  ) : PdfGenerator {

  override suspend fun gerar(dados: ExtratoData, outputFile: File): Boolean =
  withContext(Dispatchers.IO) {
  // Implementação com PdfDocument da API Android
  // ...
  true
  }
  }




#### 4.3 Exportação CSV



Kotlin
Copiar
/**
* Use case para exportação de pontos em formato CSV.
*
* Gera um arquivo CSV no diretório de cache com encoding UTF-8 e BOM
* para compatibilidade com Microsoft Excel.
*
* @author Thiago
* @since 13.0.0
  */
  class ExportarCsvUseCase @Inject constructor(
  private val pontoRepository: PontoRepository
  ) {
  /**
    * Cabeçalho padrão do CSV exportado.
      */
      private val CABECALHO = "Data;Tipo;Hora Registrada;Hora Ideal;Saldo (min);Observação\n"

  suspend operator fun invoke(
  empregoId: Long,
  periodo: Pair<LocalDate, LocalDate>
  ): Result<File>
  }




#### 4.4 Compartilhamento via Intent



Kotlin
Copiar
/**
* Compartilha um arquivo via Intent do Android.
*
* Usa [FileProvider] para garantir acesso seguro ao arquivo.
* Compatível com qualquer app registrado para o MIME type fornecido.
*
* @param file Arquivo a ser compartilhado
* @param mimeType MIME type do arquivo (ex: "application/pdf", "text/csv")
* @param titulo Título do chooser de compartilhamento
  */
  fun Context.compartilharArquivo(file: File, mimeType: String, titulo: String) {
  val uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", file)
  val intent = Intent(Intent.ACTION_SEND).apply {
  type = mimeType
  putExtra(Intent.EXTRA_STREAM, uri)
  addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
  }
  startActivity(Intent.createChooser(intent, titulo))
  }




**Critério de conclusão:** Usuário consegue gerar e compartilhar extrato do mês
em PDF e CSV pela tela de Relatórios.

---

### Fase 5 — Notificações e Workers
**Estimativa:** 1 semana  
**Objetivo:** Lembretes de ponto, alertas de saldo negativo e reset mensal (itens F08, F09, F10).

#### 5.1 Workers necessários


worker/ ├── TrashCleanupWorker.kt # Já implementado ✓ ├── LembretePontoWorker.kt # Notificação no horário de entrada/saída ├── AlertaSaldoNegativoWorker.kt # Verifica saldo e notifica se negativo └── ResetSaldoMensalWorker.kt # Zera saldo no fechamento do período

#### 5.2 Canal de notificação



Kotlin
Copiar
/**
* Inicializa os canais de notificação do app.
*
* Deve ser chamado no [MeuPontoApplication.onCreate].
* Canais são idempotentes — chamadas repetidas são ignoradas pelo sistema.
*
* @author Thiago
* @since 14.0.0
  */
  fun criarCanaisNotificacao(context: Context) {
  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
  val notificationManager = context.getSystemService(NotificationManager::class.java)

       // Canal de lembretes de ponto
       NotificationChannel(
           CHANNEL_LEMBRETES,
           "Lembretes de ponto",
           NotificationManager.IMPORTANCE_HIGH
       ).apply {
           description = "Notificações de horário de entrada e saída"
           notificationManager.createNotificationChannel(this)
       }

       // Canal de alertas de saldo
       NotificationChannel(
           CHANNEL_ALERTAS,
           "Alertas de saldo",
           NotificationManager.IMPORTANCE_DEFAULT
       ).apply {
           description = "Alertas de saldo negativo acumulado"
           notificationManager.createNotificationChannel(this)
       }
  }
  }




---

### Fase 6 — Firebase (Autenticação e Backup)
**Estimativa:** 3–4 semanas  
**Objetivo:** Autenticação, backup e sincronização (itens F11, F12, F13).

> ⚠️ Esta fase é a mais complexa e deve ser iniciada somente após todas as fases
> anteriores estarem estáveis e testadas.

#### 6.1 Sequência de implementação

1. Configurar projeto no Firebase Console (Authentication + Firestore)
2. Adicionar `google-services.json` e dependências no `build.gradle`
3. Implementar `AuthRepository` e `AuthRepositoryImpl`
4. Criar tela de Login/Cadastro com Firebase Auth (e-mail + Google Sign-In)
5. Implementar `BackupRepository` e `BackupRepositoryImpl` com Firestore
6. Definir estratégia de sincronização (last-write-wins para MVP)
7. Criar `SyncWorker` para backup periódico em background
8. Implementar resolução de conflitos

#### 6.2 Estrutura de dados no Firestore


firestore/ └── users/{userId}/ ├── empregos/{empregoId} ├── pontos/{pontoId} ├── configuracoes/{empregoId} └── metadata/ └── lastSync: Timestamp

#### 6.3 Contrato da camada de domínio



Kotlin
Copiar
/**
* Interface do repositório de autenticação.
*
* Abstrai a implementação Firebase para permitir testes e
* possível migração futura de provedor.
*
* @author Thiago
* @since 15.0.0
  */
  interface AuthRepository {

  /** Flow do usuário autenticado. Emite null quando deslogado. */
  val usuarioAtual: Flow<Usuario?>

  /**
    * Realiza login com e-mail e senha.
    *
    * @return [Result.Success] com [Usuario] ou [Result.Error] com causa
      */
      suspend fun loginComEmail(email: String, senha: String): Result<Usuario>

  /**
    * Realiza login com conta Google via [GoogleSignInAccount].
    *
    * @return [Result.Success] com [Usuario] ou [Result.Error] com causa
      */
      suspend fun loginComGoogle(idToken: String): Result<Usuario>

  /** Realiza logout e limpa estado local. */
  suspend fun logout(): Result<Unit>

  /** Cria nova conta com e-mail e senha. */
  suspend fun cadastrar(email: String, senha: String, nome: String): Result<Usuario>
  }




---

## 5. Arquitetura de Referência

### 5.1 Fluxo de Dados


UI (Screen/Composable) ↕ observa StateFlow / envia eventos ViewModel ↕ chama suspend fun Use Case (Domain) ↕ usa interface do repositório Repository Interface (Domain) ↑ implementado por Repository Impl (Data) ↕ acessa DAO (Room) / API (Retrofit) / DataStore

### 5.2 Regras de Dependência

- **Presentation** pode depender de **Domain**. Nunca de **Data** diretamente.
- **Domain** não depende de nenhuma outra camada. É puro Kotlin.
- **Data** depende de **Domain** (implementa interfaces). Nunca de **Presentation**.
- **DI** conhece todas as camadas (é o "glue"), mas não contém lógica.

### 5.3 Checklist de Pull Request

Antes de qualquer merge, verificar:

- [ ] Nenhum `e.printStackTrace()` introduzido
- [ ] Nenhum dado hardcoded fora de `BuildConfig.DEBUG`
- [ ] Todas as classes e funções públicas documentadas com KDoc
- [ ] StateFlows não expõem `MutableStateFlow` publicamente
- [ ] Funções suspensas em repositórios executam em `withContext(Dispatchers.IO)`
- [ ] Novos Use Cases cobertos por testes unitários
- [ ] Strings exibíveis na UI estão em `strings.xml` com acentuação correta
- [ ] Nenhuma lógica de negócio nos módulos Hilt
- [ ] Resultado de operações assíncronas usa `sealed class Result<T>`

---

*Documento gerado com base na análise do código-fonte do projeto MeuPonto.*  
*Revisitar e atualizar ao final de cada fase.*


O documento está estruturado para ser salvo como PLANO_DESENVOLVIMENTO.md na raiz do repositório. A sequência foi desenhada para respeitar dependências técnicas: você não pode testar o que ainda não foi corrigido, não pode integrar a UI de foto sem a base estável, e não deve implementar Firebase antes de ter a lógica local sólida e testada.