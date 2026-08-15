package com.homemonitor.app.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val HomeMonitorDarkColors = darkColorScheme(
    primary = Brand.Cyan,
    onPrimary = Color(0xFF00363A),
    primaryContainer = Brand.CyanDeep,
    onPrimaryContainer = Color(0xFFE0FCFF),
    secondary = Brand.Violet,
    onSecondary = Brand.Slate200,
    background = Brand.Navy900,
    onBackground = Brand.Slate200,
    surface = Brand.Navy700,
    onSurface = Brand.Slate200,
    surfaceVariant = Brand.Navy600,
    onSurfaceVariant = Brand.Slate400,
    outline = Brand.Navy600,
    error = Brand.Rose,
    onError = Brand.Navy900
)

private val HomeMonitorTypography = Typography(
    headlineLarge = TextStyle(fontWeight = FontWeight.ExtraBold, fontSize = 30.sp, letterSpacing = (-0.5).sp),
    headlineMedium = TextStyle(fontWeight = FontWeight.Bold, fontSize = 24.sp, letterSpacing = (-0.3).sp),
    headlineSmall = TextStyle(fontWeight = FontWeight.Bold, fontSize = 20.sp),
    titleLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 18.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 16.sp),
    bodyLarge = TextStyle(fontWeight = FontWeight.Normal, fontSize = 15.sp, lineHeight = 22.sp),
    bodyMedium = TextStyle(fontWeight = FontWeight.Normal, fontSize = 13.sp, lineHeight = 20.sp),
    bodySmall = TextStyle(fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 18.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.Medium, fontSize = 14.sp, letterSpacing = 0.2.sp)
)

@Composable
fun HomeMonitorTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = HomeMonitorDarkColors,
        typography = HomeMonitorTypography,
        shapes = HomeMonitorShapes,
        content = content
    )
}
