package com.chessapp.bot

import com.chessapp.engine.PieceType

/**
 * Centipawn value of each piece type, used when scoring material (100 = one pawn, by the
 * standard chess-engine convention this mirrors). Override individual values to experiment with
 * different weightings — e.g. valuing knights over bishops, or vice versa.
 */
data class PieceValues(
    val pawn: Int = 100,
    val knight: Int = 300,
    val bishop: Int = 300,
    val rook: Int = 500,
    val queen: Int = 900,
    val king: Int = 0
) {
    fun of(type: PieceType): Int = when (type) {
        PieceType.PAWN -> pawn
        PieceType.KNIGHT -> knight
        PieceType.BISHOP -> bishop
        PieceType.ROOK -> rook
        PieceType.QUEEN -> queen
        PieceType.KING -> king
    }

    companion object {
        val STANDARD = PieceValues()
    }
}
