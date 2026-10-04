package com.example.jungle.engine

/**
 * Стенка между двумя соседними клетками.
 * Всегда создавайте через [WallId.of]: пара нормализуется,
 * поэтому of(a, b) == of(b, a).
 */
data class WallId(val cellA: Cell, val cellB: Cell) {
    companion object {
        fun of(a: Cell, b: Cell): WallId =
            if (a.x < b.x || (a.x == b.x && a.y <= b.y)) WallId(a, b) else WallId(b, a)
    }
}
