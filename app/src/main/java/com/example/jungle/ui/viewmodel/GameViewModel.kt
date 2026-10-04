package com.example.jungle.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.jungle.engine.Action
import com.example.jungle.engine.ActionStatus
import com.example.jungle.engine.Cell
import com.example.jungle.engine.Direction
import com.example.jungle.engine.GameEngine
import com.example.jungle.engine.GameState

enum class Screen { MENU, START_CHOICE, GAME }

/** Режим следующего нажатия на компас. */
enum class ActionMode { MOVE, SHOOT, GRENADE }

/**
 * Снимок состояния для UI. GameState изменяемый и Compose не видит его изменений,
 * поэтому после каждого действия ViewModel собирает новый неизменяемый снимок.
 * Карта в снимок намеренно не попадает: игрок её не видит.
 */
data class GameUiState(
    val screen: Screen = Screen.MENU,
    val ammo: Int = 0,
    val grenades: Int = 0,
    val isWounded: Boolean = false,
    val hasTreasure: Boolean = false,
    val log: List<String> = emptyList(),
    val gameOver: Boolean = false,
    val won: Boolean = false,
    val pendingWall: Direction? = null, // не null — показываем «Взорвать стенку?»
    val mode: ActionMode = ActionMode.MOVE,
    val turn: Int = 0
)

class GameViewModel : ViewModel() {

    private val engine = GameEngine()
    private var game: GameState? = null
    private val localPlayerId = 1          // на Этапе 1 игрок один
    private val logLines = mutableListOf<String>()

    var ui by mutableStateOf(GameUiState())
        private set

    // ── навигация ──

    fun openStartChoice() {
        ui = ui.copy(screen = Screen.START_CHOICE)
    }

    fun backToMenu() {
        game = null
        logLines.clear()
        ui = GameUiState()
    }

    /** Новая партия: карту генерирует движок, игрок стартует в выбранной центральной клетке. */
    fun startGame(start: Cell) {
        val g = engine.newGame(listOf(start))
        game = g
        logLines.clear()
        logLines.add("Ты в джунглях. Карты нет — веди её сам.")
        g.log.filter { it.toPlayerId == null || it.toPlayerId == localPlayerId }
            .forEach { logLines.add(it.text) }
        ui = ui.copy(screen = Screen.GAME)
        refresh()
    }

    // ── действия ──

    fun onMode(mode: ActionMode) {
        ui = ui.copy(mode = if (ui.mode == mode) ActionMode.MOVE else mode)
    }

    fun onDirection(d: Direction) {
        when (ui.mode) {
            ActionMode.MOVE -> perform(Action.Step(d))
            ActionMode.SHOOT -> perform(Action.Shoot(d))
            ActionMode.GRENADE -> perform(Action.Step(d, explodeWall = true))
        }
    }

    /** Ответ на вопрос «Взорвать стенку?». */
    fun confirmWall(yes: Boolean) {
        val d = ui.pendingWall ?: return
        ui = ui.copy(pendingWall = null)
        if (yes) perform(Action.Step(d, explodeWall = true))
    }

    private fun perform(action: Action) {
        val g = game ?: return
        if (g.gameOver) return

        val result = engine.applyAction(g, action)

        if (result.status == ActionStatus.NEEDS_CONFIRMATION) {
            // Вопрос показываем диалогом, в лог он не попадает
            ui = ui.copy(pendingWall = (action as Action.Step).direction, mode = ActionMode.MOVE)
            return
        }
        result.messages
            .filter { it.toPlayerId == null || it.toPlayerId == localPlayerId }
            .forEach { logLines.add(it.text) }
        refresh()
    }

    /** Пересобирает снимок UI из GameState. */
    private fun refresh() {
        val g = game ?: return
        val p = g.players.first { it.id == localPlayerId }
        ui = ui.copy(
            ammo = p.ammo,
            grenades = p.grenades,
            isWounded = p.isWounded,
            hasTreasure = p.hasTreasure,
            log = logLines.takeLast(MAX_LOG_LINES).toList(),
            gameOver = g.gameOver,
            won = g.winnerId == localPlayerId,
            pendingWall = null,
            mode = ActionMode.MOVE,
            turn = g.turnCounter
        )
    }

    private companion object {
        const val MAX_LOG_LINES = 50
    }
}
