package com.example.jungle.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

@Composable
fun JungleTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = JungleColors.Accent,
            onPrimary = JungleColors.Background,
            secondary = JungleColors.SurfaceHigh,
            onSecondary = JungleColors.TextMain,
            background = JungleColors.Background,
            onBackground = JungleColors.TextMain,
            surface = JungleColors.Surface,
            onSurface = JungleColors.TextMain,
            surfaceVariant = JungleColors.SurfaceHigh,
            onSurfaceVariant = JungleColors.TextMuted,
            error = JungleColors.Danger,
            outline = JungleColors.Outline
        ),
        typography = JungleTypography,
        content = content
    )
}

/** Виньетка: затемнение к краям экрана. Рисуется поверх содержимого и не мешает нажатиям. */
fun Modifier.vignette(): Modifier = this.drawWithContent {
    drawContent()
    drawRect(
        brush = Brush.radialGradient(
            colors = listOf(Color.Transparent, Color(0xCC000000)),
            center = center,
            radius = size.maxDimension * 0.8f
        )
    )
}
