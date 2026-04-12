// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/theme/Color.kt
package br.com.tlmacedo.meuponto.presentation.theme

import androidx.compose.ui.graphics.Color

// ============================================================================
// Cores Padrão (Default Material)
// ============================================================================
val DefaultPrimary = Color(0xFF6750A4)
val DefaultSecondary = Color(0xFF625B71)
val DefaultTertiary = Color(0xFF7D5260)

// ============================================================================
// Cores Primárias - Tema Sidia (Baseadas na Logo)
// ============================================================================
val SidiaBlue = Color(0xFF007BFF)      // Azul Principal (Primary Blue)
val SidiaGreen = Color(0xFF28A745)     // Verde Destaque (Accent Green)
val SidiaNavy = Color(0xFF343A40)      // Cinza Escuro (Dark Gray) - Base Colors
val SidiaMediumGray = Color(0xFF6C757D) // Cinza Médio (Medium Gray)
val SidiaLightGray = Color(0xFFF8F9FA) // Cinza Claro (Light Gray)

// Cores de Apoio Sidia
val SidiaDarkBlue = Color(0xFF0056B3)
val SidiaSoftGreen = Color(0xFFD4EDDA)
val SidiaDarkGreen = Color(0xFF155724)

val Primary = SidiaBlue
val OnPrimary = Color.White
val Secondary = SidiaGreen
val OnSecondary = Color.Black // Verde neon pede texto preto para contraste

// ============================================================================
// Cores de Superfície
// ============================================================================
val Surface = Color(0xFFFAFAFA)
val SurfaceVariant = Color(0xFFE6F0FD) // Azul muito claro Sidia para variantes
val OnSurface = Color(0xFF001C46)      // Usar Navy para textos em superfície
val OnSurfaceVariant = Color(0xFF0056B3) // Usar Sidia Blue para variantes de texto

// ============================================================================
// Cores de Background
// ============================================================================
val Background = Color(0xFFF8FAFC)
val OnBackground = Color(0xFF0F172A)

// ============================================================================
// Cores Semânticas - Status
// ============================================================================
val Success = Color(0xFF28A745)
val SuccessLight = Color(0xFFD4EDDA)
val OnSuccess = Color.White

val Warning = Color(0xFFFFC107)
val WarningLight = Color(0xFFFFF3CD)
val OnWarning = Color(0xFF856404)

val Error = Color(0xFFDC3545)
val ErrorLight = Color(0xFFF8D7DA)
val OnError = Color.White

val Info = Color(0xFF3B82F6)
val InfoLight = Color(0xFFDBEAFE)
val OnInfo = Color.White

// ============================================================================
// Cores de Ponto
// ============================================================================
val EntradaColor = Color(0xFF10B981)      // Verde para entrada
val EntradaBg = Color(0xFFD1FAE5)         // Fundo verde claro
val SaidaColor = Color(0xFFEF4444)        // Vermelho para saída
val SaidaBg = Color(0xFFFEE2E2)           // Fundo vermelho claro

// ============================================================================
// Cores do Tema Escuro - Sidia Dark
// ============================================================================
val DarkPrimary = Color(0xFF4D88FF)    // Azul Sidia suavizado para Dark Mode
val DarkSecondary = Color(0xFF5DF25D)  // Verde Sidia vibrante para Dark Mode
val DarkSurface = Color(0xFF0F172A)    // Fundo Slate escuro
val DarkSurfaceVariant = Color(0xFF1E293B)
val DarkBackground = Color(0xFF020617)
val DarkOnSurface = Color(0xFFF8FAFC)
val DarkOnSurfaceVariant = Color(0xFF94A3B8)
val DarkOnBackground = Color(0xFFF1F5F9)

// Cores de Ponto - Tema Escuro (mais vibrantes)
val DarkEntradaColor = Color(0xFF34D399)   // Verde mais vibrante
val DarkEntradaBg = Color(0xFF064E3B)      // Fundo verde escuro
val DarkSaidaColor = Color(0xFFF87171)     // Vermelho mais vibrante
val DarkSaidaBg = Color(0xFF7F1D1D)        // Fundo vermelho escuro

// Cores de Status - Tema Escuro (mais vibrantes)
val DarkSuccess = Color(0xFF34D399)
val DarkSuccessLight = Color(0xFF065F46)
val DarkWarning = Color(0xFFFBBF24)
val DarkWarningLight = Color(0xFF78350F)
val DarkError = Color(0xFFF87171)
val DarkErrorLight = Color(0xFF7F1D1D)
val DarkInfo = Color(0xFF60A5FA)
val DarkInfoLight = Color(0xFF1E3A5F)

// ============================================================================
// Cores de Texto - Alto Contraste
// ============================================================================
val TextPrimary = Color(0xFFF1F5F9)        // Texto principal claro
val TextSecondary = Color(0xFFCBD5E1)      // Texto secundário com bom contraste
val TextTertiary = Color(0xFF94A3B8)       // Texto terciário
val TextMuted = Color(0xFF64748B)          // Texto apagado (usar com moderação)
