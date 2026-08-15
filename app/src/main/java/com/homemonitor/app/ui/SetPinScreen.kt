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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun SetPinScreen(onPinSet: (pin: String) -> Unit) {
    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    val stage = if (pin.length < 4) 1 else 2

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
        Text(
            if (stage == 1) "یک رمز ۴ رقمی انتخاب کن" else "رمز را دوباره وارد کن",
            style = MaterialTheme.typography.headlineSmall,
            color = Brand.Slate200,
            textAlign = TextAlign.Center
        )
        Text(
            "این رمز بعد از این، هر بار برای باز کردن اپ لازم است.",
            style = MaterialTheme.typography.bodyMedium,
            color = Brand.Slate400,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = Spacing.sm, bottom = Spacing.lg)
        )

        PinDots(length = 4, filled = if (stage == 1) pin.length else confirmPin.length)

        error?.let {
            Text(it, color = Brand.Rose, modifier = Modifier.padding(top = Spacing.md))
        }

        Spacer(Modifier.height(Spacing.xl))

        NumericKeypad(
            onDigit = { d ->
                error = null
                if (stage == 1) {
                    if (pin.length < 4) pin += d
                } else {
                    if (confirmPin.length < 4) {
                        confirmPin += d
                        if (confirmPin.length == 4) {
                            when {
                                pin != confirmPin -> {
                                    error = "دو رمز با هم یکسان نیستند"
                                    pin = ""
                                    confirmPin = ""
                                }
                                else -> onPinSet(pin)
                            }
                        }
                    }
                }
            },
            onBackspace = {
                error = null
                if (stage == 1 && pin.isNotEmpty()) pin = pin.dropLast(1)
                if (stage == 2 && confirmPin.isNotEmpty()) confirmPin = confirmPin.dropLast(1)
            }
        )
    }
}
