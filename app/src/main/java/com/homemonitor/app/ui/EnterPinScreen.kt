package com.homemonitor.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.homemonitor.app.data.SettingsStore
import kotlinx.coroutines.launch

@Composable
fun EnterPinScreen(onCorrect: () -> Unit) {
    val context = LocalContext.current
    val store = remember { SettingsStore(context) }
    val scope = rememberCoroutineScope()

    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    fun tryUnlock(value: String) {
        scope.launch {
            if (store.verifyPin(value)) {
                error = null
                onCorrect()
            } else {
                error = "رمز اشتباه است"
                pin = ""
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(Spacing.lg),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(AccentGradient),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.Lock, contentDescription = null, tint = Color.White)
        }
        Spacer(Modifier.height(Spacing.lg))
        Text("ورود به اپ", style = MaterialTheme.typography.headlineSmall, color = Brand.Slate200, textAlign = TextAlign.Center)
        Text(
            "رمز ۴ رقمی را وارد کن",
            style = MaterialTheme.typography.bodyMedium,
            color = Brand.Slate400,
            modifier = Modifier.padding(top = Spacing.sm, bottom = Spacing.lg)
        )

        PinDots(length = 4, filled = pin.length)

        error?.let {
            Text(it, color = Brand.Rose, modifier = Modifier.padding(top = Spacing.md))
        }

        Spacer(Modifier.height(Spacing.xl))

        NumericKeypad(
            onDigit = { d ->
                if (pin.length < 4) {
                    pin += d
                    error = null
                    if (pin.length == 4) tryUnlock(pin)
                }
            },
            onBackspace = {
                if (pin.isNotEmpty()) { pin = pin.dropLast(1); error = null }
            }
        )
    }
}
