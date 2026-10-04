package com.example.jungle.engine

/**
 * Состояние партии. Изменяется только через [GameEngine].
 *
 * Отличия от исходного ТЗ: turnCounter — var (растёт каждый ход),
 * log хранит [LogMessage] (чтобы фильтровать сообщения по игроку).
 */
class GameState(
    val map: GameMap,
    val players: List<Player>,
    var currentPlayerIndex: Int,
    /** Разрушенные стенки: стенка → номер хода, на котором её взорвали. */
    val brokenWalls: MutableMap<WallId, Int> = mutableMapOf(),
    var turnCounter: Int = 0,
    var gameOver: Boolean = false,
    var winnerId: Int? = null,
    val log: MutableList<LogMessage> = mutableListOf(),
    val moveHistory: MutableList<MoveRecord> = mutableListOf()
) {
    val currentPlayer: Player get() = players[currentPlayerIndex]

    /** Кто сейчас несёт клад (клад в мире один). */
    fun treasureHolder(): Player? = players.firstOrNull { it.hasTreasure }

    /** Целая ли стенка между клетками (есть на карте и не разрушена). */
    fun isWallIntact(a: Cell, b: Cell): Boolean {
        val id = WallId.of(a, b)
        return id in map.walls && id !in brokenWalls
    }
}
