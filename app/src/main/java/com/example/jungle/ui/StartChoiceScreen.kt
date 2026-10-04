package com.example.jungle.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.jungle.engine.Cell
import com.example.jungle.engine.GameMap

/**
 * Выбор стартовой клетки: пустое поле 4×4, подсвечены 4 центральные клетки.
 * После «Начать игру» карта скрывается (мир генерирует движок).
 */
@Composable
fun StartChoiceScreen(onStart: (Cell) -> Unit, onBack: () -> Unit) {
    var selected by remember { mutableStateOf<Cell?>(null) }

    Column(
        Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Выбери старт", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(8.dp))
        Text(
            "Стартовать можно только в центральных клетках. Что там — узнаешь, когда начнёшь.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))

        for (y in 0 until GameMap.SIZE) {
            Row {
                for (x in 0 until GameMap.SIZE) {
                    val cell = Cell(x, y)
                    StartCell(
                        isStart = cell in GameMap.START_CELLS,
                        isSelected = cell == selected,
                        onClick = { selected = cell }
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        Button(onClick = { selected?.let(onStart) }, enabled = selected != null) { Text("Начать игру") }
        TextButton(onClick = onBack) { Text("Назад") }
    }
}

@Composable
private fun StartCell(isStart: Boolean, isSelected: Boolean, onClick: () -> Unit) {
    val scale by animateFloatAsState(if (isSelected) 1.08f else 1f, label = "cellScale")
    val shape = RoundedCornerShape(8.dp)
    val colors = MaterialTheme.colorScheme

    Box(
        Modifier
            .padding(2.dp)
            .size(68.dp)
            .scale(scale)
            .clip(shape)
            .background(
                when {
                    isSelected -> colors.primary.copy(alpha = 0.35f)
                    isStart -> colors.surfaceVariant
                    else -> colors.surface.copy(alpha = 0.5f)
                }
            )
            .border(if (isStart) 2.dp else 1.dp, if (isSelected) colors.primary else colors.outline, shape)
            .clickable(enabled = isStart, onClick = onClick)
    )
}
