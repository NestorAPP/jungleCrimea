package com.example.jungle.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jungle.ui.viewmodel.NoteTool

/** Палитра: тапнул иконку — она выбрана, тапнул клетку — иконка встала. */
@Composable
fun IconPalette(selected: NoteTool?, onSelect: (NoteTool) -> Unit, modifier: Modifier = Modifier) {
    val hint = when (selected) {
        null -> "Выбери иконку, затем нажми клетку"
        NoteTool.WALL -> "Нажми на границу между клетками"
        NoteTool.ME -> "Нажми клетку, где ты сейчас находишься"
        NoteTool.ERASER -> "Нажми клетку или стенку, чтобы стереть"
        else -> "Нажми клетку: ${selected.label}"
    }
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            NoteTool.values().forEach { tool ->
                PaletteItem(tool, tool == selected) { onSelect(tool) }
            }
        }
        Text(hint, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun PaletteItem(tool: NoteTool, active: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(10.dp)
    val colors = MaterialTheme.colorScheme
    Column(
        Modifier
            .clip(shape)
            .background(if (active) colors.primary.copy(alpha = 0.25f) else colors.surface)
            .border(if (active) 2.dp else 1.dp, if (active) colors.primary else colors.outline, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        NoteIconView(tool, 36.dp)
        Text(tool.label, fontSize = 10.sp, color = colors.onSurface)
    }
}
