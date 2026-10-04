package com.example.jungle.engine

import com.example.jungle.engine.Direction.*
import org.junit.Assert.*
import org.junit.Test

/**
 * Тестовая карта (x →, y ↓):
 *   y0:  A1  EST TRE EXT
 *   y1:  A2  RIV HOS|ARS      стенка между (2,1) и (3,1)
 *   y2:  A3  RIV B1 |B2       стенка между (2,2) и (3,2)
 *   y3:  B3  RIV RIV RIV
 * Река: (1,0)устье → (1,1) → (1,2) → (1,3) → (2,3) → (3,3)
 */
class GameEngineTest {

    private fun layout(rows: List<String>, river: List<Cell>, walls: Set<WallId>): GameMap {
        val contents = mutableMapOf<Cell, CellContent>()
        rows.forEachIndexed { y, row ->
            row.trim().split(Regex("\\s+")).forEachIndexed { x, t ->
                contents[Cell(x, y)] = when (t) {
                    "EST" -> CellContent.Estuary
                    "RIV" -> CellContent.River
                    "HOS" -> CellContent.Hospital
                    "ARS" -> CellContent.Arsenal
                    "TRE" -> CellContent.Treasure
                    "EXT" -> CellContent.Exit
                    else -> CellContent.Tunnel(t[0], t[1].digitToInt())
                }
            }
        }
        return GameMap(contents, walls, river)
    }

    private val river = listOf(Cell(1, 0), Cell(1, 1), Cell(1, 2), Cell(1, 3), Cell(2, 3), Cell(3, 3))
    private val walls = setOf(
        WallId.of(Cell(2, 1), Cell(3, 1)),
        WallId.of(Cell(2, 2), Cell(3, 2))
    )
    private val testMap = layout(
        listOf("A1 EST TRE EXT", "A2 RIV HOS ARS", "A3 RIV B1 B2", "B3 RIV RIV RIV"), river, walls
    )

    private val engine = GameEngine()

    /** Соло-партия; игрока ставим в нужную клетку напрямую. */
    private fun solo(at: Cell): Pair<GameState, Player> {
        val s = engine.newGame(listOf(Cell(1, 1)), testMap, 0)
        s.players[0].position = at
        return s to s.players[0]
    }

    private fun GameEngine.step(s: GameState, d: Direction, explode: Boolean = false) =
        applyAction(s, Action.Step(d, explode))

    // ── границы и стенки ──

    @Test
    fun `выход за границу - отказ, ход не тратится`() {
        val (s, p) = solo(Cell(0, 1))
        val r = engine.step(s, WEST)
        assertEquals(ActionStatus.REJECTED, r.status)
        assertFalse(r.turnSpent)
        assertEquals(Cell(0, 1), p.position)
        assertEquals(0, s.turnCounter)
    }

    @Test
    fun `стенка без гранаты - отказ`() {
        val (s, p) = solo(Cell(2, 1)); p.grenades = 0
        val r = engine.step(s, EAST)
        assertEquals(ActionStatus.REJECTED, r.status)
        assertFalse(r.turnSpent)
    }

    @Test
    fun `стенка с гранатой - сначала подтверждение, отказ не тратит ход`() {
        val (s, p) = solo(Cell(2, 1))
        val r = engine.step(s, EAST)
        assertEquals(ActionStatus.NEEDS_CONFIRMATION, r.status)
        assertFalse(r.turnSpent)
        assertEquals(1, p.grenades)
        assertEquals(Cell(2, 1), p.position)
    }

    @Test
    fun `подтверждённый взрыв - граната тратится, игрок в арсенале`() {
        val (s, p) = solo(Cell(2, 1)); p.ammo = 0
        val r = engine.step(s, EAST, explode = true)
        assertEquals(ActionStatus.DONE, r.status)
        assertTrue(r.turnSpent)
        assertEquals(Cell(3, 1), p.position)
        assertEquals(1, p.grenades) // арсенал пополнил гранату
        assertEquals(3, p.ammo)
    }

    @Test
    fun `раненый не может взорвать стенку`() {
        val (s, p) = solo(Cell(2, 1)); p.isWounded = true
        val r = engine.step(s, EAST, explode = true)
        assertEquals(ActionStatus.REJECTED, r.status)
        assertEquals(1, p.grenades)
    }

