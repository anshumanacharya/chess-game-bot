package com.chessapp.bot

import com.chessapp.engine.Board
import com.chessapp.engine.CastlingRights
import com.chessapp.engine.ChessGame
import com.chessapp.engine.Color
import com.chessapp.engine.GameState
import com.chessapp.engine.GameStatus
import com.chessapp.engine.Piece
import com.chessapp.engine.PieceType
import com.chessapp.engine.Square
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

private fun ChessGame.play(from: String, to: String, promotion: PieceType? = null) {
    val fromSq = Square.fromAlgebraic(from)
    val toSq = Square.fromAlgebraic(to)
    val move = legalMovesFrom(fromSq).first { it.to == toSq && it.promotion == promotion }
    assertTrue(makeMove(move), "makeMove rejected $from-$to")
}

class TwoPlySearchTest {

    @Test
    fun `bestMove is null when there is no legal move`() {
        val board = Board.empty()
        board.setPiece(Square.fromAlgebraic("a8"), Piece(Color.BLACK, PieceType.KING))
        board.setPiece(Square.fromAlgebraic("c7"), Piece(Color.WHITE, PieceType.KING))
        board.setPiece(Square.fromAlgebraic("b6"), Piece(Color.WHITE, PieceType.QUEEN))
        val state = GameState(
            board = board,
            sideToMove = Color.BLACK,
            castlingRights = CastlingRights(false, false, false, false),
            enPassantTarget = null,
            halfMoveClock = 0,
            fullMoveNumber = 1
        )
        assertNull(TwoPlySearch().bestMove(state))
    }

    @Test
    fun `bestMove captures a hanging piece`() {
        val board = Board.empty()
        board.setPiece(Square.fromAlgebraic("a1"), Piece(Color.WHITE, PieceType.KING))
        // h2, not h1: h1 shares a diagonal with a8 (h1-g2-f3-e4-d5-c6-b7-a8), which would put
        // Black's king in check on White's own move here — an illegal position.
        board.setPiece(Square.fromAlgebraic("h2"), Piece(Color.WHITE, PieceType.QUEEN))
        board.setPiece(Square.fromAlgebraic("a8"), Piece(Color.BLACK, PieceType.KING))
        board.setPiece(Square.fromAlgebraic("h8"), Piece(Color.BLACK, PieceType.ROOK))
        val state = GameState(
            board = board,
            sideToMove = Color.WHITE,
            castlingRights = CastlingRights(false, false, false, false),
            enPassantTarget = null,
            halfMoveClock = 0,
            fullMoveNumber = 1
        )
        val move = TwoPlySearch().bestMove(state)
        assertNotNull(move)
        assertTrue(move.isCapture)
        assertEquals(Square.fromAlgebraic("h8"), move.to)
    }

    @Test
    fun `bestMove delivers an available mate in one`() {
        val game = ChessGame()
        game.play("f2", "f3")
        game.play("e7", "e5")
        game.play("g2", "g4")
        // Black to move: Qh4# is on the board.
        val move = TwoPlySearch().bestMove(game.state)
        assertNotNull(move)
        assertTrue(game.makeMove(move))
        assertEquals(GameStatus.CHECKMATE, game.status())
    }

    @Test
    fun `scoreMove is deterministic for the same position and move`() {
        val game = ChessGame()
        val move = game.legalMovesFrom(Square.fromAlgebraic("e2")).first { it.to == Square.fromAlgebraic("e4") }
        val search = TwoPlySearch()
        assertEquals(search.scoreMove(game.state, move), search.scoreMove(game.state, move))
    }
}
