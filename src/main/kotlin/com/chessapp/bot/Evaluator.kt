package com.chessapp.bot

import com.chessapp.engine.Board
import com.chessapp.engine.Color

/**
 * Scores a board position from one side's perspective — higher is better for [color]. This is
 * the piece to swap out if you want the bot to value something beyond material (center control,
 * king safety, pawn structure, ...): implement this interface and pass it to [TwoPlySearch]
 * instead of [MaterialEvaluator].
 */
fun interface Evaluator {
    fun evaluate(board: Board, color: Color): Int
}

/**
 * The simplest possible evaluator: sum of your own piece values minus your opponent's, using
 * [pieceValues]. Ignores where pieces are on the board, king safety, pawn structure — everything
 * except raw material.
 */
class MaterialEvaluator(private val pieceValues: PieceValues = PieceValues.STANDARD) : Evaluator {
    override fun evaluate(board: Board, color: Color): Int {
        var score = 0
        for ((_, piece) in board.allPieces()) {
            val value = pieceValues.of(piece.type)
            score += if (piece.color == color) value else -value
        }
        return score
    }
}
