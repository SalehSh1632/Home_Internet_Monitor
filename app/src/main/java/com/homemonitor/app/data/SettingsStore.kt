package com.homemonitor.app.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.security.MessageDigest

// یک نمونه‌ی سراسری از DataStore برای کل اپ
val Context.dataStore by preferencesDataStore(name = "home_monitor_settings")

object Keys {
    val DEVICE_NAME = stringPreferencesKey("device_name")
    val BOT_TOKEN = stringPreferencesKey("bot_token")
    val CHAT_ID = stringPreferencesKey("chat_id")
    // نام پکیج اپ‌های انتخابی، جدا شده با کاما. مثال: com.instagram.android,com.google.android.youtube
    val TRACKED_PACKAGES = stringPreferencesKey("tracked_packages")
    val SETUP_DONE = stringPreferencesKey("setup_done")
    val REPORT_INTERVAL_HOURS = intPreferencesKey("report_interval_hours")
    val PIN_HASH = stringPreferencesKey("pin_hash")
    // متنی که در گروه بله باعث ارسال فوری گزارش می‌شود، مثلاً /report
    val REPORT_COMMAND = stringPreferencesKey("report_command")
    // شناسه‌ی آخرین پیام بله‌ای که این دستگاه پردازش کرده (برای جلوگیری از پاسخ تکراری)
    val LAST_UPDATE_ID = longPreferencesKey("last_update_id")
    // آیا گزارش دوره‌ای (سیکلی) فعال است؟
    val CYCLIC_ENABLED = booleanPreferencesKey("cyclic_enabled")
    // آیا چک‌کردن گروه برای دستور گزارش لحظه‌ای فعال است؟
    val COMMAND_CHECK_ENABLED = booleanPreferencesKey("command_check_enabled")
}

/** هش کردن رمز با SHA-256 تا رمز خام هیچ‌جا ذخیره نشود. */
fun hashPin(pin: String): String {
    val digest = MessageDigest.getInstance("SHA-256").digest(pin.toByteArray())
    return digest.joinToString("") { "%02x".format(it) }
}

class SettingsStore(private val context: Context) {

    val deviceName: Flow<String> = context.dataStore.data.map { it[Keys.DEVICE_NAME] ?: "" }
    val botToken: Flow<String> = context.dataStore.data.map { it[Keys.BOT_TOKEN] ?: "" }
    val chatId: Flow<String> = context.dataStore.data.map { it[Keys.CHAT_ID] ?: "" }
    val trackedPackages: Flow<List<String>> = context.dataStore.data.map {
        val raw = it[Keys.TRACKED_PACKAGES] ?: ""
        if (raw.isBlank()) emptyList() else raw.split(",")
    }
    val setupDone: Flow<Boolean> = context.dataStore.data.map { (it[Keys.SETUP_DONE] ?: "false") == "true" }
    val reportIntervalHours: Flow<Int> = context.dataStore.data.map { it[Keys.REPORT_INTERVAL_HOURS] ?: 3 }
    val pinIsSet: Flow<Boolean> = context.dataStore.data.map { !(it[Keys.PIN_HASH] ?: "").isBlank() }
    val reportCommand: Flow<String> = context.dataStore.data.map { it[Keys.REPORT_COMMAND] ?: "/report" }
    val lastUpdateId: Flow<Long> = context.dataStore.data.map { it[Keys.LAST_UPDATE_ID] ?: 0L }
    val cyclicEnabled: Flow<Boolean> = context.dataStore.data.map { it[Keys.CYCLIC_ENABLED] ?: true }
    val commandCheckEnabled: Flow<Boolean> = context.dataStore.data.map { it[Keys.COMMAND_CHECK_ENABLED] ?: true }

    suspend fun saveOnboarding(deviceName: String, botToken: String, chatId: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.DEVICE_NAME] = deviceName
            prefs[Keys.BOT_TOKEN] = botToken
            prefs[Keys.CHAT_ID] = chatId
            prefs[Keys.SETUP_DONE] = "true"
        }
    }

    suspend fun saveTrackedPackages(packages: List<String>) {
        context.dataStore.edit { prefs ->
            prefs[Keys.TRACKED_PACKAGES] = packages.joinToString(",")
        }
    }

    suspend fun saveReportIntervalHours(hours: Int) {
        context.dataStore.edit { prefs ->
            prefs[Keys.REPORT_INTERVAL_HOURS] = hours
        }
    }

    suspend fun savePin(pin: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.PIN_HASH] = hashPin(pin)
        }
    }

    suspend fun saveReportCommand(command: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.REPORT_COMMAND] = command.ifBlank { "/report" }
        }
    }

    suspend fun saveLastUpdateId(id: Long) {
        context.dataStore.edit { prefs ->
            prefs[Keys.LAST_UPDATE_ID] = id
        }
    }

    suspend fun setCyclicEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[Keys.CYCLIC_ENABLED] = enabled }
    }

    suspend fun setCommandCheckEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[Keys.COMMAND_CHECK_ENABLED] = enabled }
    }

    suspend fun verifyPin(pin: String): Boolean {
        val stored = context.dataStore.data.first()[Keys.PIN_HASH] ?: ""
        return stored.isNotBlank() && stored == hashPin(pin)
    }

    // خواندن یک‌باره (برای استفاده داخل Worker که Composable نیست)
    suspend fun snapshot(): Settings {
        val prefs = context.dataStore.data.first()
        val raw = prefs[Keys.TRACKED_PACKAGES] ?: ""
        return Settings(
            deviceName = prefs[Keys.DEVICE_NAME] ?: "دستگاه ناشناس",
            botToken = prefs[Keys.BOT_TOKEN] ?: "",
            chatId = prefs[Keys.CHAT_ID] ?: "",
            trackedPackages = if (raw.isBlank()) emptyList() else raw.split(","),
            reportIntervalHours = prefs[Keys.REPORT_INTERVAL_HOURS] ?: 3
        )
    }
}

data class Settings(
    val deviceName: String,
    val botToken: String,
    val chatId: String,
    val trackedPackages: List<String>,
    val reportIntervalHours: Int
)
