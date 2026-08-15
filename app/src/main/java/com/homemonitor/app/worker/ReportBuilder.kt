package com.homemonitor.app.worker

import com.homemonitor.app.data.UsageReport
import com.homemonitor.app.data.formatBytes
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ReportBuilder {
    fun build(
        deviceName: String,
        start: Long,
        end: Long,
        report: UsageReport,
        title: String = "گزارش مصرف اینترنت"
    ): String {
        val timeFmt = SimpleDateFormat("HH:mm", Locale.getDefault())
        val sb = StringBuilder()
        sb.append("$title\n")
        sb.append("دستگاه: $deviceName\n")
        sb.append("بازه: ${timeFmt.format(Date(start))} تا ${timeFmt.format(Date(end))}\n\n")
        sb.append("مصرف کل وای‌فای: ${formatBytes(report.totalWifiBytes)}\n")

        if (report.perApp.isNotEmpty()) {
            sb.append("\nمصرف اپ‌های انتخابی:\n")
            for (app in report.perApp) {
                sb.append("• ${app.appLabel}: ${formatBytes(app.totalBytes)}\n")
            }
        }
        return sb.toString()
    }
}
