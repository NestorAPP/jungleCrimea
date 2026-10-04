package com.example.jungle.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import com.example.jungle.ui.theme.JungleColors
import com.example.jungle.ui.viewmodel.NoteTool

private object IconColors {
    val River = Color(0xFF5FB3B3)
    val Estuary = Color(0xFF7FD1D1)
    val TunnelA = Color(0xFFD1A06B)
    val TunnelB = Color(0xFFA99BE0)
    val Arsenal = Color(0xFFD08C3C)
    val Hospital = Color(0xFFE05A5A)
    val Exit = Color(0xFF9BD17A)
    val Wall = Color(0xFFB07A52)
    val Eraser = Color(0xFFD8A7B1)
}

/**
 * Иконка инструмента: Canvas + (для тоннелей) буква поверх.
 * [pulse] — масштаб пульсации (применяется только к кладу и выходу).
 */
@Composable
fun NoteIconView(tool: NoteTool, size: Dp, modifier: Modifier = Modifier, pulse: Float = 1f) {
    Box(modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) { drawNoteTool(tool, pulse) }
        val letter = when (tool) {
            NoteTool.TUNNEL_A -> "A" to IconColors.TunnelA
            NoteTool.TUNNEL_B -> "B" to IconColors.TunnelB
            else -> null
        }
        if (letter != null) {
            Text(
                text = letter.first,
                color = letter.second,
                fontWeight = FontWeight.Bold,
                fontSize = (size.value * 0.34f).sp
            )
        }
    }
}

