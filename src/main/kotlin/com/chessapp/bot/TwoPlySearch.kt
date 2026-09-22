package com.chessapp.bot

import com.chessapp.engine.Color
import com.chessapp.engine.GameState
import com.chessapp.engine.GameStatus
import com.chessapp.engine.Move
import com.chessapp.engine.MoveGenerator

/**
 * Looks one of the side-to-move's own moves ahead, then one further ply at the opponent's best
 * material-grabbing reply, so it notices a hanging piece or an available mate without a full
 * deep search. This is deliberately shallow and completely deterministic — it always returns the
 * objectively best move it can see. [ChessBot] is what actually plays with human-like
 * imperfection (noise, blunders) layered on top of [scoreMove]; use this class directly if you
 * just want "the engine's opinion" of a position, e.g. in tests.
 */
class TwoPlySearch(private val evaluator: Evaluator = MaterialEvaluator()) {

    /** The single best-scoring legal move for [state]'s side to move, or null if there is none. */
    fun bestMove(state: GameState): Move? =
        MoveGenerator.legalMoves(state).maxByOrNull { scoreMove(state, it) }

    /**
     * Score of playing [move] in [state], from the perspective of [state]'s side to move,
     * assuming the opponent then replies with their single best material-grabbing move (or, if
     * that reply would be checkmate, the worst possible outcome).
     */
    fun scoreMove(state: GameState, move: Move): Int {
        val sideToMove = state.sideToMove
        val afterOwnMove = MoveGenerator.applyMove(state, move)
        return scoreAfterOwnMove(afterOwnMove, sideToMove)
    }

    private fun scoreAfterOwnMove(afterOwnMove: GameState, sideToMove: Color): Int {
        val status = MoveGenerator.status(afterOwnMove)
        if (status == GameStatus.CHECKMATE) return MATE_SCORE
        if (status == GameStatus.STALEMATE || status == GameStatus.DRAW_INSUFFICIENT_MATERIAL) return 0

        val opponentReplies = MoveGenerator.legalMoves(afterOwnMove)
        if (opponentReplies.isEmpty()) return evaluator.evaluate(afterOwnMove.board, sideToMove)

        var worstForUs = Int.MAX_VALUE
        for (reply in opponentReplies) {
            val afterReply = MoveGenerator.applyMove(afterOwnMove, reply)
            val replyIsMate = MoveGenerator.isInCheck(afterReply, sideToMove) &&
                MoveGenerator.legalMoves(afterReply).isEmpty()
            val score = if (replyIsMate) -MATE_SCORE else evaluator.evaluate(afterReply.board, sideToMove)
            if (score < worstForUs) worstForUs = score
        }
        return worstForUs
    }

    companion object {
        private const val MATE_SCORE = 100_000
    }
}
