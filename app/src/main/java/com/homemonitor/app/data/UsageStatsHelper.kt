package com.homemonitor.app.data

import android.app.AppOpsManager
import android.app.usage.NetworkStats
import android.app.usage.NetworkStatsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.Build
import android.os.Process

data class AppUsage(val packageName: String, val appLabel: String, val totalBytes: Long)

data class UsageReport(
    val totalWifiBytes: Long,
    val perApp: List<AppUsage>
)

class UsageStatsHelper(private val context: Context) {

    /** آیا اجازه‌ی «دسترسی به آمار استفاده» برای اپ فعال است؟ */
    fun hasUsageAccess(): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.unsafeCheckOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    /** لیست اپ‌های قابل انتخاب کاربر (اپ‌های دارای آیکون در launcher) */
    fun getInstallableApps(): List<Pair<String, String>> {
        val pm = context.packageManager
        val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        return apps.filter { pm.getLaunchIntentForPackage(it.packageName) != null }
            .map { it.packageName to pm.getApplicationLabel(it).toString() }
            .sortedBy { it.second }
    }

    /**
     * مصرف وای‌فای کل دستگاه و اپ‌های مشخص‌شده در بازه [startMillis, endMillis].
     * نیازمند اجازه‌ی PACKAGE_USAGE_STATS (از تنظیمات سیستم).
     */
    fun getUsageReport(startMillis: Long, endMillis: Long, trackedPackages: List<String>): UsageReport {
        val nsm = context.getSystemService(Context.NETWORK_STATS_SERVICE) as NetworkStatsManager
        val pm = context.packageManager

        var totalBytes = 0L
        try {
            val bucket = NetworkStats.Bucket()
            @Suppress("DEPRECATION")
            val stats = nsm.querySummary(ConnectivityManager.TYPE_WIFI, null, startMillis, endMillis)
            while (stats.hasNextBucket()) {
                stats.getNextBucket(bucket)
                totalBytes += bucket.rxBytes + bucket.txBytes
            }
            stats.close()
        } catch (e: Exception) {
            totalBytes = -1L // یعنی خطا / نبود دسترسی
        }

        val perApp = mutableListOf<AppUsage>()
        for (pkg in trackedPackages) {
            if (pkg.isBlank()) continue
            try {
                val uid = pm.getApplicationInfo(pkg, 0).uid
                var appBytes = 0L
                val bucket = NetworkStats.Bucket()
                @Suppress("DEPRECATION")
                val stats = nsm.queryDetailsForUid(ConnectivityManager.TYPE_WIFI, null, startMillis, endMillis, uid)
                while (stats.hasNextBucket()) {
                    stats.getNextBucket(bucket)
                    appBytes += bucket.rxBytes + bucket.txBytes
                }
                stats.close()
                val label = try {
                    pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)).toString()
                } catch (e: Exception) { pkg }
                perApp.add(AppUsage(pkg, label, appBytes))
            } catch (e: Exception) {
                perApp.add(AppUsage(pkg, pkg, -1L))
            }
        }
        return UsageReport(totalBytes, perApp)
    }
}

/** تبدیل بایت به رشته‌ی خوانا مثل ۲۵۳ مگابایت یا ۱.۲ گیگابایت */
fun formatBytes(bytes: Long): String {
    if (bytes < 0) return "نامشخص"
    val mb = bytes / 1024.0 / 1024.0
    return if (mb >= 1024) String.format("%.2f گیگابایت", mb / 1024.0)
    else String.format("%.1f مگابایت", mb)
}
