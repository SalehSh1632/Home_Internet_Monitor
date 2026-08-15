package com.homemonitor.app.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.homemonitor.app.data.Settings
import com.homemonitor.app.data.SettingsStore
import com.homemonitor.app.data.BaleApi
import com.homemonitor.app.data.UsageStatsHelper
import kotlinx.coroutines.flow.first
import java.util.Calendar

class CommandCheckWorker(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val store = SettingsStore(applicationContext)

        try {
            if (store.commandCheckEnabled.first()) {
                val settings = store.snapshot()
                if (settings.botToken.isNotBlank() && settings.chatId.isNotBlank()) {
                    val command = store.reportCommand.first()
                    val lastSeen = store.lastUpdateId.first()
                    var newLastSeen = lastSeen

                    val updates = BaleApi.getUpdates(settings.botToken, timeoutSeconds = 20)
                    for (u in updates) {
                        if (u.updateId <= lastSeen) continue
                        if (u.updateId > newLastSeen) newLastSeen = u.updateId
                        if (u.chatId == settings.chatId && u.text.trim().startsWith(command, ignoreCase = true)) {
                            sendOnDemandReport(settings)
                        }
                    }
                    if (newLastSeen != lastSeen) store.saveLastUpdateId(newLastSeen)
                }
            }
        } catch (e: Exception) {
            // خطای موقت؛ نوبت بعدی خودش دوباره تلاش می‌کند، بدون نمایش ارور به کاربر
        }

        // در هر صورت نوبت بعدی را زمان‌بندی کن تا چرخه هیچ‌وقت متوقف نشود
        CommandCheckScheduler.scheduleNext(applicationContext)
        return Result.success()
    }

    private fun sendOnDemandReport(settings: Settings) {
        val helper = UsageStatsHelper(applicationContext)
        if (!helper.hasUsageAccess()) return

        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 1)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis
        val end = System.currentTimeMillis()

        val report = helper.getUsageReport(start, end, settings.trackedPackages)
        val text = ReportBuilder.build(
            deviceName = settings.deviceName,
            start = start,
            end = end,
            report = report,
            title = "گزارش لحظه‌ای مصرف (۰۰:۰۱ تا الان)"
        )
        BaleApi.sendMessage(settings.botToken, settings.chatId, text)
    }
}
