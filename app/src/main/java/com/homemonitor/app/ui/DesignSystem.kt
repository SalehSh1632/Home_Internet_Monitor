package com.homemonitor.app.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/** فاصله‌های استاندارد؛ به‌جای عددهای پراکنده‌ی دستی در سرتاسر اپ. */
object Spacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 16.dp
    val lg = 24.dp
    val xl = 32.dp
    val xxl = 48.dp
}

/** رنگ‌های برند، جدا از colorScheme چون در گرادیان‌ها و جزئیات تصویری استفاده می‌شوند. */
object Brand {
    val Navy900 = Color(0xFF060B18)
    val Navy800 = Color(0xFF0F172A)
    val Navy700 = Color(0xFF1E293B)
    val Navy600 = Color(0xFF334155)
    val Slate400 = Color(0xFF94A3B8)
    val Slate200 = Color(0xFFE2E8F0)
    val CyanBright = Color(0xFF67E8F9)
    val Cyan = Color(0xFF22D3EE)
    val CyanDeep = Color(0xFF0891B2)
    val Violet = Color(0xFF818CF8)
    val Amber = Color(0xFFFBBF24)
    val Rose = Color(0xFFFB7185)
    val Emerald = Color(0xFF34D399)
}

val HeroGradient = Brush.linearGradient(
    colors = listOf(Brand.Navy900, Brand.CyanDeep.copy(alpha = 0.55f), Brand.Navy800)
)

val AccentGradient = Brush.linearGradient(
    colors = listOf(Brand.Cyan, Brand.Violet)
)

fun avatarGradientFor(seed: String): Brush {
    val palettes = listOf(
        listOf(Brand.Cyan, Brand.CyanDeep),
        listOf(Brand.Violet, Brand.CyanDeep),
        listOf(Brand.Amber, Brand.Rose),
        listOf(Brand.Emerald, Brand.CyanDeep),
        listOf(Brand.Rose, Brand.Violet)
    )
    val idx = (seed.hashCode().let { if (it < 0) -it else it }) % palettes.size
    return Brush.linearGradient(palettes[idx])
}

val HomeMonitorShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(36.dp)
)
