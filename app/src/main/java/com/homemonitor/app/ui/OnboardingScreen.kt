package com.homemonitor.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun OnboardingScreen(
    onFinish: (deviceName: String, botToken: String, chatId: String) -> Unit
) {
    var deviceName by remember { mutableStateOf("") }
    var botToken by remember { mutableStateOf("") }
    var chatId by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        Spacer(Modifier.height(Spacing.md))
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(AccentGradient),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.Shield, contentDescription = null, tint = Color.White)
        }
        Spacer(Modifier.height(Spacing.sm))
        Text(
            "راه‌اندازی اولیه",
            style = MaterialTheme.typography.headlineMedium,
            color = Brand.Slate200,
            textAlign = TextAlign.Right,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            "بازو (بات) را در اپ بله با @botfather بساز و اطلاعاتش را اینجا وارد کن. این اطلاعات فقط یک‌بار لازم است.",
            style = MaterialTheme.typography.bodyMedium,
            color = Brand.Slate400
        )

        Spacer(Modifier.height(Spacing.sm))

        SectionCard {
            OutlinedTextField(
                value = deviceName,
                onValueChange = { deviceName = it },
                label = { Text("نام این دستگاه (مثلاً: گوشی علی)") },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(Spacing.sm))
            OutlinedTextField(
                value = botToken,
                onValueChange = { botToken = it },
                label = { Text("توکن بازوی بله") },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(Spacing.sm))
            OutlinedTextField(
                value = chatId,
                onValueChange = { chatId = it },
                label = { Text("Chat ID") },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.height(Spacing.sm))

        Button(
            onClick = { onFinish(deviceName.trim(), botToken.trim(), chatId.trim()) },
            enabled = deviceName.isNotBlank() && botToken.isNotBlank() && chatId.isNotBlank(),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            Text("ذخیره و ادامه")
            Spacer(Modifier.width(8.dp))
            Icon(Icons.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
        }
    }
}
