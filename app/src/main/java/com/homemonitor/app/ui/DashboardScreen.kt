package com.homemonitor.app.ui

import android.content.Intent
import android.provider.Settings as AndroidSettings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.homemonitor.app.data.SettingsStore
import com.homemonitor.app.data.UsageReport
import com.homemonitor.app.data.UsageStatsHelper
import com.homemonitor.app.data.formatBytes
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun DashboardScreen(onOpenSettings: () -> Unit) {
    val context = LocalContext.current
    val helper = remember { UsageStatsHelper(context) }
    val store = remember { SettingsStore(context) }
    val scope = rememberCoroutineScope()

    var hasAccess by remember { mutableStateOf(helper.hasUsageAccess()) }
    var report by remember { mutableStateOf<UsageReport?>(null) }
    var deviceName by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        deviceName = store.deviceName.first()
    }

    fun refresh() {
        hasAccess = helper.hasUsageAccess()
        if (!hasAccess) return
        scope.launch {
            val tracked = store.trackedPackages.first()
            val end = System.currentTimeMillis()
            val start = end - 24 * 60 * 60 * 1000L
            report = helper.getUsageReport(start, end, tracked)
        }
    }

    LaunchedEffect(hasAccess) { if (hasAccess) refresh() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(Spacing.lg)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("HomeMonitor", style = MaterialTheme.typography.headlineSmall, color = Brand.Slate200)
                Text(
                    if (deviceName.isNotBlank()) deviceName else "دستگاه",
                    style = MaterialTheme.typography.bodySmall,
                    color = Brand.Slate400
                )
            }
            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Brand.Navy700)
            ) {
                Icon(Icons.Filled.Settings, contentDescription = "تنظیمات", tint = Brand.Cyan)
            }
        }

        Spacer(Modifier.height(Spacing.lg))

        if (!hasAccess) {
            SectionCard {
                Icon(Icons.Filled.WifiOff, contentDescription = null, tint = Brand.Rose)
                Spacer(Modifier.height(Spacing.sm))
                Text(
                    "برای دیدن مصرف اینترنت، باید دسترسی «Usage Access» را از تنظیمات گوشی فعال کنی.",
                    color = Brand.Slate200
                )
                Spacer(Modifier.height(Spacing.md))
                Button(
                    onClick = { context.startActivity(Intent(AndroidSettings.ACTION_USAGE_ACCESS_SETTINGS)) },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) { Text("رفتن به تنظیمات") }
            }
        } else {
            GradientHero {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text("مصرف ۲۴ ساعت اخیر", color = Brand.Slate200.copy(alpha = 0.85f), style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(Spacing.xs))
                        Text(
                            formatBytes(report?.totalWifiBytes ?: -1L),
                            style = MaterialTheme.typography.headlineLarge,
                            color = Color.White
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Wifi, contentDescription = null, tint = Brand.CyanBright)
                    }
                }
                Spacer(Modifier.height(Spacing.md))
                OutlinedButton(
                    onClick = { refresh() },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("به‌روزرسانی")
                }
            }

            Spacer(Modifier.height(Spacing.lg))
            Text("اپ‌های انتخابی", style = MaterialTheme.typography.titleLarge, color = Brand.Slate200)
            Spacer(Modifier.height(Spacing.sm))

            val apps = report?.perApp ?: emptyList()
            val maxBytes = (apps.maxOfOrNull { it.totalBytes.coerceAtLeast(0L) } ?: 0L).coerceAtLeast(1L)

            if (apps.isEmpty()) {
                SectionCard {
                    Text("هنوز اپی برای پایش انتخاب نکرده‌ای. از تنظیمات اضافه کن.", color = Brand.Slate400)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    items(apps) { app ->
                        SectionCard {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                LetterAvatar(app.appLabel)
                                Spacer(Modifier.width(Spacing.md))
                                Column(Modifier.weight(1f)) {
                                    Text(app.appLabel, color = Brand.Slate200, style = MaterialTheme.typography.titleMedium)
                                    Spacer(Modifier.height(6.dp))
                                    UsageBar(
                                        fraction = if (app.totalBytes < 0) 0f else app.totalBytes.toFloat() / maxBytes.toFloat()
                                    )
                                }
                                Spacer(Modifier.width(Spacing.md))
                                Text(formatBytes(app.totalBytes), color = Brand.Cyan, style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }
                }
            }
        }
    }
}
