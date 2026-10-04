package com.example.jungle.ui.viewmodel

import com.example.jungle.engine.Cell
import com.example.jungle.engine.WallId

/** Инструменты палитры. [label] — подпись под иконкой. */
enum class NoteTool(val label: String) {
    RIVER("Река"),
    ESTUARY("Устье"),
    TUNNEL_A("Тоннель A"),
    TUNNEL_B("Тоннель B"),
    ARSENAL("Арсенал"),
    HOSPITAL("Больница"),
    TREASURE("Клад"),
    EXIT("Выход"),
    WALL("Стенка"),
    ME("Я здесь"),   // дополнительный маркер: где игрок считает себя
    ERASER("Ластик")
}

/**
 * Ручные заметки игрока. Это только то, что игрок сам нарисовал:
 * с истинной картой они никак не связаны (нужны для разбора партии).
 */
data class NotesState(
    val cells: Map<Cell, NoteTool> = emptyMap(),   // иконки в клетках
    val walls: Set<WallId> = emptySet(),           // нарисованные стенки (на границах клеток)
    val me: Cell? = null,                          // метка «я здесь»
    val texts: List<String> = listOf("", "", ""),  // 3 текстовые заметки
    val tool: NoteTool? = null                     // выбранный инструмент
)
