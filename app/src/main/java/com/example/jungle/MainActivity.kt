package com.example.jungle

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.jungle.engine.GameEngine

/**
 * Временная заглушка: проверяет, что движок подключён и приложение собирается.
 * Настоящие экраны (MainMenu → StartChoice → GameScreen) добавим следующим шагом.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Сгенерируем тестовую партию, чтобы убедиться, что движок работает на устройстве
        val state = GameEngine().newGame(starts = listOf(com.example.jungle.engine.Cell(1, 1)))
        setContent {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Джунгли: движок готов (игроков: ${state.players.size})")
            }
        }
    }
}
