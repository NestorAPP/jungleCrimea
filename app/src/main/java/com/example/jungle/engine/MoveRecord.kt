package com.example.jungle.engine

/** Запись о перемещении (для разбора партии). [to] — реальная конечная клетка (после снесения/тоннеля). */
data class MoveRecord(val playerId: Int, val from: Cell, val to: Cell, val turn: Int)
