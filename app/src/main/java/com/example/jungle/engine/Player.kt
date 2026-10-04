package com.example.jungle.engine

/** Состояние игрока. */
class Player(
    val id: Int,                          // 1 или 2
    var position: Cell,
    var ammo: Int = GameEngine.MAX_AMMO,
    var grenades: Int = GameEngine.MAX_GRENADES,
    var isWounded: Boolean = false,
    var hasTreasure: Boolean = false,
    var knownTreasureCell: Cell? = null,
    var inRiverAfterEstuary: Boolean = false // «плывёт вверх по течению» после выхода из устья
)