    @Test
    fun `стенка проходима 2 хода и восстанавливается на 3-й`() {
        val (s, p) = solo(Cell(2, 1))
        engine.step(s, EAST, explode = true)          // ход 0: взрыв, игрок в (3,1)
        assertEquals(ActionStatus.DONE, engine.step(s, WEST).status) // ход 1: через пролом
        assertEquals(ActionStatus.DONE, engine.step(s, EAST).status) // ход 2: через пролом
        p.grenades = 0
        assertEquals(ActionStatus.REJECTED, engine.step(s, WEST).status) // ход 3: стенка снова целая
    }

    // ── река и устье ──

    @Test
    fun `шаг в реку сносит в устье и тратит патрон`() {
        val (s, p) = solo(Cell(2, 1))
        val r = engine.step(s, WEST) // (1,1) река
        assertTrue(r.turnSpent)
        assertEquals(Cell(1, 0), p.position)
        assertEquals(2, p.ammo)
        assertFalse(p.isWounded)
    }

    @Test
    fun `крокодил без патронов ранит и отбирает клад`() {
        val (s, p) = solo(Cell(2, 1)); p.ammo = 0; p.hasTreasure = true
        engine.step(s, WEST)
        assertTrue(p.isWounded)
        assertFalse(p.hasTreasure)
    }

    @Test
    fun `автовыход из устья, плавание вверх по течению и неверный шаг`() {
        val (s, p) = solo(Cell(2, 1))
        engine.step(s, WEST)                          // снос в устье
        engine.step(s, NORTH)                         // любое действие -> автовыход
        assertEquals(Cell(1, 1), p.position)
        assertTrue(p.inRiverAfterEstuary)

        val ammo = p.ammo
        engine.step(s, SOUTH)                         // следующая по линии: безопасно
        assertEquals(Cell(1, 2), p.position)
        assertEquals(ammo, p.ammo)

        engine.step(s, NORTH)                         // назад по течению - снос
        assertEquals(Cell(1, 0), p.position)
        assertEquals(ammo - 1, p.ammo)
        assertFalse(p.inRiverAfterEstuary)
    }

    @Test
    fun `шагнуть в устье нельзя - отказ, ход и патроны не тратятся`() {
        val (s, p) = solo(Cell(1, 1))                 // клетка реки под устьем (1,0)
        val r = engine.step(s, NORTH)
        assertEquals(ActionStatus.REJECTED, r.status)
        assertFalse(r.turnSpent)
        assertEquals(Cell(1, 1), p.position)
        assertEquals(3, p.ammo)
        assertEquals(0, s.turnCounter)
        assertTrue(r.messages.any { it.text.contains("устье") })
    }

    @Test
    fun `отказ из-за устья не тратит гранату даже при стенке`() {
        // стенка между устьем и соседом на этой карте не стоит, поэтому проверяем порядок на своей карте
        val map = layout(
            listOf("A1 EST TRE EXT", "A2 RIV HOS ARS", "A3 RIV B1 B2", "B3 RIV RIV RIV"), river,
            setOf(WallId.of(Cell(0, 0), Cell(1, 0)), WallId.of(Cell(2, 2), Cell(3, 2)))
        )
        val s = engine.newGame(listOf(Cell(1, 1)), map, 0)
        s.players[0].position = Cell(0, 0)            // A1, справа - стенка и устье
        val r = engine.step(s, EAST, explode = true)
        assertEquals(ActionStatus.REJECTED, r.status)
        assertEquals(1, s.players[0].grenades)
        assertTrue(s.brokenWalls.isEmpty())
    }

    @Test
    fun `шаг на не-реку сбрасывает флаг реки`() {
        val (s, p) = solo(Cell(1, 1)); p.inRiverAfterEstuary = true
        engine.step(s, EAST) // (2,1) больница
        assertFalse(p.inRiverAfterEstuary)
    }

    // ── тоннели ──

    @Test
    fun `тоннели переносят A3 в A1 и B3 в B1`() {
        val (s, p) = solo(Cell(1, 2))
        engine.step(s, WEST)                          // (0,2) = A3
        assertEquals(Cell(0, 0), p.position)          // A1

        val (s2, p2) = solo(Cell(1, 3))
        engine.step(s2, WEST)                         // (0,3) = B3
        assertEquals(Cell(2, 2), p2.position)         // B1
    }

