package com.example.jungle.engine

/** Содержимое клетки карты. Каждая клетка занята ровно одним объектом. */
sealed class CellContent {
    object River : CellContent()
    object Estuary : CellContent()

    /** Тоннель: system — 'A' или 'B', index — 1..3. */
    data class Tunnel(val system: Char, val index: Int) : CellContent()

    object Hospital : CellContent()
    object Arsenal : CellContent()
    object Treasure : CellContent()
    object Exit : CellContent()
}
