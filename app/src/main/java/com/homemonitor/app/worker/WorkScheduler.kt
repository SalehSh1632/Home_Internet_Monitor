package com.homemonitor.app.worker

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Constraints
import java.util.concurrent.TimeUnit

object WorkScheduler {
    private const val WORK_NAME = "usage_report_worker"

    /** زمان‌بندی گزارش دوره‌ای. اگر قبلاً زمان‌بندی شده بود، با بازه‌ی جدید جایگزین می‌شود.
     *  با Constraints(CONNECTED) اگر اینترنت قطع باشد، WorkManager خودش صبر می‌کند تا وصل شود؛
     *  دیگر نیازی به مدیریت دستی ارور اینترنت نیست. */
    fun schedule(context: Context, intervalHours: Int = 3) {
        val safeHours = intervalHours.coerceAtLeast(1) // WorkManager حداقل ~۱۵ دقیقه را می‌پذیرد؛ ساعت منطقی‌تر است
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = PeriodicWorkRequestBuilder<UsageReportWorker>(safeHours.toLong(), TimeUnit.HOURS)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            request
        )
    }

    /** توقف کامل گزارش دوره‌ای. */
    fun cancel(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }
}
