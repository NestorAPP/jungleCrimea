package com.example.jungle.engine

/**
 * Направление движения/выстрела.
 * Ось Y направлена вниз: север = строка с меньшим y (верх экрана).
 */
enum class Direction(
    val dx: Int,
    val dy: Int,
    val toward: String, // «на север» — для сообщений о действиях
    val from: String    // «с севера» — для сообщений «ранили выстрелом с ...»
) {
    NORTH(0, -1, "на север", "с севера"),
    SOUTH(0, 1, "на юг", "с юга"),
    WEST(-1, 0, "на запад", "с запада"),
    EAST(1, 0, "на восток", "с востока");

    /** Противоположное направление. */
    val opposite: Direction
        get() = when (this) {
            NORTH -> SOUTH
            SOUTH -> NORTH
            WEST -> EAST
            EAST -> WEST
        }
}

/** Клетка поля: x — колонка (0..3), y — строка (0..3). */
data class Cell(val x: Int, val y: Int) {

    /** Находится ли клетка внутри поля 4×4. */
    val isInside: Boolean
        get() = x in 0 until GameMap.SIZE && y in 0 until GameMap.SIZE

    /** Сосед в заданном направлении (может оказаться вне поля). */
    operator fun plus(d: Direction): Cell = Cell(x + d.dx, y + d.dy)
}