    @Test
    fun `B1 переносит в B2`() {
        val (s, p) = solo(Cell(2, 1))
        engine.step(s, SOUTH)                         // (2,2) = B1
        assertEquals(Cell(3, 2), p.position)          // B2
    }

    // ── арсенал, больница, клад, выход ──

    @Test
    fun `арсенал пополняет здорового и не пополняет раненого`() {
        val (s, p) = solo(Cell(3, 2)); p.ammo = 0; p.grenades = 0
        engine.step(s, NORTH)
        assertEquals(3, p.ammo); assertEquals(1, p.grenades)

        val (s2, p2) = solo(Cell(3, 2)); p2.ammo = 0; p2.grenades = 0; p2.isWounded = true
        engine.step(s2, NORTH)
        assertEquals(0, p2.ammo); assertEquals(0, p2.grenades)
    }

    @Test
    fun `больница лечит`() {
        val (s, p) = solo(Cell(2, 2)); p.isWounded = true
        engine.step(s, NORTH) // (2,1) больница
        assertFalse(p.isWounded)
    }

    @Test
    fun `клад берёт только здоровый`() {
        val (s, p) = solo(Cell(2, 1))
        engine.step(s, NORTH)
        assertTrue(p.hasTreasure)
        assertEquals(Cell(2, 0), p.knownTreasureCell)

        val (s2, p2) = solo(Cell(2, 1)); p2.isWounded = true
        engine.step(s2, NORTH)
        assertFalse(p2.hasTreasure)
    }

    @Test
    fun `выход с кладом - победа, без клада - нет`() {
        val (s, p) = solo(Cell(2, 0)); p.hasTreasure = true
        engine.step(s, EAST)
        assertTrue(s.gameOver)
        assertEquals(1, s.winnerId)
        assertEquals(ActionStatus.REJECTED, engine.step(s, WEST).status) // после победы ходов нет

        val (s2, p2) = solo(Cell(2, 0))
        engine.step(s2, EAST)
        assertFalse(s2.gameOver)
        assertEquals(Cell(3, 0), p2.position)
    }

    // ── выстрелы и два игрока ──

    private fun duel(a: Cell, b: Cell): GameState {
        val s = engine.newGame(listOf(Cell(1, 1), Cell(2, 2)), testMap, 0)
        s.players[0].position = a
        s.players[1].position = b
        return s
    }

    @Test
    fun `выстрел ранит соперника и отнимает у него клад`() {
        val s = duel(Cell(2, 2), Cell(2, 1))
        s.players[1].hasTreasure = true
        val r = engine.applyAction(s, Action.Shoot(NORTH))
        assertTrue(r.turnSpent)
        assertEquals(2, s.players[0].ammo)
        assertTrue(s.players[1].isWounded)
        assertFalse(s.players[1].hasTreasure)
        assertEquals(1, s.currentPlayerIndex) // ход перешёл
        assertTrue(r.messages.any { it.toPlayerId == 2 && it.text.contains("с юга") })
    }

    @Test
    fun `целая стенка блокирует выстрел, патрон тратится`() {
        val s = duel(Cell(2, 1), Cell(3, 1))
        engine.applyAction(s, Action.Shoot(EAST))
        assertFalse(s.players[1].isWounded)
        assertEquals(2, s.players[0].ammo)
    }

    @Test
    fun `раненый и без патронов стрелять не могут`() {
        val s = duel(Cell(2, 2), Cell(2, 1))
        s.players[0].isWounded = true
        assertEquals(ActionStatus.REJECTED, engine.applyAction(s, Action.Shoot(NORTH)).status)
        s.players[0].isWounded = false; s.players[0].ammo = 0
        assertEquals(ActionStatus.REJECTED, engine.applyAction(s, Action.Shoot(NORTH)).status)
        assertEquals(0, s.currentPlayerIndex)
    }

    @Test
    fun `старт в клетке клада - клад у того кто ходит первым`() {
        val map = layout(
            listOf("A1 EST HOS EXT", "A2 RIV TRE ARS", "A3 RIV B1 B2", "B3 RIV RIV RIV"), river, walls
        )
        val s = engine.newGame(listOf(Cell(2, 1), Cell(2, 1)), map, firstPlayerIndex = 1)
        assertFalse(s.players[0].hasTreasure)
        assertTrue(s.players[1].hasTreasure)
    }
}
