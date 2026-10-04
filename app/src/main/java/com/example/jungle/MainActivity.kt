package com.example.jungle

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.jungle.ui.GameScreen
import com.example.jungle.ui.MainMenuScreen
import com.example.jungle.ui.StartChoiceScreen
import com.example.jungle.ui.theme.JungleTheme
import com.example.jungle.ui.theme.vignette
import com.example.jungle.ui.viewmodel.GameViewModel
import com.example.jungle.ui.viewmodel.Screen

/** Единственная Activity: переключает экраны по состоянию ViewModel. */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            JungleTheme {
                val vm: GameViewModel = viewModel()
                val ui = vm.ui

                Box(
                    Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .vignette()
                        .systemBarsPadding()
                ) {
                    when (ui.screen) {
                        Screen.MENU -> MainMenuScreen(onSolo = vm::openStartChoice)
                        Screen.START_CHOICE -> StartChoiceScreen(onStart = vm::startGame, onBack = vm::backToMenu)
                        Screen.GAME -> GameScreen(
                            ui = ui,
                            notes = vm.notes,
                            onDirection = vm::onDirection,
                            onMode = vm::onMode,
                            onConfirmWall = vm::confirmWall,
                            onSelectTool = vm::selectTool,
                            onCellTap = vm::onNoteCellTap,
                            onWallEdgeTap = vm::onWallEdgeTap,
                            onNoteText = vm::setNoteText,
                            onNewGame = vm::backToMenu
                        )
                    }
                }
            }
        }
    }
}
