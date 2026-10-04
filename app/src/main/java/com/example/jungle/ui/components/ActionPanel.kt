package com.example.jungle.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jungle.engine.Direction
import com.example.jungle.ui.viewmodel.ActionMode
import com.example.jungle.ui.viewmodel.GameUiState

/** Верхняя строка: патроны, гранаты, состояние, клад. */
@Composable
fun StatusBar(ui: GameUiState, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Chip("Патроны: ${ui.ammo}")
        Chip("Гранаты: ${ui.grenades}")
        Chip(
            text = if (ui.isWounded) "Ранен" else "Здоров",
            color = if (ui.isWounded) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
        )
        Chip(
            text = if (ui.hasTreasure) "Клад: есть" else "Клад: нет",
            color = if (ui.hasTreasure) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun Chip(text: String, color: Color = MaterialTheme.colorScheme.onSurface) {
    Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.surface) {
        Text(
            text = text,
            color = color,
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
        )
    }
}

/** Панель действий: компас (С/Ю/З/В) и кнопки режимов «Выстрел» / «Граната». */
@Composable
fun ActionPanel(
    ui: GameUiState,
    onDirection: (Direction) -> Unit,
    onMode: (ActionMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val hint = when (ui.mode) {
        ActionMode.MOVE -> "Нажми направление, чтобы сделать шаг"
        ActionMode.SHOOT -> "Выбери направление выстрела"
        ActionMode.GRENADE -> "Выбери направление: стенка на пути будет взорвана"
    }
    Column(
        modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(hint, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ModeButton("Выстрел", ui.mode == ActionMode.SHOOT, !ui.isWounded && ui.ammo > 0) {
                    onMode(ActionMode.SHOOT)
                }
                ModeButton("Граната", ui.mode == ActionMode.GRENADE, !ui.isWounded && ui.grenades > 0) {
                    onMode(ActionMode.GRENADE)
                }
            }
            Compass(onDirection)
        }
    }
}

@Composable
private fun ModeButton(label: String, active: Boolean, enabled: Boolean, onClick: () -> Unit) {
    val modifier = Modifier.width(120.dp)
    if (active) {
        Button(onClick = onClick, enabled = enabled, modifier = modifier) { Text(label) }
    } else {
        OutlinedButton(onClick = onClick, enabled = enabled, modifier = modifier) { Text(label) }
    }
}

/** Крестовина: север сверху. */
@Composable
private fun Compass(onDirection: (Direction) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        DirButton("С ↑") { onDirection(Direction.NORTH) }
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            DirButton("← З") { onDirection(Direction.WEST) }
            Spacer(Modifier.size(64.dp))
            DirButton("В →") { onDirection(Direction.EAST) }
        }
        DirButton("Ю ↓") { onDirection(Direction.SOUTH) }
    }
}

@Composable
private fun DirButton(label: String, onClick: () -> Unit) {
    FilledTonalButton(
        onClick = onClick,
        modifier = Modifier.size(64.dp),
        contentPadding = PaddingValues(0.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(label, fontSize = 15.sp)
    }
}
