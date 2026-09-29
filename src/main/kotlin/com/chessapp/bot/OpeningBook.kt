package com.chessapp.bot

import com.chessapp.engine.GameState
import com.chessapp.engine.Move
import com.chessapp.engine.MoveGenerator
import com.chessapp.engine.PieceType
import kotlin.random.Random

/**
 * Opening moves built from Lichess's open chess-openings dataset, bundled into the build so it works
 * offline in both the Android app and the web build. A move's weight is how many named opening
 * lines continue with it, so common moves are picked more often than obscure ones.
 *
 * Lookups match on the exact move sequence played so far (no transposition handling).
 */
object OpeningBook {
    private val moveWeightsByPrefix: Map<String, Map<String, Int>> by lazy { build(OpeningBookData.CHUNKS) }

    /** A weighted-random book move for [state], or null if the position isn't in the book. */
    fun pick(state: GameState, random: Random): Move? {
        if (!reachedFromStandardStart(state)) return null
        val weights = moveWeightsByPrefix[state.moveHistory.joinToString(" ") { uci(it) }] ?: return null
        val legal = MoveGenerator.legalMoves(state).associateBy { uci(it) }
        val candidates = weights.filterKeys { it in legal }
        if (candidates.isEmpty()) return null

        var roll = random.nextInt(candidates.values.sum())
        for ((key, weight) in candidates) {
            if (roll < weight) return legal.getValue(key)
            roll -= weight
        }
        return null
    }

    /** Book lines assume the standard start position; a custom setup must not match them. */
    private fun reachedFromStandardStart(state: GameState): Boolean {
        var replay = GameState.newGame()
        for (move in state.moveHistory) {
            replay = MoveGenerator.applyMove(replay, move)
        }
        return replay.board.allPieces().toMap() == state.board.allPieces().toMap()
    }

    internal fun uci(move: Move): String {
        val promotion = when (move.promotion) {
            PieceType.QUEEN -> "q"
            PieceType.ROOK -> "r"
            PieceType.BISHOP -> "b"
            PieceType.KNIGHT -> "n"
            else -> ""
        }
        return "${move.from.algebraic}${move.to.algebraic}$promotion"
    }

    internal fun build(chunks: List<String>): Map<String, Map<String, Int>> {
        val result = HashMap<String, HashMap<String, Int>>()
        for (chunk in chunks) {
            for (line in chunk.lineSequence()) {
                if (line.isBlank()) continue
                val moves = line.trim().split(' ')
                for (i in moves.indices) {
                    val prefix = moves.subList(0, i).joinToString(" ")
                    val weights = result.getOrPut(prefix) { HashMap() }
                    weights[moves[i]] = (weights[moves[i]] ?: 0) + 1
                }
            }
        }
        return result
    }
}
