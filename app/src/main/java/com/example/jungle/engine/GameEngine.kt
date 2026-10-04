package com.example.jungle.engine

import kotlin.random.Random

/**
 * Чистая игровая логика (без Android). Изменяет переданный [GameState] и возвращает [ActionResult].
 * Применяет действие ТЕКУЩЕГО игрока; в сети хост вызывает applyAction для каждого хода.
 *
 * Принятые допущения (где ТЗ неоднозначно):
 *  - В устье нельзя шагнуть самому: такой ход отклоняется («на пути устье реки»), ход не тратится.
 *    В устье игрока может только снести рекой.
 *  - Пока игрок стоит в устье, любое его действие заменяется автовыходом (ход тратится).
 *  - Стенка, взорванная на ходу T, снова целая, когда turnCounter достигает T+3
 *    (turnCounter общий, растёт после каждого потраченного хода любого игрока).
 *  - На старте срабатывает только клад (кто ходит первым — тот и берёт); река/тоннель на старте не работают.
 */
class GameEngine(private val random: Random = Random.Default) {

    companion object {
        const val MAX_AMMO = 3
        const val MAX_GRENADES = 1
        const val WALL_RESTORE_AFTER = 3
    }

    // ───────────────────────── Новая игра ─────────────────────────

    /**
     * @param starts стартовые клетки игроков (по одной на игрока, центральные)
     */
    fun newGame(
        starts: List<Cell>,
        map: GameMap = MapGenerator.generate(random.nextLong()),
        firstPlayerIndex: Int = random.nextInt(starts.size)
    ): GameState {
        require(starts.isNotEmpty()) { "Нужен хотя бы один игрок" }
        require(starts.all { it in GameMap.START_CELLS }) { "Старт — только центральные клетки" }

        val players = starts.mapIndexed { i, cell -> Player(id = i + 1, position = cell) }
        val state = GameState(map, players, firstPlayerIndex)

        // Старт в клетке клада: берёт тот, кто ходит первым
        for (k in players.indices) {
            val p = players[(firstPlayerIndex + k) % players.size]
            if (map.contentAt(p.position) == CellContent.Treasure && state.treasureHolder() == null) {
                val out = mutableListOf<LogMessage>()
                takeTreasure(p, out)
                state.log.addAll(out)
            }
        }
        return state
    }

    // ───────────────────────── Применение действия ─────────────────────────

    fun applyAction(state: GameState, action: Action): ActionResult {
        if (state.gameOver) return rejected(null, "Игра уже окончена")
        val player = state.currentPlayer

        val result = if (player.position == state.map.estuary) {
            autoExitEstuary(state, player)
        } else {
            when (action) {
                is Action.Step -> step(state, player, action)
                is Action.Shoot -> shoot(state, player, action)
            }
        }

        state.log.addAll(result.messages)
        if (result.turnSpent && !state.gameOver) endTurn(state)
        return result
    }

    /** Конец хода: счётчик, восстановление стенок, передача хода. */
    private fun endTurn(state: GameState) {
        state.turnCounter++
        state.brokenWalls.entries.removeAll { state.turnCounter - it.value >= WALL_RESTORE_AFTER }
        state.currentPlayerIndex = (state.currentPlayerIndex + 1) % state.players.size
    }

    // ───────────────────────── Автовыход из устья ─────────────────────────

    private fun autoExitEstuary(state: GameState, player: Player): ActionResult {
        val from = player.position
        player.position = state.map.riverLine[1]       // направление фиксировано: вдоль линии реки
        player.inRiverAfterEstuary = true
        record(state, player, from)
        val out = mutableListOf<LogMessage>()
        out.say(player, "Ты выбираешься из устья и плывёшь вверх по течению")
        hearOthers(state, player, "Соперник где-то в реке", out)
        return ActionResult(ActionStatus.DONE, true, out)
    }

    // ───────────────────────── Шаг ─────────────────────────

    private fun step(state: GameState, player: Player, action: Action.Step): ActionResult {
        val from = player.position
        val to = from + action.direction
        if (!to.isInside) return rejected(player, "Движение невозможно — граница квадрата")
        // В устье шагнуть нельзя — туда только сносит. Проверяем до стенки: граната не тратится.
        if (to == state.map.estuary) return rejected(player, "Ход невозможен: на пути устье реки")

        val out = mutableListOf<LogMessage>()

        if (state.isWallIntact(from, to)) {
            if (player.isWounded) return rejected(player, "Ты ранен — взорвать стенку не можешь")
            if (player.grenades <= 0) return rejected(player, "Здесь бронированная стенка, нужна граната")
            if (!action.explodeWall) {
                return ActionResult(
                    ActionStatus.NEEDS_CONFIRMATION, false,
                    listOf(LogMessage("Перед тобой бронированная стенка. Взорвать гранатой?", player.id))
                )
            }
            player.grenades--
            state.brokenWalls[WallId.of(from, to)] = state.turnCounter
            out.say(player, "Взрыв! Стенка разрушена")
            hearOthers(state, player, "Слышен взрыв", out)
        }

        moveInto(state, player, to, out)
        record(state, player, from)
        if (!state.gameOver) hearOthers(state, player, "Соперник сделал шаг ${action.direction.toward}", out)
        return ActionResult(ActionStatus.DONE, true, out)
    }

