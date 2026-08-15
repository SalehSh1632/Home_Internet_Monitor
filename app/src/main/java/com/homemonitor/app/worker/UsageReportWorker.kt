package com.homemonitor.app.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.homemonitor.app.data.BaleApi
import com.homemonitor.app.data.SettingsStore
import com.homemonitor.app.data.UsageStatsHelper

class UsageReportWorker(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val settingsStore = SettingsStore(applicationContext)
        val settings = settingsStore.snapshot()

        if (settings.botToken.isBlank() || settings.chatId.isBlank()) {
            return Result.success() // هنوز تنظیم نشده، کاری نکن
        }

        val helper = UsageStatsHelper(applicationContext)
        if (!helper.hasUsageAccess()) {
            return Result.retry()
        }

        val end = System.currentTimeMillis()
        val start = end - settings.reportIntervalHours * 60 * 60 * 1000L

        val report = helper.getUsageReport(start, end, settings.trackedPackages)
        val text = ReportBuilder.build(settings.deviceName, start, end, report)

        val sent = BaleApi.sendMessage(settings.botToken, settings.chatId, text)

        return if (sent) Result.success() else Result.retry()
    }
}
