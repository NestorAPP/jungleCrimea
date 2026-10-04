package com.example.jungle.engine

enum class ActionStatus {
    DONE,                // действие выполнено
    REJECTED,            // действие невозможно (ход не тратится)
    NEEDS_CONFIRMATION   // нужно подтвердить взрыв стенки (ход не тратится)
}

/** Результат применения действия. */
data class ActionResult(
    val status: ActionStatus,
    val turnSpent: Boolean,
    val messages: List<LogMessage>
)
