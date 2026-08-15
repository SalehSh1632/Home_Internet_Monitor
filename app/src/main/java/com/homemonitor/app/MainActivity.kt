package com.homemonitor.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.homemonitor.app.data.SettingsStore
import com.homemonitor.app.ui.DashboardScreen
import com.homemonitor.app.ui.EnterPinScreen
import com.homemonitor.app.ui.HomeMonitorTheme
import com.homemonitor.app.ui.OnboardingScreen
import com.homemonitor.app.ui.SetPinScreen
import com.homemonitor.app.ui.SettingsScreen
import com.homemonitor.app.worker.CommandCheckScheduler
import com.homemonitor.app.worker.WorkScheduler
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HomeMonitorTheme {
                Surface(modifier = Modifier, color = MaterialTheme.colorScheme.background) {
                    AppNav()
                }
            }
        }
    }
}

@Composable
fun AppNav() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val store = remember { SettingsStore(context) }
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()

    var checked by remember { mutableStateOf(false) }
    var pinIsSet by remember { mutableStateOf(false) }
    var setupDone by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        pinIsSet = store.pinIsSet.first()
        setupDone = store.setupDone.first()
        checked = true
        if (setupDone) {
            // زنجیره‌ی زمان‌بندی چک بات را دوباره برقرار کن (مثلاً بعد از باز شدن اپ یا ری‌استارت گوشی)
            if (store.commandCheckEnabled.first()) CommandCheckScheduler.scheduleNext(context)
        }
    }

    if (!checked) return

    val startDestination = when {
        !pinIsSet -> "set_pin"
        else -> "enter_pin"
    }

    fun goToNextAfterUnlock() {
        val dest = if (setupDone) "dashboard" else "onboarding"
        navController.navigate(dest) {
            popUpTo(0) { inclusive = true }
        }
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable("set_pin") {
            SetPinScreen { pin ->
                scope.launch {
                    store.savePin(pin)
                    pinIsSet = true
                    goToNextAfterUnlock()
                }
            }
        }
        composable("enter_pin") {
            EnterPinScreen { goToNextAfterUnlock() }
        }
        composable("onboarding") {
            OnboardingScreen { deviceName, botToken, chatId ->
                scope.launch {
                    store.saveOnboarding(deviceName, botToken, chatId)
                    val interval = store.reportIntervalHours.first()
                    WorkScheduler.schedule(context, interval)
                    CommandCheckScheduler.scheduleNext(context)
                    navController.navigate("dashboard") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
        }
        composable("dashboard") {
            DashboardScreen(onOpenSettings = { navController.navigate("settings") })
        }
        composable("settings") {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
