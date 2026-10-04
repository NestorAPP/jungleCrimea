package com.example.jungle.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.jungle.engine.Cell
import com.example.jungle.engine.Direction
import com.example.jungle.ui.components.ActionPanel
import com.example.jungle.ui.components.BoardView
import com.example.jungle.ui.components.EventLog
import com.example.jungle.ui.components.IconPalette
import com.example.jungle.ui.components.NotesArea
import com.example.jungle.ui.components.StatusBar
import com.example.jungle.ui.viewmodel.ActionMode
import com.example.jungle.ui.viewmodel.GameUiState
import com.example.jungle.ui.viewmodel.NoteTool
import com.example.jungle.ui.viewmodel.NotesState
import kotlin.math.roundToInt

/**
 * Экран игры.
 * Сверху вниз: статус → (прокручиваемая область: поле заметок, палитра, текстовые заметки)
 * → лог событий → панель действий (всегда на виду).
 */
@Composable
fun GameScreen(
    ui: GameUiState,
    notes: NotesState,
    onDirection: (Direction) -> Unit,
    onMode: (ActionMode) -> Unit,
    onConfirmWall: (Boolean) -> Unit,
    onSelectTool: (NoteTool) -> Unit,
    onCellTap: (Cell) -> Unit,
    onWallEdgeTap: (Cell, Cell) -> Unit,
    onNoteText: (Int, String) -> Unit,
    onNewGame: () -> Unit
) {
    // Тряска экрана при ранении
    val shake = remember { Animatable(0f) }
    LaunchedEffect(ui.isWounded) {
        if (ui.isWounded) {
            repeat(6) { i ->
                shake.animateTo(if (i % 2 == 0) 14f else -14f, tween(45))
            }
            shake.animateTo(0f, tween(45))
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .offset { IntOffset(shake.value.roundToInt(), 0) }
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        StatusBar(ui)

        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            BoardView(notes, onCellTap, onWallEdgeTap)
            IconPalette(notes.tool, onSelectTool)
            NotesArea(notes.texts, onNoteText)
        }

        EventLog(ui.log)
        ActionPanel(ui, onDirection, onMode)
    }

    // Вопрос о взрыве стенки
    if (ui.pendingWall != null) {
        AlertDialog(
            onDismissRequest = { onConfirmWall(false) },
            title = { Text("Бронированная стенка") },
            text = { Text("Перед тобой бронированная стенка. Взорвать гранатой?") },
            confirmButton = { Button(onClick = { onConfirmWall(true) }) { Text("Да") } },
            dismissButton = { TextButton(onClick = { onConfirmWall(false) }) { Text("Нет") } }
        )
    }

    // Победа (полноценный экран разбора партии — позже)
    if (ui.gameOver) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text(if (ui.won) "Клад найден!" else "Игра окончена") },
            text = { Text(if (ui.won) "Ты вышел из джунглей с кладом." else "Соперник вышел с кладом.") },
            confirmButton = { Button(onClick = onNewGame) { Text("Новая игра") } }
        )
    }
}
