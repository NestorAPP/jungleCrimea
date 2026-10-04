package com.example.jungle.engine

/**
 * Истинная карта мира (неизменяемая).
 *
 * @param contents  содержимое всех 16 клеток
 * @param walls     2 бронированные стенки
 * @param riverLine линия реки: [0] — устье, [1..5] — клетки реки по порядку «вверх по течению»
 */
class GameMap(
    val contents: Map<Cell, CellContent>,
    val walls: Set<WallId>,
    val riverLine: List<Cell>
) {
    init {
        require(contents.size == SIZE * SIZE) { "Ожидалось ${SIZE * SIZE} клеток" }
        require(contents[riverLine.first()] == CellContent.Estuary) { "Линия реки должна начинаться с устья" }
        require(riverLine.drop(1).all { contents[it] == CellContent.River }) { "Остальные клетки линии — река" }
    }

    val estuary: Cell = riverLine.first()
    val treasureCell: Cell = cellOf(CellContent.Treasure)
    val exitCell: Cell = cellOf(CellContent.Exit)

    fun contentAt(cell: Cell): CellContent = contents.getValue(cell)

    /** Есть ли (целая или разрушенная) стенка между клетками — в самой карте. */
    fun hasWallBetween(a: Cell, b: Cell): Boolean = WallId.of(a, b) in walls

    /** Куда переносит тоннель: A1→A2→A3→A1 (и так же для B). */
    fun tunnelDestination(cell: Cell): Cell {
        val t = contentAt(cell) as CellContent.Tunnel
        val next = CellContent.Tunnel(t.system, t.index % TUNNELS_PER_SYSTEM + 1)
        return contents.entries.first { it.value == next }.key
    }

    private fun cellOf(content: CellContent): Cell =
        contents.entries.first { it.value == content }.key

    companion object {
        const val SIZE = 4
        const val TUNNELS_PER_SYSTEM = 3

        /** Четыре центральные клетки, из которых игрок выбирает старт. */
        val START_CELLS: List<Cell> = listOf(Cell(1, 1), Cell(2, 1), Cell(1, 2), Cell(2, 2))

        /** Все клетки поля. */
        val ALL_CELLS: List<Cell> = (0 until SIZE).flatMap { y -> (0 until SIZE).map { x -> Cell(x, y) } }
    }
}