    /** Перемещение в клетку [to] и срабатывание её эффекта (сначала перемещение, потом эффект). */
    private fun moveInto(state: GameState, player: Player, to: Cell, out: MutableList<LogMessage>) {
        val map = state.map
        val from = player.position
        val content = map.contentAt(to)

        // Флаг «в реке» сбрасывается при шаге на клетку не-реки
        if (content != CellContent.River) player.inRiverAfterEstuary = false

        when (content) {
            CellContent.River -> {
                val i = map.riverLine.indexOf(from)
                val j = map.riverLine.indexOf(to)
                if (player.inRiverAfterEstuary && i >= 1 && j == i + 1) {
                    player.position = to
                    out.say(player, "Река: вверх по течению")
                } else {
                    sweepToEstuary(map, player, out)
                }
            }
            CellContent.Estuary -> error("В устье нельзя шагнуть — отсекается в step()")
            is CellContent.Tunnel -> {
                player.position = map.tunnelDestination(to)
                out.say(player, "Тоннель ${content.system}: тебя унесло в другой конец системы")
            }
            CellContent.Arsenal -> {
                player.position = to
                if (player.isWounded) {
                    out.say(player, "Арсенал: раненый не может пополнить боезапас")
                } else {
                    player.ammo = MAX_AMMO
                    player.grenades = MAX_GRENADES
                    out.say(player, "Ты в арсенале: боезапас восполнен")
                }
            }
            CellContent.Hospital -> {
                player.position = to
                if (player.isWounded) {
                    player.isWounded = false
                    out.say(player, "Ты в больнице: ранение вылечено")
                } else {
                    out.say(player, "Ты в больнице")
                }
            }
            CellContent.Treasure -> {
                player.position = to
                takeTreasure(player, out, state)
            }
            CellContent.Exit -> {
                player.position = to
                if (player.hasTreasure) {
                    state.gameOver = true
                    state.winnerId = player.id
                    out.say(player, "Ты вышел с кладом — победа!")
                    state.players.filter { it.id != player.id }
                        .forEach { out.add(LogMessage("Соперник вышел с кладом. Ты проиграл", it.id)) }
                } else {
                    out.say(player, "Ты нашёл выход. Но без клада не выйти")
                }
            }
        }
    }

    /** Снос в устье + автоматический выстрел в крокодила. */
    private fun sweepToEstuary(map: GameMap, player: Player, out: MutableList<LogMessage>) {
        player.position = map.estuary
        player.inRiverAfterEstuary = false
        out.say(player, "Река, устье, крокодил")
        when {
            player.isWounded -> out.say(player, "Ты ранен и не можешь отстреливаться")
            player.ammo >= 1 -> player.ammo--
            else -> {
                wound(player, out)
                out.say(player, "Крокодил ранил тебя! Ищи больницу")
            }
        }
    }

    /** Клад: берёт только здоровый; клад в мире один. */
    private fun takeTreasure(player: Player, out: MutableList<LogMessage>, state: GameState? = null) {
        val holder = state?.treasureHolder()
        when {
            player.isWounded -> out.say(player, "Клад здесь, но ты ранен — не можешь его взять")
            holder === player -> out.say(player, "Клад уже у тебя")
            holder != null -> out.say(player, "Здесь был клад, но его уже унесли")
            else -> {
                player.hasTreasure = true
                player.knownTreasureCell = player.position
                out.say(player, "Ты нашёл клад!")
            }
        }
    }

    // ───────────────────────── Выстрел ─────────────────────────

    private fun shoot(state: GameState, player: Player, action: Action.Shoot): ActionResult {
        if (player.isWounded) return rejected(player, "Ты ранен — стрелять нельзя")
        if (player.ammo <= 0) return rejected(player, "Нет патронов")

        player.ammo--
        val out = mutableListOf<LogMessage>()
        val from = player.position
        val target = from + action.direction

        val victim = if (target.isInside && !state.isWallIntact(from, target)) {
            state.players.firstOrNull { it.id != player.id && it.position == target }
        } else null

        if (victim != null) {
            wound(victim, out)
            out.say(player, "Ты ранил соперника!")
            out.say(victim, "Тебя ранили выстрелом ${action.direction.opposite.from}")
        } else {
            out.say(player, "Ты ни в кого не попал")
            hearOthers(state, player, "Слышен выстрел", out)
        }
        return ActionResult(ActionStatus.DONE, true, out)
    }

    // ───────────────────────── Вспомогательное ─────────────────────────

    /** Ранение: нельзя стрелять/взрывать/брать клад; клад возвращается в исходную клетку. */
    private fun wound(p: Player, out: MutableList<LogMessage>) {
        p.isWounded = true
        if (p.hasTreasure) {
            p.hasTreasure = false // клад снова лежит в своей клетке на карте
            out.say(p, "Ты потерял клад!")
        }
    }

    private fun record(state: GameState, player: Player, from: Cell) {
        state.moveHistory.add(MoveRecord(player.id, from, player.position, state.turnCounter))
    }

    private fun rejected(player: Player?, text: String) = ActionResult(
        ActionStatus.REJECTED, false, listOf(LogMessage(text, player?.id))
    )

    private fun MutableList<LogMessage>.say(player: Player, text: String) {
        add(LogMessage(text, player.id))
    }

    /** Событие, которое «слышат» остальные игроки (в соло их нет). */
    private fun hearOthers(state: GameState, actor: Player, text: String, out: MutableList<LogMessage>) {
        state.players.filter { it.id != actor.id }.forEach { out.add(LogMessage(text, it.id)) }
    }
}
