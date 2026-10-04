package com.example.jungle.engine

import org.junit.Assert.*
import org.junit.Test

class MapGeneratorTest {

    private val seeds = 0L..300L

    @Test
    fun `состав объектов соответствует ТЗ`() {
        for (seed in seeds) {
            val map = MapGenerator.generate(seed)
            val values = map.contents.values
            assertEquals(16, map.contents.size)
            assertEquals(5, values.count { it == CellContent.River })
            assertEquals(1, values.count { it == CellContent.Estuary })
            assertEquals(1, values.count { it == CellContent.Hospital })
            assertEquals(1, values.count { it == CellContent.Arsenal })
            assertEquals(1, values.count { it == CellContent.Treasure })
            assertEquals(1, values.count { it == CellContent.Exit })
            for (system in listOf('A', 'B')) for (i in 1..3) {
                assertEquals(1, values.count { it == CellContent.Tunnel(system, i) })
            }
        }
    }

    @Test
    fun `устье на границе но не в углу`() {
        for (seed in seeds) {
            val e = MapGenerator.generate(seed).estuary
            val onBorder = e.x == 0 || e.x == 3 || e.y == 0 || e.y == 3
            val corner = (e.x == 0 || e.x == 3) && (e.y == 0 || e.y == 3)
            assertTrue("seed=$seed", onBorder && !corner)
        }
    }

    @Test
    fun `линия реки - цепочка соседних клеток без самопересечений`() {
        for (seed in seeds) {
            val line = MapGenerator.generate(seed).riverLine
            assertEquals(6, line.size)
            assertEquals(6, line.toSet().size)
            line.zipWithNext().forEach { (a, b) ->
                assertEquals("seed=$seed", 1, kotlin.math.abs(a.x - b.x) + kotlin.math.abs(a.y - b.y))
            }
        }
    }

    @Test
    fun `две стенки между соседними клетками, не у устья`() {
        for (seed in seeds) {
            val map = MapGenerator.generate(seed)
            assertEquals(2, map.walls.size)
            map.walls.forEach { w ->
                assertTrue(w.cellA.isInside && w.cellB.isInside)
                assertEquals(1, kotlin.math.abs(w.cellA.x - w.cellB.x) + kotlin.math.abs(w.cellA.y - w.cellB.y))
            }
            assertFalse(map.hasWallBetween(map.riverLine[0], map.riverLine[1]))
        }
    }

    @Test
    fun `поле связно (нет тупиков)`() {
        for (seed in seeds) assertTrue("seed=$seed", MapGenerator.isFullyConnected(MapGenerator.generate(seed)))
    }

    @Test
    fun `генерация детерминирована по seed`() {
        val a = MapGenerator.generate(42)
        val b = MapGenerator.generate(42)
        assertEquals(a.contents, b.contents)
        assertEquals(a.walls, b.walls)
        assertEquals(a.riverLine, b.riverLine)
    }

    @Test
    fun `WallId не зависит от порядка клеток`() {
        assertEquals(WallId.of(Cell(1, 1), Cell(2, 1)), WallId.of(Cell(2, 1), Cell(1, 1)))
    }
}