/** Рисует иконку в квадрате текущего DrawScope (все размеры — доли стороны). */
fun DrawScope.drawNoteTool(tool: NoteTool, pulse: Float = 1f) {
    val s = size.minDimension
    val p = if (tool == NoteTool.TREASURE || tool == NoteTool.EXIT) pulse else 1f

    withTransform({ scale(p, p, center) }) {
        when (tool) {
            NoteTool.RIVER -> {
                wave(0.30f * s, IconColors.River, s)
                wave(0.50f * s, IconColors.River, s)
                wave(0.70f * s, IconColors.River, s)
            }
            NoteTool.ESTUARY -> {
                // Река расходится веером, красная точка — глаз крокодила
                listOf(0.18f, 0.5f, 0.82f).forEach { x ->
                    drawLine(
                        color = IconColors.Estuary,
                        start = Offset(0.5f * s, 0.2f * s),
                        end = Offset(x * s, 0.82f * s),
                        strokeWidth = 0.06f * s,
                        cap = StrokeCap.Round
                    )
                }
                drawCircle(JungleColors.Danger, radius = 0.08f * s, center = Offset(0.5f * s, 0.2f * s))
            }
            NoteTool.TUNNEL_A -> tunnel(IconColors.TunnelA, s)
            NoteTool.TUNNEL_B -> tunnel(IconColors.TunnelB, s)
            NoteTool.ARSENAL -> {
                // Ящик с перекрестием
                val c = IconColors.Arsenal
                drawRoundRect(
                    c, Offset(0.2f * s, 0.25f * s), Size(0.6f * s, 0.5f * s),
                    CornerRadius(0.05f * s), style = Stroke(0.06f * s)
                )
                drawLine(c, Offset(0.2f * s, 0.25f * s), Offset(0.8f * s, 0.75f * s), strokeWidth = 0.05f * s)
                drawLine(c, Offset(0.8f * s, 0.25f * s), Offset(0.2f * s, 0.75f * s), strokeWidth = 0.05f * s)
            }
            NoteTool.HOSPITAL -> {
                val c = IconColors.Hospital
                drawCircle(Color.White.copy(alpha = 0.12f), radius = 0.42f * s)
                drawRoundRect(c, Offset(0.4f * s, 0.2f * s), Size(0.2f * s, 0.6f * s), CornerRadius(0.03f * s))
                drawRoundRect(c, Offset(0.2f * s, 0.4f * s), Size(0.6f * s, 0.2f * s), CornerRadius(0.03f * s))
            }
            NoteTool.TREASURE -> {
                // Сундук: корпус, закруглённая крышка, замок
                val gold = JungleColors.Accent
                drawRoundRect(gold, Offset(0.18f * s, 0.45f * s), Size(0.64f * s, 0.35f * s), CornerRadius(0.05f * s))
                drawArc(
                    gold, startAngle = 180f, sweepAngle = 180f, useCenter = true,
                    topLeft = Offset(0.18f * s, 0.2f * s), size = Size(0.64f * s, 0.5f * s)
                )
                drawRect(JungleColors.Background, Offset(0.46f * s, 0.5f * s), Size(0.08f * s, 0.14f * s))
            }
            NoteTool.EXIT -> {
                // Дверной проём и стрелка наружу
                val c = IconColors.Exit
                drawRoundRect(
                    c, Offset(0.2f * s, 0.15f * s), Size(0.42f * s, 0.7f * s),
                    CornerRadius(0.04f * s), style = Stroke(0.06f * s)
                )
                drawLine(c, Offset(0.4f * s, 0.5f * s), Offset(0.86f * s, 0.5f * s), strokeWidth = 0.06f * s, cap = StrokeCap.Round)
                drawLine(c, Offset(0.86f * s, 0.5f * s), Offset(0.74f * s, 0.38f * s), strokeWidth = 0.06f * s, cap = StrokeCap.Round)
                drawLine(c, Offset(0.86f * s, 0.5f * s), Offset(0.74f * s, 0.62f * s), strokeWidth = 0.06f * s, cap = StrokeCap.Round)
            }
            NoteTool.WALL -> {
                // Кирпичная кладка
                val c = IconColors.Wall
                drawRect(c, Offset(0.12f * s, 0.25f * s), Size(0.76f * s, 0.5f * s), style = Stroke(0.05f * s))
                drawLine(c, Offset(0.12f * s, 0.5f * s), Offset(0.88f * s, 0.5f * s), strokeWidth = 0.04f * s)
                drawLine(c, Offset(0.4f * s, 0.25f * s), Offset(0.4f * s, 0.5f * s), strokeWidth = 0.04f * s)
                drawLine(c, Offset(0.64f * s, 0.5f * s), Offset(0.64f * s, 0.75f * s), strokeWidth = 0.04f * s)
            }
            NoteTool.ME -> {
                drawCircle(JungleColors.TextMain, radius = 0.4f * s, style = Stroke(0.06f * s))
                drawCircle(JungleColors.Accent, radius = 0.14f * s)
            }
            NoteTool.ERASER -> {
                withTransform({ rotate(-35f, center) }) {
                    drawRoundRect(IconColors.Eraser, Offset(0.18f * s, 0.34f * s), Size(0.64f * s, 0.32f * s), CornerRadius(0.06f * s))
                    drawRect(Color.White.copy(alpha = 0.35f), Offset(0.5f * s, 0.34f * s), Size(0.32f * s, 0.32f * s))
                }
            }
        }
    }
}

/** Волна реки. */
private fun DrawScope.wave(y: Float, color: Color, s: Float) {
    val path = Path().apply {
        moveTo(0.12f * s, y)
        quadraticBezierTo(0.31f * s, y - 0.12f * s, 0.5f * s, y)
        quadraticBezierTo(0.69f * s, y + 0.12f * s, 0.88f * s, y)
    }
    drawPath(path, color, style = Stroke(width = 0.07f * s, cap = StrokeCap.Round))
}

/** Тоннель: тёмная «дыра» с кольцом (буква A/B рисуется поверх в [NoteIconView]). */
private fun DrawScope.tunnel(color: Color, s: Float) {
    drawCircle(color.copy(alpha = 0.22f), radius = 0.34f * s)
    drawCircle(color, radius = 0.4f * s, style = Stroke(0.07f * s))
}
