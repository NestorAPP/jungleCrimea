package com.example.jungle.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.jungle.engine.Direction
import com.example.jungle.ui.components.ActionPanel
import com.example.jungle.ui.components.EventLog
import com.example.jungle.ui.components.StatusBar
import com.example.jungle.ui.viewmodel.ActionMode
import com.example.jungle.ui.viewmodel.GameUiState

/**
 * Экран игры. Сверху — место под поле заметок (появится на следующем шаге),
 * ниже статус, лог событий и панель действий.
 */
@Composable
fun GameScreen(
    ui: GameUiState,
    onDirection: (Direction) -> Unit,
    onMode: (ActionMode) -> Unit,
    onConfirmWall: (Boolean) -> Unit,
    onNewGame: () -> Unit
) {
    Column(
        Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatusBar(ui)

        Box(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surface),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Здесь будет поле заметок 4×4 и палитра иконок",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(16.dp)
            )
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
