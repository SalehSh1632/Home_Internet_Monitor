package com.homemonitor.app.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * به‌جای چک مداوم هر چند ثانیه، فقط سر ساعت‌های ثابت روز چک می‌کند:
 * ۰۰:۰۰، ۰۶:۰۰، ۰۹:۰۰، ۱۲:۰۰، ۱۵:۰۰، ۱۸:۰۰، ۲۱:۰۰ — و به همین ترتیب هر روز.
 * هر بار بعد از اجرا، خودش نوبت بعدی را زمان‌بندی می‌کند (زنجیره‌ی خودتکرار).
 */
object CommandCheckScheduler {
    private const val WORK_NAME = "command_check_worker"

    // دقیقه از نیمه‌شب برای هر ساعت چک
    private val DAILY_SLOTS_MINUTES = listOf(0, 360, 540, 720, 900, 1080, 1260) // 00:00,06:00,09:00,12:00,15:00,18:00,21:00

    /** نوبت بعدی را زمان‌بندی می‌کند. اگر چک غیرفعال باشد، خود Worker کاری نمی‌کند ولی چرخه زنده می‌ماند. */
    fun scheduleNext(context: Context) {
        val delayMillis = millisUntilNextSlot()
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED) // اگر اینترنت نبود، صبر می‌کند تا وصل شود
            .build()

        val request = OneTimeWorkRequestBuilder<CommandCheckWorker>()
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    /** توقف کامل چک‌کردن گروه/بات. */
    fun cancel(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }

    private fun millisUntilNextSlot(): Long {
        val now = Calendar.getInstance()
        val nowMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
        val sorted = DAILY_SLOTS_MINUTES.sorted()
        val nextMinute = sorted.firstOrNull { it > nowMinutes } ?: (sorted.first() + 24 * 60)

        val target = now.clone() as Calendar
        target.set(Calendar.SECOND, 0)
        target.set(Calendar.MILLISECOND, 0)
        target.add(Calendar.MINUTE, nextMinute - nowMinutes)

        return (target.timeInMillis - now.timeInMillis).coerceAtLeast(1000L)
    }
}
