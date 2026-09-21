package com.maykol.controlfamiliar.child.screentime

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import com.maykol.controlfamiliar.child.screentime.BlockingOverlayActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Detecta qué app está en primer plano para:
 *  1. Medir sesiones de uso reales (complementa a UsageStatsManager, que
 *     solo da totales históricos, no eventos en tiempo real).
 *  2. Mostrar la pantalla de bloqueo cuando se supera el límite diario
 *     configurado por el padre para esa app o categoría.
 *
 * Al activarse desde Ajustes → Accesibilidad, Android muestra un aviso
 * explicando exactamente qué puede observar este servicio — esto es
 * intencional y no se puede ni se debe suprimir.
 */
class AppUsageAccessibilityService : AccessibilityService() {

    private val scope = CoroutineScope(Dispatchers.Default)
    private var currentPackage: String? = null
    private var sessionStartedAt: Long = 0L

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val packageName = event?.packageName?.toString() ?: return
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        if (packageName == currentPackage) return

        closeCurrentSession()
        currentPackage = packageName
        sessionStartedAt = System.currentTimeMillis()

        scope.launch {
            val rule = ScreenTimeRulesCache.ruleFor(packageName) ?: return@launch
            val usedMinutesToday = UsageStatsRepository.minutesUsedToday(packageName)
            if (usedMinutesToday >= rule.dailyLimitMinutes) {
                showBlockingOverlay(packageName)
            }
        }
    }

    private fun closeCurrentSession() {
        val pkg = currentPackage ?: return
        val durationMs = System.currentTimeMillis() - sessionStartedAt
        if (durationMs < MIN_SESSION_MS) return
        scope.launch {
            UsageStatsRepository.reportSession(pkg, sessionStartedAt, System.currentTimeMillis())
        }
    }

    private fun showBlockingOverlay(packageName: String) {
        val intent = Intent(this, BlockingOverlayActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra(BlockingOverlayActivity.EXTRA_BLOCKED_PACKAGE, packageName)
        }
        startActivity(intent)
    }

    override fun onInterrupt() = Unit

    companion object {
        private const val MIN_SESSION_MS = 5_000L
    }
}
