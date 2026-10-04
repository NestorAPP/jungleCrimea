package com.example.jungle.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.jungle.engine.Cell
import com.example.jungle.engine.GameMap
import com.example.jungle.engine.WallId
import com.example.jungle.ui.theme.JungleColors
import com.example.jungle.ui.viewmodel.NoteTool
import com.example.jungle.ui.viewmodel.NotesState

/**
 * Поле 4×4 с заметками игрока. Изначально пустое (в тумане).
 *  - тап по клетке ставит выбранную иконку;
 *  - с инструментом «Стенка» появляются зоны на границах клеток, тап по границе ставит/снимает стенку.
 */
@Composable
fun BoardView(
    notes: NotesState,
    onCellTap: (Cell) -> Unit,
    onWallEdgeTap: (Cell, Cell) -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        val side = minOf(maxWidth, 360.dp)
        val cellSize = side / GameMap.SIZE

        // Пульсация клада и выхода
        val pulse by rememberInfiniteTransition(label = "pulse").animateFloat(
            initialValue = 0.92f,
            targetValue = 1.08f,
            animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
            label = "pulseValue"
        )

        Box(
            Modifier
                .size(side)
                .clip(RoundedCornerShape(12.dp))
                .background(JungleColors.Surface)
        ) {
            Column {
                for (y in 0 until GameMap.SIZE) {
                    Row {
                        for (x in 0 until GameMap.SIZE) {
                            val cell = Cell(x, y)
                            NoteCell(
                                cell = cell,
                                size = cellSize,
                                icon = notes.cells[cell],
                                isMe = notes.me == cell,
                                tool = notes.tool,
                                walls = notes.walls,
                                pulse = pulse,
                                onTap = { onCellTap(cell) },
                                onEdge = onWallEdgeTap
                            )
                        }
                    }
                }
            }

            // Нарисованные стенки поверх клеток (нажатия пропускает)
            Canvas(Modifier.fillMaxSize()) {
                val c = size.width / GameMap.SIZE
                notes.walls.forEach { w ->
                    val a = w.cellA
                    val b = w.cellB
                    val start: Offset
                    val end: Offset
                    if (a.y == b.y) { // соседи по горизонтали: вертикальная стенка
                        val x = maxOf(a.x, b.x) * c
                        start = Offset(x, a.y * c)
                        end = Offset(x, (a.y + 1) * c)
                    } else {          // соседи по вертикали: горизонтальная стенка
                        val y = maxOf(a.y, b.y) * c
                        start = Offset(a.x * c, y)
                        end = Offset((a.x + 1) * c, y)
                    }
                    drawLine(Color(0xFFB07A52), start, end, strokeWidth = 7.dp.toPx(), cap = StrokeCap.Round)
                    drawLine(Color(0xFF5A3A24), start, end, strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round)
                }
            }
        }
    }
}

@Composable
private fun NoteCell(
    cell: Cell,
    size: Dp,
    icon: NoteTool?,
    isMe: Boolean,
    tool: NoteTool?,
    walls: Set<WallId>,
    pulse: Float,
    onTap: () -> Unit,
    onEdge: (Cell, Cell) -> Unit
) {
    // Зона границы видна: в режиме «Стенка» везде, в режиме «Ластик» — только где стенка есть
    fun showEdge(other: Cell): Boolean =
        tool == NoteTool.WALL || (tool == NoteTool.ERASER && WallId.of(cell, other) in walls)

    Box(
        Modifier
            .size(size)
            .padding(1.dp)
            .clip(RoundedCornerShape(6.dp))
            // «Туман»: светлее в центре клетки, мгла по краям
            .background(
                Brush.radialGradient(
                    listOf(JungleColors.SurfaceHigh.copy(alpha = 0.9f), JungleColors.Surface.copy(alpha = 0.7f))
                )
            )
            .clickable(enabled = tool != null && tool != NoteTool.WALL, onClick = onTap)
    ) {
        // Иконка клетки: плавное появление (fade + scale)
        AnimatedContent(
            targetState = icon,
            transitionSpec = {
                (fadeIn(tween(250)) + scaleIn(initialScale = 0.6f, animationSpec = tween(250))) togetherWith
                    fadeOut(tween(150))
            },
            modifier = Modifier.fillMaxSize(),
            label = "cellIcon"
        ) { ic ->
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                if (ic != null) NoteIconView(ic, size * 0.8f, pulse = pulse)
            }
        }

        if (isMe) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                NoteIconView(NoteTool.ME, size * 0.9f)
            }
        }

        // Зоны границ (только внутренние границы поля)
        val last = GameMap.SIZE - 1
        if (cell.y > 0 && showEdge(Cell(cell.x, cell.y - 1))) {
            EdgeZone(Alignment.TopCenter, horizontal = true) { onEdge(cell, Cell(cell.x, cell.y - 1)) }
        }
        if (cell.y < last && showEdge(Cell(cell.x, cell.y + 1))) {
            EdgeZone(Alignment.BottomCenter, horizontal = true) { onEdge(cell, Cell(cell.x, cell.y + 1)) }
        }
        if (cell.x > 0 && showEdge(Cell(cell.x - 1, cell.y))) {
            EdgeZone(Alignment.CenterStart, horizontal = false) { onEdge(cell, Cell(cell.x - 1, cell.y)) }
        }
        if (cell.x < last && showEdge(Cell(cell.x + 1, cell.y))) {
            EdgeZone(Alignment.CenterEnd, horizontal = false) { onEdge(cell, Cell(cell.x + 1, cell.y)) }
        }
    }
}

/** Нажимаемая полоска у края клетки. */
@Composable
private fun BoxScope.EdgeZone(alignment: Alignment, horizontal: Boolean, onClick: () -> Unit) {
    val shape = if (horizontal) Modifier.fillMaxWidth(0.7f).height(22.dp) else Modifier.fillMaxHeight(0.7f).width(22.dp)
    Box(
        Modifier
            .align(alignment)
            .then(shape)
            .background(JungleColors.Accent.copy(alpha = 0.18f), RoundedCornerShape(4.dp))
            .clickable(onClick = onClick)
    )
}
