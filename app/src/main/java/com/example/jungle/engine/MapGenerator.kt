package com.example.jungle.engine

import kotlin.random.Random

/**
 * Генератор карты. Детерминирован: один и тот же seed даёт одну и ту же карту.
 * При неудачной раскладке (тупик реки, несвязное поле) пробует снова.
 */
object MapGenerator {

    private const val MAX_ATTEMPTS = 1000
    private const val RIVER_CELLS = 5                  // клеток «Река»
    private const val LINE_LENGTH = RIVER_CELLS + 1    // + устье
    private const val WALLS_COUNT = 2

    /** Кандидаты на устье: граница поля, но не углы (8 клеток). */
    private val ESTUARY_CANDIDATES: List<Cell> = GameMap.ALL_CELLS.filter { c ->
        val last = GameMap.SIZE - 1
        val onBorder = c.x == 0 || c.x == last || c.y == 0 || c.y == last
        val corner = (c.x == 0 || c.x == last) && (c.y == 0 || c.y == last)
        onBorder && !corner
    }

    fun generate(seed: Long): GameMap {
        val rnd = Random(seed)
        repeat(MAX_ATTEMPTS) {
            val map = tryGenerate(rnd)
            if (map != null && isFullyConnected(map)) return map
        }
        error("Не удалось сгенерировать карту за $MAX_ATTEMPTS попыток (seed=$seed)")
    }

    /** Одна попытка; null — если река упёрлась в тупик. */
    private fun tryGenerate(rnd: Random): GameMap? {
        // 1–2. Устье и линия реки
        val estuary = ESTUARY_CANDIDATES.random(rnd)
        val line = buildRiverLine(estuary, rnd) ?: return null

        val contents = HashMap<Cell, CellContent>()
        contents[line[0]] = CellContent.Estuary
        line.drop(1).forEach { contents[it] = CellContent.River }

        // 3–5. Остальные 10 клеток раскладываем случайно
        val free = GameMap.ALL_CELLS.filter { it !in contents }.shuffled(rnd)
        var i = 0
        for (system in listOf('A', 'B')) {
            for (index in 1..GameMap.TUNNELS_PER_SYSTEM) {
                contents[free[i++]] = CellContent.Tunnel(system, index)
            }
        }
        contents[free[i++]] = CellContent.Hospital
        contents[free[i++]] = CellContent.Arsenal
        contents[free[i++]] = CellContent.Treasure
        contents[free[i]] = CellContent.Exit

        // 6. Стенки
        return GameMap(contents, placeWalls(rnd, line), line)
    }

    /**
     * Линия реки: устье + 5 клеток, каждая — сосед предыдущей, без самопересечений.
     * Первый шаг — строго вглубь поля (от границы). Дальше — случайный поиск с откатом.
     */
    private fun buildRiverLine(estuary: Cell, rnd: Random): List<Cell>? {
        val last = GameMap.SIZE - 1
        val inward = when {
            estuary.x == 0 -> Direction.EAST
            estuary.x == last -> Direction.WEST
            estuary.y == 0 -> Direction.SOUTH
            else -> Direction.NORTH
        }
        val path = mutableListOf(estuary, estuary + inward)
        return if (extend(path, rnd)) path else null
    }

    private fun extend(path: MutableList<Cell>, rnd: Random): Boolean {
        if (path.size == LINE_LENGTH) return true
        for (d in Direction.values().toList().shuffled(rnd)) {
            val next = path.last() + d
            if (next.isInside && next !in path) {
                path.add(next)
                if (extend(path, rnd)) return true
                path.removeAt(path.lastIndex)
            }
        }
        return false
    }

    /**
     * Две стенки между разными парами соседних клеток (периметр поля не используется).
     * Между устьем и первой клеткой реки стенки нет — автовыход из устья не должен блокироваться.
     */
    private fun placeWalls(rnd: Random, line: List<Cell>): Set<WallId> {
        val forbidden = WallId.of(line[0], line[1])
        val pairs = GameMap.ALL_CELLS.flatMap { c ->
            listOf(Direction.EAST, Direction.SOUTH)
                .map { c + it }
                .filter { it.isInside }
                .map { n -> WallId.of(c, n) }
        }.filter { it != forbidden }
        return pairs.shuffled(rnd).take(WALLS_COUNT).toSet()
    }

    /**
     * Проверка «нет тупика»: все 16 клеток достижимы из центра по клеткам-соседям
     * при закрытых стенках (стенки можно взорвать, но не рассчитываем на гранаты).
     */
    fun isFullyConnected(map: GameMap): Boolean {
        val seen = mutableSetOf(GameMap.START_CELLS.first())
        val queue = ArrayDeque(seen)
        while (queue.isNotEmpty()) {
            val c = queue.removeFirst()
            for (d in Direction.values()) {
                val n = c + d
                if (n.isInside && n !in seen && !map.hasWallBetween(c, n)) {
                    seen.add(n)
                    queue.addLast(n)
                }
            }
        }
        return seen.size == GameMap.SIZE * GameMap.SIZE
    }
}
