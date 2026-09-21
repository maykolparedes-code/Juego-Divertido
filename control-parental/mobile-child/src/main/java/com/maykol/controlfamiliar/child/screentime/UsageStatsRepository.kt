package com.maykol.controlfamiliar.child.screentime

import android.app.usage.UsageStatsManager
import android.content.Context
import com.maykol.controlfamiliar.child.network.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar

/**
 * Lee estadísticas de uso reales del sistema (`UsageStatsManager`, permiso
 * `PACKAGE_USAGE_STATS`) y las sincroniza con el backend para los reportes
 * del padre.
 */
class UsageStatsRepository(private val context: Context) {

    fun minutesUsedTodaySync(packageName: String): Long {
        val usageStatsManager =
            context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val startOfDay = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }.timeInMillis

        val stats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startOfDay,
            System.currentTimeMillis(),
        )
        val totalMs = stats.filter { it.packageName == packageName }
            .sumOf { it.totalTimeInForeground }
        return totalMs / 60_000
    }

    companion object {
        // Wrapper estático usado desde el AccessibilityService; en la app
        // real se inyecta el repositorio en vez de usar un singleton.
        suspend fun minutesUsedToday(packageName: String): Long {
            val context = ApiClient.appContext ?: return 0L
            return withContext(Dispatchers.Default) {
                UsageStatsRepository(context).minutesUsedTodaySync(packageName)
            }
        }

        suspend fun reportSession(packageName: String, startedAtMs: Long, endedAtMs: Long) {
            runCatching {
                ApiClient.usageReportsApi.recordSession(
                    appPackage = packageName,
                    startedAt = startedAtMs,
                    endedAt = endedAtMs,
                )
            }
        }
    }
}

data class ScreenTimeRule(val appPackage: String, val dailyLimitMinutes: Long)

object ScreenTimeRulesCache {
    private var rules: Map<String, ScreenTimeRule> = emptyMap()

    fun update(newRules: List<ScreenTimeRule>) {
        rules = newRules.associateBy { it.appPackage }
    }

    fun ruleFor(packageName: String): ScreenTimeRule? = rules[packageName]
}
