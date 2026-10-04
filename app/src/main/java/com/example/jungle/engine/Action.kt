package com.example.jungle.engine

/** Действие игрока. Клиент в сети отправляет только его (намерение), не состояние. */
sealed class Action {
    /**
     * Шаг. [explodeWall] = true — игрок подтвердил взрыв стенки на пути.
     * Первый вызов без подтверждения вернёт NEEDS_CONFIRMATION, если на пути целая стенка и есть граната.
     */
    data class Step(val direction: Direction, val explodeWall: Boolean = false) : Action()

    /** Выстрел в соседнюю клетку по направлению. */
    data class Shoot(val direction: Direction) : Action()
}
