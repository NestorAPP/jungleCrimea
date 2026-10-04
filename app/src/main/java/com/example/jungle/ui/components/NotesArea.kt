package com.example.jungle.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** Три короткие текстовые заметки («тоннель A1 → ...», «патроны: 2» и т. п.). */
@Composable
fun NotesArea(texts: List<String>, onChange: (Int, String) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("Заметки", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        texts.forEachIndexed { i, text ->
            OutlinedTextField(
                value = text,
                onValueChange = { onChange(i, it) },
                placeholder = { Text("Заметка ${i + 1}") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
