package com.spanishapp.ui.games

/**
 * Pure-Kotlin crossword validator — no Android deps, fully unit-testable.
 *
 * A valid crossword satisfies:
 *  1. Every word contains only letter characters.
 *  2. Every word fits within [0, gridSize).
 *  3. Wherever two words share a cell the characters match.
 *  4. Every word shares at least one cell with another word (no orphans).
 *  5. The whole crossword is one connected component.
 */
internal object CrosswordValidator {

    /** Returns a list of human-readable error descriptions. Empty = valid. */
    fun errors(words: List<CrosswordWord>, gridSize: Int): List<String> {
        val errs = mutableListOf<String>()
        if (words.isEmpty()) return listOf("Word list is empty")

        // ── 1. Character check ────────────────────────────────────────────
        words.forEach { w ->
            if (w.spanish.length < 3)
                errs.add("'${w.spanish}' is too short (min 3)")
            if (!w.spanish.all { it.isLetter() })
                errs.add("'${w.spanish}' contains non-letter characters")
        }

        // ── 2. Bounds check ──────────────────────────────────────────────
        words.forEach { w ->
            val endX = if (w.isVertical) w.x else w.x + w.spanish.length - 1
            val endY = if (w.isVertical) w.y + w.spanish.length - 1 else w.y
            if (w.x < 0 || w.y < 0 || endX >= gridSize || endY >= gridSize)
                errs.add("'${w.spanish}' at (${w.x},${w.y}) exceeds ${gridSize}x${gridSize} grid")
        }

        // ── 3. Build cell map and detect character conflicts ──────────────
        // cell → (char, owning word's spanish)
        val cellMap = HashMap<Pair<Int, Int>, Pair<Char, String>>()
        words.forEach { w ->
            w.spanish.forEachIndexed { i, c ->
                val key  = cellOf(w, i)
                val ch   = c.uppercaseChar()
                val prev = cellMap[key]
                if (prev == null) {
                    cellMap[key] = ch to w.spanish
                } else if (prev.first != ch) {
                    errs.add(
                        "Character conflict at $key: " +
                        "'${prev.first}'(${prev.second}) vs '$ch'(${w.spanish})"
                    )
                }
            }
        }

        // ── 4. Each word must intersect at least one other word ───────────
        val cellSets: Map<Int, Set<Pair<Int, Int>>> = words.associate { w ->
            w.id to w.spanish.indices.map { i -> cellOf(w, i) }.toSet()
        }
        words.forEach { w ->
            val intersects = words.any { other ->
                other.id != w.id && cellSets[w.id]!!.intersect(cellSets[other.id]!!).isNotEmpty()
            }
            if (!intersects)
                errs.add("'${w.spanish}' has no intersection with any other word")
        }

        // ── 5. Connectivity (BFS) ─────────────────────────────────────────
        val adj: Map<Int, Set<Int>> = words.associate { w ->
            w.id to words
                .filter { other ->
                    other.id != w.id &&
                    cellSets[w.id]!!.intersect(cellSets[other.id]!!).isNotEmpty()
                }
                .map { it.id }
                .toSet()
        }
        val visited = mutableSetOf(words.first().id)
        val queue   = ArrayDeque(listOf(words.first().id))
        while (queue.isNotEmpty()) {
            val cur = queue.removeFirst()
            for (nb in adj[cur] ?: emptySet()) {
                if (nb !in visited) { visited.add(nb); queue.add(nb) }
            }
        }
        if (visited.size != words.size) {
            val isolated = words.filter { it.id !in visited }.map { "'${it.spanish}'" }
            errs.add("Crossword is disconnected: ${isolated.joinToString()} not reachable")
        }

        return errs
    }

    fun isValid(words: List<CrosswordWord>, gridSize: Int) =
        errors(words, gridSize).isEmpty()

    /** Returns the grid coordinates of the i-th character of word w. */
    internal fun cellOf(w: CrosswordWord, i: Int): Pair<Int, Int> =
        if (w.isVertical) w.x to (w.y + i) else (w.x + i) to w.y
}
