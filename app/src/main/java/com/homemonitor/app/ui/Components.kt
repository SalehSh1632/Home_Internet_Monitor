package com.homemonitor.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** کارت هرو با پس‌زمینه‌ی گرادیانی؛ برای هدر داشبورد. */
@Composable
fun GradientHero(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(HeroGradient)
            .border(1.dp, Brand.CyanBright.copy(alpha = 0.18f), RoundedCornerShape(28.dp))
            .padding(Spacing.lg)
    ) {
        Column(content = content)
    }
}

/** آواتار دایره‌ای رنگی با حرف اول اسم؛ جایگزین آیکون واقعی اپ‌ها بدون نیاز به کتابخانه‌ی اضافه. */
@Composable
fun LetterAvatar(label: String, size: Dp = 40.dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(avatarGradientFor(label)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label.trim().firstOrNull()?.uppercase() ?: "?",
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}

/** نوار پیشرفت باریک با گوشه‌ی گرد و رنگ گرادیانی؛ برای نمایش سهم مصرف هر اپ. */
@Composable
fun UsageBar(fraction: Float, modifier: Modifier = Modifier) {
    val safeFraction = fraction.coerceIn(0f, 1f)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(50))
            .background(Brand.Navy600)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(safeFraction)
                .fillMaxHeight()
                .clip(RoundedCornerShape(50))
                .background(AccentGradient)
        )
    }
}

/** یک کارت بخش با عنوان، آیکون و توضیح؛ برای گروه‌بندی حرفه‌ای تنظیمات. */
@Composable
fun SectionCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Brand.Navy700),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(Spacing.md), content = content)
    }
}

/** ردیف چهار دایره برای نمایش تعداد رقم واردشده‌ی پین. */
@Composable
fun PinDots(length: Int, filled: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
        repeat(length) { index ->
            val isFilled = index < filled
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(if (isFilled) Brand.Cyan else Brand.Navy600)
                    .border(1.dp, if (isFilled) Brand.CyanBright else Brand.Navy600, CircleShape)
            )
        }
    }
}

/** صفحه‌کلید عددی سفارشی برای ورود پین به‌جای فیلد متنی ساده. */
@Composable
fun NumericKeypad(
    onDigit: (String) -> Unit,
    onBackspace: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("", "0", "back")
    )
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                row.forEach { key ->
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .then(
                                if (key.isNotEmpty())
                                    Modifier
                                        .background(Brand.Navy700)
                                        .clickable {
                                            if (key == "back") onBackspace() else onDigit(key)
                                        }
                                else Modifier
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        when (key) {
                            "back" -> Icon(Icons.Filled.Backspace, contentDescription = "پاک کردن", tint = Brand.Slate200)
                            "" -> {}
                            else -> Text(key, style = MaterialTheme.typography.headlineSmall, color = Brand.Slate200)
                        }
                    }
                }
            }
        }
    }
}
