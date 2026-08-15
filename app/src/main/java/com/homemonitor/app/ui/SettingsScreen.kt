package com.homemonitor.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.homemonitor.app.data.SettingsStore
import com.homemonitor.app.data.UsageStatsHelper
import com.homemonitor.app.worker.CommandCheckScheduler
import com.homemonitor.app.worker.WorkScheduler
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private val INTERVAL_OPTIONS = listOf(1, 2, 3, 4, 6, 8, 12, 24)

@Composable
private fun SectionHeader(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(Brand.Navy600),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = Brand.Cyan, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(Spacing.sm))
        Text(title, style = MaterialTheme.typography.titleMedium, color = Brand.Slate200)
    }
}

@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val store = remember { SettingsStore(context) }
    val helper = remember { UsageStatsHelper(context) }
    val scope = rememberCoroutineScope()

    val allApps = remember { helper.getInstallableApps() }
    var selected by remember { mutableStateOf<Set<String>>(emptySet()) }
    var intervalHours by remember { mutableStateOf(3) }
    var intervalMenuExpanded by remember { mutableStateOf(false) }
    var reportCommand by remember { mutableStateOf("/report") }
    var cyclicEnabled by remember { mutableStateOf(true) }
    var commandCheckEnabled by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        selected = store.trackedPackages.first().toSet()
        intervalHours = store.reportIntervalHours.first()
        reportCommand = store.reportCommand.first()
        cyclicEnabled = store.cyclicEnabled.first()
        commandCheckEnabled = store.commandCheckEnabled.first()
    }

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
            Text("تنظیمات", style = MaterialTheme.typography.headlineSmall, color = Brand.Slate200)
            IconButton(
                onClick = onBack,
                modifier = Modifier.clip(CircleShape).background(Brand.Navy700)
            ) {
                Icon(Icons.Filled.ArrowBackIosNew, contentDescription = "بازگشت", tint = Brand.Cyan, modifier = Modifier.size(16.dp))
            }
        }

        Spacer(Modifier.height(Spacing.lg))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            item {
                SectionCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SectionHeader(Icons.Filled.Bolt, "گزارش دوره‌ای (سیکلی)")
                        Switch(
                            checked = cyclicEnabled,
                            onCheckedChange = { enabled ->
                                cyclicEnabled = enabled
                                scope.launch { store.setCyclicEnabled(enabled) }
                                if (enabled) WorkScheduler.schedule(context, intervalHours) else WorkScheduler.cancel(context)
                            }
                        )
                    }
                    Spacer(Modifier.height(Spacing.sm))
                    Text(
                        "ارسال خودکار گزارش هر چند ساعت یک‌بار به بله.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Brand.Slate400
                    )
                    Spacer(Modifier.height(Spacing.md))
                    Box {
                        OutlinedButton(
                            onClick = { intervalMenuExpanded = true },
                            shape = RoundedCornerShape(14.dp)
                        ) { Text("هر $intervalHours ساعت") }
                        DropdownMenu(
                            expanded = intervalMenuExpanded,
                            onDismissRequest = { intervalMenuExpanded = false }
                        ) {
                            INTERVAL_OPTIONS.forEach { hours ->
                                DropdownMenuItem(
                                    text = { Text("هر $hours ساعت") },
                                    onClick = {
                                        intervalHours = hours
                                        intervalMenuExpanded = false
                                        scope.launch {
                                            store.saveReportIntervalHours(hours)
                                            WorkScheduler.schedule(context, hours)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }

            item {
                SectionCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SectionHeader(Icons.Filled.RadioButtonChecked, "چک‌کردن گروه بله")
                        Switch(
                            checked = commandCheckEnabled,
                            onCheckedChange = { enabled ->
                                commandCheckEnabled = enabled
                                scope.launch { store.setCommandCheckEnabled(enabled) }
                                if (enabled) CommandCheckScheduler.scheduleNext(context) else CommandCheckScheduler.cancel(context)
                            }
                        )
                    }
                    Spacer(Modifier.height(Spacing.sm))
                    Text(
                        "چک‌کردن دستور سر ساعت‌های ۰۰:۰۰، ۰۶:۰۰، ۰۹:۰۰، ۱۲:۰۰، ۱۵:۰۰، ۱۸:۰۰ و ۲۱:۰۰.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Brand.Slate400
                    )
                    Spacer(Modifier.height(Spacing.md))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Terminal, contentDescription = null, tint = Brand.Slate400, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("دستور گزارش لحظه‌ای", style = MaterialTheme.typography.bodySmall, color = Brand.Slate400)
                    }
                    Spacer(Modifier.height(Spacing.sm))
                    OutlinedTextField(
                        value = reportCommand,
                        onValueChange = { reportCommand = it },
                        label = { Text("مثلاً /report") },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            item {
                SectionCard {
                    SectionHeader(Icons.Filled.Apps, "اپ‌های تحت نظر")
                    Spacer(Modifier.height(Spacing.sm))
                    Text(
                        "برای هر اپی که می‌خواهی مصرفش جداگانه در گزارش بیاید، تیک بزن.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Brand.Slate400
                    )
                    Spacer(Modifier.height(Spacing.sm))
                    allApps.forEach { (pkg, label) ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            LetterAvatar(label, size = 32.dp)
                            Spacer(Modifier.width(Spacing.sm))
                            Text(label, color = Brand.Slate200, modifier = Modifier.weight(1f))
                            Checkbox(
                                checked = selected.contains(pkg),
                                onCheckedChange = { checked ->
                                    selected = if (checked) selected + pkg else selected - pkg
                                }
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(Spacing.md))
        Button(
            onClick = {
                scope.launch {
                    store.saveTrackedPackages(selected.toList())
                    store.saveReportCommand(reportCommand)
                }
                onBack()
            },
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            Icon(Icons.Filled.Save, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("ذخیره")
        }
    }
}
